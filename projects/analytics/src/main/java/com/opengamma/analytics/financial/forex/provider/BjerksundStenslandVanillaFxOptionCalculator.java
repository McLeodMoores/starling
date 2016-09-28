/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Valuation and risk for American-style vanilla FX options using the Bjerksund-Stensland model.
 */
public final class BjerksundStenslandVanillaFxOptionCalculator {

  /**
   * Restricted constructor.
   */
  private BjerksundStenslandVanillaFxOptionCalculator() {
  }

  /** The pricing model */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * Calculates the present value of a vanilla FX option using the Bjerksund-Stensland (2002) model. This value is returned
   * in the domestic currency of the option and is scaled by the notional and whether the option is long or short.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the present value of the option
   */
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
    final double price = MODEL.price(spot, strike, domesticRate, domesticRate - foreignRate, expiryTime, volatility, option.isCall());
    final CurrencyAmount optionPrice = CurrencyAmount.of(domesticCurrency, price * Math.abs(foreignPayment) * (option.isLong() ? 1 : -1));
    return MultipleCurrencyAmount.of(optionPrice);
  }

  /**
   * Gets the implied volatility used in the option pricing model. The value is returned as a decimal.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the implied volatility
   */
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

  /**
   * Gets the currency exposure of a vanilla FX option. This is defined as the amount in each currency required to neutralize
   * currency movements.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the currency exposure
   */
  public static MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final PaymentFixed foreignPayment = option.getUnderlyingForex().getPaymentCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final int sign = option.isLong() ? 1 : -1;
    final double[] adjoints = getSpotAdjoints(option, marketData);
    final double price = adjoints[0] * Math.abs(foreignPayment.getAmount()) * sign;
    final double spotDelta = adjoints[1];
    final MultipleCurrencyAmount currencyExposure = MultipleCurrencyAmount.of(foreignCurrency, spotDelta * Math.abs(foreignPayment.getAmount()) * sign);
    return currencyExposure.plus(domesticCurrency, -spotDelta * Math.abs(foreignPayment.getAmount()) * spot * sign + price);
  }

  /**
   * Computes the delta of a vanilla FX option. This is the first order differential of the price with respect to the spot rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the delta
   */
  public static CurrencyAmount delta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double relativeDelta = theoreticalRelativeDelta(option, marketData, directQuote);
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), relativeDelta * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Gets the theoretical forward delta of a vanilla FX option. This is the change in value for a change in the forward rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the forward delta
   */
  public static double theoreticalForwardDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency foreignCurrency = option.getCurrency1();
    final Currency domesticCurrency = option.getCurrency2();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    return theoreticalSpotDelta(option, marketData) * domesticDf / foreignDf;
  }

  /**
   * Gets the theoretical spot delta of a vanilla FX option. This is the change in value for a change in the spot rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the spot delta
   */
  public static double theoreticalSpotDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getSpotAdjoints(option, marketData);
    return adjoints[1];
  }

  /**
   * Gets the theoretical relative delta of a vanilla FX option. For an option where the strike is in the same units as
   * the spot rate (a direct quote), this is the same as the delta. For an option where the strike is the inverse of the
   * spot rate (an indirect quote), this gives the delta with respect to this reversed quote.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the relative delta
   */
  public static double theoreticalRelativeDelta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
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
   * Gets the theoretical relative delta of a vanilla FX option multiplied or divided by the spot value depending on if the
   * quote is direct or indirect.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the relative delta spot
   */
  public static double theoreticalRelativeDeltaSpot(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double relativeDelta = theoreticalRelativeDelta(option, marketData, directQuote);
    if (directQuote) {
      return relativeDelta * spot;
    }
    // note this is divided by the spot as the quote is reversed
    return relativeDelta / spot;
  }

  /**
   * Computes the gamma of a vanilla FX option. This is the second order differential of the price with respect to the spot rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the gamma
   */
  public static CurrencyAmount gamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double relativeGamma = theoreticalRelativeGamma(option, marketData, directQuote);
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), relativeGamma * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Gets the theoretical forward gamma of a vanilla FX option. This is the second order differential of the price with respect
   * to the forward rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the forward gamma
   */
  public static double theoreticalForwardGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency foreignCurrency = option.getCurrency1();
    final Currency domesticCurrency = option.getCurrency2();
    final double paymentTime = option.getUnderlyingForex().getPaymentTime();
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, paymentTime);
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, paymentTime);
    return theoreticalSpotGamma(option, marketData) * domesticDf * domesticDf / foreignDf / foreignDf;
  }

  /**
   * Gets the theoretical spot gamma of a vanilla FX option. This is the second order differential of the price with respect
   * to the spot rate.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the forward gamma
   */
  public static double theoreticalSpotGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getSpotAdjoints(option, marketData);
    return adjoints[2];
  }

  /**
   * Gets the theoretical relative gamma of a vanilla FX option. For an option where the strike is in the same units as
   * the spot rate (a direct quote), this is the same as the gamma. For an option where the strike is the inverse of the
   * spot rate (an indirect quote), this gives the gamma with respect to this reversed quote.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the gamma with respect to the quoted spot is required, false if the gamma with respect
   * to the inverse spot is required
   * @return  the relative gamma
   */
  public static double theoreticalRelativeGamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final double spot = curves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double[] adjoints = getSpotAdjoints(option, marketData);
    final double gamma = adjoints[2] * sign;
    if (directQuote) {
      return gamma;
    }
    final double delta = adjoints[1] * sign;
    return (gamma * spot + 2 * delta) * spot * spot * spot;
  }

  /**
   * Gets the theoretical relative gamma of a vanilla FX option multiplied or divided by the spot value depending on if the
   * quote is direct or indirect.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @param directQuote  true if the delta with respect to the quoted spot is required, false if the delta with respect
   * to the inverse spot is required
   * @return  the relative gamma spot
   */
  public static double theoreticalRelativeGammaSpot(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double relativeGamma = theoreticalRelativeGamma(option, marketData, directQuote);
    if (directQuote) {
      return relativeGamma * spot;
    }
    // note this is divided by the spot as the quote is reversed
    return relativeGamma / spot;
  }

  /**
   * Gets the theoretical theta of a vanilla FX option scaled to one day (assuming 365 days in a year).
   * This is the change in value for a change in the time to expiry.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the theta
   */
  public static double theoreticalTheta(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getAdjoints(option, marketData);
    return -adjoints[5] / 365.; // negative sign is deliberate - method returns wrong sign
  }

  /**
   * Computes the vega of the present value to the volatility surface. The result is returned in the domestic currency.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the vega
   */
  public static PresentValueForexBlackVolatilitySensitivity vega(final ForexOptionVanilla option,
      final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final Currency foreignCurrency = option.getUnderlyingForex().getCurrency1();
    final Currency domesticCurrency = option.getUnderlyingForex().getCurrency2();
    final double vegaValue = theoreticalVega(option, marketData) * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (option.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(option.getTimeToExpiry(),
        option.getCurrency1().equals(marketData.getCurrencyPair().getFirst()) ? option.getStrike() : 1.0 / option.getStrike());
    final SurfaceValue result = SurfaceValue.from(point, vegaValue);
    return new PresentValueForexBlackVolatilitySensitivity(foreignCurrency, domesticCurrency, result);
  }

  /**
   * Gets the theoretical vega of a vanilla FX option. This is the change in value for a change in the volatility.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the vega
   */
  public static double theoreticalVega(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double[] adjoints = getAdjoints(option, marketData);
    return adjoints[6];
  }

  /**
   * Computes the sensitivities of the present value to the nodes of the yield curves. The sensitivity of the volatility to the
   * forward (and so on the curves) is not taken into account. The result is returned in the domestic currency.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  sensitivities to the curve
   */
  public static MultipleCurrencyMulticurveSensitivity bucketedCurveSensitivities(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final Currency domesticCurrency = option.getCurrency2();
    final Currency foreignCurrency = option.getCurrency1();
    final double payTime = option.getUnderlyingForex().getPaymentTime();
    final double[] priceAdjoint = getAdjoints(option, marketData);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double factor = Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * sign;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> foreignSensitivities = new ArrayList<>();
    // derivatives calculated wrt domestic rate and cost-of-carry (= domestic rate - foreign rate)
    // backward sweep
    final double domesticRateBar = priceAdjoint[3] + priceAdjoint[4];
    final double foreignRateBar = -priceAdjoint[4];
    foreignSensitivities.add(DoublesPair.of(payTime, foreignRateBar * factor));
    resultMap.put(curves.getName(foreignCurrency), foreignSensitivities);
    final List<DoublesPair> domesticSensitivities = new ArrayList<>();
    domesticSensitivities.add(DoublesPair.of(payTime, domesticRateBar * factor));
    resultMap.put(curves.getName(domesticCurrency), domesticSensitivities);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(domesticCurrency, result);
  }

  /**
   * Computes the sensitivities of the present value to the nodes of the volatility surface. The result is returned in the domestic currency.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  sensitivities to the curve
   */
  public static PresentValueForexBlackVolatilityNodeSensitivityDataBundle bucketedVega(final ForexOptionVanilla option,
      final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface curves = marketData.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = vega(option, marketData);
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilitySurface = marketData.getVolatility();
    final Currency foreignCurrency = option.getCurrency1();
    final Currency domesticCurrency = option.getCurrency2();
    final double domesticDf = curves.getDiscountFactor(domesticCurrency, option.getUnderlyingForex().getPaymentTime());
    final double foreignDf = curves.getDiscountFactor(foreignCurrency, option.getUnderlyingForex().getPaymentTime());
    final double spot = curves.getFxRate(foreignCurrency, domesticCurrency);
    final double forward = spot * foreignDf / domesticDf;
    final VolatilityAndBucketedSensitivities volAndSensitivities = marketData.getVolatilityAndSensitivities(foreignCurrency, domesticCurrency,
        option.getTimeToExpiry(), option.getStrike(), forward);
    final double[][] nodeWeights = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(option.getTimeToExpiry(),
        foreignCurrency.equals(marketData.getCurrencyPair().getFirst()) ? option.getStrike() : 1.0 / option.getStrike());
    final double[][] vega = new double[volatilitySurface.getNumberExpiration()][volatilitySurface.getNumberStrike()];
    for (int i = 0; i < volatilitySurface.getNumberExpiration(); i++) {
      for (int j = 0; j < volatilitySurface.getNumberStrike(); j++) {
        vega[i][j] = nodeWeights[i][j] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(foreignCurrency, domesticCurrency, new DoubleMatrix1D(
        volatilitySurface.getTimeToExpiration()), new DoubleMatrix1D(volatilitySurface.getDeltaFull()), new DoubleMatrix2D(vega));
  }

  /**
   * Gets the adjoints of the option using the Bjerksund-Stensland model.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the adjoints
   */
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
    return MODEL.getPriceAdjoint(spot, strike, domesticRate, domesticRate - foreignRate, expiryTime, volatility, option.isCall());
  }

  /**
   * Gets the adjoints of the option with respect to the spot rate using the Bjerksund-Stensland model.
   * @param option  the option, not null
   * @param marketData  the market data, not null
   * @return  the adjoints
   */
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
    return MODEL.getPriceDeltaGamma(spot, strike, domesticRate, domesticRate - foreignRate, expiryTime, volatility, option.isCall());
  }
}
