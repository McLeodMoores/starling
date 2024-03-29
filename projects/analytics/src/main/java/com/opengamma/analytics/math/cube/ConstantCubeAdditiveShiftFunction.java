/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link ConstantDoublesCube}. Only parallel shifts of the cube are supported.
 */
public class ConstantCubeAdditiveShiftFunction implements CubeShiftFunction<ConstantDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double shift) {
    Validate.notNull(cube, "cube");
    return evaluate(cube, shift, "PARALLEL_SHIFT_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double shift, final String newName) {
    Validate.notNull(cube, "cube");
    final double v = cube.getValue(0., 0., 0.);
    return ConstantDoublesCube.from(v + shift, newName);
  }

  /**
   * {@inheritDoc}
   *
   * @return Not supported
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double x, final double y, final double z, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * @return Not supported
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double x, final double y, final double z, final double shift,
      final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * @return Not supported
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift,
      final double[] shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * @return Not supported
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] shift,
      final String newName) {
    throw new UnsupportedOperationException();
  }
}
