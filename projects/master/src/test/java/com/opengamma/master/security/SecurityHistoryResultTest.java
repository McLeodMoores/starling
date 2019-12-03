/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityHistoryResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);
  private static final RawSecurity SEC_1 = new RawSecurity("FUTURE");
  private static final RawSecurity SEC_2 = new RawSecurity("FUTURE");
  private static final RawSecurity SEC_3 = new RawSecurity("FUTURE");
  private static final SecurityDocument DOC_1 = new SecurityDocument();
  private static final SecurityDocument DOC_2 = new SecurityDocument();
  private static final SecurityDocument DOC_3 = new SecurityDocument();
  static {
    SEC_1.setName("NAME");
    SEC_2.setName("NAME");
    SEC_3.setName("NAME");
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v1"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v2"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v3"));
    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(200000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_1.setSecurity(SEC_1);
    DOC_2.setSecurity(SEC_2);
    DOC_3.setSecurity(SEC_3);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new SecurityHistoryResult((Collection<SecurityDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new SecurityHistoryResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<SecurityDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecurityHistoryResult result = new SecurityHistoryResult(documents);
    result.setUnauthorizedCount(4);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstSecurity(), SEC_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getSecurities(), Arrays.asList(SEC_1, SEC_2, SEC_3));
    assertEquals(result.getUnauthorizedCount(), 4);
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new SecurityHistoryResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstSecurityNoDocuments() {
    assertNull(new SecurityHistoryResult().getFirstSecurity());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleSecurityNoDocuments() {
    new SecurityHistoryResult().getSingleSecurity();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleSecurityMultipleDocuments() {
    new SecurityHistoryResult(Arrays.asList(DOC_1, DOC_2)).getSingleSecurity();
  }

  /**
   * Tests getting a single security.
   */
  @Test
  public void testGetSingleSecurity() {
    assertEquals(new SecurityHistoryResult(Collections.singletonList(DOC_2)).getSingleSecurity(), SEC_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecurityHistoryResult result = new SecurityHistoryResult(docs);
    result.setPaging(PAGING);
    result.setUnauthorizedCount(5);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "SecurityHistoryResult{paging=Paging[first=0, size=20, totalItems=3], "
        + "documents=[SecurityDocument{versionFromInstant=1970-01-02T03:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=null, correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], "
        + "name=NAME, securityType=FUTURE, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val~v1}, "
        + "SecurityDocument{versionFromInstant=1970-01-03T07:33:20Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], name=NAME, "
        + "securityType=FUTURE, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val~v2}, "
        + "SecurityDocument{versionFromInstant=1970-01-04T11:20:00Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], name=NAME, "
        + "securityType=FUTURE, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val~v3}], unauthorizedCount=5}");
    final SecurityHistoryResult other = new SecurityHistoryResult(docs);
    other.setPaging(PAGING);
    other.setUnauthorizedCount(5);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(Collections.singletonList(DOC_1));
    assertNotEquals(result, other);
    other.setDocuments(docs);
    other.setPaging(Paging.of(PagingRequest.NONE, 3));
    assertNotEquals(result, other);
    other.setPaging(PAGING);
    other.setUnauthorizedCount(0);
    assertNotEquals(result, other);
    other.setUnauthorizedCount(5);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecurityHistoryResult result = new SecurityHistoryResult(docs);
    result.setPaging(PAGING);
    result.setUnauthorizedCount(5);
    assertEquals(result.propertyNames().size(), 3);
    assertEquals(result.metaBean().documents().get(result), docs);
    assertEquals(result.metaBean().paging().get(result), PAGING);
    assertEquals(result.metaBean().unauthorizedCount().get(result), Integer.valueOf(5));
    assertEquals(result.property("documents").get(), docs);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("unauthorizedCount").get(), Integer.valueOf(5));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecurityHistoryResult result = new SecurityHistoryResult();
    assertEncodeDecodeCycle(SecurityHistoryResult.class, result);
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(SecurityHistoryResult.class, result);
    result.setPaging(PAGING);
    assertEncodeDecodeCycle(SecurityHistoryResult.class, result);
    result.setUnauthorizedCount(5);
    assertEncodeDecodeCycle(SecurityHistoryResult.class, result);
  }
}