/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.testutils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.component.StarlingToolContext;
import com.mcleodmoores.starling.client.marketdata.MarketDataKey;
import com.mcleodmoores.starling.client.marketdata.MarketDataManager;
import com.mcleodmoores.starling.client.marketdata.MarketDataSet;
import com.mcleodmoores.starling.client.results.ResultKey;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.ResultModel.TargetType;
import com.mcleodmoores.starling.client.results.SynchronousJob;
import com.mcleodmoores.starling.client.results.TargetKey;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.stateless.StatelessAnalyticService;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.analytics.PositionOrTradeScalingFunction;
import com.opengamma.financial.analytics.PositionTradeScalingFunction;
import com.opengamma.financial.analytics.SummingFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.StandardEquityModelFunction;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class StarlingTestUtilsTest {


  /**
   * Tests the output when a primitive is requested. The data is requested through a {@link HistoricalMarketDataProvider} and
   * so an empty function configuration source can be used. The expected result is a single value for the market data identifier.
   */
  @Test
  public void testPrimitiveOnlyView() throws Exception {
    final ToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final ExternalId marketDataId = ExternalSchemes.syntheticSecurityId("TEST");
    final double marketDataValue = 100;
    dataSet.put(MarketDataKey.of(marketDataId.toBundle()), marketDataValue);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final ViewDefinition viewDefinition = createMarketDataOnlyView(toolContext, marketDataId);
    final StatelessAnalyticService analyticService = new StatelessAnalyticService(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(),
      toolContext.getPositionSource(), toolContext.getSecurityMaster(), toolContext.getSecuritySource(), toolContext.getConfigMaster(),
      toolContext.getConfigSource(), toolContext.getViewProcessor());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    try (final SynchronousJob job = analyticService.createSynchronousJob(viewKey, new SimplePortfolio("Test"), ExternalSchemes.OG_SYNTHETIC_TICKER, now, today.toLocalDate())) {
      final ResultModel resultModel = job.run();
      final List<TargetKey> targetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.PRIMITIVE));
      assertEquals(targetKeys.size(), 1);
      final Map<ResultKey, ComputedValueResult> result = resultModel.getResultsForTarget(targetKeys.get(0));
      assertEquals(result.size(), 1);
      final Map.Entry<ResultKey, ComputedValueResult> entry = result.entrySet().iterator().next();
      final ResultKey resultKey = entry.getKey();
      assertEquals(resultKey.getColumnSet(), "Test");
      assertEquals(resultKey.getResultType().getValueRequirementName(), MarketDataRequirementNames.MARKET_VALUE);
      final ComputedValueResult computedValueResult = entry.getValue();
      assertEquals(computedValueResult.getValue(), marketDataValue);
    } catch (final Exception e) {
      toolContext.close();
      throw e;
    }
    toolContext.close();
  }

  /**
   * Tests the output when a security-level output is requested but there is not an appropriate function in the function list to provide
   * this output (as an empty function configuration source was used). In this case, the graph cannot be built and so there is an empty
   * portfolio node-level output only.
   */
  @Test
  public void testNoGraphView() throws Exception {
    final ToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final ExternalId marketDataId = ExternalSchemes.syntheticSecurityId("TEST");
    final double marketValue = 100;
    final long tradeQuantity = 123;
    final long positionQuantity = 456;
    // market data is available
    dataSet.put(MarketDataKey.of(marketDataId.toBundle()), marketValue);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final ExternalId tradeId = ExternalId.of("TEST_ID", "Equity");
    final SimplePortfolio portfolio = createSingleCurrencyEquityPortfolio(toolContext, tradeId, marketDataId, tradeQuantity, positionQuantity);
    final ViewDefinition viewDefinition = createEquityPresentValueView(toolContext);
    final StatelessAnalyticService analyticService = new StatelessAnalyticService(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(),
      toolContext.getPositionSource(), toolContext.getSecurityMaster(), toolContext.getSecuritySource(), toolContext.getConfigMaster(),
      toolContext.getConfigSource(), toolContext.getViewProcessor());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    try (final SynchronousJob job = analyticService.createSynchronousJob(viewKey, portfolio, ExternalSchemes.OG_SYNTHETIC_TICKER, now, today.toLocalDate())) {
      final ResultModel resultModel = job.run();
      final List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.PORTFOLIO_NODE));
      assertEquals(portfolioTargetKeys.size(), 2); // root and child node
      assertNull(resultModel.getResultsForTarget(portfolioTargetKeys.get(0)));
      assertNull(resultModel.getResultsForTarget(portfolioTargetKeys.get(1)));
      final List<TargetKey> positionTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.POSITION));
      assertEquals(positionTargetKeys.size(), 1);
      assertNull(resultModel.getResultsForTarget(positionTargetKeys.get(0)));
      final List<TargetKey> tradeTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.TRADE));
      assertEquals(tradeTargetKeys.size(), 1);
      assertNull(resultModel.getResultsForTarget(tradeTargetKeys.get(0)));
    } catch (final Exception e) {
      toolContext.close();
      throw e;
    }
    toolContext.close();
  }

  @Test
  public void testNoMarketDataView() throws Exception {
    final StarlingToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final ExternalId marketDataId = ExternalSchemes.syntheticSecurityId("TEST");
    // no market data supplied for id
    final long tradeQuantity = 123;
    final long positionQuantity = 456;
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final ExternalId tradeId = ExternalId.of("TEST_ID", "Equity");
    final SimplePortfolio portfolio = createSingleCurrencyEquityPortfolio(toolContext, tradeId, marketDataId, tradeQuantity, positionQuantity);
    final ViewDefinition viewDefinition = createEquityPresentValueView(toolContext);
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Arrays.asList(new StaticFunctionConfiguration(StandardEquityModelFunction.class.getName())),
        Arrays.asList(
            new ParameterizedFunctionConfiguration(PositionTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(PositionOrTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(SummingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE))
            ));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(functionDefinition, functionDefinition.getName(), FunctionConfigurationDefinition.class));
    final StatelessAnalyticService analyticService = new StatelessAnalyticService(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(),
      toolContext.getPositionSource(), toolContext.getSecurityMaster(), toolContext.getSecuritySource(), toolContext.getConfigMaster(),
      toolContext.getConfigSource(), toolContext.getViewProcessor());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    try (final SynchronousJob job = analyticService.createSynchronousJob(viewKey, portfolio, ExternalScheme.of("TEST_ID"), now, today.toLocalDate())) {
      final ResultModel resultModel = job.run();
      final List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.PORTFOLIO_NODE));
      assertEquals(portfolioTargetKeys.size(), 2); // root and child node
      assertNull(resultModel.getResultsForTarget(portfolioTargetKeys.get(0)));
      assertNull(resultModel.getResultsForTarget(portfolioTargetKeys.get(1)));
      final List<TargetKey> positionTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.POSITION));
      assertEquals(positionTargetKeys.size(), 1);
      assertNull(resultModel.getResultsForTarget(positionTargetKeys.get(0)));
      final List<TargetKey> tradeTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.TRADE));
      assertEquals(tradeTargetKeys.size(), 1);
      assertNull(resultModel.getResultsForTarget(tradeTargetKeys.get(0)));
    } catch (final Exception e) {
      toolContext.close();
      throw e;
    }
    toolContext.close();
  }

  @Test
  public void testSuccessfulSingleEquityView() throws Exception {
    final StarlingToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final ExternalId marketDataId = ExternalSchemes.syntheticSecurityId("TEST");
    final double marketValue = 100.;
    final long tradeQuantity = 123;
    final long positionQuantity = 1;
    dataSet.put(MarketDataKey.of(marketDataId.toBundle()), marketValue);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final ExternalId tradeId = ExternalId.of("TEST_ID", "Equity");
    final SimplePortfolio portfolio = createSingleCurrencyEquityPortfolio(toolContext, tradeId, marketDataId, tradeQuantity, positionQuantity);
    final ViewDefinition viewDefinition = createEquityPresentValueView(toolContext);
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Arrays.asList(new StaticFunctionConfiguration(StandardEquityModelFunction.class.getName())),
        Arrays.asList(
            new ParameterizedFunctionConfiguration(PositionTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(PositionOrTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(SummingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE))
            ));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(functionDefinition, functionDefinition.getName(), FunctionConfigurationDefinition.class));
    final StatelessAnalyticService analyticService = new StatelessAnalyticService(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(),
      toolContext.getPositionSource(), toolContext.getSecurityMaster(), toolContext.getSecuritySource(), toolContext.getConfigMaster(),
      toolContext.getConfigSource(), toolContext.getViewProcessor());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    try (final SynchronousJob job = analyticService.createSynchronousJob(viewKey, portfolio, ExternalScheme.of("TEST_ID"), now, today.toLocalDate())) {
      final ResultModel resultModel = job.run();
      final List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.PORTFOLIO_NODE));
      assertEquals(portfolioTargetKeys.size(), 2); // root and child node
      final Map<ResultKey, ComputedValueResult> rootNodeResult = resultModel.getResultsForTarget(portfolioTargetKeys.get(0));
      final Map.Entry<ResultKey, ComputedValueResult> rootNodeEntry = rootNodeResult.entrySet().iterator().next();
      assertEquals(rootNodeEntry.getKey().getColumnSet(), "Test");
      assertEquals(rootNodeEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(rootNodeEntry.getValue().getValue(), marketValue * tradeQuantity);
      final Map<ResultKey, ComputedValueResult> childNodeResult = resultModel.getResultsForTarget(portfolioTargetKeys.get(1));
      final Map.Entry<ResultKey, ComputedValueResult> childNodeEntry = childNodeResult.entrySet().iterator().next();
      assertEquals(childNodeEntry.getKey().getColumnSet(), "Test");
      assertEquals(childNodeEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(childNodeEntry.getValue().getValue(), marketValue * tradeQuantity);
      final List<TargetKey> positionTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.POSITION));
      assertEquals(positionTargetKeys.size(), 1);
      final Map.Entry<ResultKey, ComputedValueResult> positionEntry = resultModel.getResultsForTarget(positionTargetKeys.get(0)).entrySet().iterator().next();
      assertEquals(positionEntry.getKey().getColumnSet(), "Test");
      assertEquals(positionEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(positionEntry.getValue().getValue(), marketValue * tradeQuantity);
      final List<TargetKey> tradeTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.TRADE));
      assertEquals(tradeTargetKeys.size(), 1);
      final Map.Entry<ResultKey, ComputedValueResult> tradeEntry = resultModel.getResultsForTarget(tradeTargetKeys.get(0)).entrySet().iterator().next();
      assertEquals(tradeEntry.getKey().getColumnSet(), "Test");
      assertEquals(tradeEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(tradeEntry.getValue().getValue(), marketValue * tradeQuantity);
    } catch (final Exception e) {
      toolContext.close();
      throw e;
    }
    toolContext.close();
  }

  @Test
  public void testSuccessfulMultiEquityView() throws Exception {
    final StarlingToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final List<ExternalId> marketDataIds = Arrays.asList(ExternalSchemes.syntheticSecurityId("TEST10"), ExternalSchemes.syntheticSecurityId("TEST20"));
    final double marketValue1 = 100.;
    final double marketValue2 = 10.;
    final List<Long> tradeQuantities = Arrays.asList(123L, 789L);
    final List<Long> positionQuantities = Arrays.asList(1L, 1L);
    final double expectedPositionValue1 = marketValue1 * tradeQuantities.get(0);
    final double expectedPositionValue2 = marketValue2 * tradeQuantities.get(1);
    final double expectedPortfolioValue = expectedPositionValue1 + expectedPositionValue2;
    dataSet.put(MarketDataKey.of(marketDataIds.get(0).toBundle()), marketValue1);
    dataSet.put(MarketDataKey.of(marketDataIds.get(1).toBundle()), marketValue2);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final List<ExternalId> tradeIds = Arrays.asList(ExternalId.of("TEST_ID", "Equity10"), ExternalId.of("TEST_ID", "Equity20"));
    final SimplePortfolio portfolio = createSingleCurrencyEquityPortfolio(toolContext, tradeIds, marketDataIds, tradeQuantities, positionQuantities);
    final ViewDefinition viewDefinition = createEquityPresentValueView(toolContext);
    final FunctionConfigurationDefinition functionDefinition = new FunctionConfigurationDefinition("TEST_FUNCTIONS", Arrays.asList("TEST_FUNCTIONS"),
        Arrays.asList(new StaticFunctionConfiguration(StandardEquityModelFunction.class.getName())),
        Arrays.asList(
            new ParameterizedFunctionConfiguration(PositionTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(PositionOrTradeScalingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE)),
            new ParameterizedFunctionConfiguration(SummingFunction.class.getName(), Collections.singletonList(ValueRequirementNames.PRESENT_VALUE))
            ));
    ConfigMasterUtils.storeByName(toolContext.getConfigMaster(), ConfigItem.of(functionDefinition, functionDefinition.getName(), FunctionConfigurationDefinition.class));
    final StatelessAnalyticService analyticService = new StatelessAnalyticService(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(),
      toolContext.getPositionSource(), toolContext.getSecurityMaster(), toolContext.getSecuritySource(), toolContext.getConfigMaster(),
      toolContext.getConfigSource(), toolContext.getViewProcessor());
    final ViewKey viewKey = ViewKey.of(viewDefinition.getName(), viewDefinition.getUniqueId());
    try (final SynchronousJob job = analyticService.createSynchronousJob(viewKey, portfolio, ExternalScheme.of("TEST_ID"), now, today.toLocalDate())) {
      final ResultModel resultModel = job.run();
      final List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.PORTFOLIO_NODE));
      assertEquals(portfolioTargetKeys.size(), 2); // root and child node
      final Map<ResultKey, ComputedValueResult> rootNodeResult = resultModel.getResultsForTarget(portfolioTargetKeys.get(0));
      final Map.Entry<ResultKey, ComputedValueResult> rootNodeEntry = rootNodeResult.entrySet().iterator().next();
      assertEquals(rootNodeEntry.getKey().getColumnSet(), "Test");
      assertEquals(rootNodeEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(rootNodeEntry.getValue().getValue(), expectedPortfolioValue);
      final Map<ResultKey, ComputedValueResult> childNodeResult = resultModel.getResultsForTarget(portfolioTargetKeys.get(1));
      final Map.Entry<ResultKey, ComputedValueResult> childNodeEntry = childNodeResult.entrySet().iterator().next();
      assertEquals(childNodeEntry.getKey().getColumnSet(), "Test");
      assertEquals(childNodeEntry.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(childNodeEntry.getValue().getValue(), expectedPortfolioValue);
      final List<TargetKey> positionTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.POSITION));
      assertEquals(positionTargetKeys.size(), 2);
      final Map.Entry<ResultKey, ComputedValueResult> positionEntry1 = resultModel.getResultsForTarget(positionTargetKeys.get(0)).entrySet().iterator().next();
      assertEquals(positionEntry1.getKey().getColumnSet(), "Test");
      assertEquals(positionEntry1.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(positionEntry1.getValue().getValue(), expectedPositionValue1);
      final Map.Entry<ResultKey, ComputedValueResult> positionEntry2 = resultModel.getResultsForTarget(positionTargetKeys.get(1)).entrySet().iterator().next();
      assertEquals(positionEntry2.getKey().getColumnSet(), "Test");
      assertEquals(positionEntry2.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(positionEntry2.getValue().getValue(), expectedPositionValue2);
      final List<TargetKey> tradeTargetKeys = resultModel.getTargetKeys(EnumSet.of(TargetType.TRADE));
      assertEquals(tradeTargetKeys.size(), 2);
      final Map.Entry<ResultKey, ComputedValueResult> tradeEntry1 = resultModel.getResultsForTarget(tradeTargetKeys.get(0)).entrySet().iterator().next();
      assertEquals(tradeEntry1.getKey().getColumnSet(), "Test");
      assertEquals(tradeEntry1.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(tradeEntry1.getValue().getValue(), expectedPositionValue1);
      final Map.Entry<ResultKey, ComputedValueResult> tradeEntry2 = resultModel.getResultsForTarget(tradeTargetKeys.get(1)).entrySet().iterator().next();
      assertEquals(tradeEntry2.getKey().getColumnSet(), "Test");
      assertEquals(tradeEntry2.getKey().getResultType().getValueRequirementName(), ValueRequirementNames.PRESENT_VALUE);
      assertEquals(tradeEntry2.getValue().getValue(), expectedPositionValue2);
    } catch (final Exception e) {
      toolContext.close();
      throw e;
    }
    toolContext.close();
  }

  /**
   * Creates a {@link ViewDefinition} that requests only market data and stores it in the {@link ConfigMaster}.
   * @param toolContext  the tool context
   * @param marketDataId  the market data id
   * @return  the view definition
   */
  private static ViewDefinition createMarketDataOnlyView(final ToolContext toolContext, final ExternalId marketDataId) {
    final ConfigMaster configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "configMaster");
    final String viewName = "Market Data Test View";
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, null, UserPrincipal.getLocalUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, "Test");
    defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE,
        marketDataId));
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(viewDefinition, viewName));
    return viewDefinition;
  }

  private static ViewDefinition createEquityPresentValueView(final ToolContext toolContext) {
    final ConfigMaster configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "configMaster");
    final String viewName = "Single Equity Test View";
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, null, UserPrincipal.getLocalUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, "Test");
    defaultCalculationConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE,
        ValueProperties.builder().with(ValuePropertyNames.AGGREGATION, MissingInputsFunction.AGGREGATION_STYLE_MISSING).withOptional(ValuePropertyNames.AGGREGATION).get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(viewDefinition, viewName));
    return viewDefinition;
  }

  private static SimplePortfolio createSingleCurrencyEquityPortfolio(final ToolContext toolContext, final ExternalId tradeId, final ExternalId marketDataId,
      final long tradeQuantity, final long positionQuantity) {
    return createSingleCurrencyEquityPortfolio(toolContext, Collections.singletonList(tradeId), Collections.singletonList(marketDataId), Collections.singletonList(tradeQuantity),
        Collections.singletonList(positionQuantity));
  }

  private static SimplePortfolio createSingleCurrencyEquityPortfolio(final ToolContext toolContext, final List<ExternalId> tradeIds, final List<ExternalId> marketDataIds,
      final List<Long> tradeQuantities, final List<Long> positionQuantities) {
    final int size = tradeIds.size();
    assertEquals(size, marketDataIds.size());
    assertEquals(size, tradeQuantities.size());
    assertEquals(size, positionQuantities.size());
    final List<SimplePosition> positions = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      final ExternalId tradeId = tradeIds.get(i);
      final ExternalId marketDataId = marketDataIds.get(i);
      final long tradeQuantity = tradeQuantities.get(i);
      final long positionQuantity = positionQuantities.get(i);
      final EquitySecurity security = new EquitySecurity("", "", marketDataId.getValue(), Currency.USD);
      security.addExternalId(marketDataId);
      security.addExternalId(tradeId);
      final SimpleTrade trade = new SimpleTrade(security, BigDecimal.valueOf(tradeQuantity), new SimpleCounterparty(ExternalId.of("Test", "Ctpty")), LocalDate.now().minusDays(7), OffsetTime.now());
      trade.addAttribute(ManageableTrade.meta().providerId().name(), tradeId.toString());
      final SimplePosition position = new SimplePosition();
      position.addAttribute(ManageableTrade.meta().providerId().name(), tradeId.toString());
      position.setSecurityLink(SimpleSecurityLink.of(security));
      position.setQuantity(BigDecimal.valueOf(positionQuantity));
      position.addTrade(trade);
      positions.add(position);
    }
    final SimplePortfolio portfolio = new SimplePortfolio("Test");
    final SimplePortfolioNode node = new SimplePortfolioNode("Test");
    portfolio.getRootNode().addChildNode(node);
    node.addPositions(positions);
    return portfolio;
  }
}
