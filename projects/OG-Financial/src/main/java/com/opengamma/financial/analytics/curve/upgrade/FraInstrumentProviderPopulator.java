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
 * FRA {@link StripInstrumentType}s. If a map for {@link com.opengamma.financial.analytics.ircurve.strips.FRANode}
 * is already present, this class will overwrite that entry.
 * <p>
 * The instrument provider name must be supplied, as there is a many-to-one mapping
 * from FRA strip instrument types to FRA nodes. The getter method of the
 * {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}
 * is called using reflection.
 * <p>
 * The supported types of strip are:
 * <p>
 * <ul>
 * <li> {@link StripInstrumentType#FRA_3M}
 * <li> {@link StripInstrumentType#FRA_6M}
 * </ul>
 *
 */
public class FraInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FraInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}. The strip instrument type must be either
   * {@link StripInstrumentType#FRA_3M} or {@link StripInstrumentType#FRA_6M}.
   * @param type  the strip instrument type, not null
   */
  public FraInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * The strip instrument type must be either {@link StripInstrumentType#FRA_3M} or {@link StripInstrumentType#FRA_6M}.
   * @param type  the strip instrument type, not null
   * @param renamingFunction  the renaming function, not null
   */
  public FraInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.FRA_3M == type || StripInstrumentType.FRA_6M == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getFRANodeIds() != null;
  }

  @Override
  protected CurveNodeIdMapper.Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.fraNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    switch (getType()) {
      case FRA_3M:
        return identifiers.getFra3MInstrumentProviders();
      case FRA_6M:
        return identifiers.getFra6MInstrumentProviders();
      default:
        LOGGER.warn("Could not find instrument provider method for {}", getType());
        return Collections.emptyMap();
    }
  }
}
