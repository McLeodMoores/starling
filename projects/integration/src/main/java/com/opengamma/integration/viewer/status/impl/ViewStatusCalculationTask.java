/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.aggregation.CurrenciesAggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.tuple.Pair;

/**
 * View status calculation task.
 */
public class ViewStatusCalculationTask implements Callable<PerViewStatusResult> {

  private final class ViewStatusResultListener extends AbstractViewResultListener {
    private final CountDownLatch _latch;
    private final PerViewStatusResult _statusResult;
    private final ViewDefinition _viewDefinition;
    private final AtomicLong _count = new AtomicLong(0);

    private ViewStatusResultListener(final CountDownLatch latch, final PerViewStatusResult statusResult, final ViewDefinition viewDefinition) {
      _latch = latch;
      _statusResult = statusResult;
      _viewDefinition = viewDefinition;
    }

    @Override
    public UserPrincipal getUser() {
      return _user;
    }

    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      LOGGER.error("View definition compiled");
      final CompiledViewCalculationConfiguration compiledCalculationConfiguration = compiledViewDefinition
          .getCompiledCalculationConfiguration(DEFAULT_CALC_CONFIG);
      final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = compiledCalculationConfiguration.getTerminalOutputSpecifications();
      for (final ValueSpecification valueSpec : terminalOutputs.keySet()) {
        final ComputationTargetType computationTargetType = valueSpec.getTargetSpecification().getType();
        if (isValidTargetType(computationTargetType)) {
          final UniqueId uniqueId = valueSpec.getTargetSpecification().getUniqueId();
          final String currency = getCurrency(uniqueId, computationTargetType);
          if (currency != null) {
            _statusResult.put(new ViewStatusKeyBean(_securityType, valueSpec.getValueName(), currency, computationTargetType.getName()), ViewStatus.NO_VALUE);
          } else {
            LOGGER.error("Discarding result as NULL return as Currency for id: {} targetType:{}", uniqueId, computationTargetType);
          }
        }
      }
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      LOGGER.debug("View definition {} failed to initialize", _viewDefinition);
      try {
        processGraphFailResult(_statusResult);
      } finally {
        _latch.countDown();
      }
    }

    @Override
    public void cycleStarted(final ViewCycleMetadata cycleInfo) {
      LOGGER.debug("Cycle started");
    }

