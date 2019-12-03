/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest.Meta;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.test.Assert;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoSearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoSearchRequestTest extends AbstractFudgeBuilderTestCase {
  private static final List<ObjectId> OIDS = Arrays.asList(ObjectId.of("A", "1"), ObjectId.of("A", "2"), ObjectId.of("B", "1"));
  private static final ExternalIdSearchType SEARCH_TYPE = ExternalIdSearchType.EXACT;
  private static final ExternalIdSearch EID_SEARCH = ExternalIdSearch.of(SEARCH_TYPE, ExternalId.of("A", "1"), ExternalId.of("A", "ONE"));
  private static final String EID_VALUE = "*O*";
  private static final LocalDate VALIDITY_DATE = LocalDate.of(2019, 1, 1);
  private static final String NAME = "*A*";
  private static final String DATA_SOURCE = "*B*";
  private static final String DATA_PROVIDER = "CM*";
  private static final String DATA_FIELD = "*CLOSE";
  private static final String OBS_TIME = "NY";
  private static final PagingRequest PAGING_REQUEST = PagingRequest.FIRST_PAGE;
  private static final String UID_SCHEME = "hist";
  private static final VersionCorrection VC = VersionCorrection.of(LocalDate.of(2018, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC),
      LocalDate.of(2018, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC));

  /**
   * Tests that the id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidConstructor() {
    new HistoricalTimeSeriesInfoSearchRequest((ExternalId) null);
  }

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidBundleConstructor() {
    new HistoricalTimeSeriesInfoSearchRequest((ExternalIdBundle) null);
  }

  /**
   * Tests that an object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOid() {
    new HistoricalTimeSeriesInfoSearchRequest().addObjectId(null);
  }

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidsVarargs() {
    new HistoricalTimeSeriesInfoSearchRequest().addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEidsIterable() {
    new HistoricalTimeSeriesInfoSearchRequest().addExternalIds((Iterable<ExternalId>) null);
  }

  /**
   * Tests that an external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEid() {
    new HistoricalTimeSeriesInfoSearchRequest().addExternalId(null);
  }

  /**
   * Tests the addition of object ids.
   */
  @Test
  public void testAddObjectId() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    for (final ObjectId oid : OIDS) {
      request.addObjectId(oid);
    }
    assertEquals(request.getObjectIds(), OIDS);
    // adds them to a list
    final List<ObjectId> ids = new ArrayList<>(OIDS);
    ids.addAll(OIDS);
    for (final ObjectId oid : OIDS) {
      request.addObjectId(oid);
    }
    assertEquals(request.getObjectIds(), ids);
  }

  /**
   * Tests the setting of object ids.
   */
  @Test
  public void testSetObjectIds() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    assertNull(request.getObjectIds());
    request.setObjectIds(OIDS);
    assertEquals(request.getObjectIds(), OIDS);
    // resets the list every time ids are added
    final List<ObjectId> newOids = Arrays.asList(ObjectId.of("C", "1"), ObjectId.of("C", "2"));
    request.setObjectIds(newOids);
    assertEquals(request.getObjectIds(), newOids);
  }

  /**
   * Tests the addition of external ids.
   */
  @Test
  public void testAddExternalId() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    for (final ExternalId eid : EID_SEARCH.getExternalIds()) {
      request.addExternalId(eid);
    }
    // uses the default search type
    Assert.assertEqualsNoOrder(request.getExternalIdSearch(), ExternalIdSearch.of(EID_SEARCH.getExternalIds()));
    request.setExternalIdSearch(null);
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    for (final ExternalId eid : EID_SEARCH.getExternalIds()) {
      request.addExternalId(eid);
    }
    Assert.assertEqualsNoOrder(request.getExternalIdSearch(), ExternalIdSearch.of(ExternalIdSearchType.NONE, EID_SEARCH.getExternalIds()));
    request.setExternalIdSearch(null);
    request.setExternalIdSearchType(SEARCH_TYPE);
    request.addExternalIds(EID_SEARCH.getExternalIds().toArray(new ExternalId[0]));
    Assert.assertEqualsNoOrder(request.getExternalIdSearch(), EID_SEARCH);
    request.setExternalIdSearch(null);
    request.setExternalIdSearchType(SEARCH_TYPE);
    request.addExternalIds(EID_SEARCH.getExternalIds());
    Assert.assertEqualsNoOrder(request.getExternalIdSearch(), EID_SEARCH);
    final List<ExternalId> eids = new ArrayList<>(EID_SEARCH.getExternalIds());
    eids.add(ExternalId.of("C", "1"));
    request.addExternalIds(eids);
    assertEquals(request.getExternalIdSearch().getExternalIds().size(), EID_SEARCH.getExternalIds().size() + 1);
  }

  /**
   * Tests the setting of the search type.
   */
  @Test
  public void testSetSearchType() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(EID_SEARCH);
    assertEquals(request.getExternalIdSearch(), EID_SEARCH);
    request.setExternalIdSearch(ExternalIdSearch.of(EID_SEARCH.getExternalIds()));
    assertEquals(request.getExternalIdSearch(), ExternalIdSearch.of(EID_SEARCH.getExternalIds()));
  }

  /**
   * Tests the document type is correct.
   */
  @Test
  public void testMatchingType() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    assertFalse(request.matches(new HolidayDocument()));
  }

  /**
   * Tests object id matching.
   */
  @Test
  public void testObjectIdMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);

    final UniqueId nonMatching = UniqueId.of("AA", "1");
    doc.setUniqueId(nonMatching);
    assertFalse(request.matches(doc));
    final UniqueId matching = UniqueId.of("A", "1");
    doc.setUniqueId(matching);
    assertTrue(request.matches(doc));
    request.setObjectIds(null);
    assertTrue(request.matches(doc));
    info.setExternalIdBundle(ExternalIdBundleWithDates.EMPTY);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests external id search matching.
   */
  @Test
  public void testExternalIdSearchMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final ExternalId nonMatching = ExternalId.of("V", "1");
    info.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(nonMatching, VALIDITY_DATE, null)));
    assertFalse(request.matches(doc));
    setFields(info);
    assertTrue(request.matches(doc));
    info.setExternalIdBundle(ExternalIdBundleWithDates.EMPTY);
    assertFalse(request.matches(doc));
    request.setExternalIdSearch(null);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests name matching.
   */
  @Test
  public void testNameMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final String nonMatching = "n";
    info.setName(nonMatching);
    assertFalse(request.matches(doc));
    final String matching = "A";
    info.setName(matching);
    assertTrue(request.matches(doc));
    final String wildcard = "VALUE";
    info.setName(wildcard);
    assertTrue(request.matches(doc));
    request.setName(null);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests data source matching.
   */
  @Test
  public void testDataSourceMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final String nonMatching = "n";
    info.setDataSource(nonMatching);
    assertFalse(request.matches(doc));
    final String matching = "B";
    info.setDataSource(matching);
    assertTrue(request.matches(doc));
    final String wildcard = "BBG";
    info.setDataSource(wildcard);
    assertTrue(request.matches(doc));
    request.setDataSource(null);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests data provider matching.
   */
  @Test
  public void testDataProviderMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final String nonMatching = "n";
    info.setDataProvider(nonMatching);
    assertFalse(request.matches(doc));
    final String matching = "CM";
    info.setDataProvider(matching);
    assertTrue(request.matches(doc));
    final String wildcard = "CMPL";
    info.setDataProvider(wildcard);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests data field matching.
   */
  @Test
  public void testDataFieldMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final String nonMatching = "n";
    info.setDataField(nonMatching);
    assertFalse(request.matches(doc));
    final String matching = "CLOSE";
    info.setDataField(matching);
    assertTrue(request.matches(doc));
    final String wildcard = "LONDON_CLOSE";
    info.setDataField(wildcard);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests observation time matching.
   */
  @Test
  public void testObservationTimeMatching() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    request.setExternalIdValue("*");
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    doc.setInfo(info);
    doc.setUniqueId(UniqueId.of("A", "1"));

    final String nonMatching = "n";
    info.setObservationTime(nonMatching);
    assertFalse(request.matches(doc));
    final String matching = "NY";
    info.setObservationTime(matching);
    assertTrue(request.matches(doc));
    final String wildcard = "NYC";
    info.setObservationTime(wildcard);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests ids value matching.
   */

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructors() {
    final ExternalId firstId = EID_SEARCH.getExternalIds().iterator().next();
    final ExternalIdBundle idBundle = ExternalIdBundle.of(EID_SEARCH.getExternalIds());
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(firstId);
    HistoricalTimeSeriesInfoSearchRequest other = new HistoricalTimeSeriesInfoSearchRequest(firstId);
    assertEquals(request, other);
    request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(idBundle);
    other = new HistoricalTimeSeriesInfoSearchRequest(idBundle);
    assertEquals(request, other);
    request = new HistoricalTimeSeriesInfoSearchRequest();
    for (final ExternalId eid : idBundle) {
      request.addExternalId(eid);
    }
    other = new HistoricalTimeSeriesInfoSearchRequest(idBundle);
    assertEquals(request, other);
    request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(idBundle.getExternalIds().toArray(new ExternalId[0]));
    other = new HistoricalTimeSeriesInfoSearchRequest(idBundle);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    final HistoricalTimeSeriesInfoSearchRequest other = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(other);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "HistoricalTimeSeriesInfoSearchRequest{uniqueIdScheme=hist, pagingRequest=PagingRequest[first=0, size=20], "
            + "versionCorrection=V2018-12-31T00:00:00Z.C2018-12-31T00:00:00Z, objectIds=[A~1, A~2, B~1], "
            + "externalIdSearch=ExternalIdSearch{externalIds=[A~1, A~ONE], searchType=EXACT}, externalIdValue=*O*, "
            + "validityDate=2019-01-01, name=*A*, dataSource=*B*, dataProvider=CM*, dataField=*CLOSE, observationTime=NY}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setDataField(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setDataProvider(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setDataSource(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdSearch(ExternalIdSearch.of());
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdSearchType(ExternalIdSearchType.ALL);
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdValue(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setName(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setObjectIds(Collections.<ObjectId> emptySet());
    assertNotEquals(request, other);
    setFields(other);
    other.setObservationTime(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setPagingRequest(PagingRequest.ALL);
    assertNotEquals(request, other);
    setFields(other);
    other.setUniqueIdScheme(null);
    assertNotEquals(request, other);
    setFields(other);
    other.setValidityDate(VALIDITY_DATE.plusDays(1));
    assertNotEquals(request, other);
    setFields(other);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(request, other);
    assertEquals(request.getDataField(), DATA_FIELD);
    assertEquals(request.getDataProvider(), DATA_PROVIDER);
    assertEquals(request.getDataSource(), DATA_SOURCE);
    assertEquals(request.getExternalIdSearch(), EID_SEARCH);
    assertEquals(request.getExternalIdValue(), EID_VALUE);
    assertEquals(request.getName(), NAME);
    assertEquals(request.getObjectIds(), OIDS);
    assertEquals(request.getObservationTime(), OBS_TIME);
    assertEquals(request.getPagingRequest(), PAGING_REQUEST);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertEquals(request.getValidityDate(), VALIDITY_DATE);
    assertEquals(request.getVersionCorrection(), VC);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    assertEquals(request.propertyNames().size(), 12);
    final Meta metaBean = request.metaBean();
    assertEquals(metaBean.dataField().get(request), DATA_FIELD);
    assertEquals(metaBean.dataProvider().get(request), DATA_PROVIDER);
    assertEquals(metaBean.dataSource().get(request), DATA_SOURCE);
    assertEquals(metaBean.externalIdSearch().get(request), EID_SEARCH);
    assertEquals(metaBean.externalIdValue().get(request), EID_VALUE);
    assertEquals(metaBean.name().get(request), NAME);
    assertEquals(metaBean.objectIds().get(request), OIDS);
    assertEquals(metaBean.observationTime().get(request), OBS_TIME);
    assertEquals(metaBean.pagingRequest().get(request), PAGING_REQUEST);
    assertEquals(metaBean.uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(metaBean.validityDate().get(request), VALIDITY_DATE);
    assertEquals(metaBean.versionCorrection().get(request), VC);
    assertEquals(request.property("dataField").get(), DATA_FIELD);
    assertEquals(request.property("dataProvider").get(), DATA_PROVIDER);
    assertEquals(request.property("dataSource").get(), DATA_SOURCE);
    assertEquals(request.property("externalIdSearch").get(), EID_SEARCH);
    assertEquals(request.property("externalIdValue").get(), EID_VALUE);
    assertEquals(request.property("name").get(), NAME);
    assertEquals(request.property("objectIds").get(), OIDS);
    assertEquals(request.property("observationTime").get(), OBS_TIME);
    assertEquals(request.property("pagingRequest").get(), PAGING_REQUEST);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
    assertEquals(request.property("validityDate").get(), VALIDITY_DATE);
    assertEquals(request.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    setFields(request);
    assertEncodeDecodeCycle(HistoricalTimeSeriesInfoSearchRequest.class, request);
  }

  private static void setFields(final HistoricalTimeSeriesInfoSearchRequest request) {
    request.setDataField(DATA_FIELD);
    request.setDataProvider(DATA_PROVIDER);
    request.setDataSource(DATA_SOURCE);
    request.setExternalIdSearch(EID_SEARCH);
    request.setExternalIdSearchType(SEARCH_TYPE);
    request.setExternalIdSearch(EID_SEARCH);
    request.setExternalIdValue(EID_VALUE);
    request.setName(NAME);
    request.setObjectIds(OIDS);
    request.setObservationTime(OBS_TIME);
    request.setPagingRequest(PAGING_REQUEST);
    request.setUniqueIdScheme(UID_SCHEME);
    request.setValidityDate(VALIDITY_DATE);
    request.setVersionCorrection(VC);
  }

  private static void setFields(final ManageableHistoricalTimeSeriesInfo info) {
    info.setDataField(DATA_FIELD);
    info.setDataProvider(DATA_PROVIDER);
    info.setDataSource(DATA_SOURCE);
    final Collection<ExternalIdWithDates> idBundle = new HashSet<>();
    for (final ExternalId eid : EID_SEARCH.getExternalIds()) {
      idBundle.add(ExternalIdWithDates.of(eid, VALIDITY_DATE, null));
    }
    info.setExternalIdBundle(ExternalIdBundleWithDates.of(idBundle));
    info.setName(NAME);
    info.setObservationTime(OBS_TIME);
  }
}
