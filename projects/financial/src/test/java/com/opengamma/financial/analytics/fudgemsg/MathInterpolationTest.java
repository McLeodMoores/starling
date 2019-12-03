/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.analytics.math.interpolation.factory.CombinedInterpolatorExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NaturalCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.StepInterpolator1dAdapter;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link MathInterpolation}.
 */
@Test(groups = TestGroup.UNIT)
public class MathInterpolationTest extends AnalyticsTestBase {
  private static final StepInterpolator1D STEP = new StepInterpolator1D();
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final NaturalCubicSplineInterpolator1D CUBIC_SPLINE = new NaturalCubicSplineInterpolator1D();
  private static final FlatExtrapolator1D FLAT_EXTRAPOLATOR = new FlatExtrapolator1D();
  private static final LinearExtrapolator1D LINEAR_EXTRAPOLATOR = new LinearExtrapolator1D(CUBIC_SPLINE);
  private static final CombinedInterpolatorExtrapolator COMBINED = new CombinedInterpolatorExtrapolator(CUBIC_SPLINE,
      FLAT_EXTRAPOLATOR, LINEAR_EXTRAPOLATOR);
  private static final GridInterpolator2D GRID_2D = new GridInterpolator2D(LINEAR, STEP);

  /**
   * Tests a cycle of an interpolator.
   */
  public void testInterpolator1D() {
    Interpolator1D cycled = cycleObject(Interpolator1D.class, LINEAR);
    assertEquals(cycled, LINEAR);
    cycled = cycleObject(Interpolator1D.class, CUBIC_SPLINE);
    assertEquals(cycled, CUBIC_SPLINE);
  }

  /**
   * Tests a cycle of an extrapolator that does not have an underlying
   * interpolator.
   */
  public void testFlatExtrapolator1D() {
    final Interpolator1D cycled = cycleObject(Interpolator1D.class, FLAT_EXTRAPOLATOR);
    assertEquals(cycled, FLAT_EXTRAPOLATOR);
  }

  /**
   * Tests a cycle of an extrapolator that has an underlying interpolator.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLinearExtrapolator1D() {
    cycleObject(Interpolator1D.class, LINEAR_EXTRAPOLATOR);
  }

  /**
   * Tests a cycle of a combined interpolator and extrapolator.
   */
  public void testCombinedInterpolator() {
    final CombinedInterpolatorExtrapolator cycled = cycleObject(CombinedInterpolatorExtrapolator.class, COMBINED);
    assertEquals(cycled, COMBINED);
  }

  /**
   * Tests a cycle of a grid interpolator.
   */
  public void testGridInterpolator2D() {
    final Interpolator2D cycled = cycleObject(Interpolator2D.class, GRID_2D);
    assertEquals(cycled, GRID_2D);
  }

  /**
   * Tests a cycle of a named interpolator.
   */
  public void testNamedInterpolator1d() {
    Interpolator1D interpolator = new LinearInterpolator1dAdapter();
    assertEquals(cycleObject(Interpolator1D.class, interpolator), interpolator);
    interpolator = new LinearInterpolator1dAdapter("new name");
    assertEquals(cycleObject(Interpolator1D.class, interpolator), interpolator);
    interpolator = new NaturalCubicSplineInterpolator1dAdapter();
    assertEquals(cycleObject(Interpolator1D.class, interpolator), interpolator);
  }

  /**
   * Tests a cycle of an extrapolator that does not have an underlying
   * interpolator.
   */
  public void testNamedFlatExtrapolator1d() {
    final FlatExtrapolator1dAdapter extrapolator = new FlatExtrapolator1dAdapter();
    final Interpolator1D cycled = cycleObject(Interpolator1D.class, extrapolator);
    assertEquals(cycled, extrapolator);
  }

  /**
   * Tests a cycle of an extrapolator that has an underlying interpolator.
   */
  public void testNamedLinearExtrapolator1d() {
    final LinearExtrapolator1dAdapter extrapolator = new LinearExtrapolator1dAdapter(new StepInterpolator1dAdapter());
    assertEquals(cycleObject(Interpolator1D.class, extrapolator), extrapolator);
  }

  /**
   * Tests a cycle of a combined interpolator and extrapolator.
   */
  public void testNamedCombinedInterpolator() {
    final LinearInterpolator1dAdapter interpolator = new LinearInterpolator1dAdapter();
    final FlatExtrapolator1dAdapter leftExtrapolator = new FlatExtrapolator1dAdapter();
    final LinearExtrapolator1dAdapter rightExtrapolator = new LinearExtrapolator1dAdapter(interpolator);
    CombinedInterpolatorExtrapolator1dAdapter combined = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, leftExtrapolator);
    CombinedInterpolatorExtrapolator1dAdapter cycled = cycleObject(CombinedInterpolatorExtrapolator1dAdapter.class, combined);
    assertEquals(cycled, combined);
    combined = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, leftExtrapolator, rightExtrapolator);
    cycled = cycleObject(CombinedInterpolatorExtrapolator1dAdapter.class, combined);
    assertEquals(cycled, combined);
  }

}
