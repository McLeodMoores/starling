/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.testutils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface.QuotingConvention;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for testing the serialization of Finmath objects.
 */
public final class FinmathSerializationTestUtils {
  /** The accuracy */
  private static final double EPS = 1e-15;

  /**
   * Restricted constructor.
   */
  private FinmathSerializationTestUtils() {
  }

  /**
   * Tests the equality of two curves by sampling.
   * @param curveInterface1 The first curve
   * @param curveInterface2 The second curve
   */
  public static void assertCurveEquals(final CurveInterface curveInterface1, final CurveInterface curveInterface2) {
    if (curveInterface1 instanceof Curve) {
      assertTrue(curveInterface2 instanceof Curve);
      final Curve curve1 = (Curve) curveInterface1;
      final Curve curve2 = (Curve) curveInterface2;
      assertEquals(curve1.getExtrapolationMethod(), curve2.getExtrapolationMethod());
      assertEquals(curve1.getInterpolationEntity(), curve2.getInterpolationEntity());
      assertEquals(curve1.getInterpolationMethod(), curve2.getInterpolationMethod());
    }
    if (curveInterface1 == null) {
      assertNull(curveInterface2);
      return;
    }
    assertEquals(curveInterface1.getName(), curveInterface2.getName());
    if (curveInterface1.getReferenceDate() != null) {
      if (curveInterface2.getReferenceDate() != null) {
        assertEquals(curveInterface1.getReferenceDate(), curveInterface2.getReferenceDate());
      } else {
        assertNull(curveInterface2.getReferenceDate());
      }
    }
    for (int i = 0; i < 100; i++) {
      assertEquals(curveInterface1.getValue(i), curveInterface2.getValue(i), EPS, "(" + i + ")");
    }
  }

  /**
   * Tests the equality of two curves by sampling. An analytic model is supplied that should provide curves
   * that are not stored internally in the curve objects.
   * @param curveInterface1 The first curve
   * @param curveInterface2 The second curve
   * @param model The analytic model used to get the value
   */
  public static void assertCurveEquals(final CurveInterface curveInterface1, final CurveInterface curveInterface2,
      final AnalyticModelInterface model) {
    if (curveInterface1 instanceof Curve) {
      assertTrue(curveInterface2 instanceof Curve);
      final Curve curve1 = (Curve) curveInterface1;
      final Curve curve2 = (Curve) curveInterface2;
      assertEquals(curve1.getExtrapolationMethod(), curve2.getExtrapolationMethod());
      assertEquals(curve1.getInterpolationEntity(), curve2.getInterpolationEntity());
      assertEquals(curve1.getInterpolationMethod(), curve2.getInterpolationMethod());
    }
    if (curveInterface1 == null) {
      assertNull(curveInterface2);
      return;
    }
    assertEquals(curveInterface1.getName(), curveInterface2.getName());
    if (curveInterface1.getReferenceDate() != null) {
      if (curveInterface2.getReferenceDate() != null) {
        assertEquals(curveInterface1.getReferenceDate(), curveInterface2.getReferenceDate());
      } else {
        assertNull(curveInterface2.getReferenceDate());
      }
    }
    for (int i = 0; i < 100; i++) {
      assertEquals(curveInterface1.getValue(model, i), curveInterface2.getValue(model, i), EPS, "(" + i + ")");
    }
  }

  /**
   * Tests the equality of two surfaces by sampling. An analytic model is supplied that should provide curves
   * that are not stored internally in the curve objects.
   * @param surfaceInterface1 The first surface
   * @param surfaceInterface2 The second surface
   * @param model The analytic model used to get the value
   */
  public static void assertSurfaceEquals(final VolatilitySurfaceInterface surfaceInterface1, final VolatilitySurfaceInterface surfaceInterface2,
      final AnalyticModelInterface model) {
    if (surfaceInterface1 == null) {
      assertNull(surfaceInterface2);
      return;
    }
    assertEquals(surfaceInterface1.getName(), surfaceInterface2.getName());
    assertEquals(surfaceInterface1.getQuotingConvention(), surfaceInterface2.getQuotingConvention());
    if (surfaceInterface1.getReferenceDate() != null) {
      if (surfaceInterface2.getReferenceDate() != null) {
        assertEquals(surfaceInterface1.getReferenceDate(), surfaceInterface2.getReferenceDate());
      } else {
        assertNull(surfaceInterface2.getReferenceDate());
      }
    }
    for (double i = 0; i < 10; i += 0.1) {
      for (double j = 0; j < 10; j += 0.1) {
        assertEquals(surfaceInterface1.getValue(model, i, j, QuotingConvention.VOLATILITYNORMAL),
            surfaceInterface2.getValue(model, i, j, QuotingConvention.VOLATILITYNORMAL), EPS, "(" + i + ", " + j + ")");
        assertEquals(surfaceInterface1.getValue(model, i, j, QuotingConvention.VOLATILITYLOGNORMAL),
            surfaceInterface2.getValue(model, i, j, QuotingConvention.VOLATILITYLOGNORMAL), EPS, "(" + i + ", " + j + ")");
        assertEquals(surfaceInterface1.getValue(model, i, j, QuotingConvention.PRICE),
            surfaceInterface2.getValue(model, i, j, QuotingConvention.PRICE), EPS, "(" + i + ", " + j + ")");
      }
    }
  }

