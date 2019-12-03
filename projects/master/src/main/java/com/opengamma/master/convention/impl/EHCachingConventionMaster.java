/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code ConventionMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a database. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingConventionMaster extends AbstractEHCachingMaster<ConventionDocument> implements ConventionMaster {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EHCachingConventionMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;
  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name  the cache name, not empty
   * @param underlying  the underlying Convention source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingConventionMaster(final String name, final ConventionMaster underlying, final CacheManager cacheManager) {
    super(name + "Convention", underlying, cacheManager);

    // Create the doc search cache and register a convention master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Convention", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final ConventionSearchResult result = ((ConventionMaster) getUnderlying()).search((ConventionSearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "ConventionHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final ConventionHistoryResult result = ((ConventionMaster) getUnderlying()).history((ConventionHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime search cache
    final ConventionSearchRequest defaultSearch = new ConventionSearchRequest();
    defaultSearch.setSortOrder(ConventionSearchSortOrder.NAME_ASC);
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);

  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionMetaDataResult metaData(final ConventionMetaDataRequest request) {
    return ((ConventionMaster) getUnderlying()).metaData(request);
  }

  @Override
  public ConventionSearchResult search(final ConventionSearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<ConventionDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final ConventionSearchResult result = new ConventionSearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      final ConventionSearchResult check = ((ConventionMaster) getUnderlying()).search(request);
      if (!result.getPaging().equals(check.getPaging())) {
        LOGGER.error("_documentSearchCache.getCache().getName() + \" returned paging:\\n\"" + result.getPaging()
        + "\nbut the underlying master returned paging:\n" + check.getPaging());
      }
      if (!result.getDocuments().equals(check.getDocuments())) {
        LOGGER.error(_documentSearchCache.getCache().getName() + " returned documents:\n" + result.getDocuments()
        + "\nbut the underlying master returned documents:\n" + check.getDocuments());
      }
    }

    return result;
  }

  @Override
  public ConventionHistoryResult history(final ConventionHistoryRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<ConventionDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final ConventionHistoryResult result = new ConventionHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;
  }

}
