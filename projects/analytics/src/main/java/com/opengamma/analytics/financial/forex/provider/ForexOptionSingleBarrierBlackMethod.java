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

import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
 * Pricing method for single barrier barrierOption transactions in the Black world.
 */
public final class ForexOptionSingleBarrierBlackMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionSingleBarrierBlackMethod INSTANCE = new ForexOptionSingleBarrierBlackMethod();

  /**
   * Private constructor.
   */
  private ForexOptionSingleBarrierBlackMethod() {
  }

  /**
   * Return the unique instance of the class.
   *
   * @return The instance.
   */
  public static ForexOptionSingleBarrierBlackMethod getInstance() {
    return INSTANCE;
  }

  private static final double DEFAULT_GAMMA_SHIFT = 0.00001; // 0.1 basis point
  private static final double DEFAULT_VOMMA_SHIFT = 0.00001; // 0.1 basis point
  private static final double DEFAULT_VANNA_SHIFT = 0.00001; // 0.1 basis point
  private static final double DEFAULT_THETA_SHIFT = 1. / 365 / 24; // one hour

  /**
   * The Black function used in the barrier pricing.
   */
  private static final BlackBarrierPriceFunction BARRIER_FUNCTION = BlackBarrierPriceFunction.getInstance();

  /**
   * Computes the present value for single barrier barrierOption in Black model (log-normal spot rate).
   *
   * @param barrierOption
   *          The barrier option.
   * @param data
   *          The curve and smile data.
   * @return The present value (in domestic currency).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    double price = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility);
    price *= Math.abs(foreignAmount) * sign;
    final CurrencyAmount priceCurrency = CurrencyAmount.of(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Computes the currency exposure for single barrier barrierOption in Black model (log-normal spot rate). The sensitivity of the
   * volatility on the spot is not taken into account. It is the currency exposure in the Black model where the volatility is suppose to be
   * constant for curve and forward changes.
   *
   * @param barrierOption
   *          The barrier option.
   * @param data
   *          The curve and smile data.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionSingleBarrier barrierOption,
      final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final double[] priceDerivatives = new double[5];
    double price = BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility,
        priceDerivatives);
    price *= Math.abs(foreignAmount) * sign;
    final double deltaSpot = priceDerivatives[0];
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency1(),
        deltaSpot * Math.abs(foreignAmount) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2(),
        -deltaSpot * Math.abs(barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount()) * spot * sign
            + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not
   * taken into account. It is the curve sensitivity in the Black model where the volatility is suppose to be constant for curve and forward
   * changes.
   *
   * @param barrierOption
   *          The barrier option.
   * @param data
   *          The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionSingleBarrier barrierOption,
      final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    // Forward sweep
    final double forward = spot * dfForeign / dfDomestic;
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    // The Barrier pricing method parameterizes as a function of rate (r=rateDomestic), and costOfCarry (b=rateDomestic-rateForeign)
    // We wish to compute derivatives wrt rateDomestic and rateForeign, not the costOfCarry parameter.
    final double[] priceDerivatives = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic, volatility, priceDerivatives);
    // Backward sweep
    final double priceBar = 1.0;
    final double rCostOfCarryBar = priceDerivatives[3] * Math.abs(foreignAmount) * sign * priceBar;
    final double rDomesticBar = (priceDerivatives[2] + priceDerivatives[3]) * Math.abs(foreignAmount) * sign * priceBar;
    final double rForeignBar = -1 * rCostOfCarryBar;
    // Sensitivity object
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    resultMap.put(curves.getName(barrierOption.getCurrency1()), listForeign);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    resultMap.put(curves.getName(barrierOption.getCurrency2()), listDomestic);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2(), result);
  }

  /**
   * Computes the volatility sensitivity of the option present value.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @return The curve sensitivity.
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionSingleBarrier barrierOption,
      final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final double[] priceDerivatives = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic, volatility, priceDerivatives);
    final double volatilitySensitivityValue = priceDerivatives[4] * Math.abs(foreignAmount) * sign;
    final DoublesPair point = DoublesPair.of(barrierOption.getUnderlyingOption().getTimeToExpiry(),
        barrierOption.getUnderlyingOption().getStrike());
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(
        barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency1(), barrierOption
            .getUnderlyingOption().getUnderlyingForex().getCurrency2(),
        result);
    return sensi;
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a vanilla option with the Black function and a volatility from a
   * volatility surface. The sensitivity is computed with respect to each node in the volatility surface.
   *
   * @param barrierOption
   *          The barrier option.
   * @param data
   *          The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(
      final ForexOptionSingleBarrier barrierOption,
      final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(barrierOption, data);
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = data.getVolatility();
    final double df = curves.getDiscountFactor(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2(),
        barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime());
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot
        * curves.getDiscountFactor(barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency1(),
            barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime())
        / df;
    final VolatilityAndBucketedSensitivities volAndSensitivities = volatilityModel
        .getVolatilityAndSensitivities(barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption()
            .getStrike(), forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(barrierOption.getUnderlyingOption().getTimeToExpiry(),
        barrierOption.getUnderlyingOption().getStrike());
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int i = 0; i < volatilityModel.getNumberExpiration(); i++) {
      for (int j = 0; j < volatilityModel.getNumberStrike(); j++) {
        vega[i][j] = nodeWeight[i][j] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(
        barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency1(),
        barrierOption.getUnderlyingOption().getUnderlyingForex()
            .getCurrency2(),
        new DoubleMatrix1D(volatilityModel.getTimeToExpiration()),
        new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

  /**
   * Computes the implied Black volatility of the barrier option.
   *
   * @param barrierOption
   *          The barrier option.
   * @param data
   *          The curve and smile data.
   * @return The implied volatility.
   */
  public double impliedVolatility(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double rateDomestic = curves.getCurve(barrierOption.getCurrency2()).getInterestRate(payTime);
    final double rateForeign = curves.getCurve(barrierOption.getCurrency1()).getInterestRate(payTime);
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    return volatility;
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up
   * to the first order divided by the option notional.
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The delta.
   */
  public double deltaRelative(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final double payTime = underlyingOption.getUnderlyingForex().getPaymentTime();
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = curves.getCurve(barrierOption.getCurrency2()).getInterestRate(payTime);
    final double rateForeign = curves.getCurve(barrierOption.getCurrency1()).getInterestRate(payTime);
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final double sign = underlyingOption.isLong() ? 1.0 : -1.0;
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double[] adjoint = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic, volatility, adjoint);
    final double deltaDirect = adjoint[0] * sign / dfForeign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the delta of the Forex option. The delta is the first order derivative of the option present value to the spot fx rate.
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @param directQuote
   *          Flag indicating if the delta should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The delta.
   */
  public CurrencyAmount delta(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final double deltaRelative = deltaRelative(barrierOption, data, directQuote);
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    return CurrencyAmount.of(underlyingOption.getUnderlyingForex().getCurrency2(),
        deltaRelative * Math.abs(underlyingOption.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the forward delta (first derivative with respect to forward).
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The forward delta
   */
  public double forwardDeltaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    return spotDeltaTheoretical(barrierOption, data) / dfForeign;
  }

  /**
   * Computes the spot delta (first derivative with respect to spot).
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The spot delta
   */
  public double spotDeltaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final double payTime = underlyingOption.getUnderlyingForex().getPaymentTime();
    final double rateDomestic = curves.getCurve(barrierOption.getCurrency2()).getInterestRate(payTime);
    final double rateForeign = curves.getCurve(barrierOption.getCurrency1()).getInterestRate(payTime);
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double volatility = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double[] adjoint = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic, volatility, adjoint);
    return adjoint[0];
  }

  /**
   * Computes the 2nd order spot fx sensitivity of the option present value by centered finite difference.
   * <p>
   * This gamma is be computed with respect to the direct quote (1 foreign = x domestic)
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Gamma
   */
  public CurrencyAmount gammaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption()
            .getStrike(),
        forward);
    // Bump and compute vega
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spotUp, rateDomestic - rateForeign, rateDomestic, vol, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spotDown, rateDomestic - rateForeign, rateDomestic, vol, adjointDown);
    final double deltaUp = adjointUp[0] * Math.abs(foreignAmount) * sign;
    final double deltaDown = adjointDown[0] * Math.abs(foreignAmount) * sign;

    final double gamma = (deltaUp - deltaDown) / (2 * relShift * spot);
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, gamma);
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv. The reason to
   * multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to
   * X(1+e)).
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @param directQuote
   *          Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote
   *          (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final boolean directQuote) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final PaymentFixed paymentCurrency2 = underlyingOption.getUnderlyingForex().getPaymentCurrency2();
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double gammaDirect = gammaFd(barrierOption, data).getAmount() * sign;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    if (directQuote) {
      return CurrencyAmount.of(paymentCurrency2.getCurrency(), gammaDirect);
    }
    final double deltaDirect = spotDeltaTheoretical(barrierOption, data) * sign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot * spot;
    return CurrencyAmount.of(paymentCurrency2.getCurrency(), gamma);
  }

  /**
   * Computes the 2nd order spot fx sensitivity of the option present value by centered finite difference and a relative shift of 10 basis
   * points.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @return Gamma
   */
  public CurrencyAmount gammaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    return gammaFd(barrierOption, data, DEFAULT_GAMMA_SHIFT);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue (point, value)
   */
  public CurrencyAmount vommaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption()
            .getStrike(),
        forward);
    // Bump and compute vega
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spot, rateDomestic - rateForeign, rateDomestic, volUp, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spot, rateDomestic - rateForeign, rateDomestic, volDown, adjointDown);
    final double vegaUp = adjointUp[4] * Math.abs(foreignAmount) * sign;
    final double vegaDown = adjointDown[4] * Math.abs(foreignAmount) * sign;

    final double vomma = (vegaUp - vegaDown) / (2 * relShift * vol);
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vomma);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference and a default relative shift of
   * 1 basis point.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vommaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    return vommaFd(barrierOption, data, DEFAULT_VOMMA_SHIFT);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vannaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    return d2PriceDSpotDVolFD(barrierOption, data, relShift);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference and a relative shift of 10
   * basis points.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vannaFd(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    return vannaFd(barrierOption, data, DEFAULT_VANNA_SHIFT);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the vega.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount dVegaDSpotFD(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    // Bump *spot* and compute vega
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spotUp, rateDomestic - rateForeign, rateDomestic, vol, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spotDown, rateDomestic - rateForeign, rateDomestic, vol, adjointDown);
    final double vegaUp = adjointUp[4] * Math.abs(foreignAmount) * sign;
    final double vegaDown = adjointDown[4] * Math.abs(foreignAmount) * sign;

    final double vanna = (vegaUp - vegaDown) / (2 * relShift * vol);
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the delta.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount dDeltaDVolFD(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption()
            .getStrike(),
        forward);
    // Bump *vol* and compute delta
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spot, rateDomestic - rateForeign, rateDomestic, volUp, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit,
        spot, rateDomestic - rateForeign, rateDomestic, volDown, adjointDown);
    final double deltaUp = adjointUp[0] * Math.abs(foreignAmount) * sign;
    final double deltaDown = adjointDown[0] * Math.abs(foreignAmount) * sign;

    final double vanna = (deltaUp - deltaDown) / (2 * relShift * spot);
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount d2PriceDSpotDVolFD(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double costOfCarry = rateDomestic - rateForeign;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    // Bump spot *and* vol and compute *price*
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double pxUpUp = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volUp);
    final double pxDownDown = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volDown);
    final double pxUpDown = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volDown);
    final double pxDownUp = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volUp);

    final double vanna = (pxUpUp - pxUpDown - pxDownUp + pxDownDown) / (2 * relShift * spot) / (2 * relShift * vol)
        * Math.abs(foreignAmount) * sign;
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price.
   *
   * @param barrierOption
   *          A single barrier barrierOption.
   * @param data
   *          The curve and smile data.
   * @param relShift
   *          The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount d2PriceDSpotDVolFdAlt(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data,
      final double relShift) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    final MulticurveProviderInterface curves = data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double costOfCarry = rateDomestic - rateForeign;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    // Bump spot *and* vol and compute *price*
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;

    final double pxBase = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spot, costOfCarry, rateDomestic, vol);

    final double pxUpUp = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volUp);
    final double pxDownDown = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volDown);

    final double pxVolUp = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spot, costOfCarry, rateDomestic, volUp);
    final double pxVolDown = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spot, costOfCarry, rateDomestic, volDown);

    final double pxSpotUp = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, vol);
    final double pxSpotDown = BARRIER_FUNCTION.getPrice(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(),
        rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, vol);

    final double vanna = (pxUpUp - pxVolUp - pxSpotUp + 2 * pxBase + pxDownDown - pxVolDown - pxSpotDown)
        / (2 * relShift * spot * relShift * vol) * Math.abs(foreignAmount) * sign;
    final Currency ccy = barrierOption.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the theta (derivative with respect to the time). The theta is not scaled.
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The theta
   */
  public CurrencyAmount forwardTheta(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final ForexOptionVanilla upOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() + DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceUp = BARRIER_FUNCTION.getPrice(upOption, barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic,
        vol) / dfDomestic;
    final ForexOptionVanilla downOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() - DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceDown = BARRIER_FUNCTION.getPrice(downOption, barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign,
        rateDomestic,
        vol) / dfDomestic;
    final double theta = -0.5 * (priceUp - priceDown) / DEFAULT_THETA_SHIFT * sign
        * Math.abs(underlyingOption.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(underlyingOption.getUnderlyingForex().getCurrency2(), theta);
  }

  /**
   * Computes the theta (derivative with respect to the time). The theta is not scaled.
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The theta
   */
  public double thetaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final double payTime = underlyingOption.getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final ForexOptionVanilla upOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() + DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceUp = BARRIER_FUNCTION.getPrice(upOption, barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign, rateDomestic,
        vol);
    final ForexOptionVanilla downOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() - DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceDown = BARRIER_FUNCTION.getPrice(downOption, barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign,
        rateDomestic,
        vol);
    return -0.5 * (priceUp - priceDown) / DEFAULT_THETA_SHIFT;
  }

  /**
   * Computes the theta (derivative with respect to the time). The theta is not scaled.
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The theta
   */
  public double forwardDriftlessThetaTheoretical(final ForexOptionSingleBarrier barrierOption,
      final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final ForexOptionVanilla underlyingOption = barrierOption.getUnderlyingOption();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double sign = barrierOption.getUnderlyingOption().isLong() ? 1.0 : -1.0;
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final ForexOptionVanilla upOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() + DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceUp = BARRIER_FUNCTION.getPrice(upOption, barrierOption.getBarrier(), rebateByForeignUnit, forward, 0., 0., vol);
    final ForexOptionVanilla downOption = new ForexOptionVanilla(underlyingOption.getUnderlyingForex(),
        underlyingOption.getTimeToExpiry() - DEFAULT_THETA_SHIFT,
        underlyingOption.isCall(), underlyingOption.isLong());
    final double priceDown = BARRIER_FUNCTION.getPrice(downOption, barrierOption.getBarrier(), rebateByForeignUnit, forward, 0., 0., vol);
    return -0.5 * (priceUp - priceDown) / DEFAULT_THETA_SHIFT;
  }

  /**
   * Computes the forward gamma (second derivative with respect to forward).
   *
   * @param barrierOption
   *          The Forex option.
   * @param data
   *          The curve and smile data.
   * @return The spot gamma
   */
  public double forwardGammaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    return spotGammaTheoretical(barrierOption, data) * dfDomestic / (dfForeign * dfForeign);
  }

  /**
   * Computes the 2nd order spot fx sensitivity of the option present value by centered finite difference.
   * <p>
   * This gamma is be computed with respect to the direct quote (1 foreign = x domestic)
   *
   * @param barrierOption
   *          A single barrier Forex option.
   * @param data
   *          The curve and smile data.
   * @return Gamma
   */
  public double spotGammaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    final double spotUp = (1.0 + DEFAULT_GAMMA_SHIFT) * spot;
    final double spotDown = (1.0 - DEFAULT_GAMMA_SHIFT) * spot;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spotUp,
        rateDomestic - rateForeign,
        rateDomestic,
        vol, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spotDown,
        rateDomestic - rateForeign,
        rateDomestic, vol, adjointDown);
    return (adjointUp[0] - adjointDown[0]) / (2 * DEFAULT_GAMMA_SHIFT * spot);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference.
   *
   * @param barrierOption
   *          A single barrier Forex option.
   * @param data
   *          The curve and smile data.
   * @return the forward vega
   */
  public double forwardVegaTheoretical(final ForexOptionSingleBarrier barrierOption, final BlackForexSmileProviderInterface data) {
    ArgumentChecker.notNull(barrierOption, "barrierOption");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.checkCurrencies(barrierOption.getCurrency1(), barrierOption.getCurrency2()),
        "Option currencies not compatible with smile data");
    // can't get interest rate data otherwise
    ArgumentChecker.isTrue(data.getMulticurveProvider() instanceof MulticurveProviderDiscount,
        "Curve data provider must be MulticurveProviderDiscount, have {}", data.getMulticurveProvider().getClass());
    final MulticurveProviderDiscount curves = (MulticurveProviderDiscount) data.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = curves.getDiscountFactor(barrierOption.getCurrency2(), payTime);
    final double dfForeign = curves.getDiscountFactor(barrierOption.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = curves.getFxRate(barrierOption.getCurrency1(), barrierOption.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = barrierOption.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = barrierOption.getRebate() / Math.abs(foreignAmount);
    final double vol = data.getVolatility(barrierOption.getCurrency1(), barrierOption.getCurrency2(),
        barrierOption.getUnderlyingOption().getTimeToExpiry(), barrierOption.getUnderlyingOption().getStrike(), forward);
    // Bump and compute vega
    final double[] adjoint = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(barrierOption.getUnderlyingOption(), barrierOption.getBarrier(), rebateByForeignUnit, spot,
        rateDomestic - rateForeign,
        rateDomestic,
        vol, adjoint);
    return adjoint[4];
  }

}
