/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link VersionCorrection}.
 */
@Test(groups = TestGroup.UNIT)
public class VersionCorrectionTest {
  private static final Instant INSTANT1 = Instant.ofEpochSecond(1);
  private static final Instant INSTANT2 = Instant.ofEpochSecond(2);
  private static final Instant INSTANT3 = Instant.ofEpochSecond(3);

  /**
   * Tests the latest version.
   */
  @Test
  public void testLatest() {
    final VersionCorrection test = VersionCorrection.LATEST;
    assertEquals(null, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
    assertEquals("VLATEST.CLATEST", test.toString());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the original version correction is returned.
   */
  @Test
  public void testOfVersionCorrection() {
    final VersionCorrection base = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection test = VersionCorrection.of(base);
    assertSame(base, test);
  }

  /**
   * Tests that LATEST is returned.
   */
  @Test
  public void testOfVersionCorrectionNull() {
    final VersionCorrection test = VersionCorrection.of((VersionCorrection) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from instants.
   */
  @Test
  public void testOfInstantInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
    assertEquals("V1970-01-01T00:00:01Z.C1970-01-01T00:00:02Z", test.toString());
  }

  /**
   * Tests construction from instants without a version.
   */
  @Test
  public void testOfInstantInstantNullVersion() {
    final VersionCorrection test = VersionCorrection.of((Instant) null, INSTANT2);
    assertEquals(null, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
    assertEquals("VLATEST.C1970-01-01T00:00:02Z", test.toString());
  }

  /**
   * Tests construction from instants without a correction.
   */
  @Test
  public void testOfInstantInstantNullCorrection() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, (Instant) null);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
    assertEquals("V1970-01-01T00:00:01Z.CLATEST", test.toString());
  }

  /**
   * Tests that null version and correction returns LATEST.
   */
  @Test
  public void testOfInstantInstantNulls() {
    final VersionCorrection test = VersionCorrection.of((Instant) null, (Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a version-correction as of a version.
   */
  @Test
  public void testOfVersionAsOfInstant() {
    final VersionCorrection test = VersionCorrection.ofVersionAsOf(INSTANT1);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
  }

  /**
   * Tests a version-correction as of a null version.
   */
  @Test
  public void testOfVersionAsOfInstantNull() {
    final VersionCorrection test = VersionCorrection.ofVersionAsOf((Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests a version-correction corrected to another instant.
   */
  @Test
  public void testOfCorrectedToInstant() {
    final VersionCorrection test = VersionCorrection.ofCorrectedTo(INSTANT2);
    assertEquals(null, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
  }

  /**
   * Tests a version-correction corrected to a null instant.
   */
  @Test
  public void testOfCorrectedToInstantNull() {
    final VersionCorrection test = VersionCorrection.ofCorrectedTo((Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Provides valid strings.
   *
   * @return  valid strings and expected values
   */
  @DataProvider(name = "parseValid")
  Object[][] dataParseValid() {
    return new Object[][] {
        {"1970-01-01T00:00:01Z", "1970-01-01T00:00:02Z", VersionCorrection.of(INSTANT1, INSTANT2)},
        {"LATEST", "1970-01-01T00:00:02Z", VersionCorrection.of(null, INSTANT2)},
        {"1970-01-01T00:00:01Z", "LATEST", VersionCorrection.of(INSTANT1, null)},
        {"LATEST", "LATEST", VersionCorrection.of(null, null)},
    };
  }

  /**
   * Tests string parsing.
   *
   * @param first  the version string
   * @param second  the correction string
   * @param expected  the expected value
   */
  @Test(dataProvider = "parseValid")
  public void testParseString(final String first, final String second, final VersionCorrection expected) {
    final VersionCorrection test = VersionCorrection.parse("V" + first + ".C" + second);
    assertEquals(expected, test);
  }

  /**
   * Tests string parsing.
   *
   * @param first  the version string
   * @param second  the correction string
   * @param expected  the expected value
   */
  @Test(dataProvider = "parseValid")
  public void testParseStringString(final String first, final String second, final VersionCorrection expected) {
    final VersionCorrection test = VersionCorrection.parse(first, second);
    assertEquals(expected, test);
  }

  /**
   * Tests string parsing.
   *
   * @param first  the version string
   * @param second  the correction string
   * @param expected  the expected value
   */
  @Test(dataProvider = "parseValid")
  public void testParseStringStringNullsAllowed(final String first, final String second, final VersionCorrection expected) {
    final String firstString = first.equals("LATEST") ? null : first;
    final String secondString = second.equals("LATEST") ? null : second;
    final VersionCorrection test = VersionCorrection.parse(firstString, secondString);
    assertEquals(expected, test);
  }

  /**
   * Provides invalid strings.
   *
   * @return  invalid strings
   */
  @DataProvider(name = "parseInvalid")
  Object[][] dataParseInvalid() {
    return new Object[][] {
        {"1970-01-01T00:00:01Z.C1970-01-01T00:00:02Z"},  // no V
        {"V1970-01-01T00:00:01Z.1970-01-01T00:00:02Z"},  // no C
        {""},  // blank
        {"V1970-01-01T00:00:01Z"},  // only half
        {"V1970-12-01 00:00:01Z.C1970-01-01T00:00:02Z"},  // invalid date 1
        {"V1970-12-01T00:00:01Z.C1970-01-20 00:00:02Z"},  // invalid date 2
        {"VLATS.CLATS"},  // invalid latest
    };
  }

  /**
   * Tests string parsing.
   *
   * @param input  the invalid string
   */
  @Test(dataProvider = "parseInvalid", expectedExceptions = IllegalArgumentException.class)
  public void testParseStringInvalid(final String input) {
    VersionCorrection.parse(input);
  }

  /**
   * Tests parsing of an invalid string.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseStringStringInvalid() {
    VersionCorrection.parse("LATS", "LATS");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withVersionAsOf() method.
   */
  @Test
  public void testWithVersionAsOfInstantToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withVersionAsOf(INSTANT3));
  }

  /**
   * Tests the withVersionAsOf() method.
   */
  @Test
  public void testWithVersionAsOfInstantToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(null, INSTANT2), test.withVersionAsOf(null));
  }

  /**
   * Tests the withVersionAsOf() method.
   */
  @Test
  public void testWithVersionAsOfNullToInstant() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withVersionAsOf(INSTANT3));
  }

  /**
   * Tests the withVersionAsOf() method.
   */
  @Test
  public void testWithVersionAsOfNullToNull() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(null, INSTANT2), test.withVersionAsOf(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withCorrectedTo() method.
   */
  @Test
  public void testWithCorrectedToInstantToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withCorrectedTo(INSTANT3));
  }

  /**
   * Tests the withCorrectedTo() method.
   */
  @Test
  public void testWithCorrectedToInstantToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT1, null), test.withCorrectedTo(null));
  }

  /**
   * Tests the withCorrectedTo() method.
   */
  @Test
  public void testWithCorrectedToNullToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withCorrectedTo(INSTANT3));
  }

