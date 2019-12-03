/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The methods associated to the pricing of deposit by discounting.
 */
public final class DepositZeroDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final DepositZeroDiscountingMethod INSTANCE = new DepositZeroDiscountingMethod();

  /**
   * Return the unique instance of the class.
   *
   * @return The instance.
   */
  public static DepositZeroDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private DepositZeroDiscountingMethod() {
  }

  /**
   * Compute the present value by discounting the final cash flow (nominal + interest) and the initial payment (initial amount).
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final DepositZero deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double pv = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    return CurrencyAmount.of(deposit.getCurrency(), pv);
  }

  /**
   * Compute the present value curve sensitivity by discounting the final cash flow (nominal + interest) and the initial payment (initial
   * amount).
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final DepositZero deposit,
      final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    // Backward sweep
    final double pvBar = 1.0;
    final double dfEndBar = deposit.getNotional() + deposit.getInterestAmount() * pvBar;
    final double dfStartBar = -deposit.getInitialAmount() * pvBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
    result = result.plus(deposit.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    return result;
  }

  /**
   * Computes the deposit fair rate given the start and end time and the accrual factor. When deposit has already start the number may not
   * be meaning full as the remaining period is not in line with the accrual factor.
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The rate.
   */
  public double parRate(final DepositZero deposit, final MulticurveProviderInterface curves) {
    final double startTime = deposit.getStartTime();
    final double endTime = deposit.getEndTime();
    final double rcc = Math
        .log(curves.getDiscountFactor(deposit.getCurrency(), startTime) / curves.getDiscountFactor(deposit.getCurrency(), endTime))
        / deposit.getPaymentAccrualFactor();
    final InterestRate rate = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc));
    return rate.getRate();
  }

  /**
   * Computes the deposit fair rate curve sensitivity. When deposit has already start the number may not be meaning full as the remaining
   * period is not in line with the accrual factor.
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The rate sensitivity.
   */
  public MulticurveSensitivity parRateCurveSensitivity(final DepositZero deposit, final MulticurveProviderInterface curves) {
    final double dfStartTime = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEndTime = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double rcc = Math.log(dfStartTime / dfEndTime) / deposit.getPaymentAccrualFactor();
    final double rateBar = 1.0;
    final double rccBar = deposit.getRate().fromContinuousDerivative(new ContinuousInterestRate(rcc)) * rateBar;
    final double dfEndTimeBar = -1.0 / dfEndTime / deposit.getPaymentAccrualFactor() * rccBar;
    final double dfStartTimeBar = 1.0 / dfStartTime / deposit.getPaymentAccrualFactor() * rccBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStartTime * dfStartTimeBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEndTime * dfEndTimeBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    return MulticurveSensitivity.ofYieldDiscounting(mapDsc);
  }

  /**
   * Computes the spread to be added to the deposit rate to have a zero present value. When deposit has already start the number may not be
   * meaning full as the remaining period is not in line with the accrual factor.
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The spread.
   */
  public double parSpread(final DepositZero deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double ccrs = Math.log(deposit.getInitialAmount() * dfStart / (deposit.getNotional() * dfEnd))
        / deposit.getPaymentAccrualFactor();
    final InterestRate rs = deposit.getRate().fromContinuous(new ContinuousInterestRate(ccrs));
    return rs.getRate() - deposit.getRate().getRate();
  }

  /**
   * Computes the par spread curve sensitivity.
   *
   * @param deposit
   *          The deposit.
   * @param curves
   *          The curves.
   * @return The spread curve sensitivity.
   */
  public MulticurveSensitivity parSpreadCurveSensitivity(final DepositZero deposit, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(deposit, "deposit");
    ArgumentChecker.notNull(curves, "curves");
    final double dfStart = curves.getDiscountFactor(deposit.getCurrency(), deposit.getStartTime());
    final double dfEnd = curves.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double ccrs = Math.log(deposit.getInitialAmount() * dfStart / (deposit.getNotional() * dfEnd))
        / deposit.getPaymentAccrualFactor();
    // Backward sweep
    final double parSpreadBar = 1.0;
    final double rsBar = parSpreadBar;
    final double ccrsBar = deposit.getRate().fromContinuousDerivative(new ContinuousInterestRate(ccrs)) * rsBar;
    final double dfEndBar = -1 / (dfEnd * deposit.getPaymentAccrualFactor()) * ccrsBar;
    final double dfStartBar = 1 / (dfEnd * deposit.getPaymentAccrualFactor()) * ccrsBar;
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(deposit.getStartTime(), -deposit.getStartTime() * dfStart * dfStartBar));
    listDiscounting.add(DoublesPair.of(deposit.getEndTime(), -deposit.getEndTime() * dfEnd * dfEndBar));
    mapDsc.put(curves.getName(deposit.getCurrency()), listDiscounting);
    return MulticurveSensitivity.ofYieldDiscounting(mapDsc);
  }

}
