/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Collections;
import java.util.List;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class IsdaCompliantYieldCurveAdapter extends YieldAndDiscountCurve {

  public static IsdaCompliantYieldCurveAdapter of(final ISDACompliantYieldCurve underlying) {
    return new IsdaCompliantYieldCurveAdapter(underlying);
  }

  private final ISDACompliantYieldCurve _underlying;

  private IsdaCompliantYieldCurveAdapter(final ISDACompliantYieldCurve underlying) {
    super(underlying.getName());
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  public ISDACompliantYieldCurve getUnderlying() {
    return _underlying;
  }

  @Override
  public double getInterestRate(final Double x) {
    return _underlying.getZeroRate(x);
  }

  @Override
  public double getDiscountFactor(final double t) {
    return _underlying.getDiscountFactor(t);
  }

  @Override
  public double getForwardRate(final double t) {
    return _underlying.getForwardRate(t);
  }

  @Override
  public double[] getInterestRateParameterSensitivity(final double time) {
    return _underlying.getNodeSensitivity(time);
  }

  @Override
  public int getNumberOfParameters() {
    return _underlying.getNumberOfKnots();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return Collections.singletonList(_underlying.getName());
  }

}
