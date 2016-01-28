/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A target key for access to particular trades, referred to by the correlation ids supplied during trade creation.
 */
public final class TradeTargetKey implements TargetKey {
  /** The correlation id of the trade */
  private final ExternalId _correlationId;

  /**
   * Restricted constructor.
   * @param correlationId  the correlation id, not null
   */
  private TradeTargetKey(final ExternalId correlationId) {
    _correlationId = correlationId;
  }

  /**
   * Static factory method used to create instances.
   * @param correlationId  the correlation id, which must be unique to this this target, that was included in the trade, not null.
   * @return the trade target key, not null
   */
  public static TradeTargetKey of(final ExternalId correlationId) {
    ArgumentChecker.notNull(correlationId, "correlationId");
    return new TradeTargetKey(correlationId);
  }

  /**
   * Gets the correlation id.
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
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof TradeTargetKey)) {
      return false;
    }
    final TradeTargetKey other = (TradeTargetKey) o;
    return other._correlationId.equals(_correlationId);
  }

  @Override
  public String toString() {
    return "TradeTargetKey[correlationId=" + _correlationId + "]";
  }
}
