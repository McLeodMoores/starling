/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableZonedDateTimeDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final ZoneId LONDON = ZoneId.of("Europe/London");

  private ZonedDateTime[] _instants;
  private double[] _values;
  private ImmutableZonedDateTimeDoubleTimeSeries _ts;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _instants = new ZonedDateTime[] { ZonedDateTime.of(2012, 6, 30, 0, 0, 0, 0, LONDON), ZonedDateTime.of(2012, 7, 1, 0, 0, 0, 0, LONDON) };
    _values = new double[] { 1.1d, 2.2d };
    _ts = ImmutableZonedDateTimeDoubleTimeSeries.of(_instants, _values, LONDON);
  }

  /**
   *
   */
  public void testCycle1() {
    final DoubleTimeSeries<?> cycleObject1 = cycleObject(DoubleTimeSeries.class, _ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject1.getClass());
    assertEquals(_ts, cycleObject1);
  }

  /**
   *
   */
  public void testCycle2() {
    final PreciseDoubleTimeSeries<?> cycleObject2 = cycleObject(PreciseDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject2.getClass());
    assertEquals(_ts, cycleObject2);
  }

  /**
   *
   */
  public void testCycle3() {
    final ZonedDateTimeDoubleTimeSeries cycleObject3 = cycleObject(ZonedDateTimeDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject3.getClass());
    assertEquals(_ts, cycleObject3);
  }

  /**
   *
   */
  public void testCycle4() {
    final ImmutableZonedDateTimeDoubleTimeSeries cycleObject4 = cycleObject(ImmutableZonedDateTimeDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject4.getClass());
    assertEquals(_ts, cycleObject4);
  }

}
