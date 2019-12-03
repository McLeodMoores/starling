/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import org.joda.convert.FromString;

import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;

/**
 * A named extrapolator that wraps {@link LinearExtrapolator1D}.
 */
@InterpolationType(name = "Linear Extrapolator", aliases = "LinearExtrapolator")
public class LinearExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Linear Extrapolator";

  /**
   * A factory method that constructs a linear interpolator from a string. The
   * string to parse must be of the form
   * <code>LinearExtrapolator[INTERPOLATOR_NAME]</code> or
   * <code>Linear Extrapolator[INTERPOLATOR_NAME]</code>, or an exception will
   * be thrown.
   * 
   * @param toParse
   *          the name of the interpolator to parse
   * @return the extrapolator
   */
  @FromString
  public static NamedInterpolator1d of(final String toParse) {
    final int beginIndex = toParse.indexOf("[");
    final int endIndex = toParse.lastIndexOf("]");
    if (beginIndex < 0 || endIndex < 1) {
      throw new IllegalArgumentException("Could not parse " + toParse);
    }
    return new LinearExtrapolator1dAdapter((Interpolator1dAdapter) NamedInterpolator1dFactory.of(toParse.substring(beginIndex, endIndex)));
  }

  /**
   * Creates an instance called "Linear Extrapolator[INTERPOLATOR_NAME]".
   *
   * @param interpolator
   *          the interpolator, not null
   */
  public LinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator) {
    super(new LinearExtrapolator1D(interpolator), NamedInterpolator1dFactory.transformName(NAME, interpolator.getName()), true);
  }

  /**
   * Creates an instance.
   * @param interpolator  the interpolator, not null
   * @param name  the extrapolator name, not null
   */
  public LinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final String name) {
    super(new LinearExtrapolator1D(interpolator), name, true);
  }

}
