/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link RestUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class RestUtilsTest {
  private static final LocalDateDoubleTimeSeries TS = ImmutableLocalDateDoubleTimeSeries.of(
      new LocalDate[] { LocalDate.of(2018, 1, 1), LocalDate.of(2018, 2, 1), LocalDate.of(2018, 3, 1) },
      new double[] { 1., 2., 3. });

  /**
   * Tests decoding a null message.
   */
  @Test
  public void testNullMessage() {
    assertNotNull(RestUtils.decodeBase64(Object.class, null));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycleBase64() {
    assertEquals(RestUtils.decodeBase64(LocalDateDoubleTimeSeries.class, RestUtils.encodeBase64(TS)), TS);
  }

}
