/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeHistoryResult;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;

/**
 * Trade master which tracks accesses using UniqueIds.
 */
public class DataTrackingTradeMaster extends AbstractDataTrackingMaster<TradeDocument, TradeMaster> implements TradeMaster {

  public DataTrackingTradeMaster(final TradeMaster delegate) {
    super(delegate);
  }

  @Override
  public TradeSearchResult search(final TradeSearchRequest request) {
    final TradeSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {
    final TradeHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    //trades are wrapped by Trades so don't need to
    //be tracked
    return delegate().getTrade(tradeId);
  }



}
