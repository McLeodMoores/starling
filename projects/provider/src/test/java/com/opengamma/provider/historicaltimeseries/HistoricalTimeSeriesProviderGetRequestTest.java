/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.historicaltimeseries;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

/**
 * Tests for {@link HistoricalTimeSeriesProviderGetRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesProviderGetRequestTest extends AbstractBeanTestCase {
  private static final Set<ExternalIdBundle> IDS = new HashSet<>(Arrays.asList(ExternalIdBundle.of("hts", "1"), ExternalIdBundle.of("hts", "2")));
  private static final String DATA_SOURCE = "source";
  private static final String DATA_PROVIDER = "provider";
  private static final String DATA_FIELD = "field";
  private static final LocalDateRange DATE_RANGE = LocalDateRange.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1), true);
  private static final Integer MAX_POINTS = 100;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(HistoricalTimeSeriesProviderGetRequest.class,
        Arrays.asList("externalIdBundles", "dataSource", "dataProvider", "dataField", "dateRange", "maxPoints"),
        Arrays.asList(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, DATE_RANGE, MAX_POINTS),
        Arrays.asList(Collections.singleton(ExternalIdBundle.of("hts", "1")), DATA_PROVIDER, DATA_FIELD, DATA_SOURCE, LocalDateRange.ALL, MAX_POINTS + 100));
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidBundle1() {
    HistoricalTimeSeriesProviderGetRequest.createGet(null, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD);
  }

  /**
   * Tests that the data source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataSource1() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), null, DATA_PROVIDER, DATA_FIELD);
  }

  /**
   * Tests that the data provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataProvider1() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), DATA_SOURCE, null, DATA_FIELD);
  }

  /**
   * Tests that the data field cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataField1() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), DATA_SOURCE, DATA_PROVIDER, null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidBundle2() {
    HistoricalTimeSeriesProviderGetRequest.createGet(null, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataSource2() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), null, DATA_PROVIDER, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataProvider2() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), DATA_SOURCE, null, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data field cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataField2() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), DATA_SOURCE, DATA_PROVIDER, null, DATE_RANGE);
  }

  /**
   * Tests that the date range cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateRange1() {
    HistoricalTimeSeriesProviderGetRequest.createGet(IDS.iterator().next(), DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidBundle3() {
    HistoricalTimeSeriesProviderGetRequest.createGetLatest(null, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD);
  }

  /**
   * Tests that the data source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataSource3() {
    HistoricalTimeSeriesProviderGetRequest.createGetLatest(IDS.iterator().next(), null, DATA_PROVIDER, DATA_FIELD);
  }

  /**
   * Tests that the data provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataProvider3() {
    HistoricalTimeSeriesProviderGetRequest.createGetLatest(IDS.iterator().next(), DATA_SOURCE, null, DATA_FIELD);
  }

  /**
   * Tests that the data field cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataField3() {
    HistoricalTimeSeriesProviderGetRequest.createGetLatest(IDS.iterator().next(), DATA_SOURCE, DATA_PROVIDER, null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidBundle4() {
    HistoricalTimeSeriesProviderGetRequest.createGetBulk(null, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataSource4() {
    HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, null, DATA_PROVIDER, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataProvider4() {
    HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, null, DATA_FIELD, DATE_RANGE);
  }

  /**
   * Tests that the data field cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataField4() {
    HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, null, DATE_RANGE);
  }

  /**
   * Tests that the date range cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateRange2() {
    HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, null);
  }

  /**
   * Tests static constructor equivalence.
   */
  public void testStaticConstructorCreateGet() {
    final ExternalIdBundle id = IDS.iterator().next();
    final HistoricalTimeSeriesProviderGetRequest request1 = HistoricalTimeSeriesProviderGetRequest.createGet(id, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD);
    final HistoricalTimeSeriesProviderGetRequest request2 = HistoricalTimeSeriesProviderGetRequest.createGet(id, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        LocalDateRange.ALL);
    assertEquals(request1, request2);
  }

  /**
   * Tests that the maximum number of points is set correctly.
   */
  public void testMaxPoints() {
    final ExternalIdBundle id = IDS.iterator().next();
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetLatest(id, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD);
    assertEquals(request.getDataField(), DATA_FIELD);
    assertEquals(request.getDataProvider(), DATA_PROVIDER);
    assertEquals(request.getDataSource(), DATA_SOURCE);
    assertEquals(request.getDateRange(), LocalDateRange.ALL);
    assertEquals(request.getMaxPoints(), Integer.valueOf(-1));
  }

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalId() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    request.addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that ids are added.
   */
  public void testAddExternalId() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    final ExternalId id = ExternalId.of("hts", "10");
    request.addExternalIds(id);
    final Set<ExternalIdBundle> expected = new HashSet<>(IDS);
    expected.add(id.toBundle());
    assertEqualsNoOrder(request.getExternalIdBundles(), expected);
  }

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBundle() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    request.addExternalIds((ExternalIdBundle) null);
  }

  /**
   * Tests that ids are added.
   */
  public void testAddExternalIdBundle() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    final ExternalIdBundle id = ExternalIdBundle.of("hts", "10");
    request.addExternalIds(id);
    final Set<ExternalIdBundle> expected = new HashSet<>(IDS);
    expected.add(id.toBundle());
    assertEqualsNoOrder(request.getExternalIdBundles(), expected);
  }

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBundleIterable() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    request.addExternalIds((Iterable<ExternalIdBundle>) null);
  }

  /**
   * Tests that ids are added.
   */
  public void testAddExternalIdBundleIterable() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(IDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD,
        DATE_RANGE);
    final ExternalIdBundle id = ExternalIdBundle.of("hts", "10");
    request.addExternalIds(Collections.singleton(id));
    final Set<ExternalIdBundle> expected = new HashSet<>(IDS);
    expected.add(id.toBundle());
    assertEqualsNoOrder(request.getExternalIdBundles(), expected);
  }
}
