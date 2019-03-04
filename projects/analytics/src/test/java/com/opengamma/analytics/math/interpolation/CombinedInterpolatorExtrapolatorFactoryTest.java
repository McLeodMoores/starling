/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory.getInterpolator;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NaturalCubicSplineInterpolator1dAdapter;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 * 
 * @deprecated
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CombinedInterpolatorExtrapolatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName1() {
    getInterpolator("Wrong name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName2() {
    getInterpolator("Wrong name", FlatExtrapolator1dAdapter.NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadInterpolatorName3() {
    getInterpolator("Wrong name", FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName1() {
    getInterpolator(LinearInterpolator1dAdapter.NAME, "Wrong name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName2() {
    getInterpolator(LinearInterpolator1dAdapter.NAME, "Wrong name", FlatExtrapolator1dAdapter.NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadExtrapolatorName3() {
    getInterpolator(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, "Wrong name");
  }

  @Test
  public void testNullExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testEmptyExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testNullLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, null, FlatExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testEmptyLeftExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, "", FlatExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testNullRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testEmptyRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testNullLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, null, null);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testEmptyLeftAndRightExtrapolatorName() {
    final CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, "", "");
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
  }

  @Test
  public void testNoExtrapolator() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertNull(combined.getLeftExtrapolator());
    assertNull(combined.getRightExtrapolator());
    combined = getInterpolator(NaturalCubicSplineInterpolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
  }

  @Test
  public void testOneExtrapolator() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
    combined = getInterpolator(NaturalCubicSplineInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), FlatExtrapolator1D.class);
  }

  @Test
  public void testTwoExtrapolators() {
    CombinedInterpolatorExtrapolator combined = getInterpolator(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), LinearInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
    combined = getInterpolator(NaturalCubicSplineInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    assertEquals(combined.getInterpolator().getClass(), NaturalCubicSplineInterpolator1D.class);
    assertEquals(combined.getLeftExtrapolator().getClass(), FlatExtrapolator1D.class);
    assertEquals(combined.getRightExtrapolator().getClass(), LinearExtrapolator1D.class);
  }
}
