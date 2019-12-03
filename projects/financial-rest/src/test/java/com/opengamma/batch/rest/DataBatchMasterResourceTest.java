/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.domain.CalculationConfiguration;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskRunProperty;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.FudgeResponse;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataBatchMasterResourceTest extends AbstractFudgeBuilderTestCase {

  @Mock
  private BatchMasterWriter _batchMaster;

  private DataBatchMasterResource _batchMasterResource;

  private final RiskRun _riskRun = new RiskRun(
    new MarketData(UniqueId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, "market-data")),
    Instant.now(),
    Instant.now(),
    0,
    newHashSet(new CalculationConfiguration("calc-config")),
    newHashSet(new RiskRunProperty()),
    false,
    VersionCorrection.LATEST,
    UniqueId.of("Scheme", "view-def"),
    "cyclename"
  );

  @BeforeMethod
  public void setUp() throws Exception {
    final List<RiskRun> list = newArrayList(_riskRun);
    final Pair<List<RiskRun>, Paging> batchSearchResult = Pairs.of(list, Paging.ofAll(Collections.emptyList()));

    initMocks(this);
    _batchMasterResource = new DataBatchMasterResource(_batchMaster);
    when(_batchMaster.searchRiskRun((BatchRunSearchRequest) any())).thenReturn(batchSearchResult);
    when(_batchMaster.getRiskRun((ObjectId) any())).thenReturn(_riskRun);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSearch() throws Exception {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();

    Object entity = _batchMasterResource.searchBatchRuns(batchRunSearchRequest).getEntity();
    entity = FudgeResponse.unwrap(entity);
    final Pair<List<RiskRun>, Paging> result = (Pair<List<RiskRun>, Paging>) entity;

    assertTrue(result.getFirst().size() > 0);
    final RiskRun run = result.getFirst().get(0);
    assertEquals(run, _riskRun);
  }

  @Test
  public void testBatchRun() throws Exception {
    final String batchUid = "Scheme~MockUniqueId";
    final DataBatchRunResource batchRunResource = _batchMasterResource.batchRuns(batchUid);

    batchRunResource.deleteBatchRun();

    final Response response = batchRunResource.get();
    assertEquals(response.getEntity(), _riskRun);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = com.opengamma.DataNotFoundException.class)
  public void testBatchRunDataNotFound() throws Exception {
    final BatchMasterWriter batchMaster = mock(BatchMasterWriter.class);
    when(batchMaster.getRiskRun((ObjectId) any())).thenThrow(DataNotFoundException.class);

    final DataBatchMasterResource batchMasterResource = new DataBatchMasterResource(batchMaster);

    final String batchUid = "Scheme~MockUniqueId";
    final DataBatchRunResource batchRunResource = batchMasterResource.batchRuns(batchUid);

    batchRunResource.get();
  }

  @Test
  public void testSnapshots() throws Exception {
    final ObjectId snapshotId = _riskRun.getMarketData().getObjectId();

    when(_batchMaster.getMarketDataById((ObjectId) any())).thenReturn(_riskRun.getMarketData());

    final DataMarketDataResource marketDataResource = _batchMasterResource.getMarketData(snapshotId.toString());

    final MarketData marketData = (MarketData) marketDataResource.get().getEntity();
    assertEquals(marketData.getObjectId(), snapshotId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSearchSnapshots() throws Exception {
    final PagingRequest pagingRequest = PagingRequest.FIRST_PAGE;

    final List<MarketData> marketDataList = newArrayList(_riskRun.getMarketData());
    final Paging paging = Paging.of(pagingRequest, marketDataList);

    when(_batchMaster.getMarketData((PagingRequest) any())).thenReturn(Pairs.of(marketDataList, paging));

    Object entity = _batchMasterResource.searchMarketData(pagingRequest).getEntity();
    entity = FudgeResponse.unwrap(entity);
    final Pair<List<MarketData>, Paging> response = (Pair<List<MarketData>, Paging>) entity;

    assertEquals(response.getFirst().size(), 1);
    assertEquals(response.getSecond(), paging);
  }

}
