/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

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

  public void test_LATEST() {
    final VersionCorrection test = VersionCorrection.LATEST;
    assertEquals(null, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
    assertEquals("VLATEST.CLATEST", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_of_VersionCorrection() {
    final VersionCorrection base = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection test = VersionCorrection.of(base);
    assertSame(base, test);
  }

  public void test_of_VersionCorrection_null() {
    final VersionCorrection test = VersionCorrection.of((VersionCorrection) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  public void test_of_InstantInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
    assertEquals("V1970-01-01T00:00:01Z.C1970-01-01T00:00:02Z", test.toString());
  }

  public void test_of_InstantInstant_nullVersion() {
    final VersionCorrection test = VersionCorrection.of((Instant) null, INSTANT2);
    assertEquals(null, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
    assertEquals("VLATEST.C1970-01-01T00:00:02Z", test.toString());
  }

  public void test_of_InstantInstant_nullCorrection() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, (Instant) null);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
    assertEquals("V1970-01-01T00:00:01Z.CLATEST", test.toString());
  }

  public void test_of_InstantInstant_nulls() {
    final VersionCorrection test = VersionCorrection.of((Instant) null, (Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  public void test_ofVersionAsOf_Instant() {
    final VersionCorrection test = VersionCorrection.ofVersionAsOf(INSTANT1);
    assertEquals(INSTANT1, test.getVersionAsOf());
    assertEquals(null, test.getCorrectedTo());
  }

  public void test_ofVersionAsOf_Instant_null() {
    final VersionCorrection test = VersionCorrection.ofVersionAsOf((Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  public void test_ofCorrectedTo_Instant() {
    final VersionCorrection test = VersionCorrection.ofCorrectedTo(INSTANT2);
    assertEquals(null, test.getVersionAsOf());
    assertEquals(INSTANT2, test.getCorrectedTo());
  }

  public void test_ofCorrectedTo_Instant_null() {
    final VersionCorrection test = VersionCorrection.ofCorrectedTo((Instant) null);
    assertSame(VersionCorrection.LATEST, test);
  }

  //-------------------------------------------------------------------------
  @DataProvider(name = "parseValid")
  Object[][] data_parseValid() {
    return new Object[][] {
        {"1970-01-01T00:00:01Z", "1970-01-01T00:00:02Z", VersionCorrection.of(INSTANT1, INSTANT2)},
        {"LATEST", "1970-01-01T00:00:02Z", VersionCorrection.of(null, INSTANT2)},
        {"1970-01-01T00:00:01Z", "LATEST", VersionCorrection.of(INSTANT1, null)},
        {"LATEST", "LATEST", VersionCorrection.of(null, null)},
    };
  }

  @Test(dataProvider = "parseValid")
  public void test_parse_String(final String first, final String second, final VersionCorrection expected) {
    final VersionCorrection test = VersionCorrection.parse("V" + first + ".C" + second);
    assertEquals(expected, test);
  }

  @Test(dataProvider = "parseValid")
  public void test_parse_StringString(final String first, final String second, final VersionCorrection expected) {
    final VersionCorrection test = VersionCorrection.parse(first, second);
    assertEquals(expected, test);
  }

  @Test(dataProvider = "parseValid")
  public void test_parse_StringString_nullsAllowed(final String first, final String second, final VersionCorrection expected) {
    final String firstString = first.equals("LATEST") ? null : first;
    final String secondString = second.equals("LATEST") ? null : second;
    final VersionCorrection test = VersionCorrection.parse(firstString, secondString);
    assertEquals(expected, test);
  }

  @DataProvider(name = "parseInvalid")
  Object[][] data_parseInvalid() {
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

  @Test(dataProvider = "parseInvalid", expectedExceptions = IllegalArgumentException.class)
  public void test_parse_String_invalid(final String input) {
    VersionCorrection.parse(input);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_StringString_invalid() {
    VersionCorrection.parse("LATS", "LATS");
  }

  //-------------------------------------------------------------------------
  public void test_withVersionAsOf_instantToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withVersionAsOf(INSTANT3));
  }

  public void test_withVersionAsOf_instantToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(null, INSTANT2), test.withVersionAsOf(null));
  }

  public void test_withVersionAsOf_nullToInstant() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withVersionAsOf(INSTANT3));
  }

  public void test_withVersionAsOf_nullToNull() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(null, INSTANT2), test.withVersionAsOf(null));
  }

  //-------------------------------------------------------------------------
  public void test_withCorrectedTo_instantToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withCorrectedTo(INSTANT3));
  }

  public void test_withCorrectedTo_instantToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT1, null), test.withCorrectedTo(null));
  }

  public void test_withCorrectedTo_nullToInstant() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withCorrectedTo(INSTANT3));
  }

  public void test_withCorrectedTo_nullToNull() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, null), test.withCorrectedTo(null));
  }

  //-------------------------------------------------------------------------
  public void test_containsLatest() {
    assertEquals(false, VersionCorrection.of(INSTANT1, INSTANT2).containsLatest());
    assertEquals(true, VersionCorrection.of(null, INSTANT2).containsLatest());
    assertEquals(true, VersionCorrection.of(INSTANT1, null).containsLatest());
    assertEquals(true, VersionCorrection.of(null, null).containsLatest());
  }

  //-------------------------------------------------------------------------
  public void test_withLatestFixed_noNulls() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    assertSame(test, test.withLatestFixed(INSTANT3));
  }

  public void test_withLatestFixed_nullVersion() {
    final VersionCorrection test = VersionCorrection.of(null, INSTANT2);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT2), test.withLatestFixed(INSTANT3));
  }

  public void test_withLatestFixed_nullCorrection() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, null);
    assertEquals(VersionCorrection.of(INSTANT1, INSTANT3), test.withLatestFixed(INSTANT3));
  }

  public void test_withLatestFixed_nulls() {
    final VersionCorrection test = VersionCorrection.of(null, null);
    assertEquals(VersionCorrection.of(INSTANT3, INSTANT3), test.withLatestFixed(INSTANT3));
  }

  //-------------------------------------------------------------------------
  public void test_compareTo_nonNull() {
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

  public void test_compareTo_nullVersion() {
    final VersionCorrection a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection b = VersionCorrection.of(null, INSTANT2);

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);

    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  public void test_compareTo_nullCorrection() {
    final VersionCorrection a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection b = VersionCorrection.of(INSTANT1, null);

    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);

    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_compareTo_null() {
    final VersionCorrection test = VersionCorrection.of(INSTANT1, INSTANT2);
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
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

    assertEquals(false, d1b.equals("d1"));
    assertEquals(false, d1b.equals(null));
  }

  public void test_hashCode() {
    final VersionCorrection d1a = VersionCorrection.of(INSTANT1, INSTANT2);
    final VersionCorrection d1b = VersionCorrection.of(INSTANT1, INSTANT2);

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
