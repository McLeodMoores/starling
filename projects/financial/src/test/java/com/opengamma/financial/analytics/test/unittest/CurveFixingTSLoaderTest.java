/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test.unittest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.NonVersionedRedisHistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class CurveFixingTSLoaderTest extends AbstractRedisTestCase {

  public void testOperation() {
    final NonVersionedRedisHistoricalTimeSeriesSource source = new NonVersionedRedisHistoricalTimeSeriesSource(getJedisPool(), getRedisPrefix());
    final CurveFixingTSLoader loader = new CurveFixingTSLoader(source);
    loader.loadCurveFixingCSVFile("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");

    final HistoricalTimeSeries historicalTimeSeries = source.getHistoricalTimeSeries(UniqueId.of(ExternalSchemes.ISDA.getName(), "CHF-LIBOR-BBA-6M"));
    assertNotNull(historicalTimeSeries);
    final LocalDateDoubleTimeSeries timeSeries = historicalTimeSeries.getTimeSeries();
    assertNotNull(timeSeries);
    assertEquals(5996, timeSeries.size());
  }

}
