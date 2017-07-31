/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.util.time.Tenor;

/**
 * Interface for conventions for particular instrument types. The information in the convention
 * is then used to construct the appropriate {@link InstrumentDefinition}. The usual use case is
 * to construct one convention and use this convention to create multiple instruments with different
 * rates, tenors, etc.
 *
 * @param <U>  the type of the instrument that can be created
 */
public interface CurveDataConvention<U extends InstrumentDefinition<?>> {

  /**
   * Represents how dates at the end of the month should be handled e.g. if a start date and end
   * date are in different months.
   */
  enum EndOfMonthConvention {
    /**
     * Take the end of month into account when calculating dates.
     */
    ADJUST_FOR_END_OF_MONTH,
    /**
     * Ignore the end of the month when calculating dates.
     */
    IGNORE_END_OF_MONTH
  }

  /**
   * Creates an instrument from the convention. Depending on the instrument type, not all fields are required.
   * @param date  the date that the instrument is constructed
   * @param startTenor  the start tenor of the instrument
   * @param endTenor  the end tenor of the instrument
   * @param notional  the notional
   * @param fixedRate  the fixed rate
   * @return  the instrument
   */
  U toCurveInstrument(ZonedDateTime date, Tenor startTenor, Tenor endTenor, double notional, double fixedRate);
}