  /**
   * Tests the withCorrectedTo() method.
   */
  @Test
  public void testWithCorrectedToNullToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, null), test.withCorrectedTo(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsLatest() method.
   */
  @Test
  public void testContainsLatest() {
    assertEquals(false, VersionCorrection.of(INSTANT1, INSTANT2).containsLatest());
    assertEquals(true, VersionCorrection.of(null, INSTANT2).containsLatest());
    assertEquals(true, VersionCorrection.of(INSTANT1, null).containsLatest());
    assertEquals(true, VersionCorrection.of(null, null).containsLatest());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that existing version/correction instants are not replaced.
   */
  @Test
  public void testWithLatestFixedNoNulls() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertSame(test, test.withLatestFixed(INSTANT3));
  }

  /**
   * Tests that only the version is replaced.
   */
  @Test
  public void testWithLatestFixedNullVersion() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withLatestFixed(INSTANT3));
  }

  /**
   * Tests that only the correction is replaced.
   */
  @Test
  public void testWithLatestFixedNullCorrection() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withLatestFixed(INSTANT3));
  }

  /**
   * Tests that the version and correction are replaced.
   */
  public void testWithLatestFixedNulls() {
    final VersionCorrection test = VersionCorrection.of(null, null);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT3), test.withLatestFixed(INSTANT3));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToNonNull() {
    final VersionCorrection a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection b = VersionCorrection.of(INSTANT1, INSTANT3);
    final VersionCorrection c = VersionCorrection.of(INSTANT2, INSTANT3);

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
  public void testCompareToNullVersion() {
    final VersionCorrection a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection b = VersionCorrection.of(null, INSTANT2);

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);

    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToNullCorrection() {
    final VersionCorrection a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection b = VersionCorrection.of(INSTANT1, null);

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);

    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCompareToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final VersionCorrection d1a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection d1b = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection d2 = VersionCorrection.of(INSTANT1, INSTANT3);

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
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final VersionCorrection d1a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection d1b = VersionCorrection.of(INSTANT1, INSTANT2);

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
