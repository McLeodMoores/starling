/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test SimpleHistoricalTimeSeries Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleHistoricalTimeSeriesFudgeEncodingTest {

  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final double[] VALUES = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
  private static final LocalDate[] TIMES = testTimes();
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycling() {
    final SimpleHistoricalTimeSeries simpleHistoricalTimeSeries = new SimpleHistoricalTimeSeries(UID,
        ImmutableLocalDateDoubleTimeSeries.of(TIMES, VALUES));
    cycleTimeSeries(simpleHistoricalTimeSeries);
  }

  private void cycleTimeSeries(final SimpleHistoricalTimeSeries original) {
    final FudgeMsgEnvelope msgEnvelope = _fudgeContext.toFudgeMsg(original);
    assertNotNull(msgEnvelope);
    final SimpleHistoricalTimeSeries fromFudgeMsg = _fudgeContext.fromFudgeMsg(SimpleHistoricalTimeSeries.class, msgEnvelope.getMessage());
    assertTrue(original != fromFudgeMsg);
    assertEquals(original, fromFudgeMsg);
  }


  private static LocalDate[] testTimes() {
    final LocalDate one = LocalDate.of(2010, Month.FEBRUARY, 8);
    final LocalDate two = LocalDate.of(2010, Month.FEBRUARY, 9);
    final LocalDate three = LocalDate.of(2010, Month.FEBRUARY, 10);
    final LocalDate four = LocalDate.of(2010, Month.FEBRUARY, 11);
    final LocalDate five = LocalDate.of(2010, Month.FEBRUARY, 12);
    final LocalDate six = LocalDate.of(2010, Month.FEBRUARY, 13);
    return new LocalDate[] {one, two, three, four, five, six };
  }

}
