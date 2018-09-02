/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ExpiryFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests a cycle.
   */
  @Test
  public void testAccuracyMinute() {
    final Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testAccuracyHour() {
    final Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testAccuracyDay() {
    final Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.DAY_MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testAccuracyMonth() {
    final Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testAccuracyYear() {
    final Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  /**
   * Tests a null message.
   */
  @Test
  public void testNullMessage() {
    assertNull(ExpiryFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), null));
  }

  /**
   * Tests a null expiry.
   */
  @Test
  public void testNullExpiry() {
    assertNull(ExpiryFudgeBuilder.toFudgeMsg(getFudgeSerializer(), null));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final Expiry expiry = new Expiry(DateUtils.getUTCDate(2018, 1, 1), ExpiryAccuracy.DAY_MONTH_YEAR);
    assertEquals(ExpiryFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), ExpiryFudgeBuilder.toFudgeMsg(getFudgeSerializer(), expiry)), expiry);
  }

  /**
   * Tests secondary type conversion from primary to secondary.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testSecondaryToPrimary() {
    ExpiryFudgeBuilder.SECONDARY_TYPE_INSTANCE.secondaryToPrimary(new Expiry(DateUtils.getUTCDate(2018, 1, 1), ExpiryAccuracy.DAY_MONTH_YEAR));
  }

  /**
   * Tests primary to secondary conversion.
   */
  @Test
  public void testPrimaryToSecondary() {
    final Expiry expiry = new Expiry(DateUtils.getUTCDate(2018, 1, 1), ExpiryAccuracy.DAY_MONTH_YEAR);
    final MutableFudgeMsg msg = ExpiryFudgeBuilder.toFudgeMsg(getFudgeSerializer(), expiry);
    assertEquals(ExpiryFudgeBuilder.SECONDARY_TYPE_INSTANCE.primaryToSecondary(msg), expiry);
  }
}
