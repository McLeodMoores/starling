/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.testutils;

import static org.testng.AssertJUnit.assertEquals;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.env.AnalyticsEnvironment;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class StarlingTestUtilsTest {

  @Test
  public void testPrimitiveOnlyView() {
    AnalyticsEnvironment.setInstance(AnalyticsEnvironment.builder()
        .modelDayCount(DayCounts.ACT_360)
        .fixedAnnuityDefinitionBuilder(new FixedAnnuityDefinitionBuilder())
        .floatingAnnuityDefinitionBuilder(new FloatingAnnuityDefinitionBuilder())
        .build());
    final ToolContext toolContext = StarlingTestUtils.getToolContext();
    final MarketDataSet dataSet = MarketDataSet.empty();
    final ExternalId marketDataId = ExternalSchemes.syntheticSecurityId("TEST");
    final double marketDataValue = 100;
    dataSet.put(MarketDataKey.of(marketDataId.toBundle()), marketDataValue);
    final ZonedDateTime today = ZonedDateTime.now();
    final Instant now = Instant.from(today);
    final MarketDataManager marketDataManager = new MarketDataManager(toolContext);
    marketDataManager.saveOrUpdate(dataSet, today.toLocalDate());
    final ViewDefinition viewDefinition = createMarketDataOnlyView(null, toolContext, marketDataId);
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
      throw new OpenGammaRuntimeException("Unexpected exception", e);
    }
    toolContext.close();
  }

  public static ViewDefinition createMarketDataOnlyView(final UniqueId portfolioId, final ToolContext toolContext, final ExternalId marketDataId) {
    final ConfigMaster configMaster = ArgumentChecker.notNull(toolContext.getConfigMaster(), "configMaster");
    final String viewName = "Market Data Test View";
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getLocalUser());
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
}
