/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeHistoryResult;

/**
 * A {@link TradeMaster} which delegates its calls to a list of underlying {@link TradeMaster}s.
 *
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link TradeMaster}.
 */
public class CombinedTradeMaster extends ChangeProvidingCombinedMaster<TradeDocument, TradeMaster> implements TradeMaster {

  public CombinedTradeMaster(final List<TradeMaster> masters) {
    super(masters);
  }

  @Override
  public TradeSearchResult search(final TradeSearchRequest overallRequest) {
    final TradeSearchResult overallResult = new TradeSearchResult();

    pagedSearch(new TradeSearchStrategy() {

      @Override
      public AbstractDocumentsResult<TradeDocument> search(final TradeMaster master, final TradeSearchRequest searchRequest) {
        final TradeSearchResult masterResult = master.search(searchRequest);
        overallResult.setVersionCorrection(masterResult.getVersionCorrection());
        return masterResult;
      }
    }, overallResult, overallRequest);


    return overallResult;
  }

  /**
   * Callback interface for Trade searches
   */
  private interface TradeSearchStrategy extends SearchStrategy<TradeDocument, TradeMaster, TradeSearchRequest> { }


  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<TradeDocument, TradeMaster> {
  }

  public void search(final TradeSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final List<TradeSearchResult> results = Lists.newArrayList();
    for (final TradeMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {
    final TradeMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return new Try<TradeHistoryResult>() {
      @Override
      public TradeHistoryResult tryMaster(final TradeMaster pm) {
        return pm.history(request);
      }
    }.each(request.getObjectId().getScheme());
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    return null;
  }

}
