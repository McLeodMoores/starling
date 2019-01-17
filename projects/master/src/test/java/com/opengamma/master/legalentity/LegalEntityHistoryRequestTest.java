/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest.Meta;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntityHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("uid", "1");
  private static final LegalEntityDocument DOC = new LegalEntityDocument();
  static {
    DOC.setUniqueId(UID);
  }
  private static final Instant VERSION = Instant.ofEpochSecond(1000);
  private static final Instant CORRECTION = Instant.ofEpochSecond(1500);

  /**
   * Tests that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObjectConstructor1() {
    new LegalEntityHistoryRequest(null);
  }

  /**
   * Tests that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObjectConstructor2() {
    new LegalEntityHistoryRequest(null, VERSION, CORRECTION);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(DOC, VERSION, CORRECTION);
    final LegalEntityHistoryRequest other = new LegalEntityHistoryRequest(DOC, VERSION, CORRECTION);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "LegalEntityHistoryRequest{pagingRequest=PagingRequest[first=0, size=2147483647], objectId=uid~1, "
            + "versionsFromInstant=1970-01-01T00:16:40Z, versionsToInstant=1970-01-01T00:16:40Z, correctionsFromInstant=1970-01-01T00:25:00Z, "
            + "correctionsToInstant=1970-01-01T00:25:00Z}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setCorrectionsFromInstant(VERSION);
    assertNotEquals(request, other);
    other.setCorrectionsFromInstant(CORRECTION);
    other.setCorrectionsToInstant(VERSION);
    assertNotEquals(request, other);
    other.setCorrectionsToInstant(CORRECTION);
    other.setObjectId(ObjectId.of("oid", "2"));
    assertNotEquals(request, other);
    other.setObjectId(UID.getObjectId());
    other.setPagingRequest(PagingRequest.NONE);
    assertNotEquals(request, other);
    other.setPagingRequest(PagingRequest.ALL);
    other.setVersionsFromInstant(CORRECTION);
    assertNotEquals(request, other);
    other.setVersionsFromInstant(VERSION);
    other.setVersionsToInstant(CORRECTION);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(DOC, VERSION, CORRECTION);
    assertEquals(request.propertyNames().size(), 6);
    final Meta bean = LegalEntityHistoryRequest.meta();
    assertEquals(bean.correctionsFromInstant().get(request), CORRECTION);
    assertEquals(bean.correctionsToInstant().get(request), CORRECTION);
    assertEquals(bean.objectId().get(request), UID.getObjectId());
    assertEquals(bean.pagingRequest().get(request), PagingRequest.ALL);
    assertEquals(bean.versionsFromInstant().get(request), VERSION);
    assertEquals(bean.versionsToInstant().get(request), VERSION);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTION);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTION);
    assertEquals(request.property("objectId").get(), UID.getObjectId());
    assertEquals(request.property("pagingRequest").get(), PagingRequest.ALL);
    assertEquals(request.property("versionsFromInstant").get(), VERSION);
    assertEquals(request.property("versionsToInstant").get(), VERSION);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(DOC, VERSION, CORRECTION);
    assertEncodeDecodeCycle(LegalEntityHistoryRequest.class, request);
  }
}
