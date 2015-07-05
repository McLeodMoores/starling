/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.generator;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link CollectionSecuritiesGenerator}.
 */
@Test(groups = TestGroup.UNIT)
public class CollectionSecuritiesGeneratorTest {
  /** An array of securities */
  private static final ManageableSecurity[] SECURITIES;

  static {
    final int n = 10;
    SECURITIES = new ManageableSecurity[n];
    for (int i = 0; i < n; i++) {
      final RawSecurity security = new RawSecurity();
      security.setName(Integer.toString(i));
      SECURITIES[i] = security;
    }
  }

  /**
   * Tests correct failure behaviour when the collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCollection() {
    new CollectionSecuritiesGenerator((List<ManageableSecurity>) null);
  }

  /**
   * Tests correct failure behaviour when the array is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    new CollectionSecuritiesGenerator<>((ManageableSecurity[]) null);
  }

  /**
   * Tests correct failure behaviour when too many securities are requested.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testTooManyRequested() {
    final CollectionSecuritiesGenerator<ManageableSecurity> generator = new CollectionSecuritiesGenerator<>(SECURITIES);
    for (int i = 0; i < SECURITIES.length + 1; i++) {
      generator.createSecurity();
    }
  }

  /**
   * Tests the generated securities.
   */
  @Test
  public void test() {
    CollectionSecuritiesGenerator<ManageableSecurity> generator = new CollectionSecuritiesGenerator<>(SECURITIES);
    for (final ManageableSecurity element : SECURITIES) {
      assertEquals(element, generator.createSecurity());
    }
    generator = new CollectionSecuritiesGenerator<>(Arrays.asList(SECURITIES));
    for (final ManageableSecurity element : SECURITIES) {
      assertEquals(element, generator.createSecurity());
    }
    final Collection<ManageableSecurity> collection = new HashSet<>();
    collection.addAll(Arrays.asList(SECURITIES));
    generator = new CollectionSecuritiesGenerator<>(collection);
    for (final ManageableSecurity security : collection) {
      assertEquals(security, generator.createSecurity());
    }
  }
}
