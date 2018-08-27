/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated probably not needed any more
 */
@Deprecated
/* package */ class UnderlyingSnapshot {

  private final MarketDataSnapshot _snapshot;
  private final Set<ValueRequirement> _requirements;

  /* package */ UnderlyingSnapshot(final MarketDataSnapshot snapshot, final Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    ArgumentChecker.notNull(requirements, "requirements");
    _snapshot = snapshot;
    _requirements = requirements;
  }

  /* package */ MarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

  /* package */ Set<ValueRequirement> getRequirements() {
    return _requirements;
  }
}
