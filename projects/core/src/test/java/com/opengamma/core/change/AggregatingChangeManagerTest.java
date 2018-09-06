/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.change;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link AggregatingChangeManager}.
 */
@Test(groups = TestGroup.UNIT)
public class AggregatingChangeManagerTest {
  private static final BasicChangeManager BCM1 = new BasicChangeManager();
  private static final BasicChangeManager BCM2 = new BasicChangeManager();
  private static final BasicChangeManager BCM3 = new BasicChangeManager();
  private static final BasicChangeManager BCM4 = new BasicChangeManager();

  /**
   * Tests that the change providers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeProviders() {
    new AggregatingChangeManager(null);
  }

  /**
   *
   */
}
