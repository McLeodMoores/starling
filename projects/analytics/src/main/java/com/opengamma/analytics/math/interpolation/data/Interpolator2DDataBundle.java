/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class Interpolator2DDataBundle {
  private final double[] _xData;
  private final double[] _yData;
  private final double[] _zData;

  public Interpolator2DDataBundle(final double[] xData, final double[] yData, final double[] zData) {
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    final int n = xData.length;
    Validate.isTrue(n == yData.length);
    Validate.isTrue(n == zData.length);
    _xData = xData;
    _yData = yData;
    _zData = zData;
  }

  public double[] getXData() {
    return _xData;
  }

  public double[] getYData() {
    return _yData;
  }

  public double[] getZData() {
    return _zData;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    result = prime * result + Arrays.hashCode(_zData);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Interpolator2DDataBundle other = (Interpolator2DDataBundle) obj;
    if (!Arrays.equals(_xData, other._xData)) {
      return false;
    }
    if (!Arrays.equals(_yData, other._yData)) {
      return false;
    }
    return Arrays.equals(_zData, other._zData);
  }

}
