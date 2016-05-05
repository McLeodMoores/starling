/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.mcleodmoores.starling.client.portfolio.PortfolioKey;
import com.mcleodmoores.starling.client.portfolio.PortfolioManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.EmptyAggregatedExecutionLog;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ResultModelImpl}.
 */
@Test(groups = TestGroup.UNIT)
public class ResultModelImplTest {
  /** A portfolio master */
  private static final PortfolioMaster PORTFOLIO_MASTER = new InMemoryPortfolioMaster();
  /** A position master */
  private static final PositionMaster POSITION_MASTER = new InMemoryPositionMaster();
  /** A position source */
  private static final PositionSource POSITION_SOURCE = new MasterPositionSource(PORTFOLIO_MASTER, POSITION_MASTER);
  /** A security master */
  private static final SecurityMaster SECURITY_MASTER = new InMemorySecurityMaster();
  /** A security source */
  private static final SecuritySource SECURITY_SOURCE = new MasterSecuritySource(SECURITY_MASTER);
  /** The first calculation configuration name */
  private static final String CALCULATION_CONFIGURATION_NAME_1 = "CalcConfig1";
  /** The second calculation configuration name */
  private static final String CALCULATION_CONFIGURATION_NAME_2 = "CalcConfig2";
  /** The correlation id for positions and trades */
  private static final String CORRELATION_ID = "TEST~A1";
  /** The portfolio node name */
  private static final String PORTFOLIO_NODE_NAME = "Portfolio Node";
  /** A security */
  private static final Security SECURITY;
  /** A trade */
  private static final Trade TRADE;
  /** A position */
  private static final Position POSITION;
  /** A portfolio node */
  private static final PortfolioNode PORTFOLIO_NODE;
  /** A portfolio */
  private static final Portfolio PORTFOLIO;
  static {
    final PortfolioManager manager = new PortfolioManager(PORTFOLIO_MASTER, POSITION_MASTER, POSITION_SOURCE, SECURITY_MASTER, SECURITY_SOURCE);
    final FinancialSecurity security = new EquitySecurity("EXCH", "EXCH", "A", Currency.USD);
    security.addExternalId(ExternalId.of("Test", "5"));
    final Trade trade = new SimpleTrade(SimpleSecurityLink.of(security), BigDecimal.ONE, new SimpleCounterparty(ExternalId.of("Test", "Ctpty")), 
        LocalDate.of(2016, 1, 1), OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));
    trade.addAttribute(ResultModelImpl.CORRELATION_ID_ATTRIBUTE, CORRELATION_ID);
    final SimplePosition position = new SimplePosition();
    position.addTrade(trade);
    position.setQuantity(BigDecimal.ONE);
    position.setSecurityLink(SimpleSecurityLink.of(security));
    position.addAttribute(ResultModelImpl.CORRELATION_ID_ATTRIBUTE, CORRELATION_ID);
    final SimplePortfolioNode portfolioNode = new SimplePortfolioNode(PORTFOLIO_NODE_NAME);
    portfolioNode.addPosition(position);
    final SimplePortfolio portfolio = new SimplePortfolio("Portfolio");
    portfolio.getRootNode().addChildNode(portfolioNode);
    // easiest way to get versions with unique ids
    final PortfolioKey key = manager.savePortfolio(portfolio);
    PORTFOLIO = manager.loadPortfolio(key);
    PORTFOLIO_NODE = PORTFOLIO.getRootNode().getChildNodes().get(0);
    POSITION = PORTFOLIO_NODE.getPositions().get(0);
    TRADE = POSITION.getTrades().iterator().next();
    SECURITY = TRADE.getSecurity();
  }
  /** The first result properties */
  private static final ValueProperties PROPERTIES_1 = ValueProperties.with(CURRENCY, "USD").with(FUNCTION, "Function1").get();
  /** The second result properties */
  private static final ValueProperties PROPERTIES_2 = ValueProperties.with(FUNCTION, "Function2").get();
  /** A portfolio node result */
  private static final ComputedValueResult PORTFOLIO_NODE_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(PORTFOLIO_NODE), PROPERTIES_1), 2000), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A position result */
  private static final ComputedValueResult POSITION_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(POSITION), PROPERTIES_1), 200), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A trade result */
  private static final ComputedValueResult TRADE_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(TRADE), PROPERTIES_1), 20), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A trade result */
  private static final ComputedValueResult TRADE_RESULT_2 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(DELTA, ComputationTargetSpecification.of(TRADE), PROPERTIES_2), 3), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A security result */
  private static final ComputedValueResult SECURITY_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(SECURITY), PROPERTIES_1), 2), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A primitive result */
  private static final ComputedValueResult PRIMITIVE_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "500"), PROPERTIES_1), 40), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A null target result */
  private static final ComputedValueResult NULL_TARGET_RESULT_1 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, PROPERTIES_1), 4), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A legacy target result */
  private static final ComputedValueResult LEGACY_TARGET_RESULT_2 = new ComputedValueResult(
      new ComputedValue(ValueSpecification.of(YIELD_CURVE, ComputationTargetType.CURRENCY, Currency.USD.getUniqueId(), PROPERTIES_2), 5), 
        EmptyAggregatedExecutionLog.INSTANCE);
  /** A view result model */
  private static final InMemoryViewComputationResultModel VIEW_RESULT_MODEL = new InMemoryViewComputationResultModel();
  /** The view name */
  private static final String VIEW_NAME = "View";
  /** The view definition */
  private static final ViewDefinition VIEW_DEFINITION;

  static {
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, PORTFOLIO_NODE_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, POSITION_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, TRADE_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, SECURITY_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, PRIMITIVE_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_1, NULL_TARGET_RESULT_1);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_2, TRADE_RESULT_2);
    VIEW_RESULT_MODEL.addValue(CALCULATION_CONFIGURATION_NAME_2, LEGACY_TARGET_RESULT_2);
    VIEW_DEFINITION = new ViewDefinition(VIEW_NAME, PORTFOLIO.getUniqueId(), UserPrincipal.getLocalUser());
    final ViewCalculationConfiguration calculationConfig1 = new ViewCalculationConfiguration(VIEW_DEFINITION, CALCULATION_CONFIGURATION_NAME_1);
    calculationConfig1.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, PRESENT_VALUE, PROPERTIES_1);
    calculationConfig1.addSpecificRequirement(new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "500")));
    calculationConfig1.addSpecificRequirement(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, PROPERTIES_1));
    final ViewCalculationConfiguration calculationConfig2 = new ViewCalculationConfiguration(VIEW_DEFINITION, CALCULATION_CONFIGURATION_NAME_2);
    calculationConfig2.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, DELTA, PROPERTIES_2);
    calculationConfig2.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.USD), PROPERTIES_2));
    VIEW_DEFINITION.addViewCalculationConfiguration(calculationConfig1);
    VIEW_DEFINITION.addViewCalculationConfiguration(calculationConfig2);
  }

  /**
   * Tests the behaviour when the view result model is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewResultModel() {
    new ResultModelImpl(null, VIEW_DEFINITION, POSITION_SOURCE);
  }

  /**
   * Tests the behaviour when the view definition is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewDefinition() {
    new ResultModelImpl(VIEW_RESULT_MODEL, null, POSITION_SOURCE);
  }

  /**
   * Tests the behaviour when the position source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPositionSource() {
    new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, null);
  }

  /**
   * Tests that the results are sorted into the correct target type keys for all calculation configurations in the view.
   */
  @Test
  public void testTargetTypeKeys() {
    final ResultModelImpl model = new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, POSITION_SOURCE);
    final List<TargetKey> portfolioNodeTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.PORTFOLIO_NODE));
    // root and node level target
    assertEquals(portfolioNodeTarget.size(), 2);
    assertEqualsNoOrder(portfolioNodeTarget, Arrays.asList(PortfolioNodeTargetKey.of(""), PortfolioNodeTargetKey.of("/" + PORTFOLIO_NODE_NAME)));
    final List<TargetKey> positionTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.POSITION));
    // one position
    assertEquals(positionTarget.size(), 1);
    assertEquals(positionTarget.get(0), PositionTargetKey.of(ExternalId.parse(CORRELATION_ID)));
    final List<TargetKey> tradeTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.TRADE));
    // one trade
    assertEquals(tradeTarget.size(), 1);
    assertEquals(tradeTarget.get(0), TradeTargetKey.of(ExternalId.parse(CORRELATION_ID)));
    // to check whether all target types are used
    final List<TargetKey> specificRequirementTargets = 
        model.getTargetKeys(EnumSet.of(ResultModel.TargetType.PRIMITIVE, ResultModel.TargetType.MARKET_DATA, ResultModel.TargetType.LEGACY));
    assertEquals(specificRequirementTargets.size(), 4);
    assertEqualsNoOrder(specificRequirementTargets, 
        Arrays.asList(PrimitiveTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("Test", "500"))), MarketDataTargetKey.instance(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(SECURITY)), LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.USD))));
  }

  /**
   * Tests that the market data results are correctly identified.
   */
  @Test
  public void testMarketDataResultKeys() {
    final ResultModelImpl model = new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, POSITION_SOURCE);
    final Set<ResultKey> marketDataResultKeys = model.getRequestedMarketDataResultKeys();
    assertEquals(marketDataResultKeys.size(), 3);
    final ResultKey firstResult = ResultKey.of(CALCULATION_CONFIGURATION_NAME_1, 
        ResultType.builder()
        .valueRequirementName(PRIMITIVE_RESULT_1.getSpecification().getValueName())
        .properties(PRIMITIVE_RESULT_1.getSpecification().getProperties())
        .build());
    final ResultKey secondResult = ResultKey.of(CALCULATION_CONFIGURATION_NAME_1, 
        ResultType.builder()
        .valueRequirementName(NULL_TARGET_RESULT_1.getSpecification().getValueName())
        .properties(NULL_TARGET_RESULT_1.getSpecification().getProperties())
        .build());
    final ResultKey thirdResult = ResultKey.of(CALCULATION_CONFIGURATION_NAME_2, 
        ResultType.builder()
        .valueRequirementName(LEGACY_TARGET_RESULT_2.getSpecification().getValueName())
        .properties(LEGACY_TARGET_RESULT_2.getSpecification().getProperties())
        .build());
    assertEqualsNoOrder(marketDataResultKeys, Arrays.asList(firstResult, secondResult, thirdResult));
  }

  /**
   * Tests that the portfolio results that are expected are correctly identified.
   */
  @Test
  public void testPortfolioResultKeys() {
    final ResultModelImpl model = new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, POSITION_SOURCE);
    final Set<ResultKey> portfolioResultKeys = model.getRequestedPortfolioResultKeys();
    assertEquals(portfolioResultKeys.size(), 2);
    final ResultKey firstResult = ResultKey.of(CALCULATION_CONFIGURATION_NAME_1, 
        ResultType.builder()
        .valueRequirementName(TRADE_RESULT_1.getSpecification().getValueName())
        .properties(TRADE_RESULT_1.getSpecification().getProperties())
        .build());
    final ResultKey secondResult = ResultKey.of(CALCULATION_CONFIGURATION_NAME_2, 
        ResultType.builder()
        .valueRequirementName(TRADE_RESULT_2.getSpecification().getValueName())
        .properties(TRADE_RESULT_2.getSpecification().getProperties())
        .build());
    assertEqualsNoOrder(portfolioResultKeys, Arrays.asList(firstResult, secondResult));
  }

  /**
   * Tests that all result keys are identified.
   */
  @Test
  public void testAllResultKeys() {
    final ResultModelImpl model = new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, POSITION_SOURCE);
    final Set<ResultKey> resultKeys = model.getAllResolvedResultKeys();
    // two portfolio results and three specific
    assertEquals(resultKeys.size(), 5);
  }

  /**
   * Tests that the results are assigned to the correct target key type.
   */
  @Test
  public void testGetResultsForTarget() {
    final ResultModelImpl model = new ResultModelImpl(VIEW_RESULT_MODEL, VIEW_DEFINITION, POSITION_SOURCE);
    assertEquals(model.getResultsForTarget(PortfolioNodeTargetKey.of("/Portfolio Node")).size(), 1);
    assertEquals(model.getResultsForTarget(PositionTargetKey.of(ExternalId.parse(CORRELATION_ID))).size(), 1);
    assertEquals(model.getResultsForTarget(TradeTargetKey.of(ExternalId.parse(CORRELATION_ID))).size(), 2);
    assertEquals(model.getResultsForTarget(MarketDataTargetKey.instance()).size(), 1);
    assertEquals(model.getResultsForTarget(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.USD))).size(), 1);
  }

  /**
   * Tests a portfolio with positions and trades that don't have correlation ids.
   */
  @Test
  public void testWithoutCorrelationIds() {
    final PortfolioManager manager = new PortfolioManager(PORTFOLIO_MASTER, POSITION_MASTER, POSITION_SOURCE, SECURITY_MASTER, SECURITY_SOURCE);
    final FinancialSecurity security = new EquitySecurity("EXCH", "EXCH", "B", Currency.USD);
    security.addExternalId(ExternalId.of("Test", "50"));
    final Trade trade = new SimpleTrade(SimpleSecurityLink.of(security), BigDecimal.ONE, new SimpleCounterparty(ExternalId.of("Test", "Ctpty")), 
        LocalDate.of(2016, 1, 1), OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));
    final SimplePosition position = new SimplePosition();
    position.addTrade(trade);
    position.setQuantity(BigDecimal.ONE);
    position.setSecurityLink(SimpleSecurityLink.of(security));
    final String portfolioNodeName = "Portfolio Node No Correlation Ids";
    final SimplePortfolioNode portfolioNode = new SimplePortfolioNode(portfolioNodeName);
    portfolioNode.addPosition(position);
    final SimplePortfolio portfolio = new SimplePortfolio("Portfolio No Correlation Ids");
    portfolio.getRootNode().addChildNode(portfolioNode);
    // easiest way to get versions with unique ids
    final PortfolioKey key = manager.savePortfolio(portfolio);
    final Portfolio portfolioWithUid = manager.loadPortfolio(key);
    final PortfolioNode portfolioNodeWithUid = portfolioWithUid.getRootNode().getChildNodes().get(0);
    final Position positionWithUid = portfolioNodeWithUid.getPositions().get(0);
    final Trade tradeWithUid = positionWithUid.getTrades().iterator().next();
    final InMemoryViewComputationResultModel viewResultModel = new InMemoryViewComputationResultModel();
    final ViewDefinition viewDefinition = new ViewDefinition("View No Correlation Ids", portfolioWithUid.getUniqueId(), UserPrincipal.getLocalUser());
    viewResultModel.addValue(CALCULATION_CONFIGURATION_NAME_1, new ComputedValueResult(
        new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(positionWithUid), PROPERTIES_1), 200), 
          EmptyAggregatedExecutionLog.INSTANCE));
    viewResultModel.addValue(CALCULATION_CONFIGURATION_NAME_1, new ComputedValueResult(
        new ComputedValue(ValueSpecification.of(PRESENT_VALUE, ComputationTargetSpecification.of(tradeWithUid), PROPERTIES_1), 20), 
          EmptyAggregatedExecutionLog.INSTANCE));
    final ViewCalculationConfiguration calculationConfig1 = new ViewCalculationConfiguration(VIEW_DEFINITION, CALCULATION_CONFIGURATION_NAME_1);
    calculationConfig1.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, PRESENT_VALUE, PROPERTIES_1);
    viewDefinition.addViewCalculationConfiguration(calculationConfig1);
    final ResultModelImpl model = new ResultModelImpl(viewResultModel, viewDefinition, POSITION_SOURCE);
    final List<TargetKey> portfolioNodeTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.PORTFOLIO_NODE));
    // root and node level target
    assertEquals(portfolioNodeTarget.size(), 2);
    assertEqualsNoOrder(portfolioNodeTarget, Arrays.asList(PortfolioNodeTargetKey.of(""), PortfolioNodeTargetKey.of("/" + portfolioNodeName)));
    final List<TargetKey> positionTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.POSITION));
    // one position
    assertEquals(positionTarget.size(), 1);
    // should use the security external id
    assertEquals(positionTarget.get(0), PositionTargetKey.of(security.getExternalIdBundle().getExternalId(ExternalScheme.of("Test"))));
    final List<TargetKey> tradeTarget = model.getTargetKeys(EnumSet.of(ResultModel.TargetType.TRADE));
    // one trade
    assertEquals(tradeTarget.size(), 1);
    assertEquals(tradeTarget.get(0), TradeTargetKey.of(security.getExternalIdBundle().getExternalId(ExternalScheme.of("Test"))));
  }
}
