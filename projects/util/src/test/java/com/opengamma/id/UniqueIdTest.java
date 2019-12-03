/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UniqueId}.
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdTest {

  /**
   * Tests construction from strings.
   */
  @Test
  public void testFactoryStringString() {
    final UniqueId test = UniqueId.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullScheme() {
    UniqueId.of((String) null, "value");
  }

  /**
   * Tests that the scheme cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyScheme() {
    UniqueId.of("", "value");
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullValue() {
    UniqueId.of("Scheme", (String) null);
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyValue() {
    UniqueId.of("Scheme", "");
  }

  /**
   *
   */
  @Test(enabled = false)
  public void testStringEscaping() {
//    final String[] strs = new String[] {"Foo", "~Foo", "Foo~", "~Foo~", "~", "~~", "~~~" };
//    for (final String scheme : strs) {
//      for (final String value : strs) {
//        UniqueId testUID = UniqueId.of(scheme, value);
//        String testStr = testUID.toString();
//        // System.out.println("scheme = " + scheme + ", value = " + value + ", version = NULL, uid = " + testUID.toString());
//        UniqueId uid = UniqueId.parse(testStr);
//        assertEquals(testUID, uid);
//        for (final String version : strs) {
//          testUID = UniqueId.of(scheme, value, version);
//          testStr = testUID.toString();
//          // System.out.println("scheme = " + scheme + ", value = " + value + ", version = " + version + ", uid = " + testUID.toString());
//          uid = UniqueId.parse(testStr);
//          assertEquals(testUID, uid);
//        }
//      }
//    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from strings.
   */
  @Test
  public void testFactoryStringStringString() {
    final UniqueId test = UniqueId.of("Scheme", "value", "version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringStringNullScheme() {
    UniqueId.of((String) null, "value", "version");
  }

  /**
   * Tests that the scheme cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringStringEmptyScheme() {
    UniqueId.of("", "value", "version");
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringStringNullValue() {
    UniqueId.of("Scheme", (String) null, "version");
  }

  /**
   * Tests that the value cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringStringEmptyValue() {
    UniqueId.of("Scheme", "", "version");
  }

  /**
   * Tests a null version.
   */
  @Test
  public void testFactoryStringStringStringNullVersion() {
    final UniqueId test = UniqueId.of("Scheme", "value", null);
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests an empty version.
   */
  public void testFactoryStringStringStringEmptyVersion() {
    final UniqueId test = UniqueId.of("Scheme", "value", "");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from an object id.
   */
  @Test
  public void testFactoryObjectIdString() {
    final UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), "version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryObjectIdStringNullObjectId() {
    UniqueId.of((ObjectId) null, "version");
  }

  /**
   * Tests a null version.
   */
  @Test
  public void testFactoryObjectIdStringNullVersion() {
    final UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), null);
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests an empty version.
   */
  @Test
  public void testFactoryObjectIdStringEmptyVersion() {
    final UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), "");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from an external id.
   */
  @Test
  public void testFactoryExternalIdString() {
    final UniqueId test = UniqueId.of(ExternalId.of("UID", "Scheme~value"));
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalIdStringNullObjectId() {
    UniqueId.of((ExternalId) null);
  }

  /**
   * Tests that the scheme must be UID.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testScheme() {
    UniqueId.of(ExternalId.of("Scheme", "value"));
  }
  //-------------------------------------------------------------------------
  /**
   * Tests the parser.
   */
  @Test
  public void testParseVersion() {
    final UniqueId test = UniqueId.parse("Scheme~value~version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  /**
   * Tests the parser.
   */
  @Test
  public void testParseNoVersion() {
    final UniqueId test = UniqueId.parse("Scheme~value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  /**
   * Tests an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat1() {
    UniqueId.parse("Scheme");
  }

  /**
   * Tests an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat2() {
    UniqueId.parse("Scheme:value");
  }

  /**
   * Tests an invalid format.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat3() {
    UniqueId.parse("Scheme~value~version~other");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests scheme replacement.
   */
  @Test
  public void testWithScheme() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("scheme", "value1", "32"), test.withScheme("scheme"));
    assertNotSame(test, test.withScheme("id1"));
  }

  /**
   * Tests that the scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithSchemeNull() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    test.withScheme(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests value replacement.
   */
  @Test
  public void testWithValue() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("id1", "newValue", "32"), test.withValue("newValue"));
  }

  /**
   * Tests that the value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithValueNull() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    test.withValue(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding a version.
   */
  @Test
  public void testWithVersionAdded() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.withVersion("32"));
  }

  /**
   * Tests version replacement.
   */
  @Test
  public void testWithVersionReplaced() {
    final UniqueId test = UniqueId.of("id1", "value1", "12");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.withVersion("32"));
  }

  /**
   * Tests the version can be replaced with null.
   */
  @Test
  public void testWithVersionReplacedToNull() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("id1", "value1"), test.withVersion(null));
  }

  /**
   * Tests that the same object is returned if the version being replaced is the same.
   */
  @Test
  public void testWithVersionReplacedToSame() {
    final UniqueId test = UniqueId.of("id1", "value1", "32");
    assertSame(test, test.withVersion("32"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the getObjectId() method.
   */
  @Test
  public void testGetObjectId() {
    final UniqueId test = UniqueId.of("id1", "value1", "version");
    assertEquals(ObjectId.of("id1", "value1"), test.getObjectId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the getUniqueId() method.
   */
  @Test
  public void testGetUniqueId() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertSame(test, test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an id with no version is the latest version.
   */
  @Test
  public void testIsLatestNoVersion() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(true, test.isLatest());
  }

  /**
   * Tests that an id with a version is not the latest version.
   */
  @Test
  public void testIsLatestVersion() {
    final UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(false, test.isLatest());
  }

  /**
   * Tests the isVersioned() method.
   */
  @Test
  public void testIsVersionedNoVersion() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(false, test.isVersioned());
  }

  /**
   * Tests the isVersioned() method.
   */
  @Test
  public void testIsVersionedVersion() {
    final UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(true, test.isVersioned());
  }

  /**
   * Tests conversion to the latest version.
   */
  @Test
  public void testToLatestNoVersion() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1"), test.toLatest());
  }

  /**
   * Tests conversion to the latest version.
   */
  @Test
  public void testToLatestVersion() {
    final UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(UniqueId.of("id1", "value1"), test.toLatest());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to an external id.
   */
  @Test
  public void testToExternalId() {
    final UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(ExternalId.of(UniqueId.EXTERNAL_SCHEME, "id1~value1"), test.toExternalId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equivalent object id.
   */
  @Test
  public void testEqualObjectIdNoVersion() {
    final UniqueId d1a = UniqueId.of("Scheme", "d1");
    final UniqueId d1b = UniqueId.of("Scheme", "d1");
    final UniqueId d2 = UniqueId.of("Scheme", "d2");

    assertTrue(d1a.equalObjectId(d1a));
    assertTrue(d1a.equalObjectId(d1b));
    assertFalse(d1a.equalObjectId(d2));

    assertTrue(d1b.equalObjectId(d1a));
    assertTrue(d1b.equalObjectId(d1b));
    assertFalse(d1b.equalObjectId(d2));

    assertFalse(d2.equalObjectId(d1a));
    assertFalse(d2.equalObjectId(d1b));
    assertTrue(d2.equalObjectId(d2));
  }

  /**
   * Tests the equivalent object id.
   */
  @Test
  public void testEqualObjectIdVersion() {
    final UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d2 = UniqueId.of("Scheme", "d1", "2");

    assertTrue(d1.equalObjectId(d2));
  }

  /**
   * Tests the equivalent object id.
   */
  @Test
  public void testEqualObjectIdScheme() {
    final UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d2 = UniqueId.of("Other", "d1", "2");

    assertFalse(d1.equalObjectId(d2));
  }

  /**
   * Tests that null is not equivalent to an object id.
   */
  @Test
  public void testEqualObjectIdNull() {
    final UniqueId d1 = UniqueId.of("Scheme", "d1", "1");

    assertFalse(d1.equalObjectId(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToNoVersion() {
    final UniqueId a = UniqueId.of("A", "1");
    final UniqueId b = UniqueId.of("A", "2");
    final UniqueId c = UniqueId.of("B", "2");

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
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToVersionOnly() {
    final UniqueId a = UniqueId.of("A", "1", null);
    final UniqueId b = UniqueId.of("A", "1", "4");
    final UniqueId c = UniqueId.of("A", "1", "5");

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
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToValueBeatsVersion() {
    final UniqueId a = UniqueId.of("A", "1", "5");
    final UniqueId b = UniqueId.of("A", "2", "4");

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToSchemeBeatsValue() {
    final UniqueId a = UniqueId.of("A", "2", "1");
    final UniqueId b = UniqueId.of("B", "1", "1");

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  /**
   * Tests that null is not an allowed input.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCompareToNull() {
    final UniqueId test = UniqueId.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsNoVersion() {
    final UniqueId d1a = UniqueId.of("Scheme", "d1");
    final UniqueId d1b = UniqueId.of("Scheme", "d1");
    final UniqueId d2 = UniqueId.of("Scheme", "d2");

    assertEquals(true, d1a.equals(d1a));
    assertEquals(true, d1a.equals(d1b));
    assertEquals(false, d1a.equals(d2));

    assertEquals(true, d1b.equals(d1a));
    assertEquals(true, d1b.equals(d1b));
    assertEquals(false, d1b.equals(d2));

    assertEquals(false, d2.equals(d1a));
    assertEquals(false, d2.equals(d1b));
    assertEquals(true, d2.equals(d2));

    assertNotEquals(d1b, "d1");
    assertNotEquals(d1b, null);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsVersion() {
    final UniqueId d1a = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d1b = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d2 = UniqueId.of("Scheme", "d2", null);

    assertEquals(true, d1a.equals(d1a));
    assertEquals(true, d1a.equals(d1b));
    assertEquals(false, d1a.equals(d2));

    assertEquals(true, d1b.equals(d1a));
    assertEquals(true, d1b.equals(d1b));
    assertEquals(false, d1b.equals(d2));

    assertEquals(false, d2.equals(d1a));
    assertEquals(false, d2.equals(d1b));
    assertEquals(true, d2.equals(d2));

    assertNotEquals(d1b, "d1");
    assertNotEquals(null, d1b);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsDifferentScheme() {
    final UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d2 = UniqueId.of("Other", "d1", "1");

    assertEquals(true, d1.equals(d1));
    assertEquals(false, d1.equals(d2));
    assertEquals(false, d2.equals(d1));
    assertEquals(true, d2.equals(d2));
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCodeNoVersion() {
    final UniqueId d1a = UniqueId.of("Scheme", "d1");
    final UniqueId d1b = UniqueId.of("Scheme", "d1");

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCodeVersion() {
    final UniqueId d1a = UniqueId.of("Scheme", "d1", "1");
    final UniqueId d1b = UniqueId.of("Scheme", "d1", "1");

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
