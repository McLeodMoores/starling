/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Valuation and risk for American-style vanilla FX options using the Bjerksund-Stensland model.
 */
public final class AmericanVanillaFxOptionPricingMethod {

  /**
   * Restricted constructor.
   */
  private AmericanVanillaFxOptionPricingMethod() {
  }

  /** The pricing model */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  public static MultipleCurrencyAmount presentValue(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double foreignPayment = option.getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double expiryTime = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    final double domesticRate = -Math.log(domesticDf) / paymentTime;
    final double foreignRate = -Math.log(foreignDf) / paymentTime;
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double forward = spot * foreignDf / domesticDf;
    final double volatility = marketData.getVolatility(foreignCurrency, domesticCurrency, expiryTime, strike, forward);
    final double price = MODEL.price(spot, strike, domesticRate, foreignRate, expiryTime, volatility, option.isCall());
    final CurrencyAmount optionPrice = CurrencyAmount.of(domesticCurrency, price * Math.abs(foreignPayment) * (option.isLong() ? 1 : -1));
    return MultipleCurrencyAmount.of(optionPrice);
  }

  public static double impliedVolatility(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double expiryTime = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double forward = spot * foreignDf / domesticDf;
    return marketData.getVolatility(foreignCurrency, domesticCurrency, expiryTime, strike, forward);
  }

  public static MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final PaymentFixed domesticPayment = option.getUnderlyingForex().getPaymentCurrency2();
    final PaymentFixed foreignPayment = option.getUnderlyingForex().getPaymentCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final int sign = option.isLong() ? 1 : -1;
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double[] adjoints = getAdjoints(option, marketData);
    final double price = adjoints[0] * Math.abs(foreignPayment.getAmount()) * sign;
    final double spotDelta = adjoints[1];
    final MultipleCurrencyAmount currencyExposure = MultipleCurrencyAmount.of(foreignCurrency, spotDelta * Math.abs(foreignPayment.getAmount()) * sign / domesticDf);
    return currencyExposure.plus(domesticCurrency, -spotDelta * Math.abs(foreignPayment.getAmount()) * spot * sign + price);
  }

  public static double forwardDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency foreignCurrency = option.getCurrency1();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    return spotDelta(option, marketData) / foreignDf;
  }

  public static double spotDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getAdjoints(option, marketData);
    return adjoints[1];
  }

  public static double relativeDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final int sign = option.isLong() ? 1 : -1;
    final double delta = spotDelta(option, marketData) * sign; //TODO
    if (directQuote) {
      return delta;
    }
    return -delta * spot * spot;
  }

  public static double forwardGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getSpotAdjoints(option, marketData);
    return adjoints[2]; //TODO
  }

  public static double spotGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    return forwardGamma(option, marketData) * foreignDf * foreignDf / domesticDf; //TODO
  }

  public static double relativeGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double[] adjoints = getAdjoints(option, marketData);
    final double gamma = adjoints[2] * (dfForeign * dfForeign) / dfDomestic * sign;
    if (directQuote) {
      return gamma * spot;
    }
    final double delta = adjoints[1] * dfForeign * sign;
    return (gamma * spot + 2 * delta) * spot * spot;
  }

  private static double[] getAdjoints(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double expiryTime = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    final double domesticRate = -Math.log(domesticDf) / paymentTime;
    final double foreignRate = -Math.log(foreignDf) / paymentTime;
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double forward = spot * foreignDf / domesticDf;
    final double volatility = marketData.getVolatility(foreignCurrency, domesticCurrency, expiryTime, strike, forward);
    final double[] adjoints = MODEL.getPriceAdjoint(spot, strike, domesticRate, foreignRate, expiryTime, volatility, option.isCall());
    return adjoints;
  }

  private static double[] getSpotAdjoints(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double expiryTime = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    final double domesticRate = -Math.log(domesticDf) / paymentTime;
    final double foreignRate = -Math.log(foreignDf) / paymentTime;
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double forward = spot * foreignDf / domesticDf;
    final double volatility = marketData.getVolatility(foreignCurrency, domesticCurrency, expiryTime, strike, forward);
    return MODEL.getPriceDeltaGamma(spot, strike, domesticRate, foreignRate, expiryTime, volatility, option.isCall());
  }
}
