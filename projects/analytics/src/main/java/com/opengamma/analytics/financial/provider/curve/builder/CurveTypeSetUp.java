/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CurveTypeSetUp extends CurveBuilderSetUp implements CurveTypeSetUpInterface {
  private final String _curveName;

  public CurveTypeSetUp(final String curveName, final CurveBuilderSetUp builder) {
    super(builder);
    _curveName = curveName;
  }

  @Override
  public CurveTypeSetUp forDiscounting(final Currency currency) {
    _discountingCurves.put(_curveName, currency);
    return this;
  }

  //TODO versions that only take a single index
  @Override
  public CurveTypeSetUp forIborIndex(final IborIndex... indices) {
    _iborCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public CurveTypeSetUp forOvernightIndex(final IndexON... indices) {
    _overnightCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public CurveTypeSetUp withInterpolator(final Interpolator1D interpolator) {
    _interpolatorForCurve.put(_curveName, interpolator);
    return this;
  }
}
