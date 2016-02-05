/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.ClampedCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.ExponentialExtrapolator1D;
import com.opengamma.analytics.math.interpolation.ExponentialInterpolator1D;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.LogLinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.LogNaturalCubicMonotonicityPreservingInterpolator1D;
import com.opengamma.analytics.math.interpolation.MonotoneConvexSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingQuinticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingQuinticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NotAKnotCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.PCHIPInterpolator1D;
import com.opengamma.analytics.math.interpolation.PCHIPYieldCurveInterpolator1D;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialInterpolator;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialInterpolator1D;
import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;
import com.opengamma.analytics.math.interpolation.QuadraticPolynomialLeftExtrapolator;
import com.opengamma.analytics.math.interpolation.QuadraticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.ShapePreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepUpperInterpolator1D;
import com.opengamma.analytics.math.interpolation.TimeSquareInterpolator1D;

/**
 * Unit tests for {@link NamedInterpolator1dFactory}.
 */
public class NamedInterpolator1dFactoryTest {

  /**
   * Tests the behaviour when an unknown interpolator is requested.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnknown() {
    NamedInterpolator1dFactory.of("Unknown");
  }

  /**
   * Tests that the factory returns the expected interpolators.
   */
  @Test
  public void test() {
    assertInterpolatorEquals("Linear", LinearInterpolator1dAdapter.class, LinearInterpolator1D.class, "Linear Interpolator", "LinearInterpolator");
    assertInterpolatorEquals("Quadratic", QuadraticInterpolator1d.class, PolynomialInterpolator1D.class, "Quadratic Interpolator", "QuadraticInterpolator");
    assertInterpolatorEquals("Cubic", CubicInterpolator1d.class, PolynomialInterpolator1D.class, "Cubic Interpolator", "CubicInterpolator");
    assertInterpolatorEquals("Quartic", QuarticInterpolator1d.class, PolynomialInterpolator1D.class, "Quartic Interpolator", "QuarticInterpolator");
    assertInterpolatorEquals("Quintic", QuinticInterpolator1d.class, PolynomialInterpolator1D.class, "Quintic Interpolator", "QuinticInterpolator");
    assertInterpolatorEquals("Exponential", ExponentialInterpolator1dAdapter.class, ExponentialInterpolator1D.class,
        "Exponential Interpolator", "ExponentialInterpolator");
    assertInterpolatorEquals("Step", StepInterpolator1dAdapter.class, StepInterpolator1D.class, "Step Interpolator", "StepInterpolator");
    assertInterpolatorEquals("Step Upper", StepUpperInterpolator1dAdapter.class, StepUpperInterpolator1D.class,
        "StepUpper", "Step Upper Interpolator", "StepUpperInterpolator");
    assertInterpolatorEquals("Log Linear", LogLinearInterpolator1dAdapter.class, LogLinearInterpolator1D.class,
        "LogLinear", "Log Linear Interpolator", "LogLinearInterpolator");
    assertInterpolatorEquals("Natural Cubic Spline", NaturalCubicSplineInterpolator1dAdapter.class, NaturalCubicSplineInterpolator1D.class,
        "NaturalCubicSpline", "Natural Cubic Spline Interpolator", "NaturalCubicSplineInterpolator");
    assertInterpolatorEquals("Double Quadratic", DoubleQuadraticInterpolator1dAdapter.class, DoubleQuadraticInterpolator1D.class,
        "DoubleQuadratic", "Double Quadratic Interpolator", "DoubleQuadraticInterpolator");
    assertInterpolatorEquals("PCHIP", PchipInterpolator1dAdapter.class, PCHIPInterpolator1D.class,
        "PCHIP Interpolator", "Piecewise Cubic Hermite Interpolating Polynomial", "Piecewise Cubic Hermite Interpolating Polynomial Interpolator",
        "MonotonicityPreservingCubicSpline");
    assertInterpolatorEquals("Modified PCHIP", ModifiedPchipInterpolator1dAdapter.class, PCHIPYieldCurveInterpolator1D.class,
        "ModifiedPCHIP", "Modified PCHIP Interpolator", "ModifiedPCHIPInterpolator", "Modified Piecewise Cubic Hermite Interpolating Polynomial",
        "Modified Piecewise Cubic Hermite Interpolating Polynomial Interpolator");
    assertInterpolatorEquals("Time Square", TimeSquareInterpolator1dAdapter.class, TimeSquareInterpolator1D.class,
        "TimeSquare", "Time Square Interpolator", "TimeSquareInterpolator");

    assertInterpolatorEquals("Clamped Cubic Spline", ClampedCubicSplineInterpolator1dAdapter.class, ClampedCubicSplineInterpolator1D.class,
        "ClampedCubicSpline", "Clamped Cubic Spline Interpolator", "ClampedCubicSplineInterpolator");

    assertInterpolatorEquals("Monotonic Natural Cubic Spline", MonotonicNaturalCubicSplineInterpolator1dAdapter.class,
        MonotonicityPreservingCubicSplineInterpolator1D.class, NaturalSplineInterpolator.class, "NaturalCubicSplineWithMonotonicity",
        "Natural Cubic Spline With Monotonicity", "MonotonicNaturalCubicSpline", "Monotonic Natural Cubic Spline Interpolator",
        "MonotonicNaturalCubicSplineInterpolator");
    assertInterpolatorEquals("Non-Negative Natural Cubic Spline", NonNegativeNaturalCubicSplineInterpolator1dAdapter.class,
        NonnegativityPreservingCubicSplineInterpolator1D.class, NaturalSplineInterpolator.class, "NaturalCubicSplineWithNonnegativity",
        "Natural Cubic Spline With Non-Negativity", "NonNegativeNaturalCubicSpline", "Non-Negative Natural Cubic Spline Interpolator",
        "NonNegativeNaturalCubicSplineInterpolator");
    assertInterpolatorEquals("Monotonic Natural Quintic Spline", MonotonicNaturalQuinticSplineInterpolator1dAdapter.class,
        MonotonicityPreservingQuinticSplineInterpolator1D.class, NaturalSplineInterpolator.class, "NaturalQuinticSplineWithMonotonicity",
        "Natural Quintic Spline With Monotonicity", "MonotonicNaturalQuinticSpline", "Monotonic Natural Quintic Spline Interpolator",
        "MonotonicNaturalQuinticSplineInterpolator");
    assertInterpolatorEquals("Non-Negative Natural Quintic Spline", NonNegativeNaturalQuinticSplineInterpolator1dAdapter.class,
        NonnegativityPreservingQuinticSplineInterpolator1D.class, NaturalSplineInterpolator.class, "NaturalQuinticSplineWithNonnegativity",
        "Natural Quintic Spline With Non-Negativity", "NonNegativeNaturalQuinticSpline", "Non-Negative Natural Quintic Spline Interpolator",
        "NonNegativeNaturalQuinticSplineInterpolator");

    assertInterpolatorEquals("Not-a-Knot Cubic Spline", NotAKnotCubicSplineInterpolator1dAdapter.class, NotAKnotCubicSplineInterpolator1D.class,
        "NotAKnotCubicSpline", "Not A Knot Cubic Spline", "Not-a-Knot Cubic Spline Interpolator", "NotAKnotCubicSplineInterpolator");

    assertInterpolatorEquals("Constrained Cubic Spline", ConstrainedCubicSplineInterpolator1dAdapter.class, ConstrainedCubicSplineInterpolator1D.class,
        "ConstrainedCubicSpline", "Constrained Cubic Spline Interpolator", "ConstrainedCubicSplineInterpolator");
    assertInterpolatorEquals("Monotonic Constrained Cubic Spline", MonotonicConstrainedCubicSplineInterpolator1dAdapter.class,
        MonotonicityPreservingCubicSplineInterpolator1D.class, ConstrainedCubicSplineInterpolator.class, "ConstrainedCubicSplineWithMonotonicity",
        "Constrained Cubic Spline With Monotonicity", "MonotonicConstrainedCubicSpline", "Monotonic Constrained Cubic Spline Interpolator",
        "MonotonicConstrainedCubicSplineInterpolator");
    assertInterpolatorEquals("Non-Negative Constrained Cubic Spline", NonNegativeConstrainedCubicSplineInterpolator1dAdapter.class,
        NonnegativityPreservingCubicSplineInterpolator1D.class, ConstrainedCubicSplineInterpolator.class, "ConstrainedCubicSplineWithNonnegativity",
        "Constrained Cubic Spline With Non-Negativity", "NonNegativeConstrainedCubicSpline", "Non-Negative Constrained Cubic Spline Interpolator",
        "NonNegativeConstrainedCubicSplineInterpolator");

    assertInterpolatorEquals("Akima Cubic Spline", AkimaCubicSplineInterpolator1dAdapter.class, SemiLocalCubicSplineInterpolator1D.class,
        "AkimaCubicSpline", "Akima Cubic Spline Interpolator", "AkimaCubicSplineInterpolator");
    assertInterpolatorEquals("Monotonic Akima Cubic Spline", MonotonicAkimaCubicSplineInterpolator1dAdapter.class,
        MonotonicityPreservingCubicSplineInterpolator1D.class, SemiLocalCubicSplineInterpolator.class, "AkimaCubicSplineWithMonotonicity",
        "Akima Cubic Spline With Monotonicity", "MonotonicAkimaCubicSpline", "Monotonic Akima Cubic Spline Interpolator",
        "MonotonicAkimaCubicSplineInterpolator");
    assertInterpolatorEquals("Non-Negative Akima Cubic Spline", NonNegativeAkimaCubicSplineInterpolator1dAdapter.class,
        NonnegativityPreservingCubicSplineInterpolator1D.class, SemiLocalCubicSplineInterpolator.class, "AkimaCubicSplineWithNonnegativity",
        "Akima Cubic Spline With Non-Negativity", "NonNegativeAkimaCubicSpline", "Non-Negative Akima Cubic Spline Interpolator",
        "NonNegativeAkimaCubicSplineInterpolator");

    assertInterpolatorEquals("Monotonic Log Natural Cubic Spline", MonotonicLogNaturalCubicSplineInterpolator1dAdapter.class,
        LogNaturalCubicMonotonicityPreservingInterpolator1D.class, "LogNaturalCubicSplineWithMonotonicity", "Log Natural Cubic Spline With Monotonicity",
        "MonotonicLogNaturalCubicSpline", "Monotonic Log Natural Cubic Spline Interpolator", "MonotonicLogNaturalCubicSplineInterpolator");

    assertInterpolatorEquals("Monotonic Convex Spline", MonotonicConvexSplineInterpolator1dAdapter.class, MonotoneConvexSplineInterpolator1D.class,
        "MonotoneConvexSpline", "Monotone Convex Spline", "MonotonicConvexSpline", "Monotonic Convex Spline Interpolator",
        "MonotonicConvexSplineInterpolator", "MonotoneConvexCubicSpline");

    assertInterpolatorEquals("C2 Shape-Preserving Cubic Spline", ShapePreservingCubicSplineInterpolator1dAdapter.class,
        ShapePreservingCubicSplineInterpolator1D.class, "C2ShapePreservingCubicSpline", "C2 Shape Preserving Cubic Spline",
        "C2 Shape-Preserving Cubic Spline Interpolator", "C2ShapePreservingCubicSplineInterpolator");

    assertInterpolatorEquals("Natural Spline", NaturalSplineInterpolator1dAdapter.class, NaturalSplineInterpolator1D.class,
        "NaturalSpline", "Natural Spline Interpolator", "NaturalSplineInterpolator");

    assertInterpolatorEquals("Quadratic Spline", QuadraticSplineInterpolator1dAdapter.class, QuadraticSplineInterpolator1D.class,
        "QuadraticSpline", "Quadratic Spline Interpolator", "QuadraticSplineInterpolator");

    assertInterpolatorEquals("Flat Extrapolator", FlatExtrapolator1dAdapter.class, FlatExtrapolator1D.class, "FlatExtrapolator");
    assertInterpolatorEquals("Exponential Extrapolator", ExponentialExtrapolator1dAdapter.class, ExponentialExtrapolator1D.class, "ExponentialExtrapolator");
    assertInterpolatorEquals("Linear Extrapolator[Linear]", LinearExtrapolator1dAdapter.class, LinearExtrapolator1D.class,
        "Linear Extrapolator[Linear]", "LinearExtrapolator[Linear]", "LinearExtrapolator[Linear Interpolator]");
    assertInterpolatorEquals("Log Linear Extrapolator[Linear]", LogLinearExtrapolator1dAdapter.class, LogLinearExtrapolator1D.class,
        "Log Linear Extrapolator[Linear]", "LogLinearExtrapolator[Linear]", "Log Linear Extrapolator[Linear Interpolator]");
    assertInterpolatorEquals("Quadratic Left Extrapolator[Linear]", QuadraticLeftExtrapolator1dAdapter.class, QuadraticPolynomialLeftExtrapolator.class,
        "Quadratic Left Extrapolator[Linear]", "QuadraticLeftExtrapolator[Linear]", "QuadraticLeftExtrapolator[Linear Interpolator]");
  }

