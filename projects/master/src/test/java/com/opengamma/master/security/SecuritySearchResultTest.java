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
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecuritySearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecuritySearchResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(50000L), null);
  private static final RawSecurity SEC_1 = new RawSecurity("FUTURE 2");
  private static final RawSecurity SEC_2 = new RawSecurity("FUTURE 1");
  private static final RawSecurity SEC_3 = new RawSecurity("FUTURE 3");
  private static final SecurityDocument DOC_1 = new SecurityDocument();
  private static final SecurityDocument DOC_2 = new SecurityDocument();
  private static final SecurityDocument DOC_3 = new SecurityDocument();
  static {
    SEC_1.setName("NAME 3");
    SEC_2.setName("NAME 2");
    SEC_3.setName("NAME 1");
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val3"), "v3"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val1"), "v1"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val2"), "v2"));
    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(200000L));
    DOC_1.setSecurity(SEC_1);
    DOC_2.setSecurity(SEC_2);
    DOC_3.setSecurity(SEC_3);
  }

  /**
   * Tests that the version-correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionConstructor() {
    new SecuritySearchResult((VersionCorrection) null);
  }

  /**
   * Tests that the version-correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionSetter() {
    new SecuritySearchResult().setVersionCorrection(null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new SecuritySearchResult((Collection<SecurityDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new SecuritySearchResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<SecurityDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecuritySearchResult result = new SecuritySearchResult(documents);
    result.setUnauthorizedCount(4);
    result.setVersionCorrection(VC);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstSecurity(), SEC_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getSecurities(), Arrays.asList(SEC_1, SEC_2, SEC_3));
    assertEquals(result.getUnauthorizedCount(), 4);
    assertEquals(result.getVersionCorrection(), VC);
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new SecuritySearchResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstSecurityNoDocuments() {
    assertNull(new SecuritySearchResult().getFirstSecurity());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleSecurityNoDocuments() {
    new SecuritySearchResult().getSingleSecurity();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleSecurityMultipleDocuments() {
    new SecuritySearchResult(Arrays.asList(DOC_1, DOC_2)).getSingleSecurity();
  }

  /**
   * Tests getting a single security.
   */
  @Test
  public void testGetSingleSecurity() {
    assertEquals(new SecuritySearchResult(Collections.singletonList(DOC_2)).getSingleSecurity(), SEC_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecuritySearchResult result = new SecuritySearchResult(docs);
    result.setPaging(PAGING);
    result.setUnauthorizedCount(5);
    result.setVersionCorrection(VC);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "SecuritySearchResult{paging=Paging[first=0, size=20, totalItems=3], "
        + "documents=[SecurityDocument{versionFromInstant=1970-01-02T03:46:40Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], name=NAME 3, "
        + "securityType=FUTURE 2, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val3~v3}, "
        + "SecurityDocument{versionFromInstant=1970-01-04T11:20:00Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], name=NAME 2, "
        + "securityType=FUTURE 1, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val1~v1}, "
        + "SecurityDocument{versionFromInstant=1970-01-03T07:33:20Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, security=RawSecurity{uniqueId=null, externalIdBundle=Bundle[], name=NAME 1, "
        + "securityType=FUTURE 3, attributes={}, requiredPermissions=[], rawData=[]}, uniqueId=oid~val2~v2}], "
        + "versionCorrection=V1970-01-01T13:53:20Z.CLATEST, unauthorizedCount=5}");
    final SecuritySearchResult other = new SecuritySearchResult(docs);
    other.setPaging(PAGING);
    other.setUnauthorizedCount(5);
    other.setVersionCorrection(VC);
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
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final SecuritySearchResult result = new SecuritySearchResult(docs);
    result.setPaging(PAGING);
    result.setUnauthorizedCount(5);
    result.setVersionCorrection(VC);
    assertEquals(result.propertyNames().size(), 4);
    assertEquals(result.metaBean().documents().get(result), docs);
    assertEquals(result.metaBean().paging().get(result), PAGING);
    assertEquals(result.metaBean().unauthorizedCount().get(result), Integer.valueOf(5));
    assertEquals(result.metaBean().versionCorrection().get(result), VC);
    assertEquals(result.property("documents").get(), docs);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("unauthorizedCount").get(), Integer.valueOf(5));
    assertEquals(result.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecuritySearchResult result = new SecuritySearchResult();
    assertEncodeDecodeCycle(SecuritySearchResult.class, result);
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(SecuritySearchResult.class, result);
    result.setPaging(PAGING);
    assertEncodeDecodeCycle(SecuritySearchResult.class, result);
    result.setUnauthorizedCount(5);
    assertEncodeDecodeCycle(SecuritySearchResult.class, result);
    result.setVersionCorrection(VC);
    assertEncodeDecodeCycle(SecuritySearchResult.class, result);
  }
}