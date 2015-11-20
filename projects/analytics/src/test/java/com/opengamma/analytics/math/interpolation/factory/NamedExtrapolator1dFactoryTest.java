/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.ExponentialExtrapolator1D;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;

/**
 * Unit tests for {@link NamedExtrapolator1dFactory}.
 */
public class NamedExtrapolator1dFactoryTest {

  /**
   * Tests the behaviour when an unknown extrapolator is requested.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnknown() {
    NamedExtrapolator1dFactory.of("Unknown");
  }

  /**
   * Tests that the factory returns the expected extrapolators.
   */
  @Test
  public void test() {
    assertExtrapolatorEquals("Flat", FlatExtrapolator1dAdapter.class, FlatExtrapolator1D.class,
        "FlatExtrapolator", "Flat Extrapolator", "Flat");
    assertExtrapolatorEquals("Exponential", ExponentialExtrapolator1dAdapter.class, ExponentialExtrapolator1D.class,
        "ExponentialExtrapolator", "Exponential Extrapolator", "Exponential");
  }

  /**
   * Tests that the extrapolator returned from the factory is the expected type.
   * @param name The extrapolator name set in the adapter
   * @param aliases Aliased names
   * @param expectedType The expected type
   * @param expectedUnderlyingType The expected underlying type
   */
  private static void assertExtrapolatorEquals(final String name, final Class<?> expectedType, final Class<?> expectedUnderlyingType, final String... aliases) {
    if (!Extrapolator1dAdapter.class.isAssignableFrom(expectedType)) {
      throw new IllegalArgumentException("Expected type must be an Extrapolator1dAdapter");
    }
    for (final String alias : aliases) {
      assertEquals(NamedExtrapolator1dFactory.of(alias).getName(), name);
      assertEquals(NamedExtrapolator1dFactory.of(alias).getClass(), expectedType);
      assertEquals(((Extrapolator1dAdapter) NamedExtrapolator1dFactory.of(alias)).getUnderlyingExtrapolator().getClass(), expectedUnderlyingType);
    }
  }
}
