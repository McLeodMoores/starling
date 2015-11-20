/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a
 * {@link com.opengamma.financial.analytics.ircurve.StripInstrumentType#CONTINUOUS_ZERO_DEPOSIT}
 * strip. If a map for {@link com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode}
 * is already present, this class will overwrite that entry.
 *
 */
public class ContinuouslyCompoundedRateInstrumentProviderPopulator extends InstrumentProviderPopulator {

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public ContinuouslyCompoundedRateInstrumentProviderPopulator() {
    this(new DefaultCsbcRenamingFunction());
  }

  /**
   * Sets the method name to null, as the getter name for continuously-compounded rate strips is known.
   * @param renamingFunction  the renaming function, not null
   */
  public ContinuouslyCompoundedRateInstrumentProviderPopulator(final Function2<String, String, String> renamingFunction) {
    super(StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getContinuouslyCompoundedRateNodeIds() != null;
  }

  @Override
  protected Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.continuouslyCompoundedRateNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    return identifiers.getContinuousZeroDepositInstrumentProviders();
  }

}
