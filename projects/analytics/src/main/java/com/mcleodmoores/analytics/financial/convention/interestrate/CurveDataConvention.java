/**
 *
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public interface CurveDataConvention<U extends InstrumentDefinition<?>> {

  public enum EndOfMonthConvention {
    ADJUST_FOR_END_OF_MONTH,
    IGNORE_END_OF_MONTH
  }

  U toCurveInstrument(ZonedDateTime date, Tenor startTenor, Tenor endTenor, double notional, double fixedRate);
}
