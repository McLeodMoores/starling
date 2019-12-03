/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test {link Expiry}.
 */
@Test(groups = TestGroup.UNIT)
public class ExpiryTest {

  private static final FudgeContext FUDGE_CONTEXT = new FudgeContext();

  static {
    FUDGE_CONTEXT.getTypeDictionary().addType(ExpiryFudgeBuilder.SECONDARY_TYPE_INSTANCE);
    FUDGE_CONTEXT.getObjectDictionary().addBuilder(Expiry.class, new ExpiryFudgeBuilder());
  }

  private static FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] encoded = FUDGE_CONTEXT.toByteArray(message);
    return FUDGE_CONTEXT.deserialize(encoded).getMessage();
  }

  private static void testExpiry(final Expiry expiry) {
    final FudgeSerializer serializer = new FudgeSerializer(FUDGE_CONTEXT);
    final MutableFudgeMsg messageIn = serializer.newMessage();
    serializer.addToMessage(messageIn, "test", null, expiry);
    final FudgeMsg messageOut = cycleMessage(messageIn);
    final FudgeDeserializer dsrContext = new FudgeDeserializer(FUDGE_CONTEXT);
    final Expiry result = dsrContext.fieldValueToObject(Expiry.class, messageOut.getByName("test"));
    assertEquals(expiry, result);
    assertEquals(expiry.getExpiry().getZone(), result.getExpiry().getZone());
  }

  /**
   * Tests an encoding/decoding cycle.
   */
  @Test
  public void testFudgeMessageUTC() {
    for (final ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), accuracy));
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), ZoneOffset.UTC)));
    }
  }

  /**
   * Tests an encoding/decoding cycle.
   */
  @Test
  public void testFudgeMessageNonUTC() {
    for (final ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("GMT+02:00")), accuracy));
    }
  }

  /**
   * Tests equality to within an accuracy.
   */
  @Test
  public void testEqualsToAccuracy() {
    final ZonedDateTime zdtMinute = ZonedDateTime.of(LocalDateTime.of(2011, 7, 12, 12, 30, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtHour = ZonedDateTime.of(LocalDateTime.of(2011, 7, 12, 12, 45, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtDay = ZonedDateTime.of(LocalDateTime.of(2011, 7, 12, 11, 45, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtMonth = ZonedDateTime.of(LocalDateTime.of(2011, 7, 11, 11, 45, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtYear = ZonedDateTime.of(LocalDateTime.of(2011, 6, 11, 11, 45, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtNone = ZonedDateTime.of(LocalDateTime.of(2010, 6, 11, 11, 45, 0, 0), ZoneOffset.UTC);
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR, zdtMinute, zdtMinute));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR, zdtMinute, zdtHour));

    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtMinute));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtHour));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtDay));

    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtMinute));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtHour));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtDay));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtMonth));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtYear));

    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtDay));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtMonth));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtMonth.plusYears(1)));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtYear));

    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtMinute));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtMonth));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtYear));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtNone));
  }

  /**
   * Tests the constructors.
   */
  @Test
  public void testConstructor() {
    assertEquals(new Expiry(DateUtils.getUTCDate(2010, 1, 1)), new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.DAY_MONTH_YEAR));
    assertNotEquals(new Expiry(DateUtils.getUTCDate(2010, 1, 1)), new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR));
  }

  /**
   * Tests the conversion to an instant.
   */
  @Test
  public void testToInstant() {
    final Instant instant = Instant.ofEpochSecond(10000L);
    assertEquals(new Expiry(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)).toInstant(), instant);
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    Expiry other = new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertEquals(expiry, expiry);
    assertEquals(expiry, other);
    assertEquals(expiry.hashCode(), other.hashCode());
    other = new Expiry(DateUtils.getUTCDate(2011, 1, 1), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertNotEquals(expiry, other);
    other = new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.DAY_MONTH_YEAR);
    assertNotEquals(expiry, other);
    assertNotEquals(null, other);
    assertNotEquals(DateUtils.getUTCDate(2010, 1, 1), other);

    expiry = new Expiry(DateUtils.getUTCDate(2010, 1, 1));
    other = new Expiry(DateUtils.getUTCDate(2010, 1, 1));
    assertEquals(expiry, expiry);
    assertEquals(expiry, other);
    assertEquals(expiry.hashCode(), other.hashCode());
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Expiry expiry = new Expiry(DateUtils.getUTCDate(2010, 1, 1), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertNotNull(expiry.metaBean());
    assertNotNull(expiry.metaBean().accuracy());
    assertNotNull(expiry.metaBean().expiry());
    assertEquals(expiry.metaBean().accuracy().get(expiry), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertEquals(expiry.metaBean().expiry().get(expiry), DateUtils.getUTCDate(2010, 1, 1));
    assertEquals(expiry.property("accuracy").get(), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertEquals(expiry.property("expiry").get(), DateUtils.getUTCDate(2010, 1, 1));
  }

}
