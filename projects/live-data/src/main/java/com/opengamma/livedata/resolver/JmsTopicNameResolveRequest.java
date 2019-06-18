/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * A market data resolve request for a JMS topic.
 */
public class JmsTopicNameResolveRequest {

  /**
   * what market data the server is going to publish
   */
  private final ExternalId _marketDataUniqueId;

  /**
   * what normalization rule will be applied to the raw market data
   */
  private final NormalizationRuleSet _normalizationRule;

  /**
   * @param marketDataUniqueId
   *          the id of the market data
   * @param normalizationRule
   *          the normalization rule
   */
  public JmsTopicNameResolveRequest(
      final ExternalId marketDataUniqueId,
      final NormalizationRuleSet normalizationRule) {
    _marketDataUniqueId = marketDataUniqueId;
    _normalizationRule = normalizationRule;

  }

  /**
   * @return what market data the server is going to publish
   */
  public ExternalId getMarketDataUniqueId() {
    return _marketDataUniqueId;
  }

  /**
   * @return what normalization rule will be applied to the raw market data
   */
  public NormalizationRuleSet getNormalizationRule() {
    return _normalizationRule;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
