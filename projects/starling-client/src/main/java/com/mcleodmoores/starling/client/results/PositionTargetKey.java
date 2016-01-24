/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A target key for access to particular positions, referred to by the correlation ids that were supplied during trade creation.
 */
public final class PositionTargetKey implements TargetKey {
  /** The correlation id */
  private final ExternalId _correlationId;

  /**
   * Restricted constructor.
   * @param correlationId  the correlation id, not null
   */
  private PositionTargetKey(final ExternalId correlationId) {
    _correlationId = correlationId;
  }

  /**
   * Static factory method used to create instances.
   * @param correlationId  the correlation id, which must be unique to this this target, that was included in the trade referenced in the position, not null.
   * @return the position target key, not null
   */
  public static PositionTargetKey of(final ExternalId correlationId) {
    ArgumentChecker.notNull(correlationId, "correlationId");
    return new PositionTargetKey(correlationId);
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
    if (!(o instanceof PositionTargetKey)) {
      return false;
    }
    final PositionTargetKey other = (PositionTargetKey) o;
    return other._correlationId.equals(_correlationId);
  }

  @Override
  public String toString() {
    return "PositionTargetKey[correlationId=" + _correlationId + "]";
  }
}
