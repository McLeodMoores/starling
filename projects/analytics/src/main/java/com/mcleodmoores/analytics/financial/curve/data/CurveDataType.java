/**
 *
 */
package com.mcleodmoores.analytics.financial.curve.data;

import org.threeten.bp.Period;

import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.money.Currency;

/**
 *
 */
public interface CurveDataType {

  <T extends CurveDataConvention, U extends InstrumentDerivative> U generateInstrument(T convention, String name, Currency currency, Period period);

}
