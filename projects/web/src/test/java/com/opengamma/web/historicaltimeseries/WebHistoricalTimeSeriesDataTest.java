/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.historicaltimeseries;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebHistoricalTimeSeriesData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebHistoricalTimeSeriesDataTest extends AbstractBeanTestCase {
  private static final String NAME = "historical time series";
  private static final String HTS_URI = "hts";
  private static final ManageableHistoricalTimeSeries HTS = new ManageableHistoricalTimeSeries();
  private static final ManageableHistoricalTimeSeriesInfo INFO = new ManageableHistoricalTimeSeriesInfo();
  private static final HistoricalTimeSeriesInfoDocument DOCUMENT = new HistoricalTimeSeriesInfoDocument();
  static {
    HTS.setTimeSeries(ImmutableLocalDateDoubleTimeSeries.of(LocalDate.now(), 0.01));
    INFO.setName(NAME);
    DOCUMENT.setInfo(INFO);
  }
  private static final WebHistoricalTimeSeriesData DATA = new WebHistoricalTimeSeriesData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("hts", "0"));
    DATA.setInfo(DOCUMENT);
    DATA.setUriHistoricalTimeSeriesId(HTS_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebHistoricalTimeSeriesData.class, Arrays.asList("uriHistoricalTimeSeriesId", "info", "timeSeries"),
        Arrays.asList(HTS_URI, DOCUMENT, HTS), Arrays.asList("otherUri", new HistoricalTimeSeriesInfoDocument(), new ManageableHistoricalTimeSeries()));
  }

  /**
   * Tests getting the best time series if the override id is not null.
   */
  public void testBestHistoricalTimeSeriesOverrideId() {
    final UniqueId uid = UniqueId.of("hts", "1");
    assertEquals(DATA.getBestHistoricalTimeSeriesUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best time series if there is no snapshot document.
   */
  public void testBestHistoricalTimeSeriesNoHistoricalTimeSeriesDocument() {
    final WebHistoricalTimeSeriesData data = DATA.clone();
    data.setInfo(null);
    assertEquals(data.getBestHistoricalTimeSeriesUriId(null), HTS_URI);
  }

  /**
   * Tests getting the best time series from the document.
   */
  public void testBestHistoricalTimeSeriesFromDocument() {
    assertEquals(DATA.getBestHistoricalTimeSeriesUriId(null), DOCUMENT.getUniqueId().toString());
  }

  /**
   * Does not deserialize the time series correctly.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
  }

}
