/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A target key for access to particular positions, referred to by the correlation ids you supplied during trade creation.
 */
public class PositionTargetKey implements TargetKey {
  private final ExternalId _correlationId;

  private PositionTargetKey(final ExternalId correlationId) {
    _correlationId = correlationId;
  }

  /**
   * Static factory method used to create instances.
   * @param correlationId  the correlation id, which must be unique to this this target, that was included in the trade you want to reference, not null.
   * @return the position target key, not null
   */
  public static PositionTargetKey of(final ExternalId correlationId) {
    ArgumentChecker.notNull(correlationId, "correlationId");
    return new PositionTargetKey(correlationId);
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
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof PositionTargetKey)) {
      return false;
    }
    PositionTargetKey other = (PositionTargetKey) o;
    return other._correlationId.equals(_correlationId);
  }

  @Override
  public String toString() {
    return "PositionTargetKey[correlationId=" + _correlationId + "]";
  }
}
