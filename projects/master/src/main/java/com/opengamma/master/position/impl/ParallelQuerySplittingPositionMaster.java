/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;

/**
 * A {@link QuerySplittingPositionMaster} implementation that makes the underlying requests in parallel.
 */
public class ParallelQuerySplittingPositionMaster extends QuerySplittingPositionMaster {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParallelQuerySplittingPositionMaster.class);

  public ParallelQuerySplittingPositionMaster(final PositionMaster underlying) {
    super(underlying);
  }

  @Override
  protected Map<UniqueId, PositionDocument> callSplitGetRequest(final Collection<Collection<UniqueId>> requests) {
    return super.parallelSplitGetRequest(requests);
  }

  @Override
  protected PositionSearchResult callSplitSearchRequest(final Collection<PositionSearchRequest> requests) {
    final PositionSearchResult mergedResult = new PositionSearchResult();
    final PoolExecutor.Service<PositionSearchResult> service = parallelService(new CompletionListener<PositionSearchResult>() {

      @Override
      public void success(final PositionSearchResult result) {
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
    for (final PositionSearchRequest request : requests) {
      service.execute(new Callable<PositionSearchResult>() {
        @Override
        public PositionSearchResult call() throws Exception {
          LOGGER.debug("Requesting {} positions", request.getPositionObjectIds().size());
          final long time = System.nanoTime();
          final PositionSearchResult result = getUnderlying().search(request);
          LOGGER.info("{} positions queried in {}ms", request.getPositionObjectIds().size(), (System.nanoTime() - time) / 1.e6);
          return result;
        }
      });
    }
    try {
      service.join();
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    LOGGER.info("Finished queries for {} position in {}ms", mergedResult.getDocuments().size(), (System.nanoTime() - t) / 1.e6);
    return mergedResult;
  }

}