  /**
   * Tests the equality of two surfaces by sampling. An analytic model is supplied that should provide curves
   * that are not stored internally in the curve objects. The quoting convention is supplied because some classes do no handle all quoting
   * convention types correctly.
   * @param surfaceInterface1 The first surface
   * @param surfaceInterface2 The second surface
   * @param model The analytic model used to get the value
   * @param quotingConventionToTest The quoting convention to test, not null
   */
  public static void assertSurfaceEquals(final VolatilitySurfaceInterface surfaceInterface1, final VolatilitySurfaceInterface surfaceInterface2,
      final AnalyticModelInterface model, final QuotingConvention quotingConventionToTest) {
    ArgumentChecker.notNull(quotingConventionToTest, "quotingConventionToTest");
    if (surfaceInterface1 == null) {
      assertNull(surfaceInterface2);
      return;
    }
    assertEquals(surfaceInterface1.getName(), surfaceInterface2.getName());
    assertEquals(surfaceInterface1.getQuotingConvention(), surfaceInterface2.getQuotingConvention());
    if (surfaceInterface1.getReferenceDate() != null) {
      if (surfaceInterface2.getReferenceDate() != null) {
        assertEquals(surfaceInterface1.getReferenceDate(), surfaceInterface2.getReferenceDate());
      } else {
        assertNull(surfaceInterface2.getReferenceDate());
      }
    }
    for (double i = 0; i < 10; i += 0.1) {
      for (double j = 0; j < 10; j += 0.1) {
        assertEquals(surfaceInterface1.getValue(model, i, j, quotingConventionToTest),
            surfaceInterface2.getValue(model, i, j, quotingConventionToTest), EPS, "(" + i + ", " + j + ")");
      }
    }
  }

  /**
   * Tests the equality of two surfaces by sampling.
   * @param surfaceInterface1 The first surface
   * @param surfaceInterface2 The second surface
   */
  public static void assertSurfaceEquals(final VolatilitySurfaceInterface surfaceInterface1, final VolatilitySurfaceInterface surfaceInterface2) {
    if (surfaceInterface1 == null) {
      assertNull(surfaceInterface2);
      return;
    }
    assertEquals(surfaceInterface1.getName(), surfaceInterface2.getName());
    assertEquals(surfaceInterface1.getQuotingConvention(), surfaceInterface2.getQuotingConvention());
    if (surfaceInterface1.getReferenceDate() != null) {
      if (surfaceInterface2.getReferenceDate() != null) {
        assertEquals(surfaceInterface1.getReferenceDate(), surfaceInterface2.getReferenceDate());
      } else {
        assertNull(surfaceInterface2.getReferenceDate());
      }
    }
    for (double i = 0; i < 10; i += 0.1) {
      for (double j = 0; j < 10; j += 0.1) {
        assertEquals(surfaceInterface1.getValue(i, j, QuotingConvention.VOLATILITYNORMAL),
            surfaceInterface2.getValue(i, j, QuotingConvention.VOLATILITYNORMAL), EPS, "(" + i + ", " + j + ")");
        assertEquals(surfaceInterface1.getValue(i, j, QuotingConvention.VOLATILITYLOGNORMAL),
            surfaceInterface2.getValue(i, j, QuotingConvention.VOLATILITYLOGNORMAL), EPS, "(" + i + ", " + j + ")");
        assertEquals(surfaceInterface1.getValue(i, j, QuotingConvention.PRICE),
            surfaceInterface2.getValue(i, j, QuotingConvention.PRICE), EPS, "(" + i + ", " + j + ")");
      }
    }
  }

  /**
   * Tests the equality of two surfaces by sampling. The quoting convention is supplied because some classes do no handle all quoting
   * convention types correctly.
   * @param surfaceInterface1 The first surface
   * @param surfaceInterface2 The second surface
   * @param quotingConventionToTest The quoting convention to test, not null
   */
  public static void assertSurfaceEquals(final VolatilitySurfaceInterface surfaceInterface1, final VolatilitySurfaceInterface surfaceInterface2,
      final QuotingConvention quotingConventionToTest) {
    ArgumentChecker.notNull(quotingConventionToTest, "quotingConventionToTest");
    if (surfaceInterface1 == null) {
      assertNull(surfaceInterface2);
      return;
    }
    assertEquals(surfaceInterface1.getName(), surfaceInterface2.getName());
    assertEquals(surfaceInterface1.getQuotingConvention(), surfaceInterface2.getQuotingConvention());
    if (surfaceInterface1.getReferenceDate() != null) {
      if (surfaceInterface2.getReferenceDate() != null) {
        assertEquals(surfaceInterface1.getReferenceDate(), surfaceInterface2.getReferenceDate());
      } else {
        assertNull(surfaceInterface2.getReferenceDate());
      }
    }
    for (double i = 0; i < 10; i += 0.1) {
      for (double j = 0; j < 10; j += 0.1) {
        assertEquals(surfaceInterface1.getValue(i, j, quotingConventionToTest),
            surfaceInterface2.getValue(i, j, quotingConventionToTest), EPS, "(" + i + ", " + j + ")");
      }
    }
  }

}
