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
import com.opengamma.util.time.Tenor;

/**
 * An instrument provider converter that does not perform conversions: to be used
 * in cases where a mapping is not possible or desirable for a particular strip
 * instrument type, e.g. {@link StripInstrumentType#FRA}, where there is no
 * information about the reset tenor, and so a {@link com.opengamma.financial.analytics.ircurve.strips.FRANode}
 * cannot be created.
 */
public class NoOpInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(NoOpInstrumentProviderPopulator.class);

  /**
   * Sets the getter method name to null.
   * @param type  the strip instrument type, not null
   */
  public NoOpInstrumentProviderPopulator(final StripInstrumentType type) {
    super(type);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return true;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return false;
  }

  @Override
  protected CurveNodeIdMapper.Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    LOGGER.error("Cannot populate node ids");
    return idMapper;
  }
  /**
   * Returns an empty map.
   * @param csbc  the curve specification builder configuration, not used
   * @return  an empty map
   */
  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    LOGGER.error("Cannot convert strips of type {}", getType());
    return Collections.emptyMap();
  }

}
