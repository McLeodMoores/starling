/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A wrapper class around {@link ViewComputationResultModel} that makes results more accessible.
 */
//TODO position result keys not used
//TODO add security outputs?
//TODO _resultKeys not filled in anywhere
public class ResultModelImpl implements ResultModel {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ResultModelImpl.class);
  /** The view definition */
  private final ViewDefinition _viewDefinition;
  /** The result model */
  private final ViewComputationResultModel _resultModel;
  /** The position source */
  private final PositionSource _positionSource;
  /** All result keys */
  private final Set<ResultKey> _resultKeys;
  /** Portfolio node level result keys */
  private final Set<ResultKey> _portfolioNodeResultKeys;
  /** Position level result keys */
  private final Set<ResultKey> _positionResultKeys;
  /** Trade level result keys */
  private final Set<ResultKey> _tradeResultKeys;
  /** Market data result keys */
  private final Set<ResultKey> _marketDataResultKeys;
  /** Primitive outputs result keys */
  private final Set<ResultKey> _primitiveResultKeys;
  /** Legacy target result keys */
  private final Set<ResultKey> _legacyResultKeys;
  /** All target keys */
  private final Set<TargetKey> _allTargetKeys;
  /** Trade level target keys */
  private final Set<TradeTargetKey> _tradeTargetKeys;
  /** Position level target keys */
  private final Set<PositionTargetKey> _positionTargetKeys;
  /** Portfolio node level target keys */
  private final Set<PortfolioNodeTargetKey> _portfolioNodeTargetKeys;
  /** Market data level target keys */
  private final Set<MarketDataTargetKey> _marketDataTargetKeys;
  /** Primitive outputs target keys */
  private final Set<PrimitiveTargetKey> _primitiveTargetKeys;
  /** Legacy target keys */
  private final Set<LegacyTargetKey> _legacyTargetKeys;
  /** The result keys */
  private final Map<TargetKey, Map<ResultKey, ComputedValueResult>> _results;
  /** The correlation id attribute */
  private static final String CORRELATION_ID_ATTRIBUTE = ManageableTrade.meta().providerId().name();

  /**
   * Creates a result model.
   * @param resultModel  the result model, not null
   * @param viewDefinition  the view definition, not null
   * @param positionSource  the position source, not null
   */
  public ResultModelImpl(final ViewComputationResultModel resultModel, final ViewDefinition viewDefinition,
                         final PositionSource positionSource) {
    _resultModel = ArgumentChecker.notNull(resultModel, "resultModel");
    _viewDefinition = ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    _positionSource = ArgumentChecker.notNull(positionSource, "positionSource");
    _resultKeys = new HashSet<>();
    _portfolioNodeResultKeys = new HashSet<>();
    _positionResultKeys = new HashSet<>();
    _tradeResultKeys = new HashSet<>();
    _marketDataResultKeys = new HashSet<>();
    _primitiveResultKeys = new HashSet<>();
    _legacyResultKeys = new HashSet<>();
    _allTargetKeys = new HashSet<>();
    _tradeTargetKeys = new HashSet<>();
    _positionTargetKeys = new HashSet<>();
    _portfolioNodeTargetKeys = new HashSet<>();
    _marketDataTargetKeys = new HashSet<>();
    _primitiveTargetKeys = new HashSet<>();
    _legacyTargetKeys = new HashSet<>();
    _results = new HashMap<>();
    buildModel();
  }

  /**
   * Builds the model.
   */
  private void buildModel() {
    // Loop over the 'column sets' aka calculation configurations
    for (final String calcConfigName : _resultModel.getCalculationConfigurationNames()) {
      final ViewCalculationResultModel calculationResult = _resultModel.getCalculationResult(calcConfigName);
      final Collection<ComputationTargetSpecification> allTargets = calculationResult.getAllTargets();
      // Loop over all the targets (portfolio and market data)
      for (final ComputationTargetSpecification target : allTargets) {
        final ComputationTargetType targetType = target.getType();
        // get results for this target, convert each target into a target key and result key and store in map of maps for retrieval.
        final Collection<ComputedValueResult> allValues = calculationResult.getAllValues(target);
        if (targetType.equals(ComputationTargetType.TRADE)) {
          final Trade trade = _positionSource.getTrade(target.getUniqueId());
          final TradeTargetKey targetKey = buildTargetKey(trade);
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addTradeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.POSITION)) {
          final Position position = _positionSource.getPosition(target.getUniqueId());
          //final Security security = trade.getSecurityLink().resolve(_securitySource);
          final PositionTargetKey targetKey = buildTargetKey(position);
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addTradeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
          final String portfolioPath = getPortfolioNodePath(target.getUniqueId(), _resultModel.getVersionCorrection());
          final PortfolioNodeTargetKey targetKey = PortfolioNodeTargetKey.of(portfolioPath);
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addPortfolioNodeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.NULL)) {
          final MarketDataTargetKey targetKey = MarketDataTargetKey.instance();
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addMarketDataResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.PRIMITIVE)) {
          final PrimitiveTargetKey targetKey = PrimitiveTargetKey.of(target);
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addPrimitiveResult(targetKey, resultKey, result);
          }
        } else {
          final LegacyTargetKey targetKey = LegacyTargetKey.of(target);
          for (final ComputedValueResult result : allValues) {
            final ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addLegacyResult(targetKey, resultKey, result);
          }
        }
      }
    }
  }

  private ResultType convertValueSpec(final ValueSpecification valueSpec) {
    final ResultType.Builder builder = ResultType.builder();
    builder.valueRequirementName(valueSpec.getValueName());
    builder.properties(valueSpec.getProperties());
    return builder.build();
  }

  private void addLegacyResult(final LegacyTargetKey legacyKey, final ResultKey resultKey, final ComputedValueResult result) {
    _legacyTargetKeys.add(legacyKey);
    _legacyResultKeys.add(resultKey);
    addResultInternal(legacyKey, resultKey, result);
  }

  private void addPrimitiveResult(final PrimitiveTargetKey primitiveKey, final ResultKey resultKey, final ComputedValueResult result) {
    _primitiveTargetKeys.add(primitiveKey);
    _primitiveResultKeys.add(resultKey);
    addResultInternal(primitiveKey, resultKey, result);
  }

  private void addMarketDataResult(final MarketDataTargetKey marketDataResultKey, final ResultKey resultKey, final ComputedValueResult result) {
    _marketDataTargetKeys.add(marketDataResultKey);
    _marketDataResultKeys.add(resultKey);
    addResultInternal(marketDataResultKey, resultKey, result);
  }

  private void addPortfolioNodeResult(final PortfolioNodeTargetKey portfolioNodeTargetKey, final ResultKey resultKey, final ComputedValueResult result) {
    _portfolioNodeTargetKeys.add(portfolioNodeTargetKey);
    _portfolioNodeResultKeys.add(resultKey);
    addResultInternal(portfolioNodeTargetKey, resultKey, result);
  }

  private void addTradeResult(final PositionTargetKey positionTargetKey, final ResultKey resultKey, final ComputedValueResult result) {
    _positionTargetKeys.add(positionTargetKey);
    _tradeResultKeys.add(resultKey);
    addResultInternal(positionTargetKey, resultKey, result);
  }

  private void addTradeResult(final TradeTargetKey tradeTargetKey, final ResultKey resultKey, final ComputedValueResult result) {
    _tradeTargetKeys.add(tradeTargetKey);
    _tradeResultKeys.add(resultKey);
    addResultInternal(tradeTargetKey, resultKey, result);
  }

  private void addResultInternal(final TargetKey targetKey, final ResultKey resultKey, final ComputedValueResult result) {
    _allTargetKeys.add(targetKey);
    if (_results.containsKey(targetKey)) {
      _results.get(targetKey).put(resultKey, result);
    } else {
      final Map<ResultKey, ComputedValueResult> map = new LinkedHashMap<>();
      map.put(resultKey, result);
      _results.put(targetKey, map);
    }
  }

  private String getPortfolioNodePath(final UniqueId nodeId, final VersionCorrection versionCorrection) {
    final StringBuilder sb = new StringBuilder();
    walkPortfolioNodePath(sb, nodeId, versionCorrection);
    return sb.toString();
  }

  private void walkPortfolioNodePath(final StringBuilder sb, final UniqueId nodeId, final VersionCorrection versionCorrection) {
    final PortfolioNode portfolioNode = _positionSource.getPortfolioNode(nodeId, _resultModel.getVersionCorrection());
    sb.insert(0, escapeSeparator(portfolioNode.getName()));
    if (portfolioNode.getParentNodeId() != null) {
      sb.insert(0, PORTFOLIO_SEPARATOR);
      walkPortfolioNodePath(sb, portfolioNode.getParentNodeId(), versionCorrection);
    }
  }

  private PositionTargetKey buildTargetKey(final Position position) {
    final Map<String, String> attributes = position.getAttributes();
    if (attributes.containsKey(CORRELATION_ID_ATTRIBUTE)) {
      return PositionTargetKey.of(ExternalId.parse(attributes.get(CORRELATION_ID_ATTRIBUTE)));
    } else {
      LOGGER.warn("Could not find correlation id attribute (providerId) in position, returning first security link id instead.  "
          + "This means correlation ids aren't being persisted correctly");
      return PositionTargetKey.of(position.getSecurityLink().getExternalId().getExternalIds().first());
    }
  }

  private TradeTargetKey buildTargetKey(final Trade trade) {
    final Map<String, String> attributes = trade.getAttributes();
    if (attributes.containsKey(CORRELATION_ID_ATTRIBUTE)) {
      return TradeTargetKey.of(ExternalId.parse(attributes.get(CORRELATION_ID_ATTRIBUTE)));
    } else {
      LOGGER.warn("Could not find correlation id attribute (providerId) in position, returning first security link id instead.  "
          + "This means correlation ids aren't being persisted correctly");
      return TradeTargetKey.of(trade.getSecurityLink().getExternalId().getExternalIds().first());
    }
  }

  @Override
  public Set<ResultKey> getRequestedPortfolioResultKeys() {
    final Set<ResultKey> resultKeys = new LinkedHashSet<>();
    for (final String calcConfigName : _viewDefinition.getAllCalculationConfigurationNames()) {
      final ViewCalculationConfiguration calculationConfiguration = _viewDefinition.getCalculationConfiguration(calcConfigName);
      for (final Pair<String, ValueProperties> requirement : calculationConfiguration.getAllPortfolioRequirements()) {
        final ResultType resultType = ResultType.builder().valueRequirementName(requirement.getFirst()).properties(requirement.getSecond()).build();
        resultKeys.add(ResultKey.of(calcConfigName, resultType));
      }
    }
    return resultKeys;
  }

  @Override
  public Set<ResultKey> getRequestedMarketDataResultKeys() {
    final Set<ResultKey> resultKeys = new LinkedHashSet<>();
    for (final String calcConfigName : _viewDefinition.getAllCalculationConfigurationNames()) {
      final ViewCalculationConfiguration calculationConfiguration = _viewDefinition.getCalculationConfiguration(calcConfigName);
      for (final ValueRequirement valueRequirement : calculationConfiguration.getSpecificRequirements()) {
        final ResultType resultType = ResultType.builder().valueRequirementName(valueRequirement.getValueName()).properties(valueRequirement.getConstraints()).build();
        resultKeys.add(ResultKey.of(calcConfigName, resultType));
      }
    }
    return resultKeys;
  }

  @Override
  public Set<ResultKey> getAllResolvedResultKeys() {
    return _resultKeys;
  }

  @Override
  public Map<ResultKey, ComputedValueResult> getResultsForTarget(final TargetKey targetKey) {
    return _results.get(targetKey);
  }

  /**
   * Get an ordered set of the portfolio target keys.  This will match the structure of the portfolio.
   * <pre>
   *   // include only trades
   *   getPortfolioTargetKeys(EnumSet.of(PortfolioTarget.TRADE)
   *   // include each portfolio node, followed by the list of trades in each one (recursively)
   *   getPortfolioTargetKeys(EnumSet.of(PortfolioTarget.PORFOLIO_NODE PortfolioTarget.TRADE)
   * </pre>
   * @param includeTargets  a set of the PortfolioTarget types you want to include in the list
   * @return an ordered list of keys, matching the portfolio structure ordering
   */
  @Override
  public List<TargetKey> getTargetKeys(final EnumSet<TargetType> includeTargets) {
    final List<TargetKey> targetKeys = new ArrayList<>();
    final EnumSet<TargetType> portfolioTargets = EnumSet.copyOf(includeTargets);
    portfolioTargets.retainAll(PORTFOLIO_TARGETS);
    if (!portfolioTargets.isEmpty()) {
      final Portfolio portfolio = _positionSource.getPortfolio(_viewDefinition.getPortfolioId(), _resultModel.getVersionCorrection());
      final PortfolioNode root = portfolio.getRootNode();
      walkPortfolio(targetKeys, null, root, includeTargets);
    }
    if (includeTargets.contains(TargetType.MARKET_DATA)) {
      targetKeys.addAll(_marketDataTargetKeys);
    }
    if (includeTargets.contains(TargetType.PRIMITIVE)) {
      targetKeys.addAll(_primitiveTargetKeys);
    }
    if (includeTargets.contains(TargetType.LEGACY)) {
      targetKeys.addAll(_legacyTargetKeys);
    }
    return targetKeys;
  }

  private void walkPortfolio(final List<TargetKey> targetKeys, final String parentPath, final PortfolioNode node, final EnumSet<TargetType> includeTargets) {
    String nodePath;
    if (parentPath == null) {
      nodePath = escapeSeparator(node.getName());
    } else {
      nodePath = parentPath + PORTFOLIO_SEPARATOR + escapeSeparator(node.getName());
    }
    if (includeTargets.contains(TargetType.PORTFOLIO_NODE)) {
      targetKeys.add(PortfolioNodeTargetKey.of(nodePath));
    }
    for (final Position position : node.getPositions()) {
      if (includeTargets.contains(TargetType.POSITION)) {
        targetKeys.add(buildTargetKey(position));
      }
      for (final Trade trade : position.getTrades()) {
        if (includeTargets.contains(TargetType.TRADE)) {
          targetKeys.add(buildTargetKey(trade));
        }
      }
    }
    for (final PortfolioNode child : node.getChildNodes()) {
      walkPortfolio(targetKeys, nodePath, child, includeTargets);
    }
  }

  private String escapeSeparator(final String nodeName) {
    return nodeName.replace(PORTFOLIO_SEPARATOR, "\\" + PORTFOLIO_SEPARATOR);
  }

  private final EnumSet<TargetType> PORTFOLIO_TARGETS = EnumSet.of(TargetType.PORTFOLIO_NODE, TargetType.POSITION, TargetType.TRADE);

}
