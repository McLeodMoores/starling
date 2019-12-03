/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding fpr {@link LocalDateRange}.
 */
@Test(groups = TestGroup.UNIT)
public class LocalDateRangeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests an inclusive range.
   */
  @Test
  public void testInclusive() {
    final LocalDateRange range = LocalDateRange.of(LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), true);
    assertEncodeDecodeCycle(LocalDateRange.class, range);
  }

  /**
   * Tests an exclusive range.
   */
  @Test
  public void testExclusive() {
    final LocalDateRange range = LocalDateRange.of(LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), false);
    assertEncodeDecodeCycle(LocalDateRange.class, range);
  }

  /**
   * Tests LocalDateRange.ALL.
   */
  @Test
  public void testAll() {
    assertEncodeDecodeCycle(LocalDateRange.class, LocalDateRange.ALL);
  }

  /**
   * Tests when the message and object are null.
   */
  @Test
  public void testNull() {
    assertNull(LocalDateRangeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), null));
    assertNull(LocalDateRangeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), null));
  }

  /**
   * Tests static to message builder.
   */
  @Test
  public void testStaticToMessage() {
    final LocalDateRange ldr = LocalDateRange.of(LocalDate.now(), LocalDate.now().plusWeeks(10), false);
    assertEquals(LocalDateRangeFudgeBuilder.fromFudgeMsg(getFudgeDeserializer(), LocalDateRangeFudgeBuilder.toFudgeMsg(getFudgeSerializer(), ldr)), ldr);
  }
}
