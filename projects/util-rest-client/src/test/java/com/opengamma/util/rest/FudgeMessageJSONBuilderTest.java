/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.rest;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.fudgemsg.timeseries.LocalDateDoubleTimeSeriesFudgeBuilder;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeMessageJSONBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeMessageJSONBuilderTest {

  /**
   * Tests that the context cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullContext() {
    new FudgeMessageJSONBuilder(null);
  }

  /**
   * Tests that the message cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMessage() {
    new FudgeMessageJSONBuilder(OpenGammaFudgeContext.getInstance()).build(null);
  }

  /**
   * Tests the builder.
   */
  @Test
  public void testBuild() {
    final LocalDateDoubleTimeSeries ts =
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2)}, new double[] {1, 2});
    final MutableFudgeMsg msg =
        new LocalDateDoubleTimeSeriesFudgeBuilder().buildMessage(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), ts);
    assertEquals(new FudgeMessageJSONBuilder().build(msg),
        "[{\"dupNamePsfxKey\":\"_\"},{\"0\":\"com.opengamma.timeseries.DoubleTimeSeries\",\"dates\":[20180101,20180102],\"values\":[1,2]}]");
  }
}
