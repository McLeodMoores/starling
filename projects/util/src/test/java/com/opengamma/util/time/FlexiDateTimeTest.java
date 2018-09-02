/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FlexiDateTime}.
 */
@Test(groups = TestGroup.UNIT)
public class FlexiDateTimeTest {
  private static final LocalDate DATE = LocalDate.of(2010, 7, 1);
  private static final LocalTime TIME = LocalTime.of(12, 30);
  private static final LocalTime TIME2 = LocalTime.of(13, 40);
  private static final ZoneOffset OFFSET = ZoneOffset.ofHours(2);
  private static final ZoneId ZONE = ZoneId.of("America/New_York");
  private static final ZoneId ZONE2 = ZoneId.of("America/Los_Angeles");
  private static final LocalDate LOCAL_DATE = DATE;
  private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(DATE, TIME);
  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(DATE, TIME, OFFSET);
  private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(DATE.atTime(TIME), ZONE);

  /**
   * Tests construction from a LocalDate.
   */
  @Test
  public void testLd() {
    final FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE);
    assertState(test, DATE, null, null);
  }

  /**
   * Tests construction from a LocalDate and LocalTime.
   */
  @Test
  public void testLdLt() {
    final FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE, TIME);
    assertState(test, DATE, TIME, null);
  }

  /**
   * Tests construction from a LocalDateTime.
   */
  @Test
  public void testLdt() {
    final FlexiDateTime test = FlexiDateTime.of(LOCAL_DATE_TIME);
    assertState(test, DATE, TIME, null);
  }

  /**
   * Tests construction from an {@link OffsetDateTime}.
   */
  @Test
  public void testOdt() {
    final FlexiDateTime test = FlexiDateTime.of(OFFSET_DATE_TIME);
    assertState(test, DATE, TIME, OFFSET);
  }

  /**
   * Tests construction from a {@link ZonedDateTime}.
   */
  @Test
  public void testZdt() {
    final FlexiDateTime test = FlexiDateTime.of(ZONED_DATE_TIME);
    assertState(test, DATE, TIME, ZONE);
  }

  /**
   * Tests construction from a LocalDate.
   */
  @Test
  public void testTemporalLd() {
    assertState(FlexiDateTime.from(LOCAL_DATE), LOCAL_DATE, null, null);
  }

  /**
   * Tests construction from a LocalDateTime.
   */
  @Test
  public void testTemporalLdt() {
    assertState(FlexiDateTime.from(LOCAL_DATE_TIME), DATE, TIME, null);
  }

  /**
   * Tests construction from an OffsetDateTime.
   */
  @Test
  public void testTemporalOdt() {
    assertState(FlexiDateTime.from(OFFSET_DATE_TIME), DATE, TIME, OFFSET);
  }

  /**
   * Tests construction from a ZonedDateTime.
   */
  @Test
  public void testTemporalZdt() {
    assertState(FlexiDateTime.from(ZONED_DATE_TIME), DATE, TIME, ZONE);
  }

  /**
   * Tests lenient construction using a LocalDate.
   */
  @Test
  public void testOfLenientLd() {
    assertState(FlexiDateTime.ofLenient(DATE, null), DATE, null, null);
  }

  /**
   * Tests lenient construction using a LocalDate and LocalTime.
   */
  @Test
  public void testOfLenientLdLt() {
    assertState(FlexiDateTime.ofLenient(DATE, OffsetTime.of(TIME, OFFSET)), DATE, TIME, OFFSET);
  }

  /**
   * Tests lenient construction using a LocalDate and LocalTime.
   */
  @Test
  public void testOfLenientLdLtZid() {
    assertState(FlexiDateTime.ofLenient(DATE, TIME, ZONE), DATE, TIME, ZONE);
  }

  /**
   * Tests the create() method.
   */
  @Test
  public void testCreate() {
    assertNull(FlexiDateTime.create(null, null));
    assertNull(FlexiDateTime.create(null, OffsetTime.of(TIME, OFFSET)));
    assertState(FlexiDateTime.create(DATE, null), DATE, null, null);
    assertState(FlexiDateTime.create(DATE, OffsetTime.of(TIME, OFFSET)), DATE, TIME, OFFSET);

    assertNull(FlexiDateTime.create(null, null, null));
    assertNull(FlexiDateTime.create(null, TIME, null));
    assertState(FlexiDateTime.create(DATE, TIME, null), DATE, TIME, null);
    assertState(FlexiDateTime.create(DATE, TIME, ZONE), DATE, TIME, ZONE);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    assertEquals(FlexiDateTime.of(DATE).hashCode(), FlexiDateTime.of(DATE).hashCode());
    assertEquals(FlexiDateTime.of(LOCAL_DATE_TIME).hashCode(), FlexiDateTime.of(LOCAL_DATE_TIME).hashCode());
    assertEquals(FlexiDateTime.of(OFFSET_DATE_TIME).hashCode(), FlexiDateTime.of(OFFSET_DATE_TIME).hashCode());
    assertEquals(FlexiDateTime.of(ZONED_DATE_TIME).hashCode(), FlexiDateTime.of(ZONED_DATE_TIME).hashCode());
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    assertEquals(FlexiDateTime.of(DATE), FlexiDateTime.ofLenient(DATE, null));
    assertEquals(FlexiDateTime.of(DATE), FlexiDateTime.ofLenient(DATE, null));
    assertEquals(FlexiDateTime.of(LOCAL_DATE_TIME), FlexiDateTime.of(LOCAL_DATE_TIME));
    assertEquals(FlexiDateTime.of(ZONED_DATE_TIME), FlexiDateTime.of(ZONED_DATE_TIME));
    assertNotEquals(null, FlexiDateTime.of(DATE));
    assertNotEquals(DATE, FlexiDateTime.of(DATE));
    assertNotEquals(DATE, FlexiDateTime.of(DATE.plusDays(1)));
    assertNotEquals(DATE, FlexiDateTime.of(LOCAL_DATE_TIME));
    assertNotEquals(LOCAL_DATE_TIME, FlexiDateTime.of(LOCAL_DATE_TIME.plusDays(1)));
    assertNotEquals(LOCAL_DATE_TIME, FlexiDateTime.of(LOCAL_DATE_TIME.plusHours(1)));
    assertNotEquals(DATE, FlexiDateTime.of(ZONED_DATE_TIME));
    assertNotEquals(ZONED_DATE_TIME, FlexiDateTime.of(ZONED_DATE_TIME.plusDays(1)));
    assertNotEquals(DATE, FlexiDateTime.of(OFFSET_DATE_TIME));
    assertNotEquals(OFFSET_DATE_TIME, FlexiDateTime.of(OFFSET_DATE_TIME.atZoneSameInstant(ZONE2)));
  }

  private static void assertState(final FlexiDateTime actual, final LocalDate expectedDate, final LocalTime expectedTime, final ZoneId expectedZone) {
    assertEquals(expectedDate, actual.getDate());
    assertEquals(expectedTime, actual.getTime());
    assertEquals(expectedZone, actual.getZone());

    // toLocalDateTime() and toLocalDateTime(LocalTime)
    if (expectedTime != null) {
      assertEquals(expectedDate.atTime(expectedTime), actual.toLocalDateTime());
      assertEquals(expectedDate.atTime(expectedTime), actual.toLocalDateTime(TIME2));
    } else {
      try {
        actual.toLocalDateTime();
        fail();
      } catch (final RuntimeException ex) {
        // expected
      }
      assertEquals(expectedDate.atTime(TIME2), actual.toLocalDateTime(TIME2));
    }

    // toZonedDateTime() and toZonedDateTime(LocalTime,TimeZone)
    if (expectedZone != null) {
      assertEquals(true, actual.isComplete());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone).toOffsetDateTime(), actual.toOffsetDateTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone).toOffsetDateTime().toOffsetTime(), actual.toOffsetTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), actual.toZonedDateTime());
      assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), actual.toZonedDateTime(TIME2, ZONE2));
    } else {
      assertEquals(false, actual.isComplete());
      try {
        actual.toOffsetDateTime();
        fail();
      } catch (final RuntimeException ex) {
        // expected
      }
      try {
        actual.toOffsetTime();
        fail();
      } catch (final RuntimeException ex) {
        // expected
      }
      try {
        actual.toZonedDateTime();
        fail();
      } catch (final RuntimeException ex) {
        // expected
      }
      if (expectedTime != null) {
        assertEquals(expectedDate.atTime(expectedTime).atZone(ZONE2), actual.toZonedDateTime(TIME2, ZONE2));
      } else {
        assertEquals(expectedDate.atTime(TIME2).atZone(ZONE2), actual.toZonedDateTime(TIME2, ZONE2));
      }
    }

    // toBest()
    if (expectedTime != null) {
      if (expectedZone != null) {
        if (expectedZone instanceof ZoneOffset) {
          assertEquals(expectedDate.atTime(expectedTime).atOffset((ZoneOffset) expectedZone), actual.toBest());
        } else {
          assertEquals(expectedDate.atTime(expectedTime).atZone(expectedZone), actual.toBest());
        }
      } else {
        assertEquals(LocalDateTime.of(expectedDate, expectedTime), actual.toBest());
      }
    } else {
      assertEquals(expectedDate, actual.toBest());
    }

    // equals
    assertEquals(true, actual.equals(actual));
    assertEquals(true, actual.equals(FlexiDateTime.ofLenient(actual.getDate(), actual.getTime(), actual.getZone())));
    assertNotEquals(actual, "");
    assertEquals(false, actual.equals(null));
    assertEquals(false, actual.equals(FlexiDateTime.of(DATE.minusDays(1))));
    assertEquals(false, actual.equals(FlexiDateTime.of(DATE.minusDays(1), TIME)));
    assertEquals(false, actual.equals(FlexiDateTime.ofLenient(DATE.minusDays(1), TIME, ZONE)));
  }

}
