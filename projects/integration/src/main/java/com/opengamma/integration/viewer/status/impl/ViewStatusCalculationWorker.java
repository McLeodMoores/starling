/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusOption;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Executes the View status calculation task with the default Executor service
 */
public class ViewStatusCalculationWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatusCalculationWorker.class);

  private static final ExecutorService DEFAULT_EXECUTOR = NamedThreadPoolFactory.newCachedThreadPool("ViewStatus");

  private final ExecutorService _executor;

  private final Map<String, Collection<String>> _valueRequirementBySecType;

  private final ToolContext _toolContext;

  private final UniqueId _portfolioId;

  private final UserPrincipal _user;

  private final MarketDataSpecification _marketDataSpecification;

  public ViewStatusCalculationWorker(final ToolContext toolContext, final UniqueId portfolioId, final ViewStatusOption option) {
    this(toolContext, portfolioId, option, DEFAULT_EXECUTOR);
  }

  public ViewStatusCalculationWorker(final ToolContext toolContext, final UniqueId portfolioId, final ViewStatusOption option, final ExecutorService executorService) {
    ArgumentChecker.notNull(toolContext, "toolContex");
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(option.getUser(), "option.user");
    ArgumentChecker.notNull(option.getMarketDataSpecification(), "option.marketDataSpecification");
    ArgumentChecker.notNull(executorService, "executorService");

    validateComponentsInToolContext(toolContext);
    _portfolioId = portfolioId;
    _user = option.getUser();
    _marketDataSpecification = option.getMarketDataSpecification();
    final Map<String, Collection<String>> valueRequirementBySecType = scanValueRequirementBySecType(portfolioId, toolContext);
    if (LOGGER.isDebugEnabled()) {
      final StringBuilder strBuf = new StringBuilder();
      for (final String securityType : Sets.newTreeSet(valueRequirementBySecType.keySet())) {
        final Set<String> valueNames = Sets.newTreeSet(valueRequirementBySecType.get(securityType));
        strBuf.append(String.format("%s\t%s\n", StringUtils.rightPad(securityType, 40), valueNames.toString()));
      }
      LOGGER.debug("\n{}\n", strBuf.toString());
    }
    _toolContext = toolContext;
    _executor = executorService;
    _valueRequirementBySecType = valueRequirementBySecType;
  }

  private void validateComponentsInToolContext(final ToolContext toolContext) {
    if (toolContext.getViewProcessor() == null) {
      throw new OpenGammaRuntimeException("Missing view processor in given toolcontext");
    }
    if (toolContext.getSecuritySource() == null) {
      throw new OpenGammaRuntimeException("Missing security source in given toolcontext");
    }
    if (toolContext.getConfigMaster() == null) {
      throw new OpenGammaRuntimeException("Missing config master in given toolcontext");
    }
    if (toolContext.getPositionSource() == null) {
      throw new OpenGammaRuntimeException("Missing position source in given toolcontext");
    }
  }

  private Map<String, Collection<String>> scanValueRequirementBySecType(final UniqueId portfolioId, final ToolContext toolContext) {
    final AvailableOutputsProvider availableOutputsProvider = toolContext.getAvaliableOutputsProvider();
    if (availableOutputsProvider == null) {
      throw new OpenGammaRuntimeException("AvailableOutputsProvider missing from ToolContext");
    }
    final SetMultimap<String, String> valueNamesBySecurityType = TreeMultimap.create();

    final AvailableOutputs portfolioOutputs = availableOutputsProvider.getPortfolioOutputs(portfolioId, null);
    final Set<String> securityTypes = portfolioOutputs.getSecurityTypes();
    for (final String securityType : securityTypes) {
      final Set<AvailableOutput> positionOutputs = portfolioOutputs.getPositionOutputs(securityType);
      for (final AvailableOutput availableOutput : positionOutputs) {
        valueNamesBySecurityType.put(securityType, availableOutput.getValueName());
      }
    }
    return valueNamesBySecurityType.asMap();
  }

  public ViewStatusResultAggregator run() {
    final ViewStatusResultAggregator aggregator = new ViewStatusResultAggregatorImpl();
    final CompletionService<PerViewStatusResult> completionService = new ExecutorCompletionService<>(_executor);
    //submit task to executor to run partitioned by security type
    for (final String securityType : _valueRequirementBySecType.keySet()) {
      final Collection<String> valueRequirements = _valueRequirementBySecType.get(securityType);
      completionService.submit(new ViewStatusCalculationTask(_toolContext, _portfolioId, _user, securityType, valueRequirements, _marketDataSpecification));
    }
    try {
      // process all completed task
      for (int i = 0; i < _valueRequirementBySecType.size(); i++) {
        final Future<PerViewStatusResult> futureTask = completionService.take();
        final PerViewStatusResult perViewStatusResult = futureTask.get();
        for (final ViewStatusKey viewStatusKey : perViewStatusResult.keySet()) {
          aggregator.putStatus(viewStatusKey, perViewStatusResult.get(viewStatusKey));
        }

      }
    } catch (final InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      throw new OpenGammaRuntimeException("Error running View status report", ex.getCause());
    }
    return aggregator;
  }
}
