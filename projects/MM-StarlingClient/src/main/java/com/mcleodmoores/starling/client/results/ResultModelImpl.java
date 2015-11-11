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
import com.opengamma.util.tuple.Pair;

/**
 * A wrapper class around ViewComputationResultModel that makes results more accessible.
 */
public class ResultModelImpl implements ResultModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResultModelImpl.class);

  private final ViewDefinition _viewDefinition;
  private final ViewComputationResultModel _resultModel;
  private final PositionSource _positionSource;
  private final Set<ResultKey> _resultKeys;
  private final Set<ResultKey> _portfolioNodeResultKeys;
  private final Set<ResultKey> _positionResultKeys;
  private final Set<ResultKey> _tradeResultKeys;
  private final Set<ResultKey> _marketDataResultKeys;
  private final Set<ResultKey> _legacyResultKeys;
  private final Set<TargetKey> _allTargetKeys;
  private final Set<TradeTargetKey> _tradeTargetKeys;
  private final Set<PositionTargetKey> _positionTargetKeys;
  private final Set<PortfolioNodeTargetKey> _portfolioNodeTargetKeys;
  private final Set<MarketDataTargetKey> _marketDataTargetKeys;
  private final Set<LegacyTargetKey> _legacyTargetKeys;
  private final Map<TargetKey, Map<ResultKey, ComputedValueResult>> _results;
  private static final String CORRELATION_ID_ATTRIBUTE = ManageableTrade.meta().providerId().name();

  public ResultModelImpl(final ViewComputationResultModel resultModel, final ViewDefinition viewDefinition,
                         final PositionSource positionSource) {
    _resultModel = resultModel;
    _viewDefinition = viewDefinition;
    _positionSource = positionSource;
    _resultKeys = new HashSet<>();
    _portfolioNodeResultKeys = new HashSet<>();
    _positionResultKeys = new HashSet<>();
    _tradeResultKeys = new HashSet<>();
    _marketDataResultKeys = new HashSet<>();
    _legacyResultKeys = new HashSet<>();
    _allTargetKeys = new HashSet<>();
    _tradeTargetKeys = new HashSet<>();
    _positionTargetKeys = new HashSet<>();
    _portfolioNodeTargetKeys = new HashSet<>();
    _marketDataTargetKeys = new HashSet<>();
    _legacyTargetKeys = new HashSet<>();
    _results = new HashMap<>();
    buildModel();
  }

  private void buildModel() {

    // Loop over the 'column sets' aka calculation configurations
    for (String calcConfigName : _resultModel.getCalculationConfigurationNames()) {
      final ViewCalculationResultModel calculationResult = _resultModel.getCalculationResult(calcConfigName);
      final ViewCalculationConfiguration calculationConfiguration = _viewDefinition.getCalculationConfiguration(calcConfigName);
      final Collection<ComputationTargetSpecification> allTargets = calculationResult.getAllTargets();
      // Loop over all the targets (portfolio and market data)
      for (ComputationTargetSpecification target : allTargets) {
        final ComputationTargetType targetType = target.getType();
        // get results for this target, convert each target into a target key and result key and store in map of maps for retrieval.
        final Collection<ComputedValueResult> allValues = calculationResult.getAllValues(target);
        if (targetType.equals(ComputationTargetType.TRADE)) {
          final Trade trade = _positionSource.getTrade(target.getUniqueId());
          final TradeTargetKey targetKey = buildTargetKey(trade);
          for (ComputedValueResult result : allValues) {
            ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addTradeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.POSITION)) {
          final Position position = _positionSource.getPosition(target.getUniqueId());
          //final Security security = trade.getSecurityLink().resolve(_securitySource);
          final PositionTargetKey targetKey = buildTargetKey(position);
          for (ComputedValueResult result : allValues) {
            ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addTradeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
          final String portfolioPath = getPortfolioNodePath(target.getUniqueId(), _resultModel.getVersionCorrection());
          final PortfolioNodeTargetKey targetKey = PortfolioNodeTargetKey.of(portfolioPath);
          for (ComputedValueResult result : allValues) {
            ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addPortfolioNodeResult(targetKey, resultKey, result);
          }
        } else if (targetType.equals(ComputationTargetType.NULL)) {
          final MarketDataTargetKey targetKey = MarketDataTargetKey.instance();
          for (ComputedValueResult result : allValues) {
            ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addMarketDataResult(targetKey, resultKey, result);
          }
        } else {
          final LegacyTargetKey targetKey = LegacyTargetKey.of(target);
          for (ComputedValueResult result : allValues) {
            ResultKey resultKey = ResultKey.of(calcConfigName, convertValueSpec(result.getSpecification()));
            addLegacyResult(targetKey, resultKey, result);
          }
        }
      }
    }
  }

  private ResultType convertValueSpec(ValueSpecification valueSpec) {
    ResultType.Builder builder = ResultType.builder();
    builder.valueRequirementName(valueSpec.getValueName());
    builder.properties(valueSpec.getProperties());
    return builder.build();
  }

  private void addLegacyResult(LegacyTargetKey legacyKey, ResultKey resultKey, ComputedValueResult result) {
    _legacyTargetKeys.add(legacyKey);
    _legacyResultKeys.add(resultKey);
    addResultInternal(legacyKey, resultKey, result);
  }

  private void addMarketDataResult(MarketDataTargetKey marketDataResultKey, ResultKey resultKey, ComputedValueResult result) {
    _marketDataTargetKeys.add(marketDataResultKey);
    _marketDataResultKeys.add(resultKey);
    addResultInternal(marketDataResultKey, resultKey, result);
  }

  private void addPortfolioNodeResult(PortfolioNodeTargetKey portfolioNodeTargetKey, ResultKey resultKey, ComputedValueResult result) {
    _portfolioNodeTargetKeys.add(portfolioNodeTargetKey);
    _portfolioNodeResultKeys.add(resultKey);
    addResultInternal(portfolioNodeTargetKey, resultKey, result);
  }

  private void addTradeResult(PositionTargetKey positionTargetKey, ResultKey resultKey, ComputedValueResult result) {
    _positionTargetKeys.add(positionTargetKey);
    _tradeResultKeys.add(resultKey);
    addResultInternal(positionTargetKey, resultKey, result);
  }

  private void addTradeResult(TradeTargetKey tradeTargetKey, ResultKey resultKey, ComputedValueResult result) {
    _tradeTargetKeys.add(tradeTargetKey);
    _tradeResultKeys.add(resultKey);
    addResultInternal(tradeTargetKey, resultKey, result);
  }

  private void addResultInternal(TargetKey targetKey, ResultKey resultKey, ComputedValueResult result) {
    _allTargetKeys.add(targetKey);
    if (_results.containsKey(targetKey)) {
      _results.get(targetKey).put(resultKey, result);
    } else {
      Map<ResultKey, ComputedValueResult> map = new LinkedHashMap<>();
      map.put(resultKey, result);
      _results.put(targetKey, map);
    }
  }

  private String getPortfolioNodePath(UniqueId nodeId, VersionCorrection versionCorrection) {
    StringBuilder sb = new StringBuilder();
    walkPortfolioNodePath(sb, nodeId, versionCorrection);
    return sb.toString();
  }

  private void walkPortfolioNodePath(StringBuilder sb, UniqueId nodeId, VersionCorrection versionCorrection) {
    final PortfolioNode portfolioNode = _positionSource.getPortfolioNode(nodeId, _resultModel.getVersionCorrection());
    sb.insert(0, escapeSeparator(portfolioNode.getName()));
    if (portfolioNode.getParentNodeId() != null) {
      sb.insert(0, PORTFOLIO_SEPARATOR);
      walkPortfolioNodePath(sb, portfolioNode.getParentNodeId(), versionCorrection);
    }
  }

  private PositionTargetKey buildTargetKey(Position position) {
    final Map<String, String> attributes = position.getAttributes();
    if (attributes.containsKey(CORRELATION_ID_ATTRIBUTE)) {
      return PositionTargetKey.of(ExternalId.parse(attributes.get(CORRELATION_ID_ATTRIBUTE)));
    } else {
      LOGGER.warn("Could not find correlation id attribute (providerId) in position, returning first security link id instead.  "
          + "This means correlation ids aren't being persisted correctly");
      return PositionTargetKey.of(position.getSecurityLink().getExternalId().getExternalIds().first());
    }
  }

  private TradeTargetKey buildTargetKey(Trade trade) {
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
    for (String calcConfigName : _viewDefinition.getAllCalculationConfigurationNames()) {
      final ViewCalculationConfiguration calculationConfiguration = _viewDefinition.getCalculationConfiguration(calcConfigName);
      for (Pair<String, ValueProperties> requirement : calculationConfiguration.getAllPortfolioRequirements()) {
        ResultType resultType = ResultType.builder().valueRequirementName(requirement.getFirst()).properties(requirement.getSecond()).build();
        resultKeys.add(ResultKey.of(calcConfigName, resultType));
      }
    }
    return resultKeys;
  }

  @Override
  public Set<ResultKey> getRequestedMarketDataResultKeys() {
    final Set<ResultKey> resultKeys = new LinkedHashSet<>();
    for (String calcConfigName : _viewDefinition.getAllCalculationConfigurationNames()) {
      final ViewCalculationConfiguration calculationConfiguration = _viewDefinition.getCalculationConfiguration(calcConfigName);
      for (ValueRequirement valueRequirement : calculationConfiguration.getSpecificRequirements()) {
        ResultType resultType = ResultType.builder().valueRequirementName(valueRequirement.getValueName()).properties(valueRequirement.getConstraints()).build();
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
  public Map<ResultKey, ComputedValueResult> getResultsForTarget(TargetKey targetKey) {
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
   * @return an otdered list of keys, matching the portfolio structure ordering
   */
  @Override
  public List<TargetKey> getTargetKeys(final EnumSet<TargetType> includeTargets) {
    final List<TargetKey> targetKeys = new ArrayList<>();
    EnumSet<TargetType> portfolioTargets = EnumSet.copyOf(includeTargets);
    portfolioTargets.retainAll(PORTFOLIO_TARGETS);
    if (!portfolioTargets.isEmpty()) {
      final Portfolio portfolio = _positionSource.getPortfolio(_viewDefinition.getPortfolioId(), _resultModel.getVersionCorrection());
      final PortfolioNode root = portfolio.getRootNode();
      walkPortfolio(targetKeys, null, root, includeTargets);
    }
    if (includeTargets.contains(TargetType.MARKET_DATA)) {
      targetKeys.addAll(_marketDataTargetKeys);
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
    for (Position position : node.getPositions()) {
      if (includeTargets.contains(TargetType.POSITION)) {
        targetKeys.add(buildTargetKey(position));
      }
      for (Trade trade : position.getTrades()) {
        if (includeTargets.contains(TargetType.TRADE)) {
          targetKeys.add(buildTargetKey(trade));
        }
      }
    }
    for (PortfolioNode child : node.getChildNodes()) {
      walkPortfolio(targetKeys, nodePath, child, includeTargets);
    }
  }

  private String escapeSeparator(final String nodeName) {
    return nodeName.replace(PORTFOLIO_SEPARATOR, "\\" + PORTFOLIO_SEPARATOR);
  }

  private final EnumSet<TargetType> PORTFOLIO_TARGETS = EnumSet.of(TargetType.PORTFOLIO_NODE, TargetType.POSITION, TargetType.TRADE);

}
