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
 * Class that populates a curve node id mapper with the curve instrument providers for
 * {@link StripInstrumentType#BASIS_SWAP} and {@link StripInstrumentType#TENOR_SWAP}.
 * If a map for {@link com.opengamma.financial.analytics.ircurve.strips.SwapNode} is
 * already present, this class will overwrite that entry.
 */
//TODO rename function should include basis swap tenor information?
public class BasisSwapInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(BasisSwapInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}. The strip instrument type must be either
   * {@link StripInstrumentType#BASIS_SWAP} or {@link StripInstrumentType#TENOR_SWAP}.
   * @param type  the strip instrument type, not null
   */
  public BasisSwapInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * The strip instrument type must be either {@link StripInstrumentType#BASIS_SWAP} or {@link StripInstrumentType#TENOR_SWAP}.
   * @param renamingFunction  the renaming function, not null
   * @param type  the strip instrument type, not null
   */
  public BasisSwapInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.BASIS_SWAP == type || StripInstrumentType.TENOR_SWAP == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getSwapNodeIds() != null;
  }

  @Override
  protected CurveNodeIdMapper.Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.swapNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    switch (getType()) {
      case BASIS_SWAP:
        return identifiers.getBasisSwapInstrumentProviders();
      case TENOR_SWAP:
        return identifiers.getTenorSwapInstrumentProviders();
      default:
        LOGGER.warn("Could not find instrument provider method for {}", getType());
        return Collections.emptyMap();
    }
  }

}
