/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;

/**
 *
 */
public class InstrumentTimeProvider extends InstrumentDerivativeVisitorAdapter<Void, Double> {

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla option) {
    return option.getUnderlyingForex().getPaymentTime();
  }

}
