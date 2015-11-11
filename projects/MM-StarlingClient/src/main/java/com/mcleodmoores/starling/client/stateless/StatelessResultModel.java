package com.mcleodmoores.starling.client.stateless;

import com.mcleodmoores.starling.client.results.PositionTargetKey;
import com.mcleodmoores.starling.client.results.ResultKey;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.TargetKey;
import com.mcleodmoores.starling.client.results.TradeTargetKey;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.id.ExternalId;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A ResultModel wrapper that marshals between user-supplied correlation ids, and the prefixed ids used by the StatelessAnalyticService
 * to prevent clashes between clients.
 */
public class StatelessResultModel implements ResultModel {
  private final String _sessionPrefix;
  private final ResultModel _underlying;

  /**
   * Create a stateless result model from an underlying result model.
   * @param underlying  the underlying result model
   * @param sessionPrefix  the session prefix for the correlation ids
   */
  public StatelessResultModel(final ResultModel underlying, final String sessionPrefix) {
    _underlying = underlying;
    _sessionPrefix = sessionPrefix;
  }
  @Override
  public Set<ResultKey> getRequestedPortfolioResultKeys() {
    return _underlying.getRequestedPortfolioResultKeys();
  }

  @Override
  public Set<ResultKey> getRequestedMarketDataResultKeys() {
    return _underlying.getRequestedMarketDataResultKeys();
  }

  @Override
  public Set<ResultKey> getAllResolvedResultKeys() {
    return _underlying.getAllResolvedResultKeys();
  }

  @Override
  public Map<ResultKey, ComputedValueResult> getResultsForTarget(final TargetKey targetKey) {
    if (targetKey instanceof PositionTargetKey) {
      PositionTargetKey positionTargetKey = (PositionTargetKey) targetKey;
      PositionTargetKey sessionPositionTargetKey = PositionTargetKey.of(addSessionPrefix(positionTargetKey.getCorrelationId()));
      return _underlying.getResultsForTarget(sessionPositionTargetKey);
    } else if (targetKey instanceof TradeTargetKey) {
      TradeTargetKey tradeTargetKey = (TradeTargetKey) targetKey;
      TradeTargetKey sessionTradeTargetKey = TradeTargetKey.of(addSessionPrefix(tradeTargetKey.getCorrelationId()));
      return _underlying.getResultsForTarget(sessionTradeTargetKey);
    } else {
      return _underlying.getResultsForTarget(targetKey);
    }
  }

  @Override
  public List<TargetKey> getTargetKeys(final EnumSet<TargetType> includeTargets) {
    final List<TargetKey> targetKeys = _underlying.getTargetKeys(includeTargets);
    if (includeTargets.contains(TargetType.POSITION) || includeTargets.contains(TargetType.TRADE)) {
      List<TargetKey> results = new ArrayList<TargetKey>();
      for (TargetKey targetKey : targetKeys) {
        if (targetKey instanceof PositionTargetKey) {
          PositionTargetKey positionTargetKey = (PositionTargetKey) targetKey;
          results.add(PositionTargetKey.of(removeSessionPrefix(positionTargetKey.getCorrelationId())));
        } else if (targetKey instanceof TradeTargetKey) {
          TradeTargetKey tradeTargetKey = (TradeTargetKey) targetKey;
          results.add(TradeTargetKey.of(removeSessionPrefix(tradeTargetKey.getCorrelationId())));
        } else {
          results.add(targetKey);
        }
      }
      return results;
    } else {
      return targetKeys;
    }
  }

  private ExternalId removeSessionPrefix(final ExternalId sessionCorrelationId) {
    return ExternalId.of(sessionCorrelationId.getScheme(), sessionCorrelationId.getValue().replaceFirst(_sessionPrefix, ""));
  }

  private ExternalId addSessionPrefix(final ExternalId sessionCorrelationId) {
    return ExternalId.of(sessionCorrelationId.getScheme(), _sessionPrefix + sessionCorrelationId.getValue());
  }
}
