/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ObjectId}.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectIdTest {

  /**
   * Tests the creation of an object id.
   */
  @Test
  public void testFactoryStringString() {
    final ObjectId test = ObjectId.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullScheme() {
    ObjectId.of((String) null, "value");
  }

  /**
   * Tests that the scheme cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyScheme() {
    ObjectId.of("", "value");
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullValue() {
    ObjectId.of("Scheme", (String) null);
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyValue() {
    ObjectId.of("Scheme", "");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void testStringEscaping() {
//    final String[] strs = new String[] {"Foo", "~Foo", "Foo~", "~Foo~", "~", "~~", "~~~" };
//    for (final String scheme : strs) {
//      for (final String value : strs) {
//        final ObjectId testOID = ObjectId.of(scheme, value);
//        final String testStr = testOID.toString();
//        // System.out.println("scheme = " + scheme + ", value = " + value + ", oid = " + testOID.toString());
//        final ObjectId oid = ObjectId.parse(testStr);
//        assertEquals(testOID, oid);
//      }
//    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests parsing.
   */
  @Test
  public void testParse() {
    final ObjectId test = ObjectId.parse("Scheme~value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests parsing an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat1() {
    ObjectId.parse("Scheme");
  }

  /**
   * Tests parsing an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat2() {
    ObjectId.parse("Scheme:value");
  }

  /**
   * Tests parsing an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat3() {
    ObjectId.parse("Scheme~value~other");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the scheme replacement.
   */
  @Test
  public void testWithScheme() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(ObjectId.of("newScheme", "value1"), test.withScheme("newScheme"));
    assertNotSame(test, test.withValue("value1"));
  }

  /**
   * Tests that the scheme cannot be replaced with an empty string.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithSchemeEmpty() {
    final ObjectId test = ObjectId.of("id1", "value1");
    test.withScheme("");
  }

  /**
   * Tests that the scheme cannot be replaced with null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithSchemeNull() {
    final ObjectId test = ObjectId.of("id1", "value1");
    test.withScheme(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the replacement of a value.
   */
  @Test
  public void testWithValue() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(ObjectId.of("id1", "newValue"), test.withValue("newValue"));
    assertNotSame(test, test.withValue("value1"));
  }

  /**
   * Tests that the value cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithValueEmpty() {
    final ObjectId test = ObjectId.of("id1", "value1");
    test.withValue("");
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithValueNull() {
    final ObjectId test = ObjectId.of("id1", "value1");
    test.withValue(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the conversion into a unique id.
   */
  @Test
  public void testAtLatestVersion() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", null), test.atLatestVersion());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the conversion into a unique id.
   */
  @Test
  public void testAtVersion() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.atVersion("32"));
  }

  /**
   * Tests the conversion into a unique id with no version.
   */
  @Test
  public void testAtVersionNull() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", null), test.atVersion(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that getObjectId() method returns the same object.
   */
  @Test
  public void testGetObjectId() {
    final ObjectId test = ObjectId.of("id1", "value1");
    assertSame(test, test.getObjectId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareTo() {
    final ObjectId a = ObjectId.of("A", "1");
    final ObjectId b = ObjectId.of("A", "2");
    final ObjectId c = ObjectId.of("B", "2");

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, a.compareTo(c) < 0);

    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
    assertEquals(true, b.compareTo(c) < 0);

    assertEquals(true, c.compareTo(a) > 0);
    assertEquals(true, c.compareTo(b) > 0);
    assertEquals(true, c.compareTo(c) == 0);
  }

  /**
   * Tests that null is not a valid input.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCompareToNull() {
    final ObjectId test = ObjectId.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final ObjectId d1a = ObjectId.of("Scheme", "d1");
    final ObjectId d1b = ObjectId.of("Scheme", "d1");
    final ObjectId d2 = ObjectId.of("Scheme", "d2");
    final ObjectId d3 = ObjectId.of("Scheme1", "d1");

    assertEquals(true, d1a.equals(d1a));
    assertEquals(true, d1a.equals(d1b));
    assertEquals(false, d1a.equals(d2));
    assertEquals(false, d1a.equals(d3));

    assertEquals(true, d1b.equals(d1a));
    assertEquals(true, d1b.equals(d1b));
    assertEquals(false, d1b.equals(d2));
    assertEquals(false, d1b.equals(d3));

    assertEquals(false, d2.equals(d1a));
    assertEquals(false, d2.equals(d1b));
    assertEquals(true, d2.equals(d2));
    assertEquals(false, d2.equals(d3));

    assertNotEquals("d1b", d1b);
    assertNotEquals(null, d1b);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ObjectId d1a = ObjectId.of("Scheme", "d1");
    final ObjectId d1b = ObjectId.of("Scheme", "d1");

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
