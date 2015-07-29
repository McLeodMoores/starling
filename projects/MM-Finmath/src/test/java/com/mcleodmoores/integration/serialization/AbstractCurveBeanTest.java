/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import net.finmath.marketdata.model.curves.CurveInterface;

/**
 * Base class for unit tests of classes that extend {@link AbstractCurveBean}.
 */
public abstract class AbstractCurveBeanTest {
  /** Equality tolerance for doubles */
  private static final double EPS = 1e-15;

  /**
   * Tests the reference date logic.
   */
  public abstract void testGetReferenceDate();

  /**
   * Tests that two curves are equal. This method uses sampling, as the curves in the Finmath
   * library do not implement an equals() method.
   * @param curve1 The first curve, not null
   * @param curve2 The second curve, not null
   */
  protected static void assertEqualCurves(final CurveInterface curve1, final CurveInterface curve2) {
    assertNotNull(curve1);
    assertNotNull(curve2);
    assertEquals(curve1.getClass().getName(), curve2.getClass().getName());
    final int startTime = -1;
    final int endTime = 101;
    for (double t = startTime; t <= endTime; t += 0.0005) { // ~ quarter day accuracy
      assertEquals(curve1.getValue(t), curve2.getValue(t), EPS);
    }
  }

}
