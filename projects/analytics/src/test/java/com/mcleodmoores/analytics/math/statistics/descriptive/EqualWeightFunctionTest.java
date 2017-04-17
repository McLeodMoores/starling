/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link EqualWeightFunction}.
 */
@Test(groups = TestGroup.UNIT)
public class EqualWeightFunctionTest {

  /**
   * Tests the weights.
   */
  @Test
  public void testWeights() {
    final double n = 0.01;
    EqualWeightFunction f = EqualWeightFunction.of(n);
    double sum = 0;
    for (int j = 0; j < 100; j++) {
      final Double x = f.get();
      assertEquals(x, n, 1e-15);
      sum += x;
    }
    assertEquals(sum, 1, 1e-15);
    f = EqualWeightFunction.ofInverse(1. / n);
    sum = 0;
    for (int j = 0; j < 100; j++) {
      final Double x = f.get();
      assertEquals(x, n, 1e-15);
      sum += x;
    }
    assertEquals(sum, 1, 1e-15);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final EqualWeightFunction f1 = EqualWeightFunction.of(1000);
    EqualWeightFunction f2 = EqualWeightFunction.of(1000);
    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
    f2 = EqualWeightFunction.of(1500);
    assertNotEquals(f1, f2);
    assertEquals(f1.toString(), "EqualWeightFunction[_weight=0.001]");
  }
}
