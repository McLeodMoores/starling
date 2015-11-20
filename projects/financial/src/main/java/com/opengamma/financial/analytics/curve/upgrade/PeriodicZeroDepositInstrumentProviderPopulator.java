/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a
 * {@link com.opengamma.financial.analytics.ircurve.StripInstrumentType#PERIODIC_ZERO_DEPOSIT} strip. If a map for
 * {@link com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode} is already present,
 * this class will overwrite that entry.
 *
 */
public class PeriodicZeroDepositInstrumentProviderPopulator extends InstrumentProviderPopulator {

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public PeriodicZeroDepositInstrumentProviderPopulator() {
    this(new DefaultCsbcRenamingFunction());
  }

  /**
   * Sets the method name to null, as the getter name for periodic zero strips is known.
   * @param renamingFunction  the renaming function, not null
   */
  public PeriodicZeroDepositInstrumentProviderPopulator(final Function2<String, String, String> renamingFunction) {
    super(StripInstrumentType.PERIODIC_ZERO_DEPOSIT, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.PERIODIC_ZERO_DEPOSIT == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getPeriodicallyCompoundedRateNodeIds() != null;
  }

  @Override
  protected CurveNodeIdMapper.Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.periodicallyCompoundedRateNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    return identifiers.getPeriodicZeroDepositInstrumentProviders();
  }

}
