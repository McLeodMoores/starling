/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesMasterPopulator {

  public static List<ExternalIdBundleWithDates> populateAndTestMaster(final HistoricalTimeSeriesMaster htsMaster,
      final int datasetSize, final String[] dataSources, final String[] dataProviders, final String[] dataFields, final String observationTime) {
    final List<ExternalIdBundleWithDates> result = new ArrayList<>();
    for (int i = 0; i < datasetSize; i++) {
      final ExternalIdBundle identifiers = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("ticker" + i), ExternalSchemes.bloombergBuidSecurityId("buid" + i));
      result.add(ExternalIdBundleWithDates.of(identifiers));

      final LocalDate start = DateUtils.previousWeekDay().minusDays(7);
      for (final String dataSource : dataSources) {
        for (final String dataProvider : dataProviders) {
          for (final String datafield : dataFields) {
            final ManageableHistoricalTimeSeriesInfo series = new ManageableHistoricalTimeSeriesInfo();
            series.setDataField(datafield);
            series.setDataProvider(dataProvider);
            series.setDataSource(dataSource);
            series.setObservationTime(observationTime);
            series.setExternalIdBundle(ExternalIdBundleWithDates.of(identifiers));
            final HistoricalTimeSeriesInfoDocument doc = htsMaster.add(new HistoricalTimeSeriesInfoDocument(series));
            assertNotNull(doc);
            assertNotNull(doc.getUniqueId());

            final LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          }
        }
      }
    }
    return result;
  }

}
