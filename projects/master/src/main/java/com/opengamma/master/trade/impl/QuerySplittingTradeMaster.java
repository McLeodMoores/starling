/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractQuerySplittingMaster;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeHistoryResult;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

/**
 * A {@link TradeMaster} implementation that divides search operations into a number of smaller operations to pass to the underlying.
 * This is intended for use with some database backed Trade masters where performance decreases, or becomes unstable, with large queries.
 */
public class QuerySplittingTradeMaster extends AbstractQuerySplittingMaster<TradeDocument, TradeMaster> implements TradeMaster {

  /**
   * The maximum size of request to pass to {@link TradeMaster#search}, zero or negative for no limit.
   */
  private int _maxSearchRequest;

  /**
   * Creates a new instance wrapping the underlying with default properties.
   *
   * @param underlying the underlying Trade master to satisfy the requests, not null
   */
  public QuerySplittingTradeMaster(final TradeMaster underlying) {
    super(underlying);
  }

  /**
   * Returns the maximum number of items to pass to the {@link TradeMaster#search} method in each call.
   *
   * @return the current limit, zero or negative if none
   */
  public int getMaxSearchRequest() {
    return _maxSearchRequest;
  }

  /**
   * Sets the maximum number of items to pass to the {@link TradeMaster#search} method in each call.
   *
   * @param maxSearchRequest the new limit, zero or negative if none
   */
  public void setMaxSearchRequest(final int maxSearchRequest) {
    _maxSearchRequest = maxSearchRequest;
  }

  protected Collection<TradeSearchRequest> splitSearchRequest(final TradeSearchRequest request) {
    if (request.getTradeObjectIds() == null) {
      // Can only split requests with multiple object ids
      return null;
    }
    if (!PagingRequest.ALL.equals(request.getPagingRequest()) && !PagingRequest.NONE.equals(request.getPagingRequest())) {
      // Can only split requests with no paging
      return null;
    }
    int chunkSize = getMaxSearchRequest();
    final int count = request.getTradeObjectIds().size();
    if (chunkSize <= 0 || chunkSize >= count) {
      // Request too small, or splitting is disabled
      return null;
    }
    int chunks = (count + chunkSize - 1) / chunkSize;
    final Collection<TradeSearchRequest> requests = new ArrayList<>();
    final Iterator<ObjectId> Trades = request.getTradeObjectIds().iterator();
    for (int i = 0; i < count;) {
      chunkSize = (count - i) / chunks--;
      final TradeSearchRequest subRequest = request.clone();
      subRequest.getTradeObjectIds().clear();
      for (int j = 0; j < chunkSize && Trades.hasNext(); j++) {
        subRequest.addTradeObjectId(Trades.next());
      }
      requests.add(subRequest);
      i += chunkSize;
    }
    return requests;
  }

  protected void mergeSplitSearchResult(final TradeSearchResult mergeWith, final TradeSearchResult result) {
    final Collection<TradeDocument> documents = result.getDocuments();
    mergeWith.getDocuments().addAll(documents);
    mergeWith.setPaging(Paging.of(PagingRequest.ALL, (mergeWith.getPaging() != null ? mergeWith.getPaging().getTotalItems() : 0) + documents.size()));
    mergeWith.setVersionCorrection(result.getVersionCorrection());
  }

  protected TradeSearchResult callSplitSearchRequest(final Collection<TradeSearchRequest> requests) {
    final TradeSearchResult result = new TradeSearchResult();
    for (final TradeSearchRequest request : requests) {
      mergeSplitSearchResult(result, getUnderlying().search(request));
    }
    return result;
  }

  // TradeMaster

  /**
   * When splitting is enabled, and the request is for more Trades than the split size, two or more requests are made to the underlying master. {@inheritDoc}
   */
  @Override
  public TradeSearchResult search(final TradeSearchRequest request) {
    if (canSplit()) {
      final Collection<TradeSearchRequest> requests = splitSearchRequest(request);
      if (requests == null) {
        // Small query pass-through
        return getUnderlying().search(request);
      }
      // Multiple queries
      return callSplitSearchRequest(requests);
    }
    // Splitting disabled
    return getUnderlying().search(request);
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {
    return getUnderlying().history(request);
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    return getUnderlying().getTrade(tradeId);
  }
}
