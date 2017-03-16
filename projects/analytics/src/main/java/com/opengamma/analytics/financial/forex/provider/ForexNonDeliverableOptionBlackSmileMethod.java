/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for Forex non-deliverable option transactions with Black function and a smile.
 */
public final class ForexNonDeliverableOptionBlackSmileMethod {

  /**
   * The method unique instance.
   */
  private static final ForexNonDeliverableOptionBlackSmileMethod INSTANCE = new ForexNonDeliverableOptionBlackSmileMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexNonDeliverableOptionBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexNonDeliverableOptionBlackSmileMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Methods.
   */
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  /**
   * Computes the present value of Forex non-deliverable option with the Black function and a volatility surface.
   * @param option  the option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the present value in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(strike, expiryTime, !option.isCall());
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(vanillaOption);
    final double price = func.evaluate(dataBlack) * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()) * (option.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(option.getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Returns the Black implied volatility that is used to price the option.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the implied volatility used in pricing
   */
  public double impliedVolatility(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    return marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
  }

  /**
   * Computes the currency exposure of Forex non-deliverable option with the Black function and a volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(strike, expiryTime, !option.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(vanillaOption, dataBlack);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double price = priceAdjoint[0] * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()) * sign;
    final double deltaSpot = priceAdjoint[1] * dfNonDelivery / dfDelivery;
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(option.getCurrency1(), deltaSpot * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(option.getCurrency2(), -deltaSpot * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()) * spot * sign + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  /**
   * Computes the forward exchange rate associated with the NDF (1 Cyy2 = fwd Cyy1).
   * @param option  the non-deliverable option, not null
   * @param curves  the curve data, not null
   * @return  the forward rate
   */
  public double forwardForexRate(final ForexNonDeliverableOption option, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(option, "option");
    return METHOD_NDF.forwardForexRate(option.getUnderlyingNDF(), curves);
  }

  /**
   * Computes the delta of a non-deliverable FX option. This is the first order differential of the price with respect to the spot rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the delta
   */
  public CurrencyAmount delta(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double relativeDelta = theoreticalRelativeDelta(option, marketData, directQuote);
    return CurrencyAmount.of(option.getCurrency2(), relativeDelta * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()));
  }

  /**
   * Gets the theoretical relative delta of a non-deliverable FX option. For an option where the strike is in the same units as
   * the spot rate (a direct quote), this is the same as the delta. For an option where the strike is the inverse of the
   * spot rate (an indirect quote), this gives the delta with respect to this reversed quote.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the relative delta
   */
  public double theoreticalRelativeDelta(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final int sign = option.isLong() ? 1 : -1;
    final double delta = theoreticalSpotDelta(option, marketData) * sign;
    if (directQuote) {
      return delta;
    }
    return -delta * spot * spot;
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the relative delta
   */
  public double relativeDelta(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(strike, expiryTime, !option.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(vanillaOption, dataBlack);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double deltaDirect = priceAdjoint[1] * dfNonDelivery * sign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the spot delta (first derivative with respect to spot).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the spot delta
   */
  public double theoreticalSpotDelta(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "FX option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingNDF().getPaymentTime());
    return theoreticalForwardDelta(option, marketData) * dfDelivery;
  }

  /**
   * Computes the forward delta (first derivative with respect to forward).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward delta
   */
  public double theoreticalForwardDelta(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "FX option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final double indirectDelta = BlackFormulaRepository.delta(forward, strike, expiryTime, volatility, !option.isCall());
    return indirectDelta;
  }

  /**
   * Computes the present value curve sensitivities of Forex non-deliverable option with the Black function and a volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the curve sensitivities
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final double sign = option.isLong() ? 1.0 : -1.0;
    // Forward sweep
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(strike, expiryTime, !option.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(vanillaOption, dataBlack);
    // Backward sweep
    final double priceBar = sign;
    final double forwardBar = priceAdjoint[1] * dfDelivery * priceBar;
    final double dfNonDeliveryBar = spot / dfDelivery * forwardBar;
    final double dfDeliveryBar = -spot / (dfDelivery * dfDelivery) * dfNonDelivery * forwardBar + priceAdjoint[0] * priceBar;
    final double rNonDeliveryBar = -paymentTime * dfNonDelivery * dfNonDeliveryBar;
    final double rDeliveryBar = -paymentTime * dfDelivery * dfDeliveryBar;
    // Sensitivity object
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listNonDelivery = new ArrayList<>();
    listNonDelivery.add(DoublesPair.of(paymentTime, rNonDeliveryBar * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1())));
    resultMap.put(curves.getName(option.getCurrency1()), listNonDelivery);
    final List<DoublesPair> listDelivery = new ArrayList<>();
    listDelivery.add(DoublesPair.of(paymentTime, rDeliveryBar * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1())));
    resultMap.put(curves.getName(option.getCurrency2()), listDelivery);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(option.getCurrency2(), result);
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to one volatility point) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return the vega
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    // Forward sweep
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDelivery, volatility);
    final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(strike, expiryTime, !option.isCall());
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(vanillaOption, dataBlack);
    // Backward sweep
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(option.getUnderlyingNDF().getNotionalCurrency1()) * (option.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(option.getExpiryTime(), option.getCurrency1().equals(marketData.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(option.getCurrency1(), option.getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the present value volatility sensitivity (sensitivity to each point of the volatility input grid) of Forex non-deliverable option with the Black function and a volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the bucketed vega
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueVolatilityNodeSensitivity(final ForexNonDeliverableOption option,
      final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double paymentTime = option.getUnderlyingNDF().getPaymentTime();
    final double expiryTime = option.getExpiryTime();
    final double strike = 1.0 / option.getStrike(); // The strike is 1 ccy2=X ccy1; we want the price in ccy2 => we need 1 ccy1 = 1/X ccy2.
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(option, marketData); // In ccy2
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = marketData.getVolatility();
    final double dfDelivery = curves.getDiscountFactor(option.getCurrency2(), paymentTime);
    final double dfNonDelivery = curves.getDiscountFactor(option.getCurrency1(), paymentTime);
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfNonDelivery / dfDelivery;
    final VolatilityAndBucketedSensitivities volAndSensitivities = marketData.getVolatilityAndSensitivities(option.getCurrency1(), option.getCurrency2(), expiryTime, strike, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(expiryTime, option.getCurrency1().equals(marketData.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int i = 0; i < volatilityModel.getNumberExpiration(); i++) {
      for (int j = 0; j < volatilityModel.getNumberStrike(); j++) {
        vega[i][j] = nodeWeight[i][j] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(option.getCurrency1(), option.getCurrency2(), new DoubleMatrix1D(volatilityModel.getTimeToExpiration()),
        new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
