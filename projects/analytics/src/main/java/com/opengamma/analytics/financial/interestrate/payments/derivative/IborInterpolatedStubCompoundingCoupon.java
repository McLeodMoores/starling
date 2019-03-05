/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.provider.calculator.discounting.InterpolatedStubCouponVisitor;

/**
 *
 */
public final class IborInterpolatedStubCompoundingCoupon extends InterpolatedStubCoupon<DepositIndexCompoundingCoupon<IborIndex>, IborIndex>
implements DepositIndexCoupon<IborIndex> {

  private IborInterpolatedStubCompoundingCoupon(final DepositIndexCompoundingCoupon<IborIndex> fullCoupon, final double firstInterpolatedTime,
      final double firstInterpolatedYearFraction, final double secondInterpolatedTime, final double secondInterpolatedYearFraction) {
    super(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, secondInterpolatedTime, secondInterpolatedYearFraction);
  }

  /**
   * @param fullCoupon
   *          the full coupon
   * @param firstInterpolatedTime
   *          the first interpolation time
   * @param firstInterpolatedYearFraction
   *          the year fraction of the first interpolation time
   * @param secondInterpolatedTime
   *          the second interpolation time
   * @param secondInterpolatedYearFraction
   *          the year fraction of the second interpolation time
   * @return the coupon
   */
  public static IborInterpolatedStubCompoundingCoupon from(final DepositIndexCompoundingCoupon<IborIndex> fullCoupon, final double firstInterpolatedTime,
      final double firstInterpolatedYearFraction, final double secondInterpolatedTime, final double secondInterpolatedYearFraction) {
    return new IborInterpolatedStubCompoundingCoupon(fullCoupon, firstInterpolatedTime, firstInterpolatedYearFraction, secondInterpolatedTime,
        secondInterpolatedYearFraction);
  }

  @Override
  public Coupon withNotional(final double notional) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S> S accept(final InterpolatedStubCouponVisitor<S> visitor) {
    return visitor.visitIborCompoundingInterpolatedStub(this);
  }
}
