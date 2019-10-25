/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;

/**
 *
 */
public class CouponAccrualDiscountFactorVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderDiscount, double[]> {

  @Override
  public double[] visitCouponIbor(final CouponIbor payment, final MulticurveProviderDiscount curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getIndex());
    return new double[] { forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()),
        forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) };
  }

  @Override
  public double[] visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderDiscount curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getIndex());
    return new double[] { forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()),
        forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) };
  }

  @Override
  public double[] visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderDiscount curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getIndex());
    return new double[] { forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()),
        forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) };
  }

}
