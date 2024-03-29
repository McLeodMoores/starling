/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * A {@link PortfolioMaster} which delegates its calls to a list of underlying {@link PortfolioMaster}s.
 *
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link PortfolioMaster}.
 */
public class CombinedPortfolioMaster extends ChangeProvidingCombinedMaster<PortfolioDocument, PortfolioMaster> implements PortfolioMaster {

  public CombinedPortfolioMaster(final List<PortfolioMaster> masterList) {
    super(masterList);
  }

  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest overallRequest) {
    final PortfolioSearchResult overallResult = new PortfolioSearchResult();

    pagedSearch(new PortfolioSearchStrategy() {

      @Override
      public AbstractDocumentsResult<PortfolioDocument> search(final PortfolioMaster master, final PortfolioSearchRequest searchRequest) {
        final PortfolioSearchResult masterResult = master.search(searchRequest);
        overallResult.setVersionCorrection(masterResult.getVersionCorrection());
        return masterResult;
      }
    }, overallResult, overallRequest);

    return overallResult;

  }

  /**
   * Callback interface for portfolio searches
   */
  private interface PortfolioSearchStrategy extends SearchStrategy<PortfolioDocument, PortfolioMaster, PortfolioSearchRequest> { }


  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<PortfolioDocument, PortfolioMaster> {
  }

  public void search(final PortfolioSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final List<PortfolioSearchResult> results = Lists.newArrayList();
    for (final PortfolioMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
  }

  @Override
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    final PortfolioMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return new Try<PortfolioHistoryResult>() {
      @Override
      public PortfolioHistoryResult tryMaster(final PortfolioMaster pm) {
        return pm.history(request);
      }
    }.each(request.getObjectId().getScheme());
  }

  @Override
  public ManageablePortfolioNode getNode(final UniqueId nodeId) {
    final PortfolioMaster master = getMasterByScheme(nodeId.getScheme());
    if (master != null) {
      return master.getNode(nodeId);
    }
    return new Try<ManageablePortfolioNode>() {
      @Override
      public ManageablePortfolioNode tryMaster(final PortfolioMaster pm) {
        return pm.getNode(nodeId);
      }
    }.each(nodeId.getScheme());
  }

}
