/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final ObjectId OID = ObjectId.of("oid", "1");
  private static final Instant VERSION = Instant.ofEpochSecond(100000);
  private static final Instant CORRECTION = Instant.ofEpochSecond(20000000);

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest();
    HistoricalTimeSeriesInfoHistoryRequest other = new HistoricalTimeSeriesInfoHistoryRequest();
    assertEquals(request, request);
    assertEquals(request, other);
    request.setObjectId(OID);
    other = new HistoricalTimeSeriesInfoHistoryRequest(OID);
    assertEquals(request, other);
    request.setCorrectionsFromInstant(CORRECTION);
    request.setCorrectionsToInstant(CORRECTION);
    request.setVersionsFromInstant(VERSION);
    request.setVersionsToInstant(VERSION);
    other = new HistoricalTimeSeriesInfoHistoryRequest(OID, VERSION, CORRECTION);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    assertEquals(request.toString(),
        "HistoricalTimeSeriesInfoHistoryRequest{pagingRequest=PagingRequest[first=0, size=2147483647], objectId=oid~1, "
            + "versionsFromInstant=1970-01-02T03:46:40Z, versionsToInstant=1970-01-02T03:46:40Z, correctionsFromInstant=1970-08-20T11:33:20Z, "
            + "correctionsToInstant=1970-08-20T11:33:20Z}");
    other = new HistoricalTimeSeriesInfoHistoryRequest(ObjectId.of("oid", "2"), VERSION, CORRECTION);
    assertNotEquals(request, other);
    other = new HistoricalTimeSeriesInfoHistoryRequest(OID, CORRECTION, CORRECTION);
    assertNotEquals(request, other);
    other = new HistoricalTimeSeriesInfoHistoryRequest(OID, VERSION, VERSION);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(OID, VERSION, CORRECTION);
    assertEquals(request.propertyNames().size(), 6);
    final Meta bean = request.metaBean();
    assertEquals(bean.correctionsFromInstant().get(request), CORRECTION);
    assertEquals(bean.correctionsToInstant().get(request), CORRECTION);
    assertEquals(bean.objectId().get(request), OID);
    assertEquals(bean.versionsFromInstant().get(request), VERSION);
    assertEquals(bean.versionsToInstant().get(request), VERSION);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTION);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTION);
    assertEquals(request.property("objectId").get(), OID);
    assertEquals(request.property("versionsFromInstant").get(), VERSION);
    assertEquals(request.property("versionsToInstant").get(), VERSION);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(OID, VERSION, CORRECTION);
    assertEncodeDecodeCycle(HistoricalTimeSeriesInfoHistoryRequest.class, request);
  }
}
