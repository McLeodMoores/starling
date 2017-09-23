/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

/**
 *
 */
public abstract class VolatilitySurfaceProvider implements VolatilityProvider {

  @Override
  public abstract VolatilitySurfaceProvider copy();

  public abstract double getVolatility(double x, double y);

  @Override
  public double getVolatility(final double x, final double y, final double z) {
    return getVolatility(x, y);
  }
}
