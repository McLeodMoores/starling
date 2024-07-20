/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeHistoryResult;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code TradeMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a {@code DbTradeMaster}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingTradeMaster extends AbstractEHCachingMaster<TradeDocument> implements TradeMaster {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EHCachingTradeMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;

  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not null
   * @param underlying    the underlying Trade master, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingTradeMaster(final String name, final TradeMaster underlying, final CacheManager cacheManager) {
    super(name + "Trade", underlying, cacheManager);

    // Create the doc search cache and register a Trade master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Trade", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final TradeSearchResult result = ((TradeMaster) getUnderlying()).search((TradeSearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "TradeHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final TradeHistoryResult result = ((TradeMaster) getUnderlying()).history((TradeHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime search cache
    final TradeSearchRequest defaultSearch = new TradeSearchRequest();
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    return ((TradeMaster) getUnderlying()).getTrade(tradeId);
  }

  @Override
  public TradeSearchResult search(final TradeSearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<TradeDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final TradeSearchResult result = new TradeSearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now());
    result.setVersionCorrection(vc);

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      final TradeSearchResult check = ((TradeMaster) getUnderlying()).search(request);
      if (!result.getPaging().equals(check.getPaging())) {
        LOGGER.error(_documentSearchCache.getCache().getName()
            + "\n\tCache:\t" + result.getPaging()
            + "\n\tUnderlying:\t" + check.getPaging());
      }
      if (!result.getDocuments().equals(check.getDocuments())) {
        System.out.println(_documentSearchCache.getCache().getName() + ": ");
        if (check.getDocuments().size() != result.getDocuments().size()) {
          System.out.println("\tSizes differ (Underlying " + check.getDocuments().size()
              + "; Cache " + result.getDocuments().size() + ")");
        } else {
          for (int i = 0; i < check.getDocuments().size(); i++) {
            if (!check.getDocuments().get(i).equals(result.getDocuments().get(i))) {
              System.out.println("\tUnderlying\t" + i + ":\t" + check.getDocuments().get(i).getUniqueId());
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<TradeDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final TradeHistoryResult result = new TradeHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;
  }

}
