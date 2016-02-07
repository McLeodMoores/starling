/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.QuadraticPolynomialLeftExtrapolator;

/**
 * A named extrapolator that wraps {@link QuadraticPolynomialLeftExtrapolator}.
 */
@InterpolationType(name = "Quadratic Left Extrapolator", aliases = "QuadraticLeftExtrapolator")
public class QuadraticLeftExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Quadratic Left Extrapolator";

  /**
   * Creates an instance called "Quadratic Left Extrapolator[INTERPOLATOR_NAME]".
   * @param interpolator  the interpolator, not null
   */
  public QuadraticLeftExtrapolator1dAdapter(final Interpolator1dAdapter interpolator) {
    super(new QuadraticPolynomialLeftExtrapolator(interpolator), NamedInterpolator1dFactory.transformName(NAME, interpolator.getName()), true);
  }

  /**
   * Creates an instance.
   * @param interpolator  the interpolator, not null
   * @param name  the extrapolator name, not null
   */
  public QuadraticLeftExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final String name) {
    super(new QuadraticPolynomialLeftExtrapolator(interpolator), name, true);
  }

}