  /**
   * Tests that the same interpolators are returned from the previous version of the factory.
   * @throws IllegalAccessException  if there is a problem accessing the fields
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testCompatibility() throws IllegalAccessException {
    final Class<Interpolator1DFactory> deprecated = Interpolator1DFactory.class;
    final Field[] fields = deprecated.getFields();
    // types that aren't handled in the original factory
    final List<String> excludedTypes = Arrays.asList(Interpolator1DFactory.BARYCENTRIC_RATIONAL_FUNCTION,
        Interpolator1DFactory.POLYNOMIAL, Interpolator1DFactory.RATIONAL_FUNCTION, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LOG_LINEAR_EXTRAPOLATOR, Interpolator1DFactory.QUADRATIC_LEFT_EXTRAPOLATOR,
        Interpolator1DFactory.ISDA_INTERPOLATOR, Interpolator1DFactory.ISDA_EXTRAPOLATOR);
    for (final Field field : fields) {
      if (field.getType().equals(String.class)) {
        final String name = (String) field.get(null);
        if (!excludedTypes.contains(name)) {
          assertEquals(Interpolator1DFactory.getInterpolator(name).getClass(),
              ((Interpolator1dAdapter) NamedInterpolator1dFactory.of(name)).getUnderlyingInterpolator().getClass());
        }
      }
    }
  }

  /**
   * Tests that the interpolator returned from the factory is the expected type.
   * @param name The interpolator name set in the adapter
   * @param aliases Aliased names
   * @param expectedType The expected type
   * @param expectedUnderlyingType The expected underlying type
   */
  private static void assertInterpolatorEquals(final String name, final Class<?> expectedType, final Class<?> expectedUnderlyingType, final String... aliases) {
    if (!Interpolator1dAdapter.class.isAssignableFrom(expectedType)) {
      throw new IllegalArgumentException("Expected type must be an Interpolator1dAdapter");
    }
    for (final String alias : aliases) {
      assertEquals(NamedInterpolator1dFactory.of(alias).getName(), name);
      assertEquals(NamedInterpolator1dFactory.of(alias).getClass(), expectedType);
      assertEquals(((Interpolator1dAdapter) NamedInterpolator1dFactory.of(alias)).getUnderlyingInterpolator().getClass(), expectedUnderlyingType);
    }
  }

