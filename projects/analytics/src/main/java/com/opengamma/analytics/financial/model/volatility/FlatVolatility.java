/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FlatVolatility implements VolatilityModel1D {

  private final double _vol;

  public FlatVolatility(final double vol) {
    ArgumentChecker.isTrue(vol >= 0.0, "negative vol");
    _vol = vol;

  }

  @Override
  public Double getVolatility(final double[] fwdKT) {
    return _vol;
  }

  @Override
  public double getVolatility(final double forward, final double strike, final double timeToExpiry) {
    return _vol;
  }

  @Override
  public double getVolatility(final SimpleOptionData option) {
    return _vol;
  }

}
