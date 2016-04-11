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
public interface CurveTypeSetUpInterface extends CurveSetUpInterface {

  public CurveTypeSetUpInterface forDiscounting(final Currency currency);

  //TODO versions that only take a single index
  public CurveTypeSetUpInterface forIborIndex(final IborIndex... indices);

  public CurveTypeSetUpInterface forOvernightIndex(final IndexON... indices);

  public CurveTypeSetUpInterface withInterpolator(final Interpolator1D interpolator);
}
