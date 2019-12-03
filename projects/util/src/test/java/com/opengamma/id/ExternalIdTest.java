/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalId}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdTest {
  private static final ExternalScheme SCHEME = ExternalScheme.of("Scheme");
  private static final ExternalScheme OTHER_SCHEME = ExternalScheme.of("Other");

  //-------------------------------------------------------------------------
  /**
   * Tests the static constructor.
   */
  public void testFactoryExternalSchemeString() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests the behaviour when the scheme is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringNullScheme() {
    ExternalId.of((ExternalScheme) null, "value");
  }

  /**
   * Tests the behaviour when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringNullValue() {
    ExternalId.of(SCHEME, (String) null);
  }

  /**
   * Tests the behaviour when the value is an empty string.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringEmptyValue() {
    ExternalId.of(SCHEME, "");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void testStringEscaping() {
//    final String[] strs = new String[] {"Foo", "~Foo", "Foo~", "~Foo~", "~", "~~", "~~~" };
//    for (final String scheme : strs) {
//      for (final String value : strs) {
//        final ExternalId eid1 = ExternalId.of(scheme, value);
//        final String testStr = eid1.toString();
//        System.out.println("scheme = " + scheme + ", value = " + value + ", eid = " + eid1.toString());
//        final ExternalId eid2 = ExternalId.parse(testStr);
//        assertEquals(eid1, eid2);
//      }
//    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the static constructor.
   */
  @Test
  public void testFactoryStringString() {
    final ExternalId test = ExternalId.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests the behaviour when the scheme string is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullScheme() {
    ExternalId.of((String) null, "value");
  }

  /**
   * Tests the behaviour when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullValue() {
    ExternalId.of("Scheme", (String) null);
  }

  /**
   * Tests the behaviour when the value is an empty string.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyValue() {
    ExternalId.of("Scheme", "");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the parser.
   */
  @Test
  public void testParse() {
    final ExternalId test = ExternalId.parse("Scheme~value");
    assertEquals(SCHEME, test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests  the behaviour when the string to parse is in the wrong format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat() {
    ExternalId.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the isScheme() method.
   */
  @Test
  public void testIsSchemeExternalScheme() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertTrue(test.isScheme(SCHEME));
    assertFalse(test.isScheme(OTHER_SCHEME));
    assertFalse(test.isScheme((ExternalScheme) null));
  }

  /**
   * Tests the isNotScheme() method.
   */
  @Test
  public void testIsNotSchemeExternalScheme() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertEquals(false, test.isNotScheme(SCHEME));
    assertEquals(true, test.isNotScheme(OTHER_SCHEME));
    assertEquals(true, test.isNotScheme((ExternalScheme) null));
  }

  /**
   * Tests the isScheme() method.
   */
  @Test
  public void testIsSchemeString() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertEquals(true, test.isScheme("Scheme"));
    assertEquals(false, test.isScheme("Other"));
    assertEquals(false, test.isScheme((String) null));
  }

  /**
   * Tests the isNotScheme() method.
   */
  @Test
  public void testIsNotSchemeString() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertEquals(false, test.isNotScheme("Scheme"));
    assertEquals(true, test.isNotScheme("Other"));
    assertEquals(true, test.isNotScheme((String) null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the getExternalId() method returns the instance.
   */
  @Test
  public void testGetIdentityKey() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertSame(test, test.getExternalId());
    assertEquals(test, test.getExternalId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the method that converts to an id bundle.
   */
  @Test
  public void testToBundle() {
    final ExternalId test = ExternalId.of(SCHEME, "value");
    assertEquals(ExternalIdBundle.of(test), test.toBundle());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final ExternalId d1a = ExternalId.of(SCHEME, "d1");
    final ExternalId d1b = ExternalId.of(SCHEME, "d1");
    final ExternalId d2a = ExternalId.of(SCHEME, "d2");
    final ExternalId d3a = ExternalId.of("Scheme1", "d1");

    assertEquals(d1a, d1a);
    assertEquals(d1a, d1b);

    assertNotEquals(d1a, d2a);
    assertNotEquals(d1a, d3a);
    assertNotEquals(null, d1a);
    assertNotEquals("Scheme~d1", d1a);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalId d1a = ExternalId.of(SCHEME, "d1");
    final ExternalId d1b = ExternalId.of(SCHEME, "d1");

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareTo() {
    final ExternalId eid = ExternalId.of("s1", "v1");

    assertEquals(eid.compareTo(eid), 0);
    assertTrue(eid.compareTo(ExternalId.of("s2", "v1")) < 0);
    assertTrue(eid.compareTo(ExternalId.of("s0", "v1")) > 0);
    assertTrue(eid.compareTo(ExternalId.of("s1", "v0")) > 0);
    assertTrue(eid.compareTo(ExternalId.of("s1", "v2")) < 0);
  }
}
