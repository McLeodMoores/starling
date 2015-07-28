/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a
 * {@link StripInstrumentType#FUTURE} or {@link StripInstrumentType#BANKERS_ACCEPTANCE} strip. If a map for
 * {@link com.opengamma.financial.analytics.ircurve.strips.RateFutureNode}s is already present, this class
 * will overwrite that entry.
 */
public class FutureInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FutureInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}. The strip instrument type must be
   * either {@link StripInstrumentType#FUTURE} or {@link StripInstrumentType#BANKERS_ACCEPTANCE}.
   * @param type  the strip instrument type, not null
   */
  public FutureInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * The strip instrument type must be either {@link StripInstrumentType#FUTURE} or {@link StripInstrumentType#BANKERS_ACCEPTANCE}.
   * @param type  the strip instrument type, not null
   * @param renamingFunction  the renaming function, not null
   */
  public FutureInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.BANKERS_ACCEPTANCE == type || StripInstrumentType.FUTURE == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getRateFutureNodeIds() != null;
  }

  @Override
  protected CurveNodeIdMapper.Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.rateFutureNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    switch (getType()) {
      case BANKERS_ACCEPTANCE:
        return identifiers.getFutureInstrumentProviders();
      case FUTURE:
        return identifiers.getFutureInstrumentProviders();
      default:
        LOGGER.warn("Could not find instrument provider method for {}", getType());
        return Collections.emptyMap();
    }
  }
}
