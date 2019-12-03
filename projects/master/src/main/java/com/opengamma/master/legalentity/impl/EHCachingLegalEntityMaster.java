/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.cache.EHCachingSearchCache;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.LegalEntitySearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code LegalEntityMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a database. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingLegalEntityMaster extends AbstractEHCachingMaster<LegalEntityDocument> implements LegalEntityMaster {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(com.opengamma.master.legalentity.impl.EHCachingLegalEntityMaster.class);

  /**
   * The document search cache
   */
  private EHCachingSearchCache _documentSearchCache;
  /**
   * The history search cache
   */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name         the cache name, not empty
   * @param underlying   the underlying LegalEntity source, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingLegalEntityMaster(final String name, final LegalEntityMaster underlying, final CacheManager cacheManager) {
    super(name + "LegalEntity", underlying, cacheManager);

    // Create the doc search cache and register a legalentity master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "LegalEntity", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final LegalEntitySearchResult result = ((LegalEntityMaster) getUnderlying()).search((LegalEntitySearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "LegalEntityHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final LegalEntityHistoryResult result = ((LegalEntityMaster) getUnderlying()).history((LegalEntityHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
            EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime search cache
    final LegalEntitySearchRequest defaultSearch = new LegalEntitySearchRequest();
    defaultSearch.setSortOrder(LegalEntitySearchSortOrder.NAME_ASC);
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);

  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityMetaDataResult metaData(final LegalEntityMetaDataRequest request) {
    return ((LegalEntityMaster) getUnderlying()).metaData(request);
  }

  @Override
  public LegalEntitySearchResult search(final LegalEntitySearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<LegalEntityDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final LegalEntitySearchResult result = new LegalEntitySearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      final LegalEntitySearchResult check = ((LegalEntityMaster) getUnderlying()).search(request);
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
  public LegalEntityHistoryResult history(final LegalEntityHistoryRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<LegalEntityDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;
  }

}
