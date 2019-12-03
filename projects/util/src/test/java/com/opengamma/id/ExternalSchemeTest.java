/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalScheme}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalSchemeTest {

  /**
   * Tests the constructor.
   */
  @Test
  public void testFactory() {
    final ExternalScheme test = ExternalScheme.of("IATA");
    assertEquals("IATA", test.getName());
  }

  /**
   * Tests that schemes are cached.
   */
  @Test
  public void testFactoryCached() {
    assertSame(ExternalScheme.of("ISO"), ExternalScheme.of("ISO"));
  }

  /**
   * Tests that a null scheme is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    ExternalScheme.of(null);
  }

  /**
   * Tests that the scheme name cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryEmptyName() {
    ExternalScheme.of("");
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareTo() {
    final ExternalScheme d1 = ExternalScheme.of("d1");
    final ExternalScheme d2 = ExternalScheme.of("d2");

    assertTrue(d1.compareTo(d1) == 0);
    assertTrue(d1.compareTo(d2) < 0);

    assertTrue(d2.compareTo(d1) > 0);
    assertTrue(d2.compareTo(d2) == 0);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final ExternalScheme d1a = ExternalScheme.of("d1");
    final ExternalScheme d1b = ExternalScheme.of("d1");
    final ExternalScheme d2 = ExternalScheme.of("d2");

    assertEquals(d1a.equals(d1a), true);
    assertEquals(d1a, d1b);
    assertNotEquals(d1a, d2);

    assertEquals(d1b, d1a);
    assertEquals(d1b.equals(d1b), true);
    assertNotEquals(d1b, d2);

    assertNotEquals(d2, d1a);
    assertNotEquals(d2, d1b);
    assertEquals(d2.equals(d2), true);

    assertNotEquals("d1b", d1b);
    assertNotEquals(null, d1b);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalScheme d1a = ExternalScheme.of("d1");
    final ExternalScheme d1b = ExternalScheme.of("d1");

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final ExternalScheme test = ExternalScheme.of("Scheme");
    assertEquals("Scheme", test.toString());
  }

}
