/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Calculates the implied volatility of Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class ImpliedVolatilityForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ImpliedVolatilityForexBlackSmileCalculator INSTANCE = new ImpliedVolatilityForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ImpliedVolatilityForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ImpliedVolatilityForexBlackSmileCalculator() {
  }

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with market data");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double dfDomestic = curves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = curves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    return marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
  }

  @Override
  public Double visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with market data");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike();
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    return marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
  }

  @Override
  public Double visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with market data");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCcy, foreignCcy;
    final double strike, dfDomestic, dfForeign;
    if (option.payDomestic()) {
      domesticCcy = option.getUnderlyingForex().getCurrency2();
      foreignCcy = option.getUnderlyingForex().getCurrency1();
      dfDomestic = curves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
      dfForeign = curves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
      strike = option.getStrike();
    } else {
      strike = 1.0 / option.getStrike();
      domesticCcy = option.getUnderlyingForex().getCurrency1();
      foreignCcy = option.getUnderlyingForex().getCurrency2();
      dfDomestic = curves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
      dfForeign = curves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    }
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    return marketData.getVolatility(foreignCcy, domesticCcy, option.getExpirationTime(), strike, forward);
  }

  @Override
  public Double visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double payTime = option.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), payTime);
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    return marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getUnderlyingOption().getTimeToExpiry(), option
        .getUnderlyingOption().getStrike(), forward);
  }

}
