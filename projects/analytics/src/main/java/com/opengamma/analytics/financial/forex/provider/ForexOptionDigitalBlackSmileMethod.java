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

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.DigitalOptionFunction;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for digital Forex option transactions with Black function and a volatility provider.
 */
public final class ForexOptionDigitalBlackSmileMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionDigitalBlackSmileMethod INSTANCE = new ForexOptionDigitalBlackSmileMethod();

  /**
   * Private constructor.
   */
  private ForexOptionDigitalBlackSmileMethod() {
  }

  /**
   * Return the unique instance of the class.
   *
   * @return The instance.
   */
  public static ForexOptionDigitalBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The normal probability distribution used.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the digital option with the Black function and a volatility from a volatility surface.
   *
   * @param digitalOption
   *          The Forex option.
   * @param smileMulticurves
   *          The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital digitalOption,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(digitalOption, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double expiry = digitalOption.getExpirationTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double dfDomestic;
    final double dfForeign;
    final double amount;
    final double omega;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      dfDomestic = multicurves.getDiscountFactor(digitalOption.getCurrency2(), digitalOption.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(digitalOption.getCurrency1(), digitalOption.getUnderlyingForex().getPaymentTime());
      strike = digitalOption.getStrike();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = digitalOption.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      dfDomestic = multicurves.getDiscountFactor(digitalOption.getCurrency1(), digitalOption.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(digitalOption.getCurrency2(), digitalOption.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = digitalOption.isCall() ? -1.0 : 1.0;
    }
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (digitalOption.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(domesticCcy, pv);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Computes the currency exposure of the digital option with the Black function and a volatility from a volatility surface. The exposure
   * is computed in both option currencies.
   *
   * @param digitalOption
   *          The Forex option.
   * @param smileMulticurves
   *          The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital digitalOption,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(digitalOption, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double expiry = digitalOption.getExpirationTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double dfDomestic;
    final double dfForeign;
    final double amount;
    final double omega;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      dfDomestic = multicurves.getDiscountFactor(digitalOption.getCurrency2(), digitalOption.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(digitalOption.getCurrency1(), digitalOption.getUnderlyingForex().getPaymentTime());
      strike = digitalOption.getStrike();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = digitalOption.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      dfDomestic = multicurves.getDiscountFactor(digitalOption.getCurrency1(), digitalOption.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(digitalOption.getCurrency2(), digitalOption.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = digitalOption.isCall() ? -1.0 : 1.0;
    }
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (digitalOption.isLong() ? 1.0 : -1.0);
    final double deltaSpot = amount * dfDomestic * NORMAL.getPDF(dM) * omega / (sigmaRootT * spot) * (digitalOption.isLong() ? 1.0 : -1.0);
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(foreignCcy, deltaSpot);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot + PV
    currencyExposure[1] = CurrencyAmount.of(domesticCcy, -deltaSpot * spot + pv);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not
   * taken into account. It is the curve sensitivity in the Black model where the volatility is suppose to be constant for curve and forward
   * changes.
   *
   * @param digitalOption
   *          The Forex option.
   * @param smileMulticurves
   *          The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital digitalOption,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(digitalOption, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final double expiry = digitalOption.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final double omega;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = digitalOption.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = digitalOption.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (digitalOption.isLong() ? 1.0 : -1.0);
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = amount * dfDomestic * NORMAL.getPDF(omega * dM) * (digitalOption.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double forwardBar = 1 / (forward * sigmaRootT) * dMBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double dfDomesticBar = -spot / (dfDomestic * dfDomestic) * dfForeign * forwardBar + pv / dfDomestic * pvBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    resultMap.put(multicurves.getName(foreignCcy), listForeign);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    resultMap.put(multicurves.getName(domesticCcy), listDomestic);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(domesticCcy, result);
  }

  /**
   * Computes the volatility sensitivity of the digital option with the Black function and a volatility from a volatility surface. The
   * sensitivity is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   *
   * @param digitalOption
   *          The Forex option.
   * @param smileMulticurves
   *          The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital digitalOption,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(digitalOption, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final double expiry = digitalOption.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final double omega;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = digitalOption.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      amount = Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = digitalOption.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = amount * dfDomestic * NORMAL.getPDF(omega * dM) * (digitalOption.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double sigmaRootTBar = (-Math.log(forward / strike) / (sigmaRootT * sigmaRootT) - 0.5) * dMBar;
    final double volatilityBar = Math.sqrt(expiry) * sigmaRootTBar;
    final DoublesPair point = DoublesPair.of(digitalOption.getExpirationTime(),
        foreignCcy.equals(smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    // Implementation note: The strike should be in the same currency order as the input data.
    final SurfaceValue result = SurfaceValue.from(point, volatilityBar);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(foreignCcy, domesticCcy,
        result);
    return sensi;
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option with the Black function and a volatility from a
   * volatility surface. The sensitivity is computed with respect to each node in the volatility surface.
   *
   * @param digitalOption
   *          The Forex option.
   * @param smileMulticurves
   *          The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(
      final ForexOptionDigital digitalOption,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(digitalOption, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(digitalOption,
        smileMulticurves); // In dom ccy
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smileMulticurves.getVolatility();
    final double payTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final double expiry = digitalOption.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final VolatilityAndBucketedSensitivities volAndSensitivities = smileMulticurves.getVolatilityAndSensitivities(foreignCcy, domesticCcy,
        expiry, strike, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(digitalOption.getExpirationTime(),
        foreignCcy.equals(smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int i = 0; i < volatilityModel.getNumberExpiration(); i++) {
      for (int j = 0; j < volatilityModel.getNumberStrike(); j++) {
        vega[i][j] = nodeWeight[i][j] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(digitalOption.getUnderlyingForex().getCurrency1(),
        digitalOption.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(volatilityModel.getTimeToExpiration()),
        new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

  /**
   * Computes the implied Black volatility of the digital option.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and volatility data
   * @return The implied volatility.
   */
  public double impliedVolatility(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double payTime = digitalOption.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
    } else {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      strike = 1. / digitalOption.getStrike();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, payTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    return volatility;
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up
   * to the first order divided by the option notional.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and volatility data
   * @param directQuote
   *          Flag indicating if the delta should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The delta.
   */
  public double deltaRelative(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall)
        * longSign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the delta of the Forex option. The delta is the first order derivative of the option present value to the spot fx rate.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and volatility data
   * @param directQuote
   *          Flag indicating if the delta should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The delta.
   */
  public CurrencyAmount delta(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final double deltaRelative = deltaRelative(digitalOption, data, directQuote);
    final Currency domesticCcy;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
    }
    return CurrencyAmount.of(domesticCcy, deltaRelative * Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the relative gamma of the Forex option. The relative gamma is the second order derivative of the pv divided by the option
   * notional.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelative(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    final double gammaDirect = DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall)
        * longSign;
    if (directQuote) {
      return gammaDirect;
    }
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall)
        * longSign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option. The gamma is the second order derivative of the option present value to the spot fx rate.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and volatility data
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gamma(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final Currency domesticCcy;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
    }
    final double gammaRelative = gammaRelative(digitalOption, data, directQuote);
    return CurrencyAmount.of(domesticCcy, gammaRelative * Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the relative gamma of the Forex option multiplied by the spot rate. The relative gamma is the second oder derivative of the pv
   * relative to the option notional. The reason to multiply by the spot rate is to be able to compute the change of delta for a relative
   * increase of e of the spot rate (from X to X(1+e)).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelativeSpot(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    final double gammaDirect = DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall)
        * longSign;
    if (directQuote) {
      return gammaDirect * spot;
    }
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall)
        * longSign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv. The reason to
   * multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to
   * X(1+e)).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and volatility data
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final Currency domesticCcy;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
    }
    final double gammaRelativeSpot = gammaRelativeSpot(digitalOption, data, directQuote);
    return CurrencyAmount.of(domesticCcy,
        gammaRelativeSpot * Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the theta (derivative with respect to the time). The theta is not scaled.
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The theta
   */
  public CurrencyAmount forwardTheta(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    final double theta = DigitalOptionFunction.theta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign
        * Math.abs(digitalOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(digitalOption.getUnderlyingForex().getCurrency2(), theta);
  }

  /**
   * Computes the spot delta (first derivative with respect to spot).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The spot delta
   */
  public double spotDeltaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }

  /**
   * Computes the forward delta (first derivative with respect to forward).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward delta
   */
  public double forwardDeltaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign / dfForeign;
  }

  /**
   * Computes the spot gamma (second derivative with respect to spot).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The spot gamma
   */
  public double spotGammaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }

  /**
   * Computes the forward gamma (second derivative with respect to forward).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward gamma
   */
  public double forwardGammaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign * dfDomestic
        / dfForeign
        / dfForeign;
  }

  /**
   * Computes the forward vega (first derivative with respect to spot).
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward vega
   */
  public double forwardVegaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.vega(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign / dfDomestic;
  }

  /**
   * Computes the forward driftless theta (derivative with respect to the time). The theta is not scaled. Reference on driftless theta: The
   * complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward driftless theta
   */
  public double forwardDriftlessThetaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.driftlessTheta(forward, strike, expiry, volatility, isCall) * longSign;
  }

  /**
   * Computes the spot theta (derivative with respect to the time). The theta is not scaled. Reference on theta: The complete guide to
   * Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   *
   * @param digitalOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward driftless theta
   */
  public double thetaTheoretical(final ForexOptionDigital digitalOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(digitalOption, "digitalOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(digitalOption.getCurrency1(), digitalOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double paymentTime = digitalOption.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    boolean isCall;
    if (digitalOption.payDomestic()) {
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency2();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency1();
      strike = digitalOption.getStrike();
      isCall = digitalOption.isCall();
    } else {
      strike = 1.0 / digitalOption.getStrike();
      domesticCcy = digitalOption.getUnderlyingForex().getCurrency1();
      foreignCcy = digitalOption.getUnderlyingForex().getCurrency2();
      isCall = !digitalOption.isCall();
    }
    final double dfDomestic = curves.getDiscountFactor(domesticCcy, paymentTime);
    final double dfForeign = curves.getDiscountFactor(foreignCcy, paymentTime);
    final double rDomestic = curves.getCurve(domesticCcy).getInterestRate(paymentTime);
    final double rForeign = curves.getCurve(foreignCcy).getInterestRate(paymentTime);
    final double spot = curves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = digitalOption.getExpirationTime();
    final double volatility = data.getVolatility(foreignCcy, domesticCcy, digitalOption.getExpirationTime(), strike,
        forward);
    final double longSign = digitalOption.isLong() ? 1.0 : -1.0;
    return DigitalOptionFunction.theta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }

}
