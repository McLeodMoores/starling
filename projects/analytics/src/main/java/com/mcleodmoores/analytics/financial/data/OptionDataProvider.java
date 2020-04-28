/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.money.OrderedCurrencyPair;

/**
 *
 */
public class OptionDataProvider {
  private double _underlying;
  private FixedIncomeDataProvider _curves;
  private VolatilityProvider _volatilitySurface;

  public class DiscountFactorProvider extends InstrumentDerivativeVisitorAdapter<Void, Double> {

    @Override
    public Double visitForexOptionVanilla(final ForexOptionVanilla option) {
      return _curves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    }
  }

  public class ForwardProvider extends InstrumentDerivativeVisitorAdapter<Void, Double> {

    @Override
    public Double visitForexOptionVanilla(final ForexOptionVanilla option) {
      final double t = option.getUnderlyingForex().getPaymentTime();
      final double spot = _underlying;
      final double dfForeign = _curves.getDiscountFactor(option.getCurrency1(), t);
      final double dfDomestic = _curves.getDiscountFactor(option.getCurrency2(), t);
      return spot * dfForeign / dfDomestic;
    }
  }

  public class VolatilityData extends InstrumentDerivativeVisitorAdapter<Void, Double> {

    @Override
    public Double visitForexOptionVanilla(final ForexOptionVanilla option) {
      final OrderedCurrencyPair ccys = OrderedCurrencyPair.of(option.getCurrency1(), option.getCurrency2());
      return 0.;//_volatilitySurface.getVolatility(0, 0, 0);
    }
  }
}