    @Override
    public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
      LOGGER.debug("Cycle execution failed", exception);
    }

    @Override
    public void processCompleted() {
      LOGGER.debug("Process completed");
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      LOGGER.debug("Process terminated");
    }

    @Override
    public void clientShutdown(final Exception e) {
      LOGGER.debug("Client shutdown");
    }

    @Override
    public void cycleFragmentCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      LOGGER.debug("cycle fragment completed");
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      LOGGER.debug("cycle {} completed", _count.get());
      if (_count.getAndIncrement() > 5) {
        processStatusResult(fullResult, _statusResult);
        _latch.countDown();
      }
    }
  }

  private static final String MIXED_CURRENCY = "MIXED_CURRENCY";
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatusCalculationTask.class);

  private static final String DEFAULT_CALC_CONFIG = "Default";

  private final String _securityType;
  private final Set<String> _valueRequirementNames;
  private final UniqueId _portfolioId;
  private final UserPrincipal _user;
  private final ToolContext _toolContext;
  private final CurrenciesAggregationFunction _currenciesAggrFunction;
  private final Map<UniqueId, String> _targetCurrenciesCache = Maps.newConcurrentMap();
  private final MarketDataSpecification _marketDataSpecification;

  public ViewStatusCalculationTask(final ToolContext toolcontext, final UniqueId portfolioId, final UserPrincipal user, final String securityType,
      final Collection<String> valueRequirementNames, final MarketDataSpecification marketDataSpecification) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(valueRequirementNames, "valueRequirementNames");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(toolcontext, "toolcontext");
    ArgumentChecker.notNull(marketDataSpecification, "marketDataSpecification");

    _portfolioId = portfolioId;
    _user = user;
    _securityType = securityType;
    _valueRequirementNames = ImmutableSet.copyOf(valueRequirementNames);
    _toolContext = toolcontext;
    _currenciesAggrFunction = new CurrenciesAggregationFunction(_toolContext.getSecuritySource());
    _marketDataSpecification = marketDataSpecification;
  }

  @Override
  public PerViewStatusResult call() throws Exception {
    LOGGER.debug("Start calculating result for security:{} with values{}", _securityType, Sets.newTreeSet(_valueRequirementNames).toString());

    final PerViewStatusResult statusResult = new PerViewStatusResult(_securityType);
    // No need to do any work if there are no ValueRequirements to compute
    if (_valueRequirementNames.isEmpty()) {
      return statusResult;
    }
    final ViewDefinition viewDefinition = createViewDefinition();
    final ViewProcessor viewProcessor = _toolContext.getViewProcessor();
    final ViewClient client = viewProcessor.createViewClient(_user);

    final CountDownLatch latch = new CountDownLatch(1);
    client.setResultListener(new ViewStatusResultListener(latch, statusResult, viewDefinition));
    client.attachToViewProcess(viewDefinition.getUniqueId(), ExecutionOptions.infinite(_marketDataSpecification));

    try {
      LOGGER.info("main thread waiting");
      if (!latch.await(30, TimeUnit.SECONDS)) {
        LOGGER.error("Timed out waiting for {}", viewDefinition);
      }
    } catch (final InterruptedException ex) {
      throw new OpenGammaRuntimeException("Interrupted while waiting for " + viewDefinition, ex);
    }
    client.detachFromViewProcess();
    removeViewDefinition(viewDefinition);
    LOGGER.debug("PerViewStatusResult for securityType:{} is {}", _securityType, statusResult);
    return statusResult;
  }

  protected boolean isValidTargetType(final ComputationTargetType computationTargetType) {
    if (ComputationTargetType.POSITION.isCompatible(computationTargetType) || ComputationTargetType.PORTFOLIO.isCompatible(computationTargetType) ||
        ComputationTargetType.PORTFOLIO_NODE.isCompatible(computationTargetType) || ComputationTargetType.TRADE.isCompatible(computationTargetType)) {
      return true;
    }
    return false;
  }

  private void removeViewDefinition(final ViewDefinition viewDefinition) {
    LOGGER.debug("Removing ViewDefintion with id: {}", viewDefinition.getUniqueId());
    final ConfigMaster configMaster = _toolContext.getConfigMaster();
    configMaster.remove(viewDefinition.getUniqueId().getObjectId());
    LOGGER.debug("ViewDefinition {} removed", viewDefinition.getUniqueId());
  }

  private ViewDefinition createViewDefinition() {
    final ViewDefinition viewDefinition = new ViewDefinition("VS_VIEW_" + GUIDGenerator.generate().toString(), _portfolioId, _user);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (final String requiredOutput : _valueRequirementNames) {
      defaultCalConfig.addPortfolioRequirementName(_securityType, requiredOutput);
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return storeViewDefinition(viewDefinition);
  }

  private ViewDefinition storeViewDefinition(final ViewDefinition viewDefinition) {
    ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    config = ConfigMasterUtils.storeByName(_toolContext.getConfigMaster(), config);
    return config.getValue();
  }

  private void processGraphFailResult(final PerViewStatusResult statusResult) {
    final PositionSource positionSource = _toolContext.getPositionSource();
    final Portfolio portfolio = positionSource.getPortfolio(_portfolioId, VersionCorrection.LATEST);
    final List<Position> positions = PortfolioAggregator.flatten(portfolio);
    final Set<String> currencies = Sets.newHashSet();
    for (final Position position : positions) {
      if (position.getSecurity() == null) {
        position.getSecurityLink().resolve(_toolContext.getSecuritySource());
      }
      if (position.getSecurity() != null && _securityType.equals(position.getSecurity().getSecurityType())) {
        currencies.add(getCurrency(position.getUniqueId(), ComputationTargetType.POSITION));
      }
    }
    for (final String valueName : _valueRequirementNames) {
      for (final String currency : currencies) {
        statusResult.put(new ViewStatusKeyBean(_securityType, valueName, currency, ComputationTargetType.POSITION.getName()), ViewStatus.GRAPH_FAIL);
      }
    }
  }

  private void processStatusResult(final ViewComputationResultModel fullResult, final PerViewStatusResult statusResult) {
    final ViewCalculationResultModel calculationResult = fullResult.getCalculationResult(DEFAULT_CALC_CONFIG);
    final Collection<ComputationTargetSpecification> allTargets = calculationResult.getAllTargets();
    for (final ComputationTargetSpecification targetSpec : allTargets) {
      final ComputationTargetType targetType = targetSpec.getSpecification().getType();
      if (isValidTargetType(targetType)) {
        final Map<Pair<String, ValueProperties>, ComputedValueResult> values = calculationResult.getValues(targetSpec);
        for (final Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> valueEntry : values.entrySet()) {
          final String valueName = valueEntry.getKey().getFirst();
          final String currency = getCurrency(targetSpec.getUniqueId(), targetType);
          LOGGER.debug("{} currency returned for id:{} targetType:{}", currency, targetSpec.getUniqueId(), targetType);
          if (currency != null) {
            final ComputedValueResult computedValue = valueEntry.getValue();
            if (isGoodValue(computedValue)) {
              statusResult.put(new ViewStatusKeyBean(_securityType, valueName, currency, targetType.getName()), ViewStatus.VALUE);
            }
          } else {
            LOGGER.error("Discarding result as NULL return as Currency for id: {} targetType:{}", targetSpec.getUniqueId(), targetType);
          }
        }
      }
    }
  }

  private String getCurrency(final UniqueId uniqueId, final ComputationTargetType computationTargetType) {
    synchronized (_targetCurrenciesCache) {
      String currency = _targetCurrenciesCache.get(uniqueId);
      if (currency == null) {
        if (ComputationTargetType.PORTFOLIO_NODE.isCompatible(computationTargetType) || ComputationTargetType.PORTFOLIO.isCompatible(computationTargetType)) {
          currency = MIXED_CURRENCY;
        } else if (ComputationTargetType.POSITION.isCompatible(computationTargetType)) {
          final PositionSource positionSource = _toolContext.getPositionSource();
          final Position position = positionSource.getPosition(uniqueId);
          if (position.getSecurity() == null) {
            position.getSecurityLink().resolve(_toolContext.getSecuritySource());
          }
          if (position.getSecurity() != null) {
            currency = _currenciesAggrFunction.classifyPosition(position);
          }
        } else if (ComputationTargetType.TRADE.isCompatible(computationTargetType)) {
          final PositionSource positionSource = _toolContext.getPositionSource();
          final Trade trade = positionSource.getTrade(uniqueId);
          if (trade.getSecurity() == null) {
            trade.getSecurityLink().resolve(_toolContext.getSecuritySource());
          }
          if (trade.getSecurity() != null) {
            currency = CurrenciesAggregationFunction.classifyBasedOnSecurity(trade.getSecurity(), _toolContext.getSecuritySource());
          }
        }
      }
      if (currency == null) {
        currency = CurrenciesAggregationFunction.NO_CURRENCY;
      }
      _targetCurrenciesCache.put(uniqueId, currency);
      return currency;
    }
  }

  private boolean isGoodValue(final ComputedValueResult computedValue) {
    if (computedValue == null || computedValue.getValue() == null || StringUtils.EMPTY.equals(computedValue.getValue())) {
      return false;
    }
    return !(computedValue.getValue() instanceof MissingValue);
  }

}
