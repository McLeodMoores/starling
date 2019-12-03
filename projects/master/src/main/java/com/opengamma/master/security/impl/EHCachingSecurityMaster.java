/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

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
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.IntObjectPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code SecurityMaster}, mainly intended to reduce the frequency and repetition of queries to
 * the underlying master.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingSecurityMaster extends AbstractEHCachingMaster<SecurityDocument> implements SecurityMaster {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EHCachingSecurityMaster.class);

  /** The document search cache */
  private EHCachingSearchCache _documentSearchCache;

  /** The history search cache */
  private EHCachingSearchCache _historySearchCache;

  /**
   * Creates an instance over an underlying master specifying the cache manager.
   *
   * @param name          the cache name, not null
   * @param underlying    the underlying security master, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingSecurityMaster(final String name, final SecurityMaster underlying, final CacheManager cacheManager) {
    super(name + "Security", underlying, cacheManager);

    // Create the document search cache and register a security master searcher
    _documentSearchCache = new EHCachingSearchCache(name + "Security", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final SecuritySearchResult result = ((SecurityMaster) getUnderlying()).search((SecuritySearchRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Create the history search cache and register a security master searcher
    _historySearchCache = new EHCachingSearchCache(name + "SecurityHistory", cacheManager, new EHCachingSearchCache.Searcher() {
      @Override
      public IntObjectPair<List<UniqueId>> search(final Bean request, final PagingRequest pagingRequest) {
        // Fetch search results from underlying master
        final SecurityHistoryResult result = ((SecurityMaster) getUnderlying()).history((SecurityHistoryRequest)
            EHCachingSearchCache.withPagingRequest(request, pagingRequest));

        // Cache the result documents
        EHCachingSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return IntObjectPair.of(result.getPaging().getTotalItems(),
                                 EHCachingSearchCache.extractUniqueIds(result.getDocuments()));
      }
    });

    // Prime document search cache
    final SecuritySearchRequest defaultSearch = new SecuritySearchRequest();
    defaultSearch.setSortOrder(SecuritySearchSortOrder.NAME_ASC);
    _documentSearchCache.prefetch(defaultSearch, PagingRequest.FIRST_PAGE);
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _documentSearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false);

    final List<SecurityDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final SecuritySearchResult result = new SecuritySearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now());
    result.setVersionCorrection(vc);

    // Debug: check result against underlying
    if (EHCachingSearchCache.TEST_AGAINST_UNDERLYING) {
      final SecuritySearchResult check = ((SecurityMaster) getUnderlying()).search(request);
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
              System.out.println("\tCache     \t" + i + ":\t" + result.getDocuments().get(i).getUniqueId());
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {

    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _historySearchCache.prefetch(EHCachingSearchCache.withPagingRequest(request, null), request.getPagingRequest());

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    final IntObjectPair<List<UniqueId>> pair = _historySearchCache.search(
        EHCachingSearchCache.withPagingRequest(request, null),
        request.getPagingRequest(), false); // don't block until cached

    final List<SecurityDocument> documents = new ArrayList<>();
    for (final UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    final SecurityHistoryResult result = new SecurityHistoryResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirstInt()));
    return result;
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    return ((SecurityMaster) getUnderlying()).metaData(request);
  }

}
