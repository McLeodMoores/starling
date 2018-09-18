/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link CurrencyFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyFactoryTest {

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    CurrencyFactory.INSTANCE.of(null);
  }

  /**
   * Tests the instances.
   */
  public void testFactory() {
    final Map<String, Currency> instances = CurrencyFactory.INSTANCE.instanceMap();
    for (final Map.Entry<String, Currency> entry : instances.entrySet()) {
      assertEquals(CurrencyFactory.INSTANCE.of(entry.getKey()), entry.getValue());
    }
    assertEquals(CurrencyFactory.INSTANCE.of("AAA"), Currency.of("AAA"));
  }
}
