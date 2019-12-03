/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
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
import org.threeten.bp.ZoneOffset;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeHistoryResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);
  private static final ManageableExchange EX_1 =
      new ManageableExchange(ExternalIdBundle.of("exch", "LSE"), "LSE", ExternalSchemes.currencyRegionId(Currency.GBP).toBundle(), ZoneOffset.UTC);
  private static final ManageableExchange EX_2 =
      new ManageableExchange(ExternalIdBundle.of("exch", "LSE"), "LSE", ExternalSchemes.currencyRegionId(Currency.GBP).toBundle(), ZoneOffset.UTC);
  private static final ManageableExchange EX_3 =
      new ManageableExchange(ExternalIdBundle.of("exch", "LSE"), "LSE", ExternalSchemes.currencyRegionId(Currency.GBP).toBundle(), ZoneOffset.UTC);
  private static final ExchangeDocument DOC_1 = new ExchangeDocument();
  private static final ExchangeDocument DOC_2 = new ExchangeDocument();
  private static final ExchangeDocument DOC_3 = new ExchangeDocument();
  static {
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v1"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v2"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v3"));
    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(200000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_1.setExchange(EX_1);
    DOC_2.setExchange(EX_2);
    DOC_3.setExchange(EX_3);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new ExchangeHistoryResult((Collection<ExchangeDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new ExchangeHistoryResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<ExchangeDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ExchangeHistoryResult result = new ExchangeHistoryResult(documents);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstExchange(), EX_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getExchanges(), Arrays.asList(EX_1, EX_2, EX_3));
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new ExchangeHistoryResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstExchangeNoDocuments() {
    assertNull(new ExchangeHistoryResult().getFirstExchange());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleExchangeNoDocuments() {
    new ExchangeHistoryResult().getSingleExchange();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleExchangeMultipleDocuments() {
    new ExchangeHistoryResult(Arrays.asList(DOC_1, DOC_2)).getSingleExchange();
  }

  /**
   * Tests getting a single exchange.
   */
  @Test
  public void testGetSingleExchange() {
    assertEquals(new ExchangeHistoryResult(Collections.singletonList(DOC_2)).getSingleExchange(), EX_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<ExchangeDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ExchangeHistoryResult result = new ExchangeHistoryResult(docs);
    result.setPaging(PAGING);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "ExchangeHistoryResult{paging=Paging[first=0, size=20, totalItems=3], "
        + "documents=[ExchangeDocument{versionFromInstant=1970-01-02T03:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=null, correctionToInstant=null, exchange=ManageableExchange{uniqueId=null, "
        + "externalIdBundle=Bundle[exch~LSE], name=LSE, regionIdBundle=Bundle[ISO_CURRENCY_ALPHA3~GBP], "
        + "timeZone=Z, detail=[]}, uniqueId=oid~val~v1}, ExchangeDocument{versionFromInstant=1970-01-03T07:33:20Z, "
        + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
        + "exchange=ManageableExchange{uniqueId=null, externalIdBundle=Bundle[exch~LSE], name=LSE, "
        + "regionIdBundle=Bundle[ISO_CURRENCY_ALPHA3~GBP], timeZone=Z, detail=[]}, uniqueId=oid~val~v2}, "
        + "ExchangeDocument{versionFromInstant=1970-01-04T11:20:00Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, exchange=ManageableExchange{uniqueId=null, externalIdBundle=Bundle[exch~LSE], "
        + "name=LSE, regionIdBundle=Bundle[ISO_CURRENCY_ALPHA3~GBP], timeZone=Z, detail=[]}, uniqueId=oid~val~v3}]}");
    final ExchangeHistoryResult other = new ExchangeHistoryResult(docs);
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
    final List<ExchangeDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final ExchangeHistoryResult result = new ExchangeHistoryResult(docs);
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
    final ExchangeHistoryResult result = new ExchangeHistoryResult();
    assertEncodeDecodeCycle(ExchangeHistoryResult.class, result);
    final List<ExchangeDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(ExchangeHistoryResult.class, result);
    result.setPaging(PAGING);
    assertEncodeDecodeCycle(ExchangeHistoryResult.class, result);
  }
}