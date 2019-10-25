/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument
 * for which the present value of the instrument is zero. The notion of "spread" will depend of each instrument.
 */
public final class PresentValueBasisPointCurveSensitivityDiscountingCalculator
    extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBasisPointCurveSensitivityDiscountingCalculator INSTANCE = new PresentValueBasisPointCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static PresentValueBasisPointCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBasisPointCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, multicurve);
  }

  @Override
  public MulticurveSensitivity visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface data) {
    return 0.0;
  }

  public MulticurveSensitivity visitCoupon(final Coupon coupon, final MulticurveProviderInterface curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(coupon.getFundingCurveName());
    return fundingCurve.getDiscountFactor(coupon.getPaymentTime()) * coupon.getPaymentYearFraction() * coupon.getNotional();
  }

  @Override
  public MulticurveSensitivity visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public MulticurveSensitivity visitCouponIbor(final CouponIbor coupon, final MulticurveProviderInterface curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public MulticurveSensitivity visitCouponIborSpread(final CouponIborSpread coupon, final MulticurveProviderInterface curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public MulticurveSensitivity visitCouponIborCompounding(final CouponIborCompounding coupon, final MulticurveProviderInterface curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public MulticurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface curves) {
    return ForwardRateAgreementDiscountingProviderMethod.getInstance().presentValueCouponSensitivity(fra, curves) * fra.getNotional();
  }

  // ----- Futures ------

  @Override
  public MulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction future,
      final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(future, "Futures");
    ArgumentChecker.notNull(curves, "Bundle");
    return future.getUnderlyingSecurity().getNotional() * future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getQuantity();
  }

}
