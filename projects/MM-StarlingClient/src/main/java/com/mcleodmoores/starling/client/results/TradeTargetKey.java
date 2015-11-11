/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A target key for access to particular trades, referred to by the correlation ids you supplied during trade creation.
 */
public class TradeTargetKey implements TargetKey {
  private final ExternalId _correlationId;

  private TradeTargetKey(ExternalId correlationId) {
    _correlationId = correlationId;
  }

  /**
   * Static factory method used to create instances.
   * @param correlationId  the correlation id, which must be unique to this this target, that was included in the trade you want to reference, not null.
   * @return the position target key, not null
   */
  public static TradeTargetKey of(ExternalId correlationId) {
    ArgumentChecker.notNull(correlationId, "correlationId");
    return new TradeTargetKey(correlationId);
  }

  /**
   * @return the correlation id
   */
  public ExternalId getCorrelationId() {
    return _correlationId;
  }

  @Override
  public int hashCode() {
    return _correlationId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof TradeTargetKey)) {
      return false;
    }
    TradeTargetKey other = (TradeTargetKey) o;
    return other._correlationId.equals(_correlationId);
  }

  @Override
  public String toString() {
    return "TradeTargetKey[correlationId=" + _correlationId + "]";
  }
}
