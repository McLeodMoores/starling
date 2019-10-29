/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositZeroDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * For an instrument, this calculates the sensitivity of the par rate (the exact meaning of par rate depends on the instrument - for swaps
 * it is the par swap rate) to points on the yield curve(s) (i.e. dPar/dR at every point the instrument has sensitivity).
 */
public final class ParRateCurveSensitivityDiscountingCalculator
    extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateCurveSensitivityDiscountingCalculator INSTANCE = new ParRateCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static ParRateCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParRateCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator
      .getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the par rate of a swap with one fixed leg.
   *
   * @param swap
   *          The Fixed coupon swap.
   * @param multicurves
   *          The multi-curves provider.
   * @return The par swap rate. If the fixed leg has been set up with some fixed payments these are ignored for the purposes of finding the
   *         swap rate
   */
  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy)
        * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, multicurves);
    final double pvbpBar = -pvSecond / (pvbp * pvbp);
    final double pvSecondBar = 1.0 / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy)
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity result = pvSecondDr.multipliedBy(pvSecondBar).plus(pvbpDr.multipliedBy(pvbpBar));
    return result;
  }

  /**
   * Computes the swap convention-modified par rate for a fixed coupon swap.
   * <P>
   * Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   *
   * @param swap
   *          The swap.
   * @param dayCount
   *          The day count convention to modify the swap rate.
   * @param multicurves
   *          The multi-curves provider.
   * @return The modified rate.
   */
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DayCount dayCount,
      final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy)
        * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, multicurves);
    final double pvbpBar = -pvSecond / (pvbp * pvbp);
    final double pvSecondBar = 1.0 / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy)
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity result = pvSecondDr.multipliedBy(pvSecondBar).plus(pvbpDr.multipliedBy(pvbpBar));
    return result;
  }

  /**
   * Computes the swap convention-modified par rate curve sensitivity for a fixed coupon swap.
   * <P>
   * Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   *
   * @param swap
   *          The swap.
   * @param dayCount
   *          The day count convention to modify the swap rate.
   * @param multicurves
   *          The multi-curves provider.
   * @return The modified rate curve sensitivity.
   */
  public MulticurveSensitivity visitFixedCouponSwapDerivative(final SwapFixedCoupon<?> swap, final DayCount dayCount,
      final MulticurveProviderInterface multicurves) {
    final Currency ccy = swap.getSecondLeg().getCurrency();
    final double pvSecond = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(ccy)
        * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, multicurves);
    final double pvCoeff = 1. / pvbp;
    final double crossCoeff = -1.0 / pvbp / pvbp;
    final double pvbpCoeff = 2.0 * pvSecond / pvbp / pvbp;
    final MulticurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, multicurves);
    final MulticurveSensitivity pvSecondDr = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(ccy)
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final MulticurveSensitivity pvbpDr2 = METHOD_SWAP.presentValueBasisPointSecondOrderCurveSensitivity(swap, dayCount, multicurves);

    final int len = swap.getSecondLeg().getNumberOfPayments();
    final CouponIbor couponInitial = (CouponIbor) swap.getSecondLeg().getPayments()[0];
    final MulticurveSensitivity pvSecondDr2 = CouponIborDiscountingMethod.getInstance()
        .presentValueSecondOrderCurveSensitivity(couponInitial, multicurves)
        .getSensitivity(ccy);
    for (int i = 1; i < len; ++i) {
      final CouponIbor coupon = (CouponIbor) swap.getSecondLeg().getPayments()[i];
      pvSecondDr2
          .plus(CouponIborDiscountingMethod.getInstance().presentValueSecondOrderCurveSensitivity(coupon, multicurves).getSensitivity(ccy));
    }

    final MulticurveSensitivity result = pvSecondDr2.multipliedBy(pvCoeff).plus(pvbpDr2.multipliedBy(pvbpCoeff))
        .plus(pvSecondDr.productOf(pvbpDr.multipliedBy(crossCoeff)))
        .multipliedBy(Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    return result;
  }

  @Override
  public MulticurveSensitivity visitDepositZero(final DepositZero deposit, final MulticurveProviderInterface curves) {
    return DepositZeroDiscountingMethod.getInstance().parRateCurveSensitivity(deposit, curves);
  }

  @Override
  public MulticurveSensitivity visitCash(final Cash cash, final MulticurveProviderInterface curves) {
    return CashDiscountingMethod.getInstance().parRateCurveSensitivity(cash, curves);
  }

  // @Override
  // public MulticurveSensitivity visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface data) {
  // return CouponIborDiscountingMethod.getInstance().parRateCurveSensitivity(payment, data);
  // }
  //
  // @Override
  // public MulticurveSensitivity visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface data) {
  // return CouponIborSpreadDiscountingMethod.getInstance().parRateCurveSensitivity(payment, data);
  // }
  //
  // @Override
  // public MulticurveSensitivity visitCouponOIS(final CouponON payment, final MulticurveProviderInterface data) {
  // return CouponONDiscountingMethod.getInstance().parRateCurveSensitivity(payment, data);
  // }
  //
  // @Override
  // public MulticurveSensitivity visitCapFloorIbor(final CapFloorIbor payment, final MulticurveProviderInterface data) {
  // return visitCouponIborSpread(payment.toCoupon(), data);
  // }
  //
  // @Override
  // public MulticurveSensitivity visitBondFixedSecurity(final BondFixedSecurity bond, final MulticurveProviderInterface curves) {
  // final Annuity<CouponFixed> coupons = bond.getCoupon();
  // final int n = coupons.getNumberOfPayments();
  // final CouponFixed[] unitCoupons = new CouponFixed[n];
  // for (int i = 0; i < n; i++) {
  // unitCoupons[i] = coupons.getNthPayment(i).withUnitCoupon();
  // }
  // final Annuity<CouponFixed> unitCouponAnnuity = new Annuity<>(unitCoupons);
  // final double a = unitCouponAnnuity.accept(PresentValueDiscountingCalculator., curves);
  // final Map<String, List<DoublesPair>> senseA = unitCouponAnnuity.accept(PV_SENSITIVITY_CALCULATOR, curves);
  // final Map<String, List<DoublesPair>> result = new HashMap<>();
  // final PaymentFixed principlePayment = bond.getNominal().getNthPayment(0);
  // final double df = principlePayment.accept(PV_CALCULATOR, curves);
  // final double factor = -(1 - df) / a / a;
  // for (final String name : curves.getAllNames()) {
  // if (senseA.containsKey(name)) {
  // final List<DoublesPair> temp = new ArrayList<>();
  // final List<DoublesPair> list = senseA.get(name);
  // final int m = list.size();
  // for (int i = 0; i < m - 1; i++) {
  // final DoublesPair pair = list.get(i);
  // temp.add(DoublesPair.of(pair.getFirstDouble(), factor * pair.getSecondDouble()));
  // }
  // final DoublesPair pair = list.get(m - 1);
  // temp.add(DoublesPair.of(pair.getFirstDouble(), principlePayment.getPaymentTime() * df / a + factor * pair.getSecondDouble()));
  // result.put(name, temp);
  // }
  // }
  // return result;
  // }

}
