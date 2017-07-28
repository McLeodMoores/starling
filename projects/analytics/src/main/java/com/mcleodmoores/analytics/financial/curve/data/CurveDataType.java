/**
 *
 */
package com.mcleodmoores.analytics.financial.curve.data;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.util.money.Currency;

/**
 *
 */
public interface CurveDataType {

  GeneratorInstrument<?> getInstrumentGenerator(String name, Currency currency, Period period);

}
