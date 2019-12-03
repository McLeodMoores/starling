/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesGetFilter}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesGetFilterTest extends AbstractFudgeBuilderTestCase {
  private static final LocalDate START = LocalDate.of(2016, 1, 1);
  private static final LocalDate END = LocalDate.of(2019, 1, 1);
  private static final Integer MAX_POINTS = 1000;

  /**
   * Tests the all points filter.
   */
  @Test
  public void testOfAll() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofAll();
    assertNull(filter.getEarliestDate());
    assertNull(filter.getLatestDate());
    assertNull(filter.getMaxPoints());
  }

  /**
   * Tests the latest point filter.
   */
  @Test
  public void testOfLatestPoint() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofLatestPoint();
    assertNull(filter.getEarliestDate());
    assertNull(filter.getLatestDate());
    assertEquals(filter.getMaxPoints(), Integer.valueOf(-1));
  }

  /**
   * Tests the latest point filter.
   */
  @Test
  public void testOfLatestPointWithPeriod() {
    final Period period = Period.ofMonths(19);
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofLatestPoint(period);
    assertEquals(filter.getEarliestDate(), LocalDate.now().minus(period));
    assertEquals(filter.getLatestDate(), LocalDate.now());
    assertEquals(filter.getMaxPoints(), Integer.valueOf(-1));
  }

  /**
   * Tests the latest point filter.
   */
  @Test
  public void testLatestPointInRange() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofLatestPoint(START, END);
    assertEquals(filter.getEarliestDate(), START);
    assertEquals(filter.getLatestDate(), END);
    assertEquals(filter.getMaxPoints(), Integer.valueOf(-1));
  }

  /**
   * Tests the earliest point filter.
   */
  @Test
  public void testOfEarliestPoint() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofEarliestPoint();
    assertNull(filter.getEarliestDate());
    assertNull(filter.getLatestDate());
    assertEquals(filter.getMaxPoints(), Integer.valueOf(1));
  }

  /**
   * Tests a date range filter.
   */
  @Test
  public void testDateRange() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(START, END);
    assertEquals(filter.getEarliestDate(), START);
    assertEquals(filter.getLatestDate(), END);
    assertNull(filter.getMaxPoints());
  }

  /**
   * Tests a date range filter.
   */
  @Test
  public void testDateRangeMaxPoints() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(START, END, MAX_POINTS);
    assertEquals(filter.getEarliestDate(), START);
    assertEquals(filter.getLatestDate(), END);
    assertEquals(filter.getMaxPoints(), MAX_POINTS);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(START, END, MAX_POINTS);
    assertEquals(filter, filter);
    assertEquals(filter.toString(), "HistoricalTimeSeriesGetFilter{earliestDate=2016-01-01, latestDate=2019-01-01, maxPoints=1000}");
    final HistoricalTimeSeriesGetFilter other = HistoricalTimeSeriesGetFilter.ofRange(START, END, MAX_POINTS);
    assertEquals(filter, other);
    assertEquals(filter.hashCode(), other.hashCode());
    other.setEarliestDate(END);
    assertNotEquals(filter, other);
    other.setEarliestDate(START);
    other.setLatestDate(START);
    assertNotEquals(filter, other);
    other.setLatestDate(END);
    other.setMaxPoints(MAX_POINTS + 1);
    assertNotEquals(filter, other);
  }

  /**
   * Test the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(START, END, MAX_POINTS);
    assertEquals(filter.propertyNames().size(), 3);
    final Meta bean = filter.metaBean();
    assertEquals(bean.earliestDate().get(filter), START);
    assertEquals(bean.latestDate().get(filter), END);
    assertEquals(bean.maxPoints().get(filter), MAX_POINTS);
    assertEquals(filter.property("earliestDate").get(), START);
    assertEquals(filter.property("latestDate").get(), END);
    assertEquals(filter.property("maxPoints").get(), MAX_POINTS);
  }

  /**
   * Test a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(START, END, MAX_POINTS);
    assertEncodeDecodeCycle(HistoricalTimeSeriesGetFilter.class, filter);
  }
}
