/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.convention;

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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.impl.MockConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConventionHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionHistoryResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);
  private static final ManageableConvention CONV_1 = new MockConvention("EUR CONVENTION", ExternalIdBundle.of("conv", "conv"), Currency.EUR);
  private static final ManageableConvention CONV_2 = new MockConvention("EUR CONVENTION", ExternalIdBundle.of("conv", "conv"), Currency.EUR);
  private static final ManageableConvention CONV_3 = new MockConvention("EUR CONVENTION", ExternalIdBundle.of("conv", "conv"), Currency.EUR);
  private static final ConventionDocument DOC_1 = new ConventionDocument();
  private static final ConventionDocument DOC_2 = new ConventionDocument();
  private static final ConventionDocument DOC_3 = new ConventionDocument();
  static {
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v1"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v2"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v3"));
    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(200000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_1.setConvention(CONV_1);
    DOC_2.setConvention(CONV_2);
    DOC_3.setConvention(CONV_3);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new ConventionHistoryResult((Collection<ConventionDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new ConventionHistoryResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<ConventionDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ConventionHistoryResult result = new ConventionHistoryResult(documents);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstConvention(), CONV_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getConventions(), Arrays.asList(CONV_1, CONV_2, CONV_3));
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new ConventionHistoryResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstConventionNoDocuments() {
    assertNull(new ConventionHistoryResult().getFirstConvention());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleConventionNoDocuments() {
    new ConventionHistoryResult().getSingleConvention();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleConventionMultipleDocuments() {
    new ConventionHistoryResult(Arrays.asList(DOC_1, DOC_2)).getSingleConvention();
  }

  /**
   * Tests getting a single convention.
   */
  @Test
  public void testGetSingleConvention() {
    assertEquals(new ConventionHistoryResult(Collections.singletonList(DOC_2)).getSingleConvention(), CONV_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<ConventionDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ConventionHistoryResult result = new ConventionHistoryResult(docs);
    result.setPaging(PAGING);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "ConventionHistoryResult{paging=Paging[first=0, size=20, totalItems=3], "
        + "documents=[ConventionDocument{versionFromInstant=1970-01-02T03:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=null, correctionToInstant=null, convention=MockConvention{uniqueId=null, "
        + "externalIdBundle=Bundle[conv~conv], attributes={}, name=EUR CONVENTION, currency=EUR}, uniqueId=oid~val~v1}, "
        + "ConventionDocument{versionFromInstant=1970-01-03T07:33:20Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, convention=MockConvention{uniqueId=null, externalIdBundle=Bundle[conv~conv], attributes={}, "
        + "name=EUR CONVENTION, currency=EUR}, uniqueId=oid~val~v2}, ConventionDocument{versionFromInstant=1970-01-04T11:20:00Z, "
        + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, convention=MockConvention{uniqueId=null, "
        + "externalIdBundle=Bundle[conv~conv], attributes={}, name=EUR CONVENTION, currency=EUR}, uniqueId=oid~val~v3}]}");
    final ConventionHistoryResult other = new ConventionHistoryResult(docs);
    other.setPaging(PAGING);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(Collections.singletonList(DOC_1));
    assertNotEquals(result, other);
    other.setDocuments(docs);
    other.setPaging(Paging.of(PagingRequest.NONE, 3));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final List<ConventionDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ConventionHistoryResult result = new ConventionHistoryResult(docs);
    result.setPaging(PAGING);
    assertEquals(result.propertyNames().size(), 2);
    assertEquals(result.metaBean().documents().get(result), docs);
    assertEquals(result.metaBean().paging().get(result), PAGING);
    assertEquals(result.property("documents").get(), docs);
    assertEquals(result.property("paging").get(), PAGING);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConventionHistoryResult result = new ConventionHistoryResult();
    assertEncodeDecodeCycle(ConventionHistoryResult.class, result);
    final List<ConventionDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(ConventionHistoryResult.class, result);
    result.setPaging(PAGING);
    assertEncodeDecodeCycle(ConventionHistoryResult.class, result);
  }
}