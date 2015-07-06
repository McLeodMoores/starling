/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for
 * cash {@link StripInstrumentType}s. If a map for
 * {@link com.opengamma.financial.analytics.ircurve.strips.CashNode} is already present,
 * this class will overwrite that entry.
 * <p>
 * The instrument provider name must be supplied, as there is a many-to-one mapping
 * from cash strip instrument types to cash nodes. The getter method of the
 * {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}
 * is called using reflection.
 * <p>
 * The supported types of strip are:
 * <p>
 * <ul>
 * <li> {@link StripInstrumentType#CASH}
 * <li> {@link StripInstrumentType#CDOR}
 * <li> {@link StripInstrumentType#CIBOR}
 * <li> {@link StripInstrumentType#EURIBOR}
 * <li> {@link StripInstrumentType#LIBOR}
 * <li> {@link StripInstrumentType#STIBOR}
 * </ul>
 *
 */
public class CashInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(CashInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}. The strip instrument type must be one of
   * {@link StripInstrumentType#CASH}, {@link StripInstrumentType#CDOR}, {@link StripInstrumentType#CIBOR},
   * {@link StripInstrumentType#EURIBOR}, {@link StripInstrumentType#LIBOR} or {@link StripInstrumentType#STIBOR}
   * @param type  the strip instrument type, not null
   */
  public CashInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * The strip instrument type must be one of {@link StripInstrumentType#CASH}, {@link StripInstrumentType#CDOR}, {@link StripInstrumentType#CIBOR},
   * {@link StripInstrumentType#EURIBOR}, {@link StripInstrumentType#LIBOR} or {@link StripInstrumentType#STIBOR}.
   * @param type  the strip instrument type, not null
   * @param renamingFunction  the renaming function, not null
   */
  public CashInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(type, renamingFunction);
  }

  @Override
  protected boolean isValidStripInstrumentType(final StripInstrumentType type) {
    return StripInstrumentType.CASH == type || StripInstrumentType.CDOR == type || StripInstrumentType.CIBOR == type
        || StripInstrumentType.EURIBOR == type || StripInstrumentType.LIBOR == type || StripInstrumentType.STIBOR == type;
  }

  @Override
  protected boolean areNodesPopulated(final CurveNodeIdMapper idMapper) {
    return idMapper.getCashNodeIds() != null;
  }

  @Override
  protected Builder populateNodeIds(final CurveNodeIdMapper.Builder idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    return idMapper.cashNodeIds(instrumentProviders);
  }

  @Override
  protected Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    switch (getType()) {
      case CASH:
        return identifiers.getCashInstrumentProviders();
      case CDOR:
        return identifiers.getCDORInstrumentProviders();
      case CIBOR:
        return identifiers.getCiborInstrumentProviders();
      case EURIBOR:
        return identifiers.getEuriborInstrumentProviders();
      case LIBOR:
        return identifiers.getLiborInstrumentProviders();
      case STIBOR:
        return identifiers.getStiborInstrumentProviders();
      default:
        LOGGER.warn("Could not find instrument provider method for {}", getType());
        return Collections.emptyMap();
    }
  }
}
