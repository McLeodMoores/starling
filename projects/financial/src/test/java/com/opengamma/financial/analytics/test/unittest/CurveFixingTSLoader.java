/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test.unittest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.impl.NonVersionedRedisHistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class CurveFixingTSLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(CurveFixingTSLoader.class);
  private final NonVersionedRedisHistoricalTimeSeriesSource _timeSeriesSource;

  public CurveFixingTSLoader(final NonVersionedRedisHistoricalTimeSeriesSource timeSeriesSource) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    _timeSeriesSource = timeSeriesSource;
  }

  /**
   * Gets the timeSeriesSource.
   * 
   * @return the timeSeriesSource
   */
  protected NonVersionedRedisHistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  public void loadCurveFixingCSVFile(final String fileName) {
    loadCurveFixingCSVFile(new File(fileName));
  }

  public void loadCurveFixingCSVFile(final File file) {
    LOGGER.info("Loading from file {}", file.getAbsolutePath());
    try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
      loadCurveFixingCSVFile(stream);
    } catch (final IOException ioe) {
      LOGGER.error("Unable to open file " + file, ioe);
      throw new OpenGammaRuntimeException("Unable to open file " + file, ioe);
    }
  }

  public void loadCurveFixingCSVFile(final InputStream stream) throws IOException {
    // The calling code is responsible for closing the underlying stream.
    @SuppressWarnings("resource")
    final
    // assume first line is the header
    CSVReader csvReader = new CSVReader(new InputStreamReader(stream), CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1);

    String[] currLine = null;
    int lineNum = 0;
    final Map<UniqueId, LocalDateDoubleTimeSeriesBuilder> timeseriesMap = Maps.newHashMap();
    while ((currLine = csvReader.readNext()) != null) {
      lineNum++;
      if (currLine.length == 0 || currLine[0].startsWith("#")) {
        LOGGER.debug("Empty line on {}", lineNum);
      } else if (currLine.length != 4) {
        LOGGER.error("Invalid number of fields ({}) in CSV on line {}", currLine.length, lineNum);
      } else {
        final String curveName = StringUtils.trimToNull(currLine[0]);
        if (curveName == null) {
          LOGGER.error("Invalid curve name in CSV on line {}", lineNum);
          continue;
        }
        final String tenor = StringUtils.trimToNull(currLine[1]);
        if (tenor == null) {
          LOGGER.error("Invalid tenor: {} in CSV on line {}", currLine[1], lineNum);
          continue;
        }
        final String dateStr = StringUtils.trimToNull(currLine[2]);
        LocalDate date = null;
        try {
          date = LocalDate.parse(dateStr);
        } catch (final DateTimeParseException ex) {
          LOGGER.error("Invalid date format in CSV on line {}", lineNum);
          continue;
        }
        final String valueStr = StringUtils.trimToNull(currLine[3]);
        Double value = null;
        try {
          value = Double.parseDouble(valueStr);
        } catch (final NumberFormatException ex) {
          LOGGER.error("Invalid amount in CSV on line {}", lineNum);
          continue;
        }
        final String idName = String.format("%s-%s", curveName, tenor);
        final UniqueId uniqueId = UniqueId.of(ExternalSchemes.ISDA.getName(), idName);

        LocalDateDoubleTimeSeriesBuilder tsBuilder = timeseriesMap.get(uniqueId);
        if (tsBuilder == null) {
          tsBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
          timeseriesMap.put(uniqueId, tsBuilder);
        }
        tsBuilder.put(date, value);
      }
    }
    LOGGER.info("Populating {} time series for fixing data", timeseriesMap.size());
    for (final Entry<UniqueId, LocalDateDoubleTimeSeriesBuilder> entry : timeseriesMap.entrySet()) {
      LOGGER.info("Fixing series {} has {} elements", entry.getKey(), entry.getValue().size());
      getTimeSeriesSource().updateTimeSeries(entry.getKey(), entry.getValue().build());
    }
  }
}
