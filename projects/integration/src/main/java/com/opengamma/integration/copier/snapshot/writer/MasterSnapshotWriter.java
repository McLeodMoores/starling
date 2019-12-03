/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * A class that writes securities and snapshot positions and trades to the OG masters.
 */
public class MasterSnapshotWriter implements SnapshotWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MasterSnapshotWriter.class);

  private final MarketDataSnapshotMaster _snapshotMaster;
  private final ManageableMarketDataSnapshot _snapshot;

  /**
   * Create a master snapshot writer.
   * 
   * @param snapshotMaster
   *          The snapshot master to which to write the snapshot
   */

  public MasterSnapshotWriter(final MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
    _snapshot = new ManageableMarketDataSnapshot();
  }

  @Override
  public void flush() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeCurves(final Map<CurveKey, CurveSnapshot> curves) {
    if (curves != null) {
      _snapshot.setCurves(curves);
    }
  }

  @Override
  public void writeGlobalValues(final UnstructuredMarketDataSnapshot globalValues) {
    if (globalValues != null) {
      _snapshot.setGlobalValues((ManageableUnstructuredMarketDataSnapshot) globalValues);
    }
  }

  @Override
  public void writeVolatilitySurface(final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {
    if (volatilitySurface != null) {
      _snapshot.setVolatilitySurfaces(volatilitySurface);
    }
  }

  @Override
  public void writeYieldCurves(final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    if (yieldCurves != null) {
      _snapshot.setYieldCurves(yieldCurves);
    }
  }

  @Override
  public void writeName(final String name) {
    if (name != null) {
      _snapshot.setName(name);
    }
  }

  @Override
  public void writeBasisViewName(final String basisName) {
    if (basisName != null) {
      _snapshot.setBasisViewName(basisName);
    }
  }

  @Override
  public void close() {
    _snapshotMaster.add(new MarketDataSnapshotDocument(_snapshot));
  }
}
