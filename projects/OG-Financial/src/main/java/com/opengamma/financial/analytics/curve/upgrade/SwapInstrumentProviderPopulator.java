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
 * swap {@link com.opengamma.financial.analytics.ircurve.StripInstrumentType}s. If a map
 * for {@link com.opengamma.financial.analytics.ircurve.strips.SwapNode}
 * is already present, this class will overwrite that entry.
 * <p>
 * The instrument provider name must be supplied, as there is a many-to-one mapping
 * from swap strip instrument types to swap nodes. The getter method of the
 * {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}
 * is called using reflection.
 * <p>
 * The supported types of strip are:
 * <p>
 * <ul>
 * <li> {@link StripInstrumentType#SWAP_3M}
 * <li> {@link StripInstrumentType#SWAP_6M}
 * <li> {@link StripInstrumentType#SWAP_12M}
 * <li> {@link StripInstrumentType#SWAP_28D}
 * <li> {@link StripInstrumentType#OIS_SWAP}
 * </ul>
 *
 */
public class SwapInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(SwapInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}. The strip instrument type must be one of
   * {@link StripInstrumentType#SWAP_3M}, {@link StripInstrumentType#SWAP_6M}, {@link StripInstrumentType#SWAP_12M}
   * {@link StripInstrumentType#SWAP_28D} or {@link StripInstrumentType#OIS_SWAP}.
   * @param type  the strip instrument type, not null
   */
  public SwapInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * The strip instrument type must be one of {@link StripInstrumentType#SWAP_3M}, {@link StripInstrumentType#SWAP_6M},
   * {@link StripInstrumentType#SWAP_12M} {@link StripInstrumentType#SWAP_28D} or {@link StripInstrumentType#OIS_SWAP}.
   * @param type  the strip instrument type, not null
   * @param renamingFunction  the renaming function, not null
   */
  public SwapInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.SWAP_12M == type || StripInstrumentType.SWAP_28D == type || StripInstrumentType.SWAP_3M == type
        || StripInstrumentType.SWAP_6M == type || StripInstrumentType.OIS_SWAP == type;
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
      case SWAP_12M:
        return identifiers.getSwap12MInstrumentProviders();
      case SWAP_28D:
        return identifiers.getSwap28DInstrumentProviders();
      case SWAP_3M:
        return identifiers.getSwap3MInstrumentProviders();
      case SWAP_6M:
        return identifiers.getSwap6MInstrumentProviders();
      case OIS_SWAP:
        return identifiers.getOISSwapInstrumentProviders();
      default:
        LOGGER.warn("Could not find instrument provider method for {}", getType());
        return Collections.emptyMap();
    }
  }

}
