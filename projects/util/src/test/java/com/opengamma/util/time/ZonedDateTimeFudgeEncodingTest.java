/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding of {@link ZonedDateTime}.
 */
@Test(groups = TestGroup.UNIT)
public class ZonedDateTimeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests with UTC.
   */
  @Test
  public void testUtc() {
    final ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneOffset.UTC);
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtUTC);
  }

  /**
   * Tests with New York time zone.
   */
  @Test
  public void testNewYork() {
    final ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime zdtPST = ZonedDateTime.ofInstant(zdtUTC.toInstant(), ZoneId.of("America/New_York"));
    assertTrue(zdtUTC.isEqual(zdtPST));
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtPST);
  }

  /**
   * Tests null inputs.
   */
  @Test
  public void testToFromNull() {
    assertNull(ZonedDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), null));
    assertNull(ZonedDateTimeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), null));
  }

  /**
   * Tests conversion to a message.
   */
  @Test
  public void testStaticTo() {
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneId.of("America/New_York"));
    final MutableFudgeMsg msg1 = ZonedDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), date);
    final MutableFudgeMsg msg2 = getFudgeSerializer().newMessage();
    ZonedDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), date, msg2);
    assertEquals(msg1, msg2);
  }

  /**
   * Tests conversion from a message.
   */
  @Test
  public void testStaticFrom() {
    final ZonedDateTime date = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneId.of("America/New_York"));
    final MutableFudgeMsg msg = ZonedDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), date);
    assertEquals(ZonedDateTimeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), msg), date);
  }
}
