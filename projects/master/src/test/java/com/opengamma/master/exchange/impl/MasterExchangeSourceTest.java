/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterExchangeSource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterExchangeSourceTest {

  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullMaster() {
    new MasterExchangeSource(null);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetExchangeUniqueIdNoOverrideFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);

    final ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetExchangeUniqueIdNotFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    try {
      test.get(UID);
    } finally {
      verify(mock, times(1)).get(UID);
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetExchangeObjectIdFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);

    final ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetExchangeObjectIdNotFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    try {
      test.get(OID, VC);
    } finally {
      verify(mock, times(1)).get(OID, VC);
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetSingleExchangeExternalIdFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);
    final ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);

    final ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));

    when(mock.search(request)).thenReturn(result);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.getSingle(ID);
    verify(mock, times(1)).search(request);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  public void testGetSingleExchangeExternalIdNotFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);
    final ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);

    final ExchangeSearchResult result = new ExchangeSearchResult();

    when(mock.search(request)).thenReturn(result);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.getSingle(ID);
    verify(mock, times(1)).search(request);

    assertEquals(null, testResult);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetSingleExchangeExternalIdBundleFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);
    final ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);

    final ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));

    when(mock.search(request)).thenReturn(result);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.getSingle(BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  public void testGetSingleExchangeExternalIdBundleVcFound() {
    final ExchangeMaster mock = mock(ExchangeMaster.class);
    final ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);

    final ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));

    when(mock.search(request)).thenReturn(result);
    final MasterExchangeSource test = new MasterExchangeSource(mock);
    final Exchange testResult = test.getSingle(BUNDLE, VC);
    verify(mock, times(1)).search(request);

    assertEquals(example(), testResult);
  }

  // -------------------------------------------------------------------------
  /**
   * @return an example exchange
   */
  protected ManageableExchange example() {
    final ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueId(UID);
    exchange.setName("NYSE");
    exchange.setRegionIdBundle(ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.US)));
    return exchange;
  }

}
