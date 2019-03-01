/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import java.net.URI;
import java.util.List;

import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.ObjectId;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;

/**
 * Provides access to a remote {@link BatchMaster}.
 */
public class RemoteBatchMaster extends AbstractRemoteMaster implements BatchMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteBatchMaster(final URI baseUri) {
    super(baseUri);
  }


  @Override
  public Pair<List<RiskRun>, Paging> searchRiskRun(final BatchRunSearchRequest batchRunSearchRequest) {
    ArgumentChecker.notNull(batchRunSearchRequest, "batchRunSearchRequest");
    final URI uri = DataBatchRunUris.uriSearch(getBaseUri());
    return accessRemote(uri).post(Pair.class, batchRunSearchRequest);
  }

  @Override
  public RiskRun getRiskRun(final ObjectId batchId) {
    ArgumentChecker.notNull(batchId, "batchId");
    final URI uri = DataBatchRunUris.uri(getBaseUri(), batchId);
    return accessRemote(uri).get(RiskRun.class);
  }

  @Override
  public void deleteRiskRun(final ObjectId batchId) {
    ArgumentChecker.notNull(batchId, "batchId");
    final URI uri = DataBatchRunUris.uri(getBaseUri(), batchId);
    accessRemote(uri).delete();
  }

  @Override
  public Pair<List<ViewResultEntry>, Paging> getBatchValues(final ObjectId batchId, final PagingRequest pagingRequest) {
    ArgumentChecker.notNull(batchId, "batchId");
    final URI uri = DataBatchRunUris.uriBatchValues(getBaseUri(), batchId);
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }

  //////////////////////////////////


  @Override
  public Pair<List<MarketData>, Paging> getMarketData(final PagingRequest pagingRequest) {
    final URI uri = DataMarketDataUris.uriMarketData(getBaseUri());
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }

  @Override
  public MarketData getMarketDataById(final ObjectId marketDataId) {
    final URI uri = DataMarketDataUris.uriMarketData(getBaseUri(), marketDataId);
    return accessRemote(uri).get(MarketData.class);
  }

  @Override
  public Pair<List<MarketDataValue>, Paging> getMarketDataValues(final ObjectId marketDataId, final PagingRequest pagingRequest) {
    final URI uri = DataMarketDataUris.uriMarketDataValues(getBaseUri(), marketDataId);
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }
}
