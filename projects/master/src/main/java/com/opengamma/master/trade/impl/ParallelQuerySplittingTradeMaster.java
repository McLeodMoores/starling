/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;

/**
 * A {@link QuerySplittingTradeMaster} implementation that makes the underlying requests in parallel.
 */
public class ParallelQuerySplittingTradeMaster extends QuerySplittingTradeMaster {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelQuerySplittingTradeMaster.class);

  public ParallelQuerySplittingTradeMaster(final TradeMaster underlying) {
    super(underlying);
  }

  @Override
  protected Map<UniqueId, TradeDocument> callSplitGetRequest(final Collection<Collection<UniqueId>> requests) {
    return super.parallelSplitGetRequest(requests);
  }

  @Override
  protected TradeSearchResult callSplitSearchRequest(final Collection<TradeSearchRequest> requests) {
    final TradeSearchResult mergedResult = new TradeSearchResult();
    final PoolExecutor.Service<TradeSearchResult> service = parallelService(new CompletionListener<TradeSearchResult>() {

      @Override
      public void success(final TradeSearchResult result) {
        synchronized (mergedResult) {
          mergeSplitSearchResult(mergedResult, result);
        }
      }

      @Override
      public void failure(final Throwable error) {
        LOGGER.error("Caught exception", error);
      }

    });
    LOGGER.debug("Issuing {} parallel queries", requests.size());
    final long t = System.nanoTime();
    for (final TradeSearchRequest request : requests) {
      service.execute(new Callable<TradeSearchResult>() {
        @Override
        public TradeSearchResult call() throws Exception {
          LOGGER.debug("Requesting {} Trades", request.getTradeObjectIds().size());
          final long time = System.nanoTime();
          final TradeSearchResult result = getUnderlying().search(request);
          LOGGER.info("{} Trades queried in {}ms", request.getTradeObjectIds().size(), (System.nanoTime() - time) / 1.e6);
          return result;
        }
      });
    }
    try {
      service.join();
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    LOGGER.info("Finished queries for {} Trade in {}ms", mergedResult.getDocuments().size(), (System.nanoTime() - t) / 1.e6);
    return mergedResult;
  }

}
