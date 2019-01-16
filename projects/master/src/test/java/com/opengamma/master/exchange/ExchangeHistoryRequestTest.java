/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.master.exchange.ExchangeHistoryRequest.Meta;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final ObjectId OID = ObjectId.of("oid", "1");
  private static final Instant VERSION = Instant.ofEpochSecond(10000);
  private static final Instant CORRECTION = Instant.ofEpochSecond(160000);

  /**
   * Tests that the object identifiable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObjectIdentifiableConstructor1() {
    new ExchangeHistoryRequest(null);
  }

  /**
   * Tests that the object identifiable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObjectIdentifiableConstructor2() {
    new ExchangeHistoryRequest(null, VERSION, CORRECTION);
  }

  /**
   * Tests that the object identifiable can be set to null.
   */
  @Test
  public void testNullObjectIdentifiableSetter() {
    new ExchangeHistoryRequest().setObjectId(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(OID, VERSION, CORRECTION);
    final ExchangeHistoryRequest other = new ExchangeHistoryRequest(OID, VERSION, CORRECTION);
    assertEquals(request, other);
    assertEquals(request.toString(),
        "ExchangeHistoryRequest{pagingRequest=PagingRequest[first=0, size=2147483647], objectId=oid~1, "
            + "versionsFromInstant=1970-01-01T02:46:40Z, versionsToInstant=1970-01-01T02:46:40Z, correctionsFromInstant=1970-01-02T20:26:40Z, "
            + "correctionsToInstant=1970-01-02T20:26:40Z, fullDetail=false}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setCorrectionsFromInstant(VERSION);
    assertNotEquals(request, other);
    other.setCorrectionsFromInstant(CORRECTION);
    other.setVersionsFromInstant(CORRECTION);
    assertNotEquals(request, other);
    other.setVersionsFromInstant(VERSION);
    other.setFullDetail(!request.isFullDetail());
    assertNotEquals(request, other);
    other.setObjectId(ObjectId.of("oid", "10000"));
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(OID, VERSION, CORRECTION);
    assertEquals(request.propertyNames().size(), 7);
    final Meta bean = request.metaBean();
    assertEquals(bean.correctionsFromInstant().get(request), CORRECTION);
    assertEquals(bean.correctionsToInstant().get(request), CORRECTION);
    assertEquals(bean.fullDetail().get(request), Boolean.FALSE);
    assertEquals(bean.objectId().get(request), OID);
    assertEquals(bean.pagingRequest().get(request), PagingRequest.ALL);
    assertEquals(bean.versionsFromInstant().get(request), VERSION);
    assertEquals(bean.versionsToInstant().get(request), VERSION);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTION);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTION);
    assertEquals(request.property("fullDetail").get(), Boolean.FALSE);
    assertEquals(request.property("objectId").get(), OID);
    assertEquals(request.property("pagingRequest").get(), PagingRequest.ALL);
    assertEquals(request.property("versionsFromInstant").get(), VERSION);
    assertEquals(request.property("versionsToInstant").get(), VERSION);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(OID, VERSION, CORRECTION);
    assertEncodeDecodeCycle(ExchangeHistoryRequest.class, request);
  }
}
