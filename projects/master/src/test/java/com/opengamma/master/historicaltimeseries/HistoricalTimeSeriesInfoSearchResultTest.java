/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoSearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoSearchResultTest extends AbstractFudgeBuilderTestCase {
  private static final Integer UNAUTHORIZED_COUNT = 100;
  private static final List<ManageableHistoricalTimeSeriesInfo> INFO = new ArrayList<>();
  private static final List<HistoricalTimeSeriesInfoDocument> DOCS = new ArrayList<>();
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, DOCS);
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(20000));
  static {
    for (int i = 0; i < 5; i++) {
      final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setName("name" + i);
      info.setUniqueId(UniqueId.of("hts", Integer.toString(i)));
      final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
      INFO.add(info);
      DOCS.add(doc);
    }
  }

  /**
   * Tests the document info list.
   */
  @Test
  public void testDocumentInfoList() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    assertTrue(result.getInfoList().isEmpty());
    result.setDocuments(DOCS);
    assertEquals(result.getInfoList(), INFO);
  }

  /**
   * Tests getting the first info.
   */
  @Test
  public void testGetFirstInfo() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    assertNull(result.getFirstDocument());
    assertNull(result.getFirstInfo());
    result.setDocuments(DOCS);
    assertEquals(result.getFirstDocument(), DOCS.get(0));
    assertEquals(result.getFirstInfo(), INFO.get(0));
  }

  /**
   * The getSingleInfo() method cannot be used if there are no documents.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleInfoNoDocument() {
    new HistoricalTimeSeriesInfoSearchResult().getSingleInfo();
  }

  /**
   * The getSingleInfo() method cannot be used if there is more than one
   * document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleInfoMultipleDocuments() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setDocuments(DOCS);
    result.getSingleInfo();
  }

  /**
   * Tests getting a single piece of information.
   */
  @Test
  public void testGetSingleInfo() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setDocuments(Collections.singletonList(DOCS.get(3)));
    assertEquals(result.getSingleInfo(), INFO.get(3));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setUnauthorizedCount(UNAUTHORIZED_COUNT);
    result.setDocuments(DOCS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEquals(result, result);
    assertEquals(result.toString(), "HistoricalTimeSeriesInfoSearchResult{paging=Paging[first=0, size=20, totalItems=0], "
        + "documents=[HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, info=ManageableHistoricalTimeSeriesInfo{uniqueId=hts~0, externalIdBundle=null, name=name0, "
        + "dataField=null, dataSource=null, dataProvider=null, observationTime=null, timeSeriesObjectId=null, requiredPermissions=[]}}, "
        + "HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
        + "info=ManageableHistoricalTimeSeriesInfo{uniqueId=hts~1, externalIdBundle=null, name=name1, dataField=null, dataSource=null, "
        + "dataProvider=null, observationTime=null, timeSeriesObjectId=null, requiredPermissions=[]}}, "
        + "HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, info=ManageableHistoricalTimeSeriesInfo{uniqueId=hts~2, externalIdBundle=null, name=name2, "
        + "dataField=null, dataSource=null, dataProvider=null, observationTime=null, timeSeriesObjectId=null, requiredPermissions=[]}}, "
        + "HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
        + "info=ManageableHistoricalTimeSeriesInfo{uniqueId=hts~3, externalIdBundle=null, name=name3, dataField=null, dataSource=null, "
        + "dataProvider=null, observationTime=null, timeSeriesObjectId=null, requiredPermissions=[]}}, "
        + "HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
        + "info=ManageableHistoricalTimeSeriesInfo{uniqueId=hts~4, externalIdBundle=null, name=name4, dataField=null, dataSource=null, "
        + "dataProvider=null, observationTime=null, timeSeriesObjectId=null, requiredPermissions=[]}}], "
        + "versionCorrection=V1970-01-01T00:16:40Z.C1970-01-01T05:33:20Z, unauthorizedCount=100}");
    final HistoricalTimeSeriesInfoSearchResult other = new HistoricalTimeSeriesInfoSearchResult(DOCS);
    other.setUnauthorizedCount(UNAUTHORIZED_COUNT);
    other.setPaging(PAGING);
    other.setVersionCorrection(VC);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(Collections.<HistoricalTimeSeriesInfoDocument> emptyList());
    assertNotEquals(result, other);
    other.setDocuments(DOCS);
    other.setPaging(Paging.of(PagingRequest.NONE, 0));
    assertNotEquals(result, other);
    other.setPaging(PAGING);
    other.setUnauthorizedCount(UNAUTHORIZED_COUNT + 1);
    assertNotEquals(result, other);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Test constructor.
   */
  @Test
  public void testVcConstructor() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setVersionCorrection(VC);
    assertEquals(result, new HistoricalTimeSeriesInfoSearchResult(VC));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setUnauthorizedCount(UNAUTHORIZED_COUNT);
    result.setDocuments(DOCS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEquals(result.propertyNames().size(), 4);
    final Meta bean = result.metaBean();
    assertEquals(bean.documents().get(result), DOCS);
    assertEquals(bean.paging().get(result), PAGING);
    assertEquals(bean.unauthorizedCount().get(result), UNAUTHORIZED_COUNT);
    assertEquals(bean.versionCorrection().get(result), VC);
    assertEquals(result.property("documents").get(), DOCS);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("unauthorizedCount").get(), UNAUTHORIZED_COUNT);
    assertEquals(result.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    result.setUnauthorizedCount(UNAUTHORIZED_COUNT);
    result.setDocuments(DOCS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEncodeDecodeCycle(HistoricalTimeSeriesInfoSearchResult.class, result);
  }
}
