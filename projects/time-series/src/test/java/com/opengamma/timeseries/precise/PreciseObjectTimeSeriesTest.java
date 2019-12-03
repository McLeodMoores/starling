/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.timeseries.BigDecimalObjectTimeSeriesTest;

/**
 * Abstract test class for {@code PreciseObjectTimeSeries}.
 *
 * @param <T>  the time type
 */
@Test(groups = "unit")
public abstract class PreciseObjectTimeSeriesTest<T> extends BigDecimalObjectTimeSeriesTest<T> {

  /**
   * Tests an intersection of two time series using the values from the first.
   */
  @Test
  public void testIntersectionFirstValue() {
    final PreciseObjectTimeSeries<T, BigDecimal> dts = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries();
    final PreciseObjectTimeSeries<T, BigDecimal> dts2 = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries2();
    final PreciseObjectTimeSeries<T, BigDecimal> ets = (PreciseObjectTimeSeries<T, BigDecimal>) createEmptyTimeSeries();
    assertEquals(ets, ets.intersectionFirstValue(dts));
    assertEquals(ets, dts.intersectionFirstValue(ets));

    final PreciseObjectTimeSeries<T, BigDecimal> result = dts.intersectionFirstValue(dts2);
    assertEquals(3, result.size());
    assertEquals(testValues()[3], result.getValueAtIndex(0));
    assertEquals(testValues()[4], result.getValueAtIndex(1));
    assertEquals(testValues()[5], result.getValueAtIndex(2));
    assertEquals(dts.getTimeAtIndex(3), result.getTimeAtIndex(0));
    assertEquals(dts.getTimeAtIndex(4), result.getTimeAtIndex(1));
    assertEquals(dts.getTimeAtIndex(5), result.getTimeAtIndex(2));
  }

  /**
   * Tests the no intersection operation.
   */
  @Test
  public void testNoIntersectionOperation() {
    final PreciseObjectTimeSeries<T, BigDecimal> dts = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries();
    final PreciseObjectTimeSeries<T, BigDecimal> dts2 = (PreciseObjectTimeSeries<T, BigDecimal>) createStandardTimeSeries2();
    final PreciseObjectTimeSeries<T, BigDecimal> ets = (PreciseObjectTimeSeries<T, BigDecimal>) createEmptyTimeSeries();
    assertEquals(dts, ets.noIntersectionOperation(dts));
    assertEquals(dts, dts.noIntersectionOperation(ets));
    try {
      dts.noIntersectionOperation(dts2);
      fail("Should have failed");
    } catch (final IllegalStateException ex) {
      //do nothing - expected exception because the two timeseries have overlapping dates which will require intersection operation
    }
    final PreciseObjectTimeSeries<T, BigDecimal> dts3 = dts2.subSeries(dts.getLatestTime(), false, dts2.getLatestTime(), false);
    final PreciseObjectTimeSeries<T, BigDecimal> noIntersecOp = dts.noIntersectionOperation(dts3);
    assertEquals(dts.getValueAtIndex(0), noIntersecOp.getValueAtIndex(0));
    assertEquals(dts.getValueAtIndex(1), noIntersecOp.getValueAtIndex(1));
    assertEquals(dts.getValueAtIndex(2), noIntersecOp.getValueAtIndex(2));
    assertEquals(dts.getValueAtIndex(3), noIntersecOp.getValueAtIndex(3));
    assertEquals(dts.getValueAtIndex(4), noIntersecOp.getValueAtIndex(4));
    assertEquals(dts.getValueAtIndex(5), noIntersecOp.getValueAtIndex(5));
    assertEquals(dts3.getValueAtIndex(0), noIntersecOp.getValueAtIndex(6));
    assertEquals(dts3.getValueAtIndex(1), noIntersecOp.getValueAtIndex(7));
  }

}
