/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ExponentialWeightFunction}.
 */
@Test(groups = TestGroup.UNIT)
public class ExponentialWeightFunctionTest {

  /**
   * Tests that the weight must be greater than zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowWeight() {
    ExponentialWeightFunction.of(0);
  }

  /**
   * Tests that the weight must be less than or equal to one.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighWeight() {
    ExponentialWeightFunction.of(1.1);
  }

  /**
   * Tests the sum of the weights when the weight is 1.
   */
  @Test
  public void testWeightEqualsOne() {
    final ExponentialWeightFunction f = ExponentialWeightFunction.of(1);
    for (int i = 0; i < 2000; i++) {
      final double weight = f.get();
      if (i == 0) {
        assertEquals(weight, 1, 1e-15);
      } else {
        assertEquals(weight, 0, 1e-15);
      }
    }
  }

  /**
   * Tests that the sum of the weights tends to one.
   */
  @Test
  public void testSumOfWeightsEqualsOne() {
    for (int i = 0; i < 20; i++) {
      final double weight = (Math.random() + 0.5) / 2;
      final ExponentialWeightFunction f = ExponentialWeightFunction.of(weight);
      double sum = 0;
      for (int j = 0; j < 2000; j++) {
        final Double x = f.get();
        sum += x;
      }
      assertEquals(sum, 1, 1e-5);
    }
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ExponentialWeightFunction f1 = ExponentialWeightFunction.of(0.06);
    ExponentialWeightFunction f2 = ExponentialWeightFunction.of(0.06);
    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
    f2 = ExponentialWeightFunction.of(0.05);
    assertNotEquals(f1, f2);
    assertEquals(f1.toString(), "ExponentialWeightFunction[_alpha=0.06]");
  }
}
