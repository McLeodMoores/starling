/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class FlexiDateTimeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests a cycle.
   */
  @Test
  public void testLd() {
    final FlexiDateTime ld = FlexiDateTime.of(LocalDate.of(2010, 7, 1));
    assertEncodeDecodeCycle(FlexiDateTime.class, ld);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testLdt() {
    final FlexiDateTime ldt = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0));
    assertEncodeDecodeCycle(FlexiDateTime.class, ldt);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testOdt() {
    final FlexiDateTime odt = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0).atOffset(ZoneOffset.ofHours(3)));
    assertEncodeDecodeCycle(FlexiDateTime.class, odt);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testZdtUtc() {
    final FlexiDateTime zdtUTC = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0, 0).atZone(ZoneOffset.UTC));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtUTC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testZdtNewYork() {
    final FlexiDateTime zdtPST = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0, 0).atZone(ZoneId.of("America/New_York")));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtPST);
  }

  /**
   * Tests the behaviour when there is no date in the message.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoDateInMessage() {
    FlexiDateTimeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), getFudgeContext().newMessage());
  }

  /**
   * Tests the behaviour when the message is null.
   */
  @Test
  public void testNullMessage() {
    FlexiDateTimeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), null);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final FlexiDateTime date = FlexiDateTime.of(LocalDate.now());
    final MutableFudgeMsg msg = FlexiDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), date);
    assertEquals(FlexiDateTimeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), msg), date);
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test
  public void testNullDate() {
    FlexiDateTimeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), null);
  }
}
