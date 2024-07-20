/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Iterator;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a Trade master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class TradeSearchIterator extends AbstractSearchIterator<TradeDocument, TradeMaster, TradeSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<TradeDocument> iterable(final TradeMaster master, final TradeSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<TradeDocument>() {
      @Override
      public Iterator<TradeDocument> iterator() {
        return new TradeSearchIterator(master, request);
      }
    };
  }

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   *
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   */
  public TradeSearchIterator(final TradeMaster master, final TradeSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected TradeSearchResult doSearch(final TradeSearchRequest request) {
    return getMaster().search(request);
  }

}
