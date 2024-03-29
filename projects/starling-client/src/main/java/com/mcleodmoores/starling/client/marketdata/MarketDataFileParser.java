/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A file parser for csv files that adds market data to a {@link MarketDataSet}. The available headers are:
 * <ul>
 *   <li> Date - the date of the market data point
 *   <li> Value - the value of the market data
 *   <li> Field - the market data field (e.g. last price, last volatility)
 *   <li> Source - the market data source (e.g. Bloomberg)
 *   <li> Provider - the market data provider (e.g. ICAP)
 *   <li> Normalizer - the normalizer to use on the market data
 *   <li> ExternalId - the id of the market data. There can multiple ids associated with a data point, as long as the scheme is different
 *   for each one (e.g. ExternalId[BLOOMBERG_TICKER], ExternalId[REUTERS]).
 * </ul>
 * If the csv file contains more than one (date, data point) pair for a particular id, then a time series is created in the
 * market data set. If the date of the market data point is not supplied, the data loading date is assumed to be the date
 * of the point.
 */
public class MarketDataFileParser {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataFileParser.class);
  /** The date column name */
  private static final String DATE_COLUMN_NAME = "Date".toUpperCase();
  /** The value column name */
  private static final String VALUE_COLUMN_NAME = "Value".toUpperCase();
  /** The field column name */
  private static final String FIELD_COLUMN_NAME = "Field".toUpperCase();
  /** The data source column name */
  private static final String SOURCE_COLUMN_NAME = "Source".toUpperCase();
  /** The data provider column name */
  private static final String PROVIDER_COLUMN_NAME = "Provider".toUpperCase();
  /** The normalizer column name */
  private static final String NORMALIZER_COLUMN_NAME = "Normalizer".toUpperCase();
  /** The ids column name */
  private static final String EXTERNAL_ID_COLUMN_PREFIX = "ExternalId".toUpperCase();
  /** Pattern for parsing multiple ids from the header e.g. ExternalId[BLOOMBERG_TICKER] */
  private static final Pattern PATTERN = Pattern.compile("^ExternalId\\[(.*?)\\]$", Pattern.CASE_INSENSITIVE);
  /** The date formatter */
  private final DateTimeFormatter _dateFormatter;
  /** The data loading date */
  private final LocalDate _date;

  /**
   * @param dateFormatter  the date formatter, not null
   * @param date  the data loading date, not null
   */
  public MarketDataFileParser(final DateTimeFormatter dateFormatter, final LocalDate date) {
    _dateFormatter = ArgumentChecker.notNull(dateFormatter, "dateFormatter");
    _date = ArgumentChecker.notNull(date, "date");
  }

  /**
   * Read from a CSV file into a market data set.
   * @param reader  a reader for the CSV file to read from
   * @return the market data set parsed
   */
  public MarketDataSet readFile(final Reader reader) {
    final MarketDataSet dataSet = MarketDataSet.empty();
    int lineNum = 0;
    try (CSVReader csvLoader = new CSVReader(new BufferedReader(reader))) {
      final String[] headerLine = csvLoader.readNext();
      final Map<String, Integer> headerMap = extractHeader(headerLine);
      lineNum++;
      String[] line;
      while ((line = csvLoader.readNext()) != null) {
        final MarketDataKey key = readMarketDataKey(line, headerMap, lineNum);
        addValue(dataSet, key, line, headerMap, lineNum);
        lineNum++;
      }
    } catch (final Exception e) {
      LOGGER.error("Error while loading CSV file", e);
      System.exit(1);
    }
    buildTimeSeries(dataSet);
    return dataSet;
  }

  /**
   * Adds a value to the set. If there are multiple values for the same id, or if there is a date that is not equal
   * to the loading date, then a time series is created. Otherwise, the data point is added to the set as a single
   * value.
   * If there is a problem when adding the point, the code will exit.
   * @param dataSet  the data set
   * @param key  the market data key
   * @param line  a line from the market data file
   * @param headerMap  the header map
   * @param lineNum  the line number
   */
  private void addValue(final MarketDataSet dataSet, final MarketDataKey key, final String[] line, final Map<String, Integer> headerMap, final int lineNum) {
    Double scalar = null;
    LocalDate date = null;
    if (headerMap.containsKey(VALUE_COLUMN_NAME)) {
      final String value = line[headerMap.get(VALUE_COLUMN_NAME)];
      if (value != null && !value.isEmpty()) {
        try {
          scalar = Double.parseDouble(value);
        } catch (final NumberFormatException nfe) {
          LOGGER.error("Couldn't parse value {} on line {}", value, lineNum);
          System.exit(1);
        }
      } else {
        LOGGER.error("Blank or missing Value field on line {}", lineNum);
        System.exit(1);
      }
    }
    if (headerMap.containsKey(DATE_COLUMN_NAME)) {
      final String dateStr = line[headerMap.get(DATE_COLUMN_NAME)];
      if (dateStr != null && !dateStr.isEmpty()) {
        try {
          date = LocalDate.parse(dateStr, _dateFormatter);
        } catch (final DateTimeParseException dtpe) {
          LOGGER.error("Could not parse date on line {}.  Currently using SHORT style for locale {}, "
              + "us --locale switch to change", lineNum, Locale.getDefault());
          System.exit(1);
        }
      }
    }
    final Object value = dataSet.get(key);
    if (date != null) {
      if (value == null) {
        // no existing value, but given a date, so assume it's part of a time series.
        dataSet.put(key, ImmutableLocalDateDoubleTimeSeries.builder().put(date, scalar));
      } else {
        // there's an existing value, see if it's a time series or a scalar double
        if (value instanceof LocalDateDoubleTimeSeriesBuilder) {
          // it's a time series (builder)
          final LocalDateDoubleTimeSeriesBuilder builder = (LocalDateDoubleTimeSeriesBuilder) value;
          // note this is a bit dangerous because we can't tell if it's already set in the builder
          // TODO: add get to builder.
          builder.put(date, scalar); // because it's a reference, we don't need to update the dataSet.
        } else if (value instanceof Double) {
          // it's a scalar, we might be able to upgrade it to a time series if the dates don't clash.
          final Double existingScalar = (Double) value;
          if (date.equals(_date)) {
            LOGGER.error("You put a value in key {} on date {} explicitly and implicitly (by leaving date blank) "
                + "on line {}", new String[] {key.toString(), date.format(_dateFormatter), Integer.toString(lineNum) });
            System.exit(1);
          } else {
            // in this case we found a scalar already registered against this key, but we've been asked to add another scalar on a different date,
            // so we convert the thing into a time series with the original scalar registered on the snapshot date and the new value/date too.
            dataSet.put(key, ImmutableLocalDateDoubleTimeSeries.builder().put(_date, existingScalar).put(date, scalar));
          }
        } else {
          LOGGER.error("Unrecognised type in data set {}", value.getClass());
          System.exit(1);
        }
      }
    } else {
      // we don't have a date
      if (value == null) {
        dataSet.put(key, scalar);
      } else {
        // there's already a time series there, so we can add it with an implicit date of the snapshot date.
        if (value instanceof LocalDateDoubleTimeSeriesBuilder) {
          final LocalDateDoubleTimeSeriesBuilder builder = (LocalDateDoubleTimeSeriesBuilder) value;
          // note this is a bit dangerous because we can't tell if it's already set in the builder
          // TODO: add get to builder.
          builder.put(_date, scalar);
        } else if (value instanceof Double) {
          LOGGER.error("There is already a value for key {} on line {}", key, lineNum);
          System.exit(1);
        }
      }
    }
  }

  /**
   * Builds a time series from the builder.
   * @param dataSet  the market data set
   */
  private static void buildTimeSeries(final MarketDataSet dataSet) {
    for (final MarketDataKey key : new HashSet<>(dataSet.keySet())) { // avoid concurrent exceptions...
      final Object data = dataSet.get(key);
      if (data instanceof LocalDateDoubleTimeSeriesBuilder) {
        final LocalDateDoubleTimeSeriesBuilder builder = (LocalDateDoubleTimeSeriesBuilder) data;
        dataSet.put(key, builder.build());
      }
    }
  }

  /**
   * Creates the market data key, filling in any values that were set in the file.
   * @param line  a line from the file
   * @param headerMap  the header map
   * @param lineNum  the line number
   * @return  the market data key
   */
  private static MarketDataKey readMarketDataKey(final String[] line, final Map<String, Integer> headerMap, final int lineNum) {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    if (headerMap.containsKey(FIELD_COLUMN_NAME)) {
      final String field = line[headerMap.get(FIELD_COLUMN_NAME)];
      if (field != null && !field.isEmpty()) {
        builder.field(DataField.of(field));
      }
    }
    if (headerMap.containsKey(SOURCE_COLUMN_NAME)) {
      final String source = line[headerMap.get(SOURCE_COLUMN_NAME)];
      if (source != null && !source.isEmpty()) {
        builder.source(DataSource.of(source));
      }
    }
    if (headerMap.containsKey(PROVIDER_COLUMN_NAME)) {
      final String provider = line[headerMap.get(PROVIDER_COLUMN_NAME)];
      if (provider != null && !provider.isEmpty()) {
        builder.provider(DataProvider.of(provider));
      }
    }
    if (headerMap.containsKey(NORMALIZER_COLUMN_NAME)) {
      final String normalizerName = line[headerMap.get(NORMALIZER_COLUMN_NAME)];
      if (normalizerName != null && !normalizerName.isEmpty()) {
        builder.normalizer(normalizerName);
      }
    }
    final Set<ExternalId> externalIds = new HashSet<>();

    for (final String fieldName : headerMap.keySet()) {
      final String value = line[headerMap.get(fieldName)];

      if (value != null && !value.isEmpty()) {
        if (fieldName.toUpperCase().startsWith(EXTERNAL_ID_COLUMN_PREFIX)) {
          final Matcher matcher = PATTERN.matcher(fieldName);
          if (matcher.matches()) {
            final String scheme = matcher.group(1);
            if (!scheme.isEmpty()) {
              externalIds.add(ExternalId.of(scheme, value));
            } else {
              LOGGER.error("Scheme in ExternalId[] column header cannot be the empty string");
              System.exit(1);
            }
          } else {
            try {
              externalIds.add(ExternalId.parse(value));
            } catch (final IllegalArgumentException iae) {
              LOGGER.error(
                  "Couldn't parse External ID on line {}: If column header ExternalId doesn't contain a scheme "
                      + "(e.g. ExternalId[MY_SCHEME]) then each id must be of form MY_SCHEME~ID_VALUE",
                  lineNum);
              System.exit(1);
            }
          }
        }
      }
    }
    if (externalIds.isEmpty()) {
      LOGGER.error("No ExternalIds on line {}, need at least one", lineNum);
      System.exit(1);
    }
    final ExternalIdBundle bundle = ExternalIdBundle.of(externalIds);
    builder.externalIdBundle(bundle);
    try {
      return builder.build();
    } catch (final Exception e) {
      LOGGER.error("Cound't create market data key for {} on line {}", Arrays.asList(line), lineNum);
      System.exit(1);
      throw new OpenGammaRuntimeException("System.exit(1) should have been called");
    }
  }

  /**
   * Extracts the header row from the file to a map.
   * @param headerRow  the header row.
   * @return  the headers
   */
  private static Map<String, Integer> extractHeader(final String[] headerRow) {
    final Map<String, Integer> columnHeaders = new HashMap<>();
    for (int i = 0; i < headerRow.length; i++) {
      final String columnName = headerRow[i];
      if (!columnName.isEmpty()) {
        if (!columnHeaders.containsKey(columnName)) {
          columnHeaders.put(columnName.toUpperCase(), i);
        } else {
          LOGGER.error("Column header contains duplicate column label");
          System.exit(1);
        }
      } else {
        LOGGER.error("Column {} does not have a non-empty header", i);
        System.exit(1);
      }
    }
    return columnHeaders;
  }
}
