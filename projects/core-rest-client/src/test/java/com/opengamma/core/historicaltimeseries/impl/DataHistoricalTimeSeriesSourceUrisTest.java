/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriExternalIdBundleGet;
import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriGet;
import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriSearchBulk;
import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriSearchBulkData;
import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriSearchResolve;
import static com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceUris.uriSearchSingle;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataHistoricalTimeSeriesSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataHistoricalTimeSeriesSourceUrisTest {
  private static final UniqueId UID = UniqueId.of("hts", "1");
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "%1"));
  private static final LocalDate START = LocalDate.of(2018, 1, 1);
  private static final LocalDate END = LocalDate.of(2020, 1, 1);
  private static final LocalDate IDENTIFIER_VALIDITY_DATE = LocalDate.of(2020, 12, 1);
  private static final boolean INCLUDE_START = true;
  private static final boolean INCLUDE_END = false;
  private static final Integer MAX_POINTS = 100;
  private static final String DATA_SOURCE = "dataSource";
  private static final String DATA_PROVIDER = "dataProvider";
  private static final String DATA_FIELD = "dataField";
  private static final String RESOLUTION_KEY = "resolutionKey";
  private URI _baseUri;

  /**
   * Sets up the URI
   *
   * @throws URISyntaxException
   *           if the path is wrong
   */
  @BeforeMethod
  public void createUri() throws URISyntaxException {
    _baseUri = new URI("path/to/");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri1() {
    uriGet(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid1() {
    uriGet(_baseUri, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidNoVersion1() {
    final URI uri = uriGet(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidVersion1() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriGet(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriExternalIdBundleGetNullBaseUri() {
    uriExternalIdBundleGet(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriExternalIdBundleGetNullUid() {
    uriExternalIdBundleGet(_baseUri, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriExternalIdBundleGetNoVersion() {
    final URI uri = uriExternalIdBundleGet(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/htsMeta/externalIdBundle/" + UID);
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriExternalIdBundleGetVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriExternalIdBundleGet(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/htsMeta/externalIdBundle/" + UID);
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri2() {
    uriGet(null, UID, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests that the unique identifier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid2() {
    uriGet(_baseUri, null, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidNoVersion2() {
    final URI uri = uriGet(_baseUri, UID, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidVersion2() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriGet(_baseUri, uid, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetStartDate1() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriGet(_baseUri, uid, START, INCLUDE_END, null, false, null);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST&start=2018-01-01&includeStart=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetEndDate1() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriGet(_baseUri, uid, START, INCLUDE_END, END, INCLUDE_END, null);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST&start=2018-01-01&includeStart=false&end=2020-01-01&includeEnd=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetMaxPoints() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = uriGet(_baseUri, uid, START, INCLUDE_END, END, INCLUDE_END, MAX_POINTS);
    assertEquals(uri.getPath(), "path/to/hts/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST&start=2018-01-01&includeStart=false&end=2020-01-01&includeEnd=false&maxPoints=100");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullBaseUri1() {
    uriSearchSingle(null, EIDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullEids1() {
    uriSearchSingle(_baseUri, null, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, false, END, true, MAX_POINTS);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingle1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, null, null, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataSource1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, null, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataSource=dataSource");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataProvider1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, DATA_PROVIDER, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataSource=dataSource&dataProvider=dataProvider");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataField1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataSource=dataSource&dataProvider=dataProvider&dataField=dataField");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleStart1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataSource=dataSource&dataProvider=dataProvider&dataField=dataField&start=2018-01-01&includeStart=true");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleEnd1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&dataSource=dataSource&dataProvider=dataProvider&dataField=dataField&"
            + "start=2018-01-01&includeStart=true&end=2020-01-01&includeEnd=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleMaxPoints1() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&dataSource=dataSource&dataProvider=dataProvider&dataField=dataField&"
            + "start=2018-01-01&includeStart=true&end=2020-01-01&includeEnd=false&maxPoints=100");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullBaseUri2() {
    uriSearchSingle(null, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests that the id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullEids2() {
    uriSearchSingle(_baseUri, null, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, false, END, true, MAX_POINTS);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingle2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, null, null, null, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=ALL");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleValidityDate() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, null, null, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataSource2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, null, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataProvider2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, null, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource&dataProvider=dataProvider");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleDataField2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, null, true, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource&dataProvider=dataProvider&dataField=dataField");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleStart2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, null, true, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource&dataProvider=dataProvider"
            + "&dataField=dataField&start=2018-01-01&includeStart=true");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleEnd2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END,
        null);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource&dataProvider=dataProvider"
            + "&dataField=dataField&start=2018-01-01&includeStart=true&end=2020-01-01&includeEnd=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleMaxPoints2() {
    final URI uri = uriSearchSingle(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END,
        MAX_POINTS);
    assertEquals(uri.getPath(), "path/to/htsSearches/single");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataSource=dataSource&dataProvider=dataProvider"
            + "&dataField=dataField&start=2018-01-01&includeStart=true&end=2020-01-01&includeEnd=false&maxPoints=100");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchResolveNullBaseUri1() {
    uriSearchResolve(null, EIDS, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests that the identifier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchResolveNullId() {
    uriSearchResolve(_baseUri, null, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveId1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, null, null, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveDataField1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, DATA_FIELD, null, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataField=dataField");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveResolutionKey1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, DATA_FIELD, RESOLUTION_KEY, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataField=dataField&resolutionKey=resolutionKey");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveStart1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_START, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&dataField=dataField&resolutionKey=resolutionKey&start=2018-01-01&includeStart=true");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveEnd1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_END, END, INCLUDE_END, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&dataField=dataField&resolutionKey=resolutionKey&start=2018-01-01&includeStart=false&end=2020-01-01&includeEnd=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveMaxPoints1() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_END, END, INCLUDE_END, MAX_POINTS);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&dataField=dataField&resolutionKey=resolutionKey&start=2018-01-01"
            + "&includeStart=false&end=2020-01-01&includeEnd=false&maxPoints=100");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveId2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, null, null, null, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=ALL");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveValidityDate() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, null, null, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveDataField2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, null, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataField=dataField");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveResolutionKey2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, RESOLUTION_KEY, null, false, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(), "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataField=dataField&resolutionKey=resolutionKey");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveStart2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_START, null, false, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataField=dataField&resolutionKey=resolutionKey&start=2018-01-01&includeStart=true");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveEnd2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_END, END, INCLUDE_END, null);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataField=dataField&resolutionKey=resolutionKey"
            + "&start=2018-01-01&includeStart=false&end=2020-01-01&includeEnd=false");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchResolveMaxPoints2() {
    final URI uri = uriSearchResolve(_baseUri, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_END, END, INCLUDE_END, MAX_POINTS);
    assertEquals(uri.getPath(), "path/to/htsSearches/resolve");
    assertEquals(uri.getQuery(),
        "id=eid1~1&id=eid2~%1&idValidityDate=2020-12-01&dataField=dataField&resolutionKey=resolutionKey"
            + "&start=2018-01-01&includeStart=false&end=2020-01-01&includeEnd=false&maxPoints=100");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchResolveNullBaseUri2() {
    uriSearchResolve(null, EIDS, IDENTIFIER_VALIDITY_DATE, DATA_FIELD, RESOLUTION_KEY, START, INCLUDE_START, END, INCLUDE_END, MAX_POINTS);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchBulkNullBaseUri() {
    uriSearchBulk(null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchBulk() {
    final URI uri = uriSearchBulk(_baseUri);
    assertEquals(uri.getPath(), "path/to/htsSearches/bulk");
    assertNull(uri.getQuery());
  }

  /**
   * Tests that null inputs return no entry in the message.
   */
  public void testUriSearchBulkDataNullInputs() {
    final FudgeMsg message = uriSearchBulkData(null, null, null, null, null, false, null, false);
    assertNull(message.getValue("id"));
    assertNull(message.getValue("dataSource"));
    assertNull(message.getValue("dataProvider"));
    assertNull(message.getValue("dataField"));
    assertNull(message.getValue("start"));
    assertFalse((boolean) message.getValue("includeStart"));
    assertNull(message.getValue("end"));
    assertFalse((boolean) message.getValue("includeEnd"));
  }

  /**
   * Tests the message.
   */
  public void testUriSearchBulkData() {
    final FudgeMsg message = uriSearchBulkData(Collections.singleton(EIDS), DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, START, INCLUDE_START, END, INCLUDE_END);
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final Set<?> ids = deserializer.fudgeMsgToObject(Set.class, message.getMessage("id"));
    assertEquals(ids, Collections.singleton(EIDS));
    assertEquals(message.getValue("dataSource"), DATA_SOURCE);
    assertEquals(message.getValue("dataProvider"), DATA_PROVIDER);
    assertEquals(message.getValue("dataField"), DATA_FIELD);
    assertEquals(message.getValue("start"), START);
    assertEquals(message.getValue("includeStart"), INCLUDE_START);
    assertEquals(message.getValue("end"), END);
    assertEquals((boolean) message.getValue("includeEnd"), INCLUDE_END);
  }
}
