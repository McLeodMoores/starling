/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.value;

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link MarketDataRequirementNamesHelper}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataRequirementNamesHelperTest {

  /**
   * Tests the field names.
   */
  @Test
  public void test() {
    final Set<String> names = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    assertEquals(names.size(), 33);
  }
}
