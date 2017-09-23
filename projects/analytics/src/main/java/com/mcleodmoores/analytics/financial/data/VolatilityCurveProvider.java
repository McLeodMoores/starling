/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

/**
 *
 */
public abstract class VolatilityCurveProvider implements VolatilityProvider {

  @Override
  public abstract VolatilityCurveProvider copy();

  public abstract double getVolatility(double x);

  @Override
  public double getVolatility(final double x, final double y, final double z) {
    return getVolatility(x);
  }

}
