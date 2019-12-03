/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Parameter object for interpolated stub instrument derivative visitor.
 */
public final class InterpolatedStubData<C extends DepositIndexCoupon<I>, I extends IndexDeposit> {

  private final MulticurveProviderInterface _multicurve;

  private final InterpolatedStubCoupon<C, I> _interpolatedStubCoupon;

  private InterpolatedStubData(
      final MulticurveProviderInterface multicurve,
      final InterpolatedStubCoupon<C, I> interpolatedStubCoupon) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(interpolatedStubCoupon, "interpolatedStubCoupon");
    _multicurve = multicurve;
    _interpolatedStubCoupon = interpolatedStubCoupon;
  }

  public MulticurveProviderInterface getMulticurve() {
    return _multicurve;
  }

  public InterpolatedStubCoupon<C, I> getInterpolatedStubCoupon() {
    return _interpolatedStubCoupon;
  }

  public static <C extends DepositIndexCoupon<I>, I extends IndexDeposit> InterpolatedStubData<C, I> of(
      final MulticurveProviderInterface multicurve,
      final InterpolatedStubCoupon interpolatedStubCoupon) {
    return new InterpolatedStubData(multicurve, interpolatedStubCoupon);
  }
}
