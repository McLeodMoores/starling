/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LocalDateFudgeBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class LocalDateFudgeBuilderTest extends AbstractFudgeBuilderTestCase {
  private static final LocalDateFudgeBuilder BUILDER = new LocalDateFudgeBuilder();

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(LocalDate.class, LocalDate.of(2018, 1, 1));
    assertEncodeDecodeCycle(LocalDate.class, LocalDate.MIN);
    assertEncodeDecodeCycle(LocalDate.class, LocalDate.MAX);
  }

  /**
   * Tests that the message must contain a date.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoDate() {
    BUILDER.buildObject(getFudgeDeserializer(), getFudgeContext().newMessage());
  }

  /**
   * Tests serialization and deserialization.
   */
  @Test
  public void test() {
    final LocalDate date = LocalDate.now();
    assertEquals(BUILDER.buildObject(getFudgeDeserializer(), BUILDER.buildMessage(getFudgeSerializer(), date)), date);
  }
}
