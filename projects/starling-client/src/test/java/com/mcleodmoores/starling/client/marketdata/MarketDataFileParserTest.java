/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link MarketDataFileParser}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataFileParserTest {
  /** The date formatter to use */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  /** The data loading date */
  private static final LocalDate LOADING_DATE = LocalDate.now();
  /** The parser */
  private static final MarketDataFileParser PARSER = new MarketDataFileParser(DATE_FORMATTER, LOADING_DATE);

  /**
   * Tests the behaviour when the date formatter is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateFormatter() {
    new MarketDataFileParser(null, LOADING_DATE);
  }

  /**
   * Tests the behaviour when the loading date is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLoadingDate() {
    new MarketDataFileParser(DATE_FORMATTER, null);
  }

  /**
   * Tests that single data points with different ids are added as individual data points to the data set.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testParseSingleDataPoints() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("single-market-data-points-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 5);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 100.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID2"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID3"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 300.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID4"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 400.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID5"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 500.);
  }

  /**
   * Tests that dated single data points are added to a time series.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testParseSingleDataPointsWithDate() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("single-market-data-points-with-date-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 5);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2016, 1, 1), 100.).build());
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID2"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2016, 1, 1), 200.).build());
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID3"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2016, 1, 1), 300.).build());
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID4"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2016, 1, 1), 400.).build());
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID5"))
        .field(DataField.of("Market_Value")).source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), ImmutableLocalDateDoubleTimeSeries.builder().put(LocalDate.of(2016, 1, 1), 500.).build());
  }

  /**
   * Tests that multiple data field values can be used for the same id.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testParseMultipleFieldValues() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("multiple-market-data-fields-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 2);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value1"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 100.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value2"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
  }

  /**
   * Tests that multiple data sources can be used for the same id and provider.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testParseMultipleDataSources() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("multiple-market-data-sources-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 2);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE1"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 100.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE2"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
  }

  /**
   * Tests that multiple data providers can be used for the same id and source.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testParseMultipleDataProviders() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("multiple-market-data-providers-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 2);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.DEFAULT)
        .provider(DataProvider.of("TEST_PROVIDER1"))
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 100.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.DEFAULT)
        .provider(DataProvider.of("TEST_PROVIDER2"))
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
  }

  /**
   * Tests that multiple data points with the same market data fields are added to a time series.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testMultipleDataPoints() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("multiple-market-data-points-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 1);
    final LocalDateDoubleTimeSeries expectedTs = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2016, 1, 1), 100.)
        .put(LocalDate.of(2016, 1, 2), 101.)
        .put(LocalDate.of(2016, 1, 3), 102.)
        .put(LocalDate.of(2016, 1, 4), 103.)
        .put(LocalDate.of(2016, 1, 5), 104.)
        .build();
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), expectedTs);
  }

  /**
   * Tests that time series and single data points can be read from the same file.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testMixedDataPoints() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("mixed-market-data-points-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 3);
    final LocalDateDoubleTimeSeries expectedTs1 = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2016, 1, 1), 100.)
        .put(LocalDate.of(2016, 1, 2), 101.)
        .put(LocalDate.of(2016, 1, 3), 102.)
        .put(LocalDate.of(2016, 1, 4), 103.)
        .put(LocalDate.of(2016, 1, 5), 104.)
        .build();
    final LocalDateDoubleTimeSeries expectedTs3 = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2016, 1, 1), 300.)
        .build();
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), expectedTs1);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID2"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID3"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), expectedTs3);
  }

  /**
   * Tests that a time series is created from multiple values for the same id.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testConvertDoubleToTimeSeries() throws URISyntaxException, FileNotFoundException {
    URL url = getClass().getResource("multiple-market-data-points-as-time-series-example-1.csv");
    File inputFile = new File(url.toURI());
    FileReader reader = new FileReader(inputFile);
    MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 1);
    LocalDateDoubleTimeSeries expectedTs = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2016, 1, 1), 100.)
        .put(LOADING_DATE, 101.)
        .build();
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), expectedTs);
    url = getClass().getResource("multiple-market-data-points-as-time-series-example-2.csv");
    inputFile = new File(url.toURI());
    reader = new FileReader(inputFile);
    dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 1);
    expectedTs = ImmutableLocalDateDoubleTimeSeries.builder()
        .put(LocalDate.of(2016, 1, 1), 100.)
        .put(LocalDate.of(2016, 2, 1), 101.)
        .put(LOADING_DATE, 102.)
        .build();
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), expectedTs);
  }

  /**
   * Tests that the normalizer is read from the file.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testNormalization() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("single-market-data-points-with-normalizer-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 2);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.DEFAULT)
        .provider(DataProvider.DEFAULT)
        .normalizer(Div100Normalizer.INSTANCE.getName())
        .build()), 100.);
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(ExternalIdBundle.of(ExternalScheme.of("TEST"), "ID1"))
        .field(DataField.of("Market_Value"))
        .source(DataSource.DEFAULT)
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 200.);
  }

  /**
   * Tests that multiple ids for a row are combined into a single bundle.
   * @throws URISyntaxException  if the resource could not be found
   * @throws FileNotFoundException  if the file could not be found
   */
  @Test
  public void testIdBundle() throws URISyntaxException, FileNotFoundException {
    final URL url = getClass().getResource("single-market-data-points-external-id-example.csv");
    final File inputFile = new File(url.toURI());
    final FileReader reader = new FileReader(inputFile);
    final MarketDataSet dataSet = PARSER.readFile(reader);
    assertEquals(dataSet.size(), 1);
    final ExternalIdBundle expectedIdBundle = ExternalIdBundle.of(ExternalId.of("TEST1", "ID1"), ExternalId.of("TEST2", "ID2"));
    assertEquals(dataSet.get(MarketDataKey.builder()
        .externalIdBundle(expectedIdBundle)
        .field(DataField.of("Market_Value"))
        .source(DataSource.of("TEST_SOURCE"))
        .provider(DataProvider.DEFAULT)
        .normalizer(UnitNormalizer.INSTANCE.getName())
        .build()), 100.);
  }
}
