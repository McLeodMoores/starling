/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.historical;

import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Generates random historical time series data.
 */
public class SimulatedHistoricalDataGenerator extends SimulatedHistoricalData {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedHistoricalDataGenerator.class);

  /** Master for writing the generated time series data. */
  private final HistoricalTimeSeriesMaster _htsMaster;

  /** Default length of time series to load in months (2.5 years). */
  private static final int TS_LENGTH = 30;

  /** Length of time series to load in months. */
  private final int _timeSeriesLengthMonths;

  /**
   * @param timeSeriesMaster master for writing the generated time series data
   */
  public SimulatedHistoricalDataGenerator(final HistoricalTimeSeriesMaster timeSeriesMaster) {
    this(timeSeriesMaster, TS_LENGTH);
  }

  /**
   * @param timeSeriesMaster master for writing the generated time series data
   * @param timeSeriesLengthMonths length in months of the time series
   */
  public SimulatedHistoricalDataGenerator(final HistoricalTimeSeriesMaster timeSeriesMaster, final int timeSeriesLengthMonths) {
    _htsMaster = ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    _timeSeriesLengthMonths = ArgumentChecker.notNegativeOrZero(timeSeriesLengthMonths, "timeSeriesLengthMonths");
  }

  public void run() {
    final Random random = new Random(); // no need for SecureRandom here..
    final StringBuilder buf = new StringBuilder("loading ").append(getFinishValues().size()).append(" timeseries");
    for (final Entry<Pair<ExternalId, String>, Double> entry : getFinishValues().entrySet()) {
      final ExternalId identifier = entry.getKey().getFirst();
      final String dataField = entry.getKey().getSecond();
      final Double finishValue = entry.getValue();
      final ManageableHistoricalTimeSeriesInfo info = getSimulatedTimeSeriesInfo(dataField, identifier);
      buf.append("\t").append(identifier).append(" ").append(dataField).append("\n");
      final HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      final LocalDateDoubleTimeSeries timeSeries = getHistoricalDataPoints(random, finishValue, _timeSeriesLengthMonths);
      _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
    LOGGER.info(buf.toString());
  }

  private static ManageableHistoricalTimeSeriesInfo getSimulatedTimeSeriesInfo(final String dataField, final ExternalId identifier) {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName(dataField + " " + identifier);
    info.setDataField(dataField);
    info.setDataSource(OG_DATA_SOURCE);
    info.setDataProvider(OG_DATA_PROVIDER);
    info.setObservationTime(HistoricalTimeSeriesConstants.LONDON_CLOSE);
    final ExternalIdWithDates id = ExternalIdWithDates.of(identifier, null, null);
    final ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    return info;
  }

  private static LocalDateDoubleTimeSeries getHistoricalDataPoints(final Random random, final Double finishValue, final int tsLength) {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate now = LocalDate.now();
    final LocalDate stopDate = DateUtils.previousWeekDay(now.minusMonths(tsLength));
    double currentValue = finishValue;
    do {
      currentValue = wiggleValue(random, currentValue, finishValue);
      bld.put(now, currentValue);
      now = DateUtils.previousWeekDay(now);
    } while (now.isAfter(stopDate));
    return bld.build();
  }

}

