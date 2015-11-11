package com.mcleodmoores.starling.client.marketdata;

import com.mcleodmoores.starling.client.utils.TestUtils;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Created by jim on 09/06/15.
 */
public class MarketDataManagerTest {

  private ToolContext _toolContext;

  private MarketDataSet createTestMarketData() {
    MarketDataSet dataSet = MarketDataSet.empty();
    dataSet.put(MarketDataKey.of(ExternalId.of("MY_SCHEME", "AUDUSD").toBundle(), DataField.of("Market_Value")), 1.8);
    dataSet.put(MarketDataKey.of(ExternalId.of("MY_SCHEME", "NZDUSD").toBundle(), DataField.of("Market_Value")), 2.2);
    dataSet.put(MarketDataKey.of(ExternalId.of("MY_SCHEME", "GBPUSD").toBundle(), DataField.of("Market_Value")), 1.5);
    return dataSet;
  }

  @BeforeMethod
  public void setUp() {
    _toolContext = TestUtils.getToolContext();
  }

  @Test
  public void testSaveOrUpdate() throws Exception {
    LocalDate today = LocalDate.now();
    MarketDataManager marketDataManager = new MarketDataManager(_toolContext);
    marketDataManager.saveOrUpdate(createTestMarketData(), today);
    final HistoricalTimeSeriesSource source = _toolContext.getHistoricalTimeSeriesSource();
    final HistoricalTimeSeries historicalTimeSeries = source
        .getHistoricalTimeSeries(ExternalId.of("MY_SCHEME", "AUDUSD").toBundle(), DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), "Market_Value");
    final LocalDateDoubleTimeSeries timeSeries = historicalTimeSeries.getTimeSeries();
    Assert.assertEquals(timeSeries.size(), 1);
    Assert.assertEquals(timeSeries.getValue(today), 1.8);
    final HistoricalTimeSeries historicalTimeSeries2 = source
        .getHistoricalTimeSeries(ExternalId.of("MY_SCHEME", "NZDUSD").toBundle(), DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), "Market_Value");
    final LocalDateDoubleTimeSeries timeSeries2 = historicalTimeSeries2.getTimeSeries();
    Assert.assertEquals(timeSeries2.size(), 1);
    Assert.assertEquals(timeSeries2.getValue(today), 2.2);
    final HistoricalTimeSeries historicalTimeSeries3 = source
        .getHistoricalTimeSeries(ExternalId.of("MY_SCHEME", "GBPUSD").toBundle(), DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), "Market_Value");
    final LocalDateDoubleTimeSeries timeSeries3 = historicalTimeSeries3.getTimeSeries();
    Assert.assertEquals(timeSeries3.size(), 1);
    Assert.assertEquals(timeSeries3.getValue(today), 1.5);
  }

  @Test
  public void testGetRequiredData() throws Exception {

  }
}