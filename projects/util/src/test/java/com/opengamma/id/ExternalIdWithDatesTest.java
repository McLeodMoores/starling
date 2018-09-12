/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdWithDates}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdWithDatesTest {
  private static final ExternalScheme SCHEME = ExternalScheme.of("Scheme");
  private static final ExternalId IDENTIFIER = ExternalId.of(SCHEME, "value");
  private static final LocalDate VALID_FROM = LocalDate.of(2010, JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, DECEMBER, 1);

  /**
   * Tests the creation of an id.
   */
  @Test
  public void testFactoryExternalIdLocalDateLocalDate() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme~value~S~2010-01-01~E~2010-12-01", test.toString());
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalIdLocalDateLocalDateNullExternalId() {
    ExternalIdWithDates.of((ExternalId) null, VALID_FROM, VALID_TO);
  }

  /**
   * Tests that an id can have a null valid from entry.
   */
  @Test
  public void testFactoryExternalIdNullValidFrom() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, (LocalDate) null, VALID_TO);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());
    assertEquals("Scheme~value~E~2010-12-01", test.toString());
  }

  /**
   * Tests that an id can have a null valid to entry.
   */
  @Test
  public void testFactoryExternalIdNullValidTo() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, (LocalDate) null);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidTo());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals("Scheme~value~S~2010-01-01", test.toString());
  }

  /**
   * Tests that an id can have a null valid from and to entries.
   */
  @Test
  public void testFactoryExternalIdNullValidFromTo() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, (LocalDate) null, (LocalDate) null);
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidTo());
    assertNull(test.getValidFrom());
    assertEquals("Scheme~value", test.toString());
    assertEquals(test, ExternalIdWithDates.of(IDENTIFIER));
  }

  /**
   * Tests that the valid from date must be before the valid to date.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryValidFromAfterValidTo() {
    ExternalIdWithDates.of(IDENTIFIER, VALID_TO, VALID_FROM);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the parser.
   */
  @Test
  public void testParse() {
    ExternalIdWithDates test = ExternalIdWithDates.parse("Scheme~value~S~2010-01-01~E~2010-12-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertEquals(VALID_TO, test.getValidTo());

    test = ExternalIdWithDates.parse("Scheme~value~S~2010-01-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_FROM, test.getValidFrom());
    assertNull(test.getValidTo());

    test = ExternalIdWithDates.parse("Scheme~value~E~2010-12-01");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertEquals(VALID_TO, test.getValidTo());
    assertNull(test.getValidFrom());

    test = ExternalIdWithDates.parse("Scheme~value");
    assertEquals(IDENTIFIER, test.toExternalId());
    assertNull(test.getValidTo());
    assertNull(test.getValidFrom());
  }

  /**
   * Tests the error when an invalid string is parsed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidFormat() {
    ExternalIdWithDates.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the getExternalId() method returns the same object.
   */
  @Test
  public void testGetIdentityKey() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(IDENTIFIER, test.getExternalId());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the isValid() method.
   */
  @Test
  public void testIsValid() {
    final ExternalIdWithDates test = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertTrue(test.isValidOn(null));
    assertFalse(test.isValidOn(LocalDate.of(1999, 1, 1)));
    assertTrue(test.isValidOn(VALID_FROM));
    assertTrue(test.isValidOn(VALID_TO));
    assertFalse(test.isValidOn(LocalDate.of(2099, 1, 1)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final ExternalIdWithDates d1a = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    final ExternalIdWithDates d1b = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    final ExternalIdWithDates d2 = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM.minusDays(1), VALID_TO);
    final ExternalIdWithDates d3 = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO.plusDays(1));
    final ExternalIdWithDates d4 = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);

    assertEquals(d1a, d1a);
    assertEquals(d1a, d1b);
    assertNotEquals(d1a, d2);

    assertEquals(d1b, d1a);
    assertEquals(d1b, d1b);
    assertNotEquals(d1b, d2);

    assertNotEquals(d2, d1a);
    assertNotEquals(d2, d1b);
    assertEquals(d2, d2);

    assertNotEquals(d3, d1a);
    assertNotEquals(d3, d1b);
    assertEquals(d3, d3);

    assertNotEquals(d4, d1a);
    assertNotEquals(d4, d1b);
    assertEquals(d4, d4);

    assertNotEquals("d1b", d1b);
    assertNotEquals(null, d1b);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalIdWithDates d1a = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    final ExternalIdWithDates d1b = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);

    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  /**
   * Tests the compareTo() method. The comparison ignores the validity dates.
   */
  @Test
  public void testCompareTo() {
    final ExternalIdWithDates eid1 = ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO);
    assertEquals(eid1.compareTo(ExternalIdWithDates.of(IDENTIFIER, VALID_FROM.minusDays(1), VALID_TO)), 0);
    assertEquals(eid1.compareTo(ExternalIdWithDates.of(IDENTIFIER, VALID_FROM, VALID_TO.plusDays(1))), 0);
    assertTrue(eid1.compareTo(ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO)) > 0);
    assertTrue(eid1.compareTo(ExternalIdWithDates.of(ExternalId.of("Z", "B"), VALID_FROM, VALID_TO)) < 0);
  }
}