  /**
   * Tests that the interpolator returned from the factory is the expected type with the expected base type.
   * @param name The interpolator name set in the adapter
   * @param aliases Aliased names
   * @param expectedType The expected type
   * @param expectedUnderlyingType The expected underlying type
   * @param expectedBaseType The expected base type
   */
  private static void assertInterpolatorEquals(final String name, final Class<?> expectedType, final Class<?> expectedUnderlyingType,
      final Class<?> expectedBaseType, final String... aliases) {
    if (!Interpolator1dAdapter.class.isAssignableFrom(expectedType)) {
      throw new IllegalArgumentException("Expected type must be an Interpolator1dAdapter");
    }
    for (final String alias : aliases) {
      assertEquals(NamedInterpolator1dFactory.of(alias).getName(), name);
      assertEquals(NamedInterpolator1dFactory.of(alias).getClass(), expectedType);
      final Interpolator1D underlyingInterpolator = ((Interpolator1dAdapter) NamedInterpolator1dFactory.of(alias)).getUnderlyingInterpolator();
      assertEquals(underlyingInterpolator.getClass(), expectedUnderlyingType);
      if (underlyingInterpolator instanceof PiecewisePolynomialInterpolator1D) {
        final PiecewisePolynomialInterpolator piecewise = ((PiecewisePolynomialInterpolator1D) underlyingInterpolator).getInterpolator();
        assertEquals(expectedBaseType, piecewise.getPrimaryMethod().getClass());
      } else {
        throw new IllegalArgumentException("This method should only be used for interpolators with a base type of PiecewisePolynomialInterpolator1D");
      }
    }
  }
}
