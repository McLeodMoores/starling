/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A target key that allows access to primitive target types.
 */
public final class PrimitiveTargetKey implements TargetKey {
  /** The target specification */
  private final ComputationTargetSpecification _targetSpecification;

  /**
   * Restricted constructor.
   * @param targetSpecification  the target specification, not null
   */
  private PrimitiveTargetKey(final ComputationTargetSpecification targetSpecification) {
    _targetSpecification = targetSpecification;
  }

  /**
   * Static factory method used to create instances.
   * @param targetSpecification  the underlying target specification associated with this target, not null
   * @return  the legacy target key, not null
   */
  public static PrimitiveTargetKey of(final ComputationTargetSpecification targetSpecification) {
    ArgumentChecker.notNull(targetSpecification, "targetSpecification");
    return new PrimitiveTargetKey(targetSpecification);
  }

  @Override
  public int hashCode() {
    return _targetSpecification.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof PrimitiveTargetKey)) {
      return false;
    }
    final PrimitiveTargetKey other = (PrimitiveTargetKey) o;
    return other._targetSpecification.equals(_targetSpecification);
  }

  @Override
  public String toString() {
    return "PrimitiveTargetKey[targetSpecification=" + _targetSpecification.toString() + "]";
  }
}
