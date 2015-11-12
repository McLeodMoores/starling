/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A taget key that allows access to legacy target types that are occasionally required.  Included more for completeness than anything else.
 */
public final class LegacyTargetKey implements TargetKey {
  private final ComputationTargetSpecification _targetSpecification;

  private LegacyTargetKey(final ComputationTargetSpecification targetSpecification) {
    _targetSpecification = targetSpecification;
  }

  /**
   * Static factory method used to create instances.
   * @param targetSpecification  the underlying target specifciation associated with this target, not null
   * @return the legacy target key, not null
   */
  public static LegacyTargetKey of(final ComputationTargetSpecification targetSpecification) {
    ArgumentChecker.notNull(targetSpecification, "targetSpecification");
    return new LegacyTargetKey(targetSpecification);
  }

  @Override
  public int hashCode() {
    return _targetSpecification.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof LegacyTargetKey)) {
      return false;
    }
    LegacyTargetKey other = (LegacyTargetKey) o;
    return other._targetSpecification.equals(_targetSpecification);
  }

  @Override
  public String toString() {
    return "LegacyTargetKey[targetSpecification=" + _targetSpecification.toString() + "]";
  }
}
