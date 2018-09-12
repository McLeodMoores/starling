/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableLocalDateDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private LocalDate[] _dates;
  private double[] _values;
  private ImmutableLocalDateDoubleTimeSeries _ts;
  private ImmutableLocalDateDoubleTimeSeries _empty;

  @BeforeMethod
  public void setUp() {
    _dates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1) };
    _values = new double[] {1.1d, 2.2d };
    _ts = ImmutableLocalDateDoubleTimeSeries.of(_dates, _values);
    _empty = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
  }

  public void testCycle1() {
    DoubleTimeSeries<?> cycleObject = cycleObject(DoubleTimeSeries.class, _ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
    cycleObject = cycleObject(DoubleTimeSeries.class, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, cycleObject);
  }

  public void testCycle2() {
    DateDoubleTimeSeries<?> cycleObject = cycleObject(DateDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
    cycleObject = cycleObject(DateDoubleTimeSeries.class, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, cycleObject);
  }

  public void testCycle3() {
    LocalDateDoubleTimeSeries cycleObject = cycleObject(LocalDateDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
    cycleObject = cycleObject(LocalDateDoubleTimeSeries.class, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, cycleObject);
  }

  public void testCycle4() {
    ImmutableLocalDateDoubleTimeSeries cycleObject = cycleObject(ImmutableLocalDateDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
    cycleObject = cycleObject(ImmutableLocalDateDoubleTimeSeries.class, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES, cycleObject);
  }
  
  public void testEmptyCycle1() {
    DoubleTimeSeries<?> cycleObject = cycleObject(DoubleTimeSeries.class, _empty);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_empty, cycleObject);
  }

  public void testEmptyCycle2() {
    DateDoubleTimeSeries<?> cycleObject = cycleObject(DateDoubleTimeSeries.class, _empty);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_empty, cycleObject);
  }

  public void testEmptyCycle3() {
    LocalDateDoubleTimeSeries cycleObject = cycleObject(LocalDateDoubleTimeSeries.class, _empty);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_empty, cycleObject);
  }

  public void testEmptyCycle4() {
    ImmutableLocalDateDoubleTimeSeries cycleObject = cycleObject(ImmutableLocalDateDoubleTimeSeries.class, _empty);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_empty, cycleObject);
  }

}
