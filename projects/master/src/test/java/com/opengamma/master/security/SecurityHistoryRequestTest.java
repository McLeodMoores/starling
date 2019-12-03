/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final PagingRequest PAGING = PagingRequest.FIRST_PAGE;
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(10000L);
  private static final Instant VERSION_TO = Instant.ofEpochSecond(20000L);
  private static final Instant CORRECTED_FROM = Instant.ofEpochSecond(15000L);
  private static final Instant CORRECTED_TO = Instant.ofEpochSecond(25000L);
  private static final RawSecurity SEC = new RawSecurity("EQUITY");
  private static final SecurityDocument DOC = new SecurityDocument();
  static {
    SEC.setName("NAME");
    DOC.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v1"));
    DOC.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC.setSecurity(SEC);
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentConstructor1() {
    new SecurityHistoryRequest(null);
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentConstructor() {
    new SecurityHistoryRequest(null, VERSION_FROM, CORRECTED_TO);
  }

  /**
   * Tests that the object id can be set to null.
   */
  @Test
  public void testSetNullObjectId() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest();
    request.setObjectId(null);
    assertNull(request.getObjectId());
  }

  /**
   * Tests that the version instant can be null.
   */
  @Test
  public void testNullVersionInstant() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest(DOC, null, CORRECTED_TO);
    assertNull(request.getVersionsFromInstant());
    assertNull(request.getVersionsToInstant());
  }

  /**
   * Tests that the correction instant can be null.
   */
  @Test
  public void testNullCorrectionInstant() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest(DOC, VERSION_TO, null);
    assertNull(request.getCorrectionsFromInstant());
    assertNull(request.getCorrectionsToInstant());
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    assertTrue(request.isFullDetail());
    assertEquals(request.getCorrectionsFromInstant(), CORRECTED_TO);
    assertEquals(request.getCorrectionsToInstant(), CORRECTED_TO);
    assertEquals(request.getObjectId(), DOC.getObjectId());
    assertEquals(request.getPagingRequest(), PagingRequest.ALL);
    assertEquals(request.getVersionsFromInstant(), VERSION_TO);
    assertEquals(request.getVersionsToInstant(), VERSION_TO);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    request.setFullDetail(false);
    request.setPagingRequest(PAGING);
    request.setVersionsFromInstant(VERSION_FROM);
    assertFalse(request.isFullDetail());
    assertEquals(request.getCorrectionsFromInstant(), CORRECTED_FROM);
    assertEquals(request.getCorrectionsToInstant(), CORRECTED_TO);
    assertEquals(request.getObjectId(), DOC.getObjectId());
    assertEquals(request.getPagingRequest(), PAGING);
    assertEquals(request.getVersionsFromInstant(), VERSION_FROM);
    assertEquals(request.getVersionsToInstant(), VERSION_TO);
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "SecurityHistoryRequest{pagingRequest=PagingRequest[first=0, size=20], objectId=oid~val, "
        + "versionsFromInstant=1970-01-01T02:46:40Z, versionsToInstant=1970-01-01T05:33:20Z, correctionsFromInstant=1970-01-01T04:10:00Z, "
        + "correctionsToInstant=1970-01-01T06:56:40Z, fullDetail=false}");
    final SecurityHistoryRequest other = new SecurityHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    other.setCorrectionsFromInstant(CORRECTED_FROM);
    other.setFullDetail(false);
    other.setPagingRequest(PAGING);
    other.setVersionsFromInstant(VERSION_FROM);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setCorrectionsFromInstant(null);
    assertNotEquals(request, other);
    other.setCorrectionsFromInstant(CORRECTED_FROM);
    other.setCorrectionsToInstant(null);
    assertNotEquals(request, other);
    other.setCorrectionsToInstant(CORRECTED_TO);
    other.setFullDetail(true);
    assertNotEquals(request, other);
    other.setFullDetail(false);
    other.setObjectId(ObjectId.of("oid", "new"));
    assertNotEquals(request, other);
    other.setObjectId(DOC.getObjectId());
    other.setVersionsFromInstant(null);
    assertNotEquals(request, other);
    other.setVersionsFromInstant(VERSION_FROM);
    other.setVersionsToInstant(null);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    request.setFullDetail(true);
    request.setPagingRequest(PAGING);
    request.setVersionsFromInstant(VERSION_FROM);
    assertEquals(request.propertyNames().size(), 7);
    assertEquals(request.metaBean().correctionsFromInstant().get(request), CORRECTED_FROM);
    assertEquals(request.metaBean().correctionsToInstant().get(request), CORRECTED_TO);
    assertEquals(request.metaBean().fullDetail().get(request), Boolean.TRUE);
    assertEquals(request.metaBean().pagingRequest().get(request), PAGING);
    assertEquals(request.metaBean().objectId().get(request), DOC.getObjectId());
    assertEquals(request.metaBean().versionsFromInstant().get(request), VERSION_FROM);
    assertEquals(request.metaBean().versionsToInstant().get(request), VERSION_TO);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTED_FROM);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTED_TO);
    assertEquals(request.property("fullDetail").get(), Boolean.TRUE);
    assertEquals(request.property("pagingRequest").get(), PAGING);
    assertEquals(request.property("objectId").get(), DOC.getObjectId());
    assertEquals(request.property("versionsFromInstant").get(), VERSION_FROM);
    assertEquals(request.property("versionsToInstant").get(), VERSION_TO);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest();
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setCorrectionsToInstant(CORRECTED_TO);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setFullDetail(false);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setObjectId(ObjectId.of("oid", "new"));
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setPagingRequest(PAGING);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setVersionsFromInstant(VERSION_FROM);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
    request.setVersionsToInstant(VERSION_TO);
    assertEncodeDecodeCycle(SecurityHistoryRequest.class, request);
  }
}
