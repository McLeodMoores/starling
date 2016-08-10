/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute present value and present value sensitivity for Ibor compounding coupon with spread and compounding type "Flat Compounding".
 * The definition of "Flat Compounding" is available in the ISDA document:
 * Reference: Alternative compounding methods for over-the-counter derivative transactions (2009)
 */
public final class CouponIborCompoundingFlatSpreadDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponIborCompoundingFlatSpreadDiscountingMethod INSTANCE = new CouponIborCompoundingFlatSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponIborCompoundingFlatSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponIborCompoundingFlatSpreadDiscountingMethod() {
  }

  /**
   * Compute the present value of a Ibor compounded coupon with compounding type "Flat Compounding" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    return presentValue(coupon, multicurve, IborForwardRateProvider.getInstance());
  }

  /**
   * Compute the present value of a Ibor compounded coupon with compounding type "Flat Compounding" using the specified
   * forward rate provider by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @param forwardRateProvider The forward rate provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(
      final CouponIborCompoundingFlatSpread coupon,
      final MulticurveProviderInterface multicurve,
      final ForwardRateProvider<IborIndex> forwardRateProvider) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(forwardRateProvider, "forwardRateProvider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    double cpaAccumulated = coupon.getCompoundingPeriodAmountAccumulated();
    for (int i = 0; i < nbSubPeriod; i++) {
      final double forward = forwardRateProvider.getRate(
          multicurve,
          coupon,
          coupon.getFixingPeriodStartTimes()[i],
          coupon.getFixingPeriodEndTimes()[i],
          coupon.getFixingPeriodAccrualFactors()[i]);
      cpaAccumulated += cpaAccumulated * forward * coupon.getSubperiodsAccrualFactors()[i]; // Additional Compounding Period Amount
      cpaAccumulated += coupon.getNotional() * (forward + coupon.getSpread()) * coupon.getSubperiodsAccrualFactors()[i]; // Basic Compounding Period Amount
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    final double pv = cpaAccumulated * df;
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Compute the sensitivity of the present value of a Ibor compounded coupon with compounding type "Flat Compounding" to the spread.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  public double presentValueSpreadSensitivity(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    final double spread = coupon.getSpread();
    final double[] cpa = new double[nbSubPeriod + 1];
    final double[] cpaAccumulated = new double[nbSubPeriod + 1];
    final double[] forward = new double[nbSubPeriod];
    cpa[0] = coupon.getCompoundingPeriodAmountAccumulated();
    cpaAccumulated[0] = coupon.getCompoundingPeriodAmountAccumulated();
    for (int i = 0; i < nbSubPeriod; i++) {
      forward[i] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTimes()[i], coupon.getFixingPeriodEndTimes()[i],
          coupon.getFixingPeriodAccrualFactors()[i]);
      cpa[i + 1] += coupon.getNotional() * (forward[i] + spread) * coupon.getSubperiodsAccrualFactors()[i]; // Basic Compounding Period Amount
      cpa[i + 1] += cpaAccumulated[i] * forward[i] * coupon.getSubperiodsAccrualFactors()[i]; // Additional Compounding Period Amount
      cpaAccumulated[i + 1] = cpaAccumulated[i] + cpa[i + 1];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double[] cpaAccumulatedBar = new double[nbSubPeriod + 1];
    cpaAccumulatedBar[nbSubPeriod] = df * pvBar;
    final double[] cpaBar = new double[nbSubPeriod + 1];
    double spreadBar = 0;
    for (int i = nbSubPeriod - 1; i >= 0; i--) {
      cpaAccumulatedBar[i] = cpaAccumulatedBar[i + 1];
      cpaBar[i + 1] += cpaAccumulatedBar[i + 1];
      cpaAccumulatedBar[i] += forward[i] * coupon.getSubperiodsAccrualFactors()[i] * cpaBar[i + 1];
      spreadBar += coupon.getNotional() * coupon.getSubperiodsAccrualFactors()[i] * cpaBar[i + 1];
    }
    return spreadBar;
  }

  /**
   * Compute the present value sensitivity to rates of a Ibor compounded coupon with compounding type "Flat Compounding" by discounting.
   * @param coupon The coupon.
   * @param multicurve The multi-curve provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(multicurve, "Multi-curve provider");
    final int nbSubPeriod = coupon.getFixingTimes().length;
    final double[] cpa = new double[nbSubPeriod + 1];
    final double[] cpaAccumulated = new double[nbSubPeriod + 1];
    final double[] forward = new double[nbSubPeriod];
    cpa[0] = coupon.getCompoundingPeriodAmountAccumulated();
    cpaAccumulated[0] = coupon.getCompoundingPeriodAmountAccumulated();
    for (int i = 0; i < nbSubPeriod; i++) {
      forward[i] = multicurve.getSimplyCompoundForwardRate(coupon.getIndex(), coupon.getFixingPeriodStartTimes()[i], coupon.getFixingPeriodEndTimes()[i],
          coupon.getFixingPeriodAccrualFactors()[i]);
      cpa[i + 1] += coupon.getNotional() * (forward[i] + coupon.getSpread()) * coupon.getSubperiodsAccrualFactors()[i]; // Basic Compounding Period Amount
      cpa[i + 1] += cpaAccumulated[i] * forward[i] * coupon.getSubperiodsAccrualFactors()[i]; // Additional Compounding Period Amount
      cpaAccumulated[i + 1] = cpaAccumulated[i] + cpa[i + 1];
    }
    final double df = multicurve.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfBar = cpaAccumulated[nbSubPeriod] * pvBar;
    final double[] cpaAccumulatedBar = new double[nbSubPeriod + 1];
    cpaAccumulatedBar[nbSubPeriod] = df * pvBar;
    final double[] cpaBar = new double[nbSubPeriod + 1];
    final double[] forwardBar = new double[nbSubPeriod];
    for (int i = nbSubPeriod - 1; i >= 0; i--) {
      cpaAccumulatedBar[i] = cpaAccumulatedBar[i + 1];
      cpaBar[i + 1] += cpaAccumulatedBar[i + 1];
      cpaAccumulatedBar[i] += forward[i] * coupon.getSubperiodsAccrualFactors()[i] * cpaBar[i + 1];
      forwardBar[i] += cpaAccumulated[i] * coupon.getSubperiodsAccrualFactors()[i] * cpaBar[i + 1];
      forwardBar[i] += coupon.getNotional() * coupon.getSubperiodsAccrualFactors()[i] * cpaBar[i + 1];
    }
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(coupon.getPaymentTime(), -coupon.getPaymentTime() * df * dfBar));
    mapDsc.put(multicurve.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    for (int i = 0; i < nbSubPeriod; i++) {
      listForward.add(new SimplyCompoundedForwardSensitivity(coupon.getFixingPeriodStartTimes()[i], coupon.getFixingPeriodEndTimes()[i],
          coupon.getFixingPeriodAccrualFactors()[i],
          forwardBar[i]));
    }
    mapFwd.put(multicurve.getName(coupon.getIndex()), listForward);
    return MultipleCurrencyMulticurveSensitivity.of(coupon.getCurrency(), MulticurveSensitivity.of(mapDsc, mapFwd));
  }

}
