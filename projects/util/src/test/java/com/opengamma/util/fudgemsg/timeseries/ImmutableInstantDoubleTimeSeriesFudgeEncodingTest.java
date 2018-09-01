/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.InstantDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableInstantDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private Instant[] _instants;
  private double[] _values;
  private ImmutableInstantDoubleTimeSeries _ts;

  @BeforeMethod
  public void setUp() {
    _instants = new Instant[] {Instant.ofEpochSecond(30), Instant.ofEpochSecond(31)};
    _values = new double[] {1.1d, 2.2d};
    _ts = ImmutableInstantDoubleTimeSeries.of(_instants, _values);
  }

  public void testCycle1() {
    DoubleTimeSeries<?> cycleObject = cycleObject(DoubleTimeSeries.class, _ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
  }

  public void testCycle2() {
    PreciseDoubleTimeSeries<?> cycleObject = cycleObject(PreciseDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
  }

  public void testCycle3() {
    InstantDoubleTimeSeries cycleObject = cycleObject(InstantDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
  }

  public void testCycle4() {
    ImmutableInstantDoubleTimeSeries cycleObject = cycleObject(ImmutableInstantDoubleTimeSeries.class, _ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(_ts, cycleObject);
  }

}
