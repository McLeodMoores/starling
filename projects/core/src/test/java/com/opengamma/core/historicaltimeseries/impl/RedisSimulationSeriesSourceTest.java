/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RedisSimulationSeriesSourceTest extends AbstractRedisTestCase {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisSimulationSeriesSourceTest.class);

  public void basicOperation() {
    final LocalDate simulationSeriesDate = LocalDate.of(2013, 4, 24);
    final RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(getJedisPool(), getRedisPrefix());
    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate);
    final UniqueId id = generateId(5);
    HistoricalTimeSeries hts = null;

    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNull(hts);

    final LocalDateDoubleTimeSeriesBuilder timeSeriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    for (int i = 1; i < 30; i++) {
      timeSeriesBuilder.put(LocalDate.of(2013, 4, i), i);
    }
    simulationSource.updateTimeSeries(id, simulationSeriesDate, timeSeriesBuilder.build());

    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNotNull(hts);
    assertEquals(id, hts.getUniqueId());
    assertEquals(29, hts.getTimeSeries().size());
    final LocalDateDoubleEntryIterator iterator = hts.getTimeSeries().iterator();
    int i = 1;
    while (iterator.hasNext()) {
      iterator.next();
      assertEquals(LocalDate.of(2013, 4, i), iterator.currentTime());
      assertEquals(i, iterator.currentValueFast(), 0.001);
      i++;
    }

    hts = simulationSource.getHistoricalTimeSeries(generateId(6), null, false, null, false);
    assertNull(hts);

    simulationSource.setCurrentSimulationExecutionDate(LocalDate.of(2013, 4, 25));
    hts = simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    assertNull(hts);

  }

  private static UniqueId generateId(final int x) {
    return UniqueId.of("TEST", Integer.toString(x));
  }

  public void clearSpecificDate() {
    final RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(getJedisPool(), getRedisPrefix());
    LocalDate simulationSeriesDate = LocalDate.now();
    HistoricalTimeSeries hts = null;

    final int numDaysHistory = 5;
    for (int i = 0; i < numDaysHistory; i++) {
      writeOneSimulationSeriesDate(simulationSource, simulationSeriesDate, 5);
      simulationSeriesDate = simulationSeriesDate.minusDays(1);
    }

    simulationSeriesDate = simulationSeriesDate.plusDays(1);
    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate);
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNotNull(hts);

    simulationSource.clearExecutionDate(simulationSeriesDate);
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNull(hts);

    simulationSource.setCurrentSimulationExecutionDate(simulationSeriesDate.plusDays(1));
    hts = simulationSource.getHistoricalTimeSeries(generateId(3), null, false, null, false);
    assertNotNull(hts);
  }

  @Test(enabled = false)
  public void largePerformanceTest() {
    final RedisSimulationSeriesSource simulationSource = new RedisSimulationSeriesSource(getJedisPool());
    LocalDate simulationSeriesDate = LocalDate.now();

    final int numDaysHistory = 20;
    for (int i = 0; i < numDaysHistory; i++) {
      // 20 points, 10 curves
      writeOneSimulationSeriesDate(simulationSource, simulationSeriesDate, 20 * 10);
      simulationSeriesDate = simulationSeriesDate.minusDays(1);
    }

    final OperationTimer timer = new OperationTimer(LOGGER, "Loading TS");
    final Random random = new Random();
    for (int i = 0; i < 1000; i++) {
      final LocalDate seriesDate = simulationSeriesDate.minusDays(random.nextInt(numDaysHistory));
      final UniqueId id = generateId(random.nextInt(1260));
      simulationSource.setCurrentSimulationExecutionDate(seriesDate);
      simulationSource.getHistoricalTimeSeries(id, null, false, null, false);
    }
    System.out.println("Loading 1000 TS took " + timer.finished());
  }

  private void writeOneSimulationSeriesDate(final RedisSimulationSeriesSource simulationSource, final LocalDate simulationSeriesDate, final int nSeries) {
    final OperationTimer timer = new OperationTimer(LOGGER, "Storing many time series");

    // 20 points, 10 curves
    for (int i = 0; i < nSeries; i++) {
      performanceWriteOneSeries(simulationSource, generateId(i), simulationSeriesDate);
    }

    System.out.println("Writing " + nSeries + " series took " + timer.finished());
  }

  private static void performanceWriteOneSeries(final RedisSimulationSeriesSource source, final UniqueId id, final LocalDate simulationSeriesDate) {
    LocalDate valueDate = LocalDate.now();
    final LocalDateDoubleTimeSeriesBuilder seriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    for (int i = 1; i <= 1260; i++) {
      seriesBuilder.put(valueDate, i);
      valueDate = valueDate.minusDays(1);
    }
    source.updateTimeSeries(id, simulationSeriesDate, seriesBuilder.build());
  }

}
