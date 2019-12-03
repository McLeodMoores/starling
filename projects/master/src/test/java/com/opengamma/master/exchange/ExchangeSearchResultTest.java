/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeSearchResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeSearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeSearchResultTest extends AbstractFudgeBuilderTestCase {
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(1500));
  private static final ManageableExchange EXCH_1 = new ManageableExchange();
  private static final ManageableExchange EXCH_2 = new ManageableExchange();
  private static final ManageableExchange EXCH_3 = new ManageableExchange();
  private static final ExchangeDocument DOC_1 = new ExchangeDocument(EXCH_1);
  private static final ExchangeDocument DOC_2 = new ExchangeDocument(EXCH_2);
  private static final ExchangeDocument DOC_3 = new ExchangeDocument(EXCH_3);
  private static final List<ExchangeDocument> DOCS = Arrays.asList(DOC_1, DOC_2, DOC_3);
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, DOCS);
  static {
    EXCH_1.setName("exch1");
    EXCH_2.setName("exch2");
    EXCH_3.setName("exch3");
    DOC_1.setUniqueId(UniqueId.of("uid", "1"));
    DOC_2.setUniqueId(UniqueId.of("uid", "2"));
    DOC_3.setUniqueId(UniqueId.of("uid", "3"));
    DOC_1.setVersionFromInstant(VC.getVersionAsOf());
    DOC_2.setVersionFromInstant(VC.getVersionAsOf());
    DOC_3.setVersionFromInstant(VC.getVersionAsOf());
    DOC_1.setCorrectionFromInstant(VC.getCorrectedTo());
    DOC_2.setCorrectionFromInstant(VC.getCorrectedTo());
    DOC_3.setCorrectionFromInstant(VC.getCorrectedTo());
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new ExchangeSearchResult((Collection<ExchangeDocument>) null);
  }

  /**
   * Tests that the version/correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionConstructor() {
    new ExchangeSearchResult((VersionCorrection) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new ExchangeSearchResult().setDocuments(null);
  }

  /**
   * Tests that the version/correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionSetter() {
    new ExchangeSearchResult().setVersionCorrection(null);
  }

  /**
   * Tests that the paging can be null, although the object is not valid if this
   * is the case.
   */
  public void testNullPagingSetter() {
    new ExchangeSearchResult().setPaging(null);
  }

  /**
   * Tests getting the documents.
   */
  public void testGetDocuments() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    setFields(result);
    // it's a list, so the order will be maintained
    assertEquals(result.getExchanges(), Arrays.asList(EXCH_1, EXCH_2, EXCH_3));
  }

  /**
   * Tests that setting new documents clears the originals.
   */
  public void testSetDocuments() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    result.setDocuments(Collections.singletonList(DOC_1));
    assertEquals(result.getSingleExchange(), EXCH_1);
    result.setDocuments(Collections.singletonList(DOC_2));
    assertEquals(result.getSingleExchange(), EXCH_2);
  }

  /**
   * Tests getting the first document.
   */
  public void testGetFirstDocument() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    assertNull(result.getFirstDocument());
    result.setDocuments(DOCS);
    assertEquals(result.getFirstDocument(), DOC_1);
  }

  /**
   * Tests getting the first exchange.
   */
  public void testGetFirstExchange() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    assertNull(result.getFirstExchange());
    result.setDocuments(DOCS);
    assertEquals(result.getFirstExchange(), EXCH_1);
  }

  /**
   * Test that an exception is thrown if there are multiple exchanges and a
   * single exchanges is requested.
   */
  public void testGetSingleExchangeMultipleExchanges() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    setFields(result);
    result.getFirstExchange();
  }

  /**
   * Tests getting a single exchange.
   */
  public void testGetSingleExchange() {
    final ExchangeSearchResult result = new ExchangeSearchResult(Collections.singletonList(DOC_1));
    assertEquals(result.getSingleExchange(), EXCH_1);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    setFields(result);
    final ExchangeSearchResult other = new ExchangeSearchResult();
    setFields(other);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "ExchangeSearchResult{paging=Paging[first=0, size=20, totalItems=3], "
            + "documents=[ExchangeDocument{versionFromInstant=1970-01-01T00:16:40Z, versionToInstant=null, "
            + "correctionFromInstant=1970-01-01T00:25:00Z, correctionToInstant=null, exchange=ManageableExchange{uniqueId=null, "
            + "externalIdBundle=Bundle[], name=exch1, regionIdBundle=null, timeZone=null, detail=[]}, uniqueId=uid~1}, "
            + "ExchangeDocument{versionFromInstant=1970-01-01T00:16:40Z, versionToInstant=null, correctionFromInstant=1970-01-01T00:25:00Z, "
            + "correctionToInstant=null, exchange=ManageableExchange{uniqueId=null, externalIdBundle=Bundle[], name=exch2, "
            + "regionIdBundle=null, timeZone=null, detail=[]}, uniqueId=uid~2}, ExchangeDocument{versionFromInstant=1970-01-01T00:16:40Z, "
            + "versionToInstant=null, correctionFromInstant=1970-01-01T00:25:00Z, correctionToInstant=null, exchange=ManageableExchange{uniqueId=null, "
            + "externalIdBundle=Bundle[], name=exch3, regionIdBundle=null, timeZone=null, detail=[]}, uniqueId=uid~3}], "
            + "versionCorrection=V1970-01-01T00:16:40Z.C1970-01-01T00:25:00Z}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(DOCS.subList(0, 1));
    assertNotEquals(result, other);
    setFields(other);
    other.setPaging(Paging.ofAll(DOCS));
    assertNotEquals(result, other);
    setFields(other);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    setFields(result);
    assertEquals(result.propertyNames().size(), 3);
    final Meta bean = result.metaBean();
    assertEquals(bean.documents().get(result), DOCS);
    assertEquals(bean.paging().get(result), PAGING);
    assertEquals(bean.versionCorrection().get(result), VC);
    assertEquals(result.property("documents").get(), DOCS);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ExchangeSearchResult result = new ExchangeSearchResult();
    setFields(result);
    assertEncodeDecodeCycle(ExchangeSearchResult.class, result);
  }

  private static void setFields(final ExchangeSearchResult result) {
    result.setDocuments(DOCS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
  }
}
