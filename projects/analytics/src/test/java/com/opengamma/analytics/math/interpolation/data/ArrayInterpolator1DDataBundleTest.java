/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ArrayInterpolator1DDataBundleTest extends Interpolator1DDataBundleTestCase {

  @Override
  protected Interpolator1DDataBundle createDataBundle(final double[] keys, final double[] values) {
    return new ArrayInterpolator1DDataBundle(keys, values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullKeys() {
    new ArrayInterpolator1DDataBundle(null, new double[] { 1., 2. });
  }

  @Test
  public void particularSort() {
    final double[] keys = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1 };
    final double[] values = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1 };

    final Interpolator1DDataBundle model = new ArrayInterpolator1DDataBundle(keys, values);
    final double[] resultKeys = model.getKeys();
    assertEquals(0.0, resultKeys[0], 1e-10);
  }

  @Test
  public void brokenSort_ANA_102() {
    final double[] keys = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1 };
    final double[] values = new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1 };
    final Interpolator1DDataBundle model = new ArrayInterpolator1DDataBundle(keys, values);
    // If the array isn't sorted properly, the binary search doesn't find the keys
    for (final double key : keys) {
      assertTrue(model.containsKey(key));
    }
  }

}
