/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link BuilderUtils}.
 */
public class BuilderUtilsTest {

  /**
   * Tests the behaviour when the value is null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIsNotSet() {
    BuilderUtils.isSet(null, "name");
  }

  /**
   * Tests the behaviour when the value is not null.
   */
  @Test
  public void testIsSet() {
    assertEquals(BuilderUtils.isSet("W", "name"), "W");
  }

  /**
   * Tests that the collection cannot be null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullCollection() {
    BuilderUtils.notEmpty((Collection<?>) null, "name");
  }

  /**
   * Tests the behaviour when the collection is empty.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEmptyCollection() {
    BuilderUtils.notEmpty(new ArrayList<Double>(), "name");
  }

  /**
   * Tests the behaviour when the collection is not empty.
   */
  @Test
  public void testCollection() {
    assertEquals(BuilderUtils.notEmpty(Arrays.asList(1, 2, 3), "name"), Arrays.asList(1, 2, 3));
  }

  /**
   * Tests that the array cannot be null.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNullArray() {
    BuilderUtils.notEmpty((Object[]) null, "name");
  }

  /**
   * Tests the behaviour when the array is empty.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEmptyArray() {
    BuilderUtils.notEmpty(new Double[0], "name");
  }

  /**
   * Tests the behaviour when the array is not empty.
   */
  @Test
  public void testArray() {
    assertEquals(BuilderUtils.notEmpty(new Double[] {1., 2., 3.}, "name"), new Double[] {1., 2., 3.});
  }
}
