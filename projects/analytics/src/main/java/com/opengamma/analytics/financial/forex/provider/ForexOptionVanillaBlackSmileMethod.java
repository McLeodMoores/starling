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

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for vanilla Forex option transactions with Black function and a volatility provider.
 * OG-Implementation: Vanilla Forex options: Garman-Kohlhagen and risk reversal/strangle, version 1.5, May 2012.
 */
public final class ForexOptionVanillaBlackSmileMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionVanillaBlackSmileMethod INSTANCE = new ForexOptionVanillaBlackSmileMethod();

  /**
   * Private constructor.
   */
  private ForexOptionVanillaBlackSmileMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionVanillaBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Methods.
   */
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();

  /**
   * Computes the present value of the vanilla option with the Black function and a volatility from a volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the present value in the domestic currency
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (option.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Returns the Black implied volatility that is used to price the option.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the implied volatility used in pricing
   */
  public double impliedVolatility(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    return volatility;
  }

  /**
   * Computes the currency exposure of the vanilla option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double price = priceAdjoint[0] * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * sign;
    final double deltaSpot = priceAdjoint[1] * dfForeign / dfDomestic;
    MultipleCurrencyAmount currencyExposure = MultipleCurrencyAmount.of(option.getUnderlyingForex().getCurrency1(),
        deltaSpot * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * sign);
    currencyExposure = currencyExposure.plus(option.getUnderlyingForex().getCurrency2(), -deltaSpot * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * spot * sign
        + price);
    return currencyExposure;
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the relative delta
   */
  public double deltaRelative(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double deltaDirect = BlackFormulaRepository.delta(forward, option.getStrike(), option.getTimeToExpiry(), volatility, option.isCall()) * dfForeign * sign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the relative delta of the Forex option multiplied by the spot rate.
   * The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * The reason to multiply by the spot rate is to be able to compute the change of value for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the relative delta multiplied by the spot
   */
  public double deltaRelativeSpot(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double deltaDirect = BlackFormulaRepository.delta(forward, option.getStrike(), option.getTimeToExpiry(), volatility, option.isCall()) * dfForeign * sign;
    if (directQuote) {
      return deltaDirect * spot;
    }
    final double deltaReverse = -deltaDirect * spot;
    return deltaReverse;

  }

  /**
   * Computes the spot delta (first derivative with respect to spot).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the spot delta
   */
  public double spotDeltaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "FX option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    return forwardDeltaTheoretical(option, marketData) * dfForeign;
  }

  /**
   * Computes the forward delta (first derivative with respect to forward).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward delta
   */
  public double forwardDeltaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "FX option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    return BlackFormulaRepository.delta(forward, option.getStrike(), option.getTimeToExpiry(), volatility, option.isCall());
  }

  /**
   * Computes the relative gamma of the Forex option.
   * The relative gamma is the second order derivative of the pv divided by the option notional.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the relative gamma
   */
  public double gammaRelative(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double gammaDirect = BlackFormulaRepository.gamma(forward, option.getStrike(), option.getTimeToExpiry(), volatility) * (dfForeign * dfForeign) / dfDomestic * sign;
    if (directQuote) {
      return gammaDirect;
    }
    final double deltaDirect = BlackFormulaRepository.delta(forward, option.getStrike(), option.getTimeToExpiry(), volatility, option.isCall()) * dfForeign * sign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot * spot;
    return gamma;
  }

  /**
   * Computes the relative gamma of the Forex option multiplied by the spot rate.
   * The relative gamma is the second order derivative of the pv relative to the option notional.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the relative gamma multiplied by the spot
   */
  public double gammaRelativeSpot(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double gammaDirect = BlackFormulaRepository.gamma(forward, option.getStrike(), option.getTimeToExpiry(), volatility) * (dfForeign * dfForeign) / dfDomestic * sign;
    if (directQuote) {
      return gammaDirect * spot;
    }
    final double deltaDirect = BlackFormulaRepository.delta(forward, option.getStrike(), option.getTimeToExpiry(), volatility, option.isCall()) * dfForeign * sign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option. The gamma is the second order derivative of the option present value to the spot fx rate.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the gamma
   */
  public CurrencyAmount gamma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final double gammaRelative = gammaRelative(option, marketData, directQuote);
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), gammaRelative * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the spot gamma (second derivative with respect to spot).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the spot gamma
   */
  public double spotGammaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    return forwardGammaTheoretical(option, marketData) * (dfForeign * dfForeign) / dfDomestic;
  }

  /**
   * Computes the forward gamma (second derivative with respect to forward).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward gamma
   */
  public double forwardGammaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRates().getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    return BlackFormulaRepository.gamma(forward, option.getStrike(), option.getTimeToExpiry(), volatility);
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @param directQuote  true if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return  the gamma multiplied by the spot
   */
  public CurrencyAmount gammaSpot(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData, final boolean directQuote) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final double gammaRelativeSpot = gammaRelativeSpot(option, marketData, directQuote);
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), gammaRelativeSpot * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the Theta (derivative with respect to the time) using the forward driftless theta in the Black formula. The theta is not scaled.
   * Reference on driftless theta: The complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the theta in the domestic currency
   */
  public CurrencyAmount thetaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double theta = BlackFormulaRepository.driftlessTheta(forward, option.getStrike(), option.getTimeToExpiry(), volatility) * sign
        * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), theta);
  }

  /**
   * Computes the forward driftless theta (derivative with respect to the time). The theta is not scaled.
   * Reference on driftless theta: The complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward driftless theta
   */
  public double forwardDriftlessThetaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRates().getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    return BlackFormulaRepository.driftlessTheta(forward, option.getStrike(), option.getTimeToExpiry(), volatility);
  }

  /**
   * Computes the forward vega (first derivative with respect to volatility).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward vega
   */
  public double forwardVegaTheoretical(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRates().getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    return BlackFormulaRepository.vega(forward, option.getStrike(), option.getTimeToExpiry(), volatility);
  }

  /**
   * Computes the Vanna (2nd order cross-sensitivity of the option present value to the spot fx and implied vol),
   *
   * $\frac{\partial^2 (PV)}{\partial FX \partial \sigma}$
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the vanna in the domestic currency
   */

  public CurrencyAmount vanna(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double vanna = dfForeign * BlackFormulaRepository.vanna(forward, option.getStrike(), option.getTimeToExpiry(), volatility) * sign
        * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount());

    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), vanna);
  }

  /**
   * Computes the Vomma (aka Volga) (2nd order sensitivity of the option present value to the implied vol)
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the volga in the domesti currency
   */
  public CurrencyAmount vomma(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final double sign = option.isLong() ? 1.0 : -1.0;
    final double vomma = dfDomestic * BlackFormulaRepository.vomma(forward, option.getStrike(), option.getTimeToExpiry(), volatility) * sign
        * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(option.getUnderlyingForex().getCurrency2(), vomma);
  }

  /**
   * Computes the forward exchange rate associated to the Forex option (1 Cyy1 = fwd Cyy2).
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the forward rate.
   */
  public double forwardForexRate(final ForexOptionVanilla option, final MulticurveProviderInterface marketData) {
    return METHOD_FOREX.forwardForexRate(option.getUnderlyingForex(), marketData);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the curve sensitivities
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double payTime = option.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), payTime);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = priceAdjoint[1] * dfDomestic * priceBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double dfDomesticBar = -spot / (dfDomestic * dfDomestic) * dfForeign * forwardBar + priceAdjoint[0] * priceBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final double factor = Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (option.isLong() ? 1.0 : -1.0);
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar * factor));
    resultMap.put(multicurves.getName(option.getCurrency1()), listForeign);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar * factor));
    resultMap.put(multicurves.getName(option.getCurrency2()), listDomestic);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(option.getUnderlyingForex().getCurrency2(), result);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double payTime = option.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final double dfDomestic = multicurves.getDiscountFactor(option.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(option.getCurrency1(), payTime);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = marketData.getVolatility(option.getCurrency1(), option.getCurrency2(), option.getTimeToExpiry(), option.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, dfDomestic, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double volatilitySensitivityValue = priceAdjoint[2] * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount()) * (option.isLong() ? 1.0 : -1.0);
    final DoublesPair point = DoublesPair.of(option.getTimeToExpiry(),
        option.getCurrency1().equals(marketData.getCurrencyPair().getFirst()) ? option.getStrike() : 1.0 / option.getStrike());
    // Implementation note: The strike should be in the same currency order as the input data.
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(option.getUnderlyingForex().getCurrency1(), option.getUnderlyingForex()
        .getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param option  the Forex option, not null
   * @param marketData  the curve and smile data, not null
   * @return  the volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionVanilla option,
      final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.isTrue(marketData.checkCurrencies(option.getCurrency1(), option.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = marketData.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(option, marketData); // In ccy2
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = marketData.getVolatility();
    final double df = multicurves.getDiscountFactor(option.getCurrency2(), option.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(option.getCurrency1(), option.getCurrency2());
    final double forward = spot * multicurves.getDiscountFactor(option.getCurrency1(), option.getUnderlyingForex().getPaymentTime()) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivities = marketData.getVolatilityAndSensitivities(option.getCurrency1(), option.getCurrency2(),
        option.getTimeToExpiry(), option.getStrike(), forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(option.getTimeToExpiry(),
        option.getCurrency1().equals(marketData.getCurrencyPair().getFirst()) ? option.getStrike() : 1.0 / option.getStrike());
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int i = 0; i < volatilityModel.getNumberExpiration(); i++) {
      for (int j = 0; j < volatilityModel.getNumberStrike(); j++) {
        vega[i][j] = nodeWeight[i][j] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(option.getUnderlyingForex().getCurrency1(), option.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(
        volatilityModel.getTimeToExpiration()), new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
