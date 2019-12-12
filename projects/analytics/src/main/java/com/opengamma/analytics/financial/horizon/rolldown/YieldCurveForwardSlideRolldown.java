/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Produces a yield curve where each curve has a forward slide.
 */
public final class YieldCurveForwardSlideRolldown implements RolldownFunction<YieldAndDiscountCurve> {
  /**
   * A static instance.
   */
  public static final YieldCurveForwardSlideRolldown INSTANCE = new YieldCurveForwardSlideRolldown();

  /**
   * Private constructor
   */
  private YieldCurveForwardSlideRolldown() {
  }

  @Override
  public YieldAndDiscountCurve rollDown(final YieldAndDiscountCurve yieldCurve, final double time) {
    return yieldCurve;
  }

}
