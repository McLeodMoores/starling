/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.starling.client.marketdata;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.portfolio.PortfolioKey;
import com.mcleodmoores.starling.client.portfolio.PortfolioManager;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.testutils.StarlingTestUtils;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveMarketDataFunction;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFuturePresentValueFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunction;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link MarketDataManager}.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class MarketDataManagerTest {
  /** Scheme for market data used in this test */
  private static final String TEST_SCHEME = "MY_SCHEME";

  /**
   * Creates a test market data.
   * @return  the market data set
   */
  private static MarketDataSet createTestMarketData() {
    final MarketDataSet dataSet = MarketDataSet.empty();
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "AUDUSD").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)), 1.8);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "NZDUSD").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)), 2.2);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "GBPUSD").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)), 1.5);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "GBP1Y").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)),
        ImmutableLocalDateDoubleTimeSeries.builder()
        .putAll(new LocalDate[] {LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 2)}, new double[] {0.01, 0.02}).build());
    return dataSet;
  }

  /**
   * Tests saving and updating market data using the manager.
   */
  @Test
  public void testSaveOrUpdate() {
    // note these configurations should be used as time series are actually stored and retrieved from the source
    final ToolContext toolContext = StarlingTestUtils.getToolContext("/inmemory/marketdata-test.properties");
    final LocalDate today = LocalDate.now();
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    // saves the data
    marketDataManager.saveOrUpdate(createTestMarketData(), today);
    final HistoricalTimeSeriesSource source = toolContext.getHistoricalTimeSeriesSource();
    HistoricalTimeSeries historicalTimeSeries1 = 
        source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "AUDUSD").toBundle(), DataSource.DEFAULT.getName(), 
            DataProvider.DEFAULT.getName(), MARKET_VALUE);
    LocalDateDoubleTimeSeries timeSeries1 = historicalTimeSeries1.getTimeSeries();
    assertEquals(timeSeries1.size(), 1);
    assertEquals(timeSeries1.getValue(today), 1.8);
    HistoricalTimeSeries historicalTimeSeries2 = 
        source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "NZDUSD").toBundle(), DataSource.DEFAULT.getName(), 
            DataProvider.DEFAULT.getName(), MARKET_VALUE);
    LocalDateDoubleTimeSeries timeSeries2 = historicalTimeSeries2.getTimeSeries();
    assertEquals(timeSeries2.size(), 1);
    assertEquals(timeSeries2.getValue(today), 2.2);
    HistoricalTimeSeries historicalTimeSeries3 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "GBPUSD").toBundle(), 
        DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), MARKET_VALUE);
    LocalDateDoubleTimeSeries timeSeries3 = historicalTimeSeries3.getTimeSeries();
    assertEquals(timeSeries3.size(), 1);
    assertEquals(timeSeries3.getValue(today), 1.5);
    HistoricalTimeSeries historicalTimeSeries4 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "GBP1Y").toBundle(), 
        DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), MARKET_VALUE);
    LocalDateDoubleTimeSeries timeSeries4 = historicalTimeSeries4.getTimeSeries();
    assertEquals(timeSeries4.size(), 2);
    assertEquals(timeSeries4.getValue(LocalDate.of(2016, 1, 1)), 0.01);
    assertEquals(timeSeries4.getValue(LocalDate.of(2016, 1, 2)), 0.02);
    // updates the data for some of the series
    final MarketDataSet updatedData = MarketDataSet.empty();
    updatedData.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "AUDUSD").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)), 1.9);
    updatedData.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "GBP1Y").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)),
        ImmutableLocalDateDoubleTimeSeries.builder().putAll(new LocalDate[] {LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 2)}, 
            new double[] {0.01, 0.03}).build());
    marketDataManager.saveOrUpdate(updatedData, today);
    historicalTimeSeries1 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "AUDUSD").toBundle(), DataSource.DEFAULT.getName(), 
        DataProvider.DEFAULT.getName(), MARKET_VALUE);
    timeSeries1 = historicalTimeSeries1.getTimeSeries();
    assertEquals(timeSeries1.size(), 1);
    assertEquals(timeSeries1.getValue(today), 1.9);
    historicalTimeSeries2 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "NZDUSD").toBundle(), DataSource.DEFAULT.getName(), 
        DataProvider.DEFAULT.getName(), MARKET_VALUE);
    timeSeries2 = historicalTimeSeries2.getTimeSeries();
    assertEquals(timeSeries2.size(), 1);
    assertEquals(timeSeries2.getValue(today), 2.2);
    historicalTimeSeries3 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "GBPUSD").toBundle(), DataSource.DEFAULT.getName(), 
        DataProvider.DEFAULT.getName(), MARKET_VALUE);
    timeSeries3 = historicalTimeSeries3.getTimeSeries();
    assertEquals(timeSeries3.size(), 1);
    assertEquals(timeSeries3.getValue(today), 1.5);
    historicalTimeSeries4 = source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "GBP1Y").toBundle(), DataSource.DEFAULT.getName(), 
        DataProvider.DEFAULT.getName(), MARKET_VALUE);
    timeSeries4 = historicalTimeSeries4.getTimeSeries();
    assertEquals(timeSeries4.size(), 2);
    assertEquals(timeSeries4.getValue(LocalDate.of(2016, 1, 1)), 0.01);
    assertEquals(timeSeries4.getValue(LocalDate.of(2016, 1, 2)), 0.03);
  }

  /**
   * Tests that the save or update method can handle an input that is neither a scalar nor time series.
   */
  @Test
  public void testSaveWrongDataType() {
    final MarketDataSet dataSet = MarketDataSet.empty();
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SCHEME, "JPYUSD").toBundle(), DataField.of(MarketDataRequirementNames.MARKET_VALUE)), "NaN");
    final ToolContext toolContext = StarlingTestUtils.getToolContext("/inmemory/marketdata-test.properties");
    final LocalDate today = LocalDate.now();
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    // saves the data
    marketDataManager.saveOrUpdate(dataSet, today);
    final HistoricalTimeSeriesSource source = toolContext.getHistoricalTimeSeriesSource();
    assertNull(source.getHistoricalTimeSeries(ExternalId.of(TEST_SCHEME, "JPYUSD").toBundle(), 
        DataSource.DEFAULT.getName(), DataProvider.DEFAULT.getName(), MARKET_VALUE));
  }

  /**
   * Tests the behaviour when required market data is requested for a view that is not stored in the master.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoViewInMaster() {
    final ToolContext toolContext = StarlingTestUtils.getToolContext("/inmemory/marketdata-test.properties");
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Collections.<StaticFunctionConfiguration>emptyList(),
        Collections.<ParameterizedFunctionConfiguration>emptyList());
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), 
        ConfigItem.of(functionDefinition, functionDefinition.getName(), FunctionConfigurationDefinition.class));
    final ViewKey viewKey = ViewKey.of("TEST", UniqueId.of("TEST", "1"));
    marketDataManager.getRequiredDataForView(viewKey, now);
  }

  /**
   * Tests that all of the required market data is identified for a view. In this test, the market data
   * required to produce the {@link ValueRequirementNames#CURVE_MARKET_DATA} for a single curve is all
   * of the ids returned from the curve node id mapper referenced in the curve nodes and does not include
   * any time series data.
   */
  @Test
  public void testGetRequiredDataNoTimeSeries() {
    // the always available live market data provider in this configuration means that the graph will build
    final ToolContext toolContext = StarlingTestUtils.getToolContext("/inmemory/marketdata-test.properties");
    final Set<CurveNode> nodes = new HashSet<>();
    final Map<Tenor, CurveInstrumentProvider> ids = new HashMap<>();
    final Set<ExternalId> expectedIds = new HashSet<>();
    final ExternalId conventionId = ExternalId.of("TEST", "Deposit convention");
    final String curveNodeIdMapperName = "Ids";
    for (int i = 0; i < 10; i++) {
      final Tenor tenor = Tenor.ofMonths(i);
      final ExternalId id = ExternalSchemes.syntheticSecurityId(i + "MCASH");
      expectedIds.add(id);
      nodes.add(new CashNode(Tenor.of(Period.ZERO), tenor, conventionId, curveNodeIdMapperName));
      ids.put(tenor, new StaticCurveInstrumentProvider(id));
    }
    final CurveNodeIdMapper cnim = CurveNodeIdMapper.builder()
        .name(curveNodeIdMapperName)
        .cashNodeIds(ids)
        .build();
    final String curveName = "Curve";
    final CurveDefinition curveDefinition = new InterpolatedCurveDefinition(curveName, nodes, LinearInterpolator1dAdapter.NAME);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Collections.<StaticFunctionConfiguration>emptyList(),
        Collections.singletonList(new ParameterizedFunctionConfiguration(CurveMarketDataFunction.class.getName(), Collections.singletonList(curveName))));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(functionDefinition, functionDefinition.getName(), 
        FunctionConfigurationDefinition.class));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(curveDefinition, curveDefinition.getName(), InterpolatedCurveDefinition.class));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(cnim, cnim.getName(), CurveNodeIdMapper.class));
    final ViewDefinition viewDefinition = createCurveDataView(toolContext, curveDefinition.getName());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    final MarketDataInfo requiredData = marketDataManager.getRequiredDataForView(viewKey, now);
    assertEquals(requiredData.getScalars().size(), nodes.size());
    assertTrue(requiredData.getTimeSeries().isEmpty());
    for (final Map.Entry<MarketDataKey, ? extends MarketDataMetaData> entry : requiredData.getScalars().entrySet()) {
      final MarketDataKey key = entry.getKey();
      final ExternalIdBundle idBundle = key.getExternalIdBundle();
      assertEquals(idBundle.size(), 1);
      final Set<ExternalId> idsForScheme = idBundle.getExternalIds(ExternalSchemes.OG_SYNTHETIC_TICKER);
      assertEquals(idsForScheme.size(), 1);
      assertTrue(expectedIds.contains(idsForScheme.iterator().next()));
    }
  }

  /**
   * Tests that all of the required market data is identified for a view requesting the mark-to-market price of
   * a single future. In this case, the pricing function requires a "live" market data value for the future and
   * a time series looking back seven days to get the previous close price.
   */
  @Test
  public void testGetRequiredDataWithTimeSeries() {
    // the always available historical time series resolver in this configuration means that the graph will build
    final ToolContext toolContext = StarlingTestUtils.getToolContext("/inmemory/marketdata-test.properties");
    final ExternalId futureTicker = ExternalSchemes.syntheticSecurityId("ABH6 Index");
    final ExternalId tradeId = ExternalId.of("TEST_ID", "FUTURE");
    final PortfolioKey portfolioKey = createSingleFuturePortfolio(toolContext, futureTicker, tradeId);
    final ViewDefinition viewDefinition = createSimpleFutureView(toolContext, portfolioKey.getUniqueId());
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Arrays.asList(
            new StaticFunctionConfiguration(SimpleFuturePresentValueFunction.class.getName()),
            new StaticFunctionConfiguration(HistoricalTimeSeriesFunction.class.getName())),
        Collections.<ParameterizedFunctionConfiguration>emptyList());
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(functionDefinition, functionDefinition.getName(), 
        FunctionConfigurationDefinition.class));
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    final MarketDataInfo requiredData = marketDataManager.getRequiredDataForView(viewKey, now);
    // test the scalar data (i.e. the live future price)
    assertEquals(requiredData.getScalars().size(), 1);
    final Map.Entry<MarketDataKey, ? extends MarketDataMetaData> scalarEntry = requiredData.getScalars().entrySet().iterator().next();
    final MarketDataKey scalarKey = scalarEntry.getKey();
    final ExternalIdBundle scalarIdBundle = scalarKey.getExternalIdBundle();
    // two ids in request - the future ticker referenced in the security and the trade id
    assertEquals(scalarIdBundle.size(), 2);
    assertEquals(scalarIdBundle.getExternalIds(ExternalSchemes.OG_SYNTHETIC_TICKER).size(), 1);
    assertEquals(scalarIdBundle.getExternalIds(ExternalSchemes.OG_SYNTHETIC_TICKER).iterator().next(), futureTicker);
    assertEquals(scalarIdBundle.getExternalIds(tradeId.getScheme()).size(), 1);
    assertEquals(scalarIdBundle.getExternalIds(tradeId.getScheme()).iterator().next(), tradeId);
    // test the time series data (i.e. the historical prices of the future)
    assertEquals(requiredData.getTimeSeries().size(), 1);
    final Map.Entry<MarketDataKey, ? extends MarketDataMetaData> tsEntry = requiredData.getTimeSeries().entrySet().iterator().next();
    final MarketDataKey tsKey = tsEntry.getKey();
    final ExternalIdBundle tsIdBundle = tsKey.getExternalIdBundle();
    // two ids in request - the future ticker referenced in the security and the trade id
    assertEquals(tsIdBundle.size(), 2);
    assertEquals(tsIdBundle.getExternalIds(ExternalSchemes.OG_SYNTHETIC_TICKER).size(), 1);
    assertEquals(tsIdBundle.getExternalIds(ExternalSchemes.OG_SYNTHETIC_TICKER).iterator().next(), futureTicker);
    assertEquals(tsIdBundle.getExternalIds(tradeId.getScheme()).size(), 1);
    assertEquals(tsIdBundle.getExternalIds(tradeId.getScheme()).iterator().next(), tradeId);
  }

  //TODO multi-equity portfolio with multiple currencies that are converted
  /**
   * Creates a {@link ViewDefinition} that requests the market data required to construct a single curve.
   * @param toolContext  the tool context
   * @param curveName  the curve name
   * @return  the view definition
   */
  private static ViewDefinition createCurveDataView(final ToolContext toolContext, final String curveName) {
    final ConfigMaster configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "configMaster");
    final String viewName = "Curve Data Test View";
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, null, UserPrincipal.getLocalUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, "Test");
    defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL,
        ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(viewDefinition, viewName));
    return viewDefinition;
  }

  /**
   * Creates a {@link ViewDefinition} that requests the market data and time series required to produce a
   * mark-to-market price for an energy future.
   * @param toolContext  the tool context
   * @param portfolioId  the portfolio id
   * @return  the view definition
   */
  private static ViewDefinition createSimpleFutureView(final ToolContext toolContext, final UniqueId portfolioId) {
    final ConfigMaster configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "configMaster");
    final String viewName = "Simple Future Test View";
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getLocalUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, "Test");
    defaultCalculationConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE, ValueProperties.none());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(viewDefinition, viewName));
    return viewDefinition;
  }

  /**
   * Creates and saves a portfolio containing a single energy future trade.
   * @param toolContext  the tool context
   * @param futureTicker  the future ticker
   * @param tradeId  the future trade id
   * @return the portfolio key
   */
  private static PortfolioKey createSingleFuturePortfolio(final ToolContext toolContext, final ExternalId futureTicker, final ExternalId tradeId) {
    final List<SimplePosition> positions = new ArrayList<>();
    final ExternalId marketDataId = futureTicker;
    final long tradeQuantity = 1;
    final long positionQuantity = 1;
    final FutureSecurity security = new EnergyFutureSecurity(new Expiry(ZonedDateTime.now().plusMonths(3)), "EXCH", "EXCH", Currency.USD, 1000, "Energy");
    security.addExternalId(marketDataId);
    security.addExternalId(tradeId);
    final SimpleTrade trade = new SimpleTrade(security, BigDecimal.valueOf(tradeQuantity), new SimpleCounterparty(ExternalId.of("Test", "Ctpty")), 
        LocalDate.now().minusDays(7), OffsetTime.now());
    trade.addAttribute(ManageableTrade.meta().providerId().name(), tradeId.toString());
    final SimplePosition position = new SimplePosition();
    position.addAttribute(ManageableTrade.meta().providerId().name(), tradeId.toString());
    position.setSecurityLink(SimpleSecurityLink.of(security));
    position.setQuantity(BigDecimal.valueOf(positionQuantity));
    position.addTrade(trade);
    positions.add(position);
    final SimplePortfolio portfolio = new SimplePortfolio("Test");
    final SimplePortfolioNode node = new SimplePortfolioNode("Test");
    portfolio.getRootNode().addChildNode(node);
    node.addPositions(positions);
    return new PortfolioManager(toolContext).savePortfolio(portfolio);
  }
}