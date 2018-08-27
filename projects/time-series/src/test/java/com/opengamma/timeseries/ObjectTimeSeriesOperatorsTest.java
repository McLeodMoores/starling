/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.timeseries;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;

/**
 * Unit tests for {@link ObjectTimeSeriesOperators}.
 */
@Test(groups = "unit")
public class ObjectTimeSeriesOperatorsTest {
  private static final ObjectTimeSeries<LocalDate, String> TS_1 = ImmutableLocalDateObjectTimeSeries.of(
      new LocalDate[] { LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2) },
      new String[] { "A", "B" });
  private static final ObjectTimeSeries<LocalDate, String> TS_2 = ImmutableLocalDateObjectTimeSeries.of(
      new LocalDate[] { LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2) },
      new String[] { "C", "D" });

  /**
   * Tests binary first and second operators.
   */
  @Test
  public void testBinary() {
    for (int i = 0; i < TS_1.size(); i++) {
      assertEquals(ObjectTimeSeriesOperators.firstOperator().operate(TS_1.getTimeAtIndex(i), TS_2.getTimeAtIndex(i)), TS_1.getTimeAtIndex(i));
      assertEquals(ObjectTimeSeriesOperators.firstOperator().operate(TS_1.getValueAtIndex(i), TS_2.getValueAtIndex(i)), TS_1.getValueAtIndex(i));
      assertEquals(ObjectTimeSeriesOperators.secondOperator().operate(TS_1.getTimeAtIndex(i), TS_2.getTimeAtIndex(i)), TS_2.getTimeAtIndex(i));
      assertEquals(ObjectTimeSeriesOperators.secondOperator().operate(TS_1.getValueAtIndex(i), TS_2.getValueAtIndex(i)), TS_2.getValueAtIndex(i));
    }
  }

  /**
   * Tests the binary no intersection operator.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBinaryNoIntersection() {
    ObjectTimeSeriesOperators.noIntersectionOperator().operate(TS_1.getTimeAtIndex(0), TS_2.getTimeAtIndex(0));
  }
}
