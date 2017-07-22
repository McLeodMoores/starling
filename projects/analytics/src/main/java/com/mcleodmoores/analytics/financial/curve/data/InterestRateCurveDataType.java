/**
 *
 */
package com.mcleodmoores.analytics.financial.curve.data;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.util.money.Currency;

/**
 * @author emcleod
 *
 */
public enum InterestRateCurveDataType /*implements CurveDataType*/ {

  OVERNIGHT_DEPOSIT {
    //@Override
    public GeneratorInstrument<?> getInstrumentGenerator(final String name, final Currency currency, final Period period) {
      return null; //new GeneratorDepositON(name, currency, holidays);
    }
  },

  OIS;

}
