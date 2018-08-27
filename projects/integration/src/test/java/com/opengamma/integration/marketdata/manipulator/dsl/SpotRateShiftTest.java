/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateShiftTest {

  private static final double DELTA = 0.00000001;
  private static final SpotRateShift ABSOLUTE_SHIFT =
      new SpotRateShift(ScenarioShiftType.ABSOLUTE, 1d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                        CurrencyPair.parse("CHF/JPY")));
  private static final SpotRateShift RELATIVE_SHIFT =
      new SpotRateShift(ScenarioShiftType.RELATIVE, 0.1, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                        CurrencyPair.parse("CHF/JPY")));

  private static ValueSpecification valueSpec(final String currencyPairStr) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    final CurrencyPair currencyPair = CurrencyPair.parse(currencyPairStr);
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(CurrencyPair.TYPE, currencyPair.getUniqueId());
    return new ValueSpecification("SpotRate", targetSpec, properties);
  }

  @Test
  public void normalPair() {
    assertEquals(3d, ABSOLUTE_SHIFT.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(5d, ABSOLUTE_SHIFT.execute(4d, valueSpec("CHF/JPY"), new FunctionExecutionContext()), DELTA);
    assertEquals(2.2d, RELATIVE_SHIFT.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(4.4d, RELATIVE_SHIFT.execute(4d, valueSpec("CHF/JPY"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void inversePair() {
    assertEquals(0.6666666666, ABSOLUTE_SHIFT.execute(2d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.8d, ABSOLUTE_SHIFT.execute(4d, valueSpec("JPY/CHF"), new FunctionExecutionContext()), DELTA);
    assertEquals(1.8181818181818181, RELATIVE_SHIFT.execute(2d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(3.6363636363636362, RELATIVE_SHIFT.execute(4d, valueSpec("JPY/CHF"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void boundedRate() {
    final SpotRateShift upOne = new SpotRateShift(ScenarioShiftType.ABSOLUTE, 1d, 0, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(3d, upOne.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(6d, upOne.execute(5.5, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift downOne = new SpotRateShift(ScenarioShiftType.ABSOLUTE, -1d, 0.5, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1d, downOne.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.5, downOne.execute(1d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift upFiftyPc = new SpotRateShift(ScenarioShiftType.RELATIVE, 0.5, 0.1, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1.5d, upFiftyPc.execute(1d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(6d, upFiftyPc.execute(5d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift downFiftyPc = new SpotRateShift(ScenarioShiftType.RELATIVE, -0.5, 0.2, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1d, downFiftyPc.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.2, downFiftyPc.execute(0.3, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void boundedInverseRate() {
    final SpotRateShift upTwo = new SpotRateShift(ScenarioShiftType.ABSOLUTE, 2d, 0, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(0.4, upTwo.execute(2d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.166666666, upTwo.execute(0.2, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift downTwo = new SpotRateShift(ScenarioShiftType.ABSOLUTE, -2d, 0.1, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(0.5, downTwo.execute(0.25, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(10d, downTwo.execute(0.5, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift upFiftyPc = new SpotRateShift(ScenarioShiftType.RELATIVE, 0.5, 0.1, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(0.333333333, upFiftyPc.execute(0.5, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.166666666, upFiftyPc.execute(0.2, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);

    final SpotRateShift downFiftyPc = new SpotRateShift(ScenarioShiftType.RELATIVE, -0.5, 0.2, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1d, downFiftyPc.execute(0.5d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(5d, downFiftyPc.execute(4d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedTargetType() {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                   Currency.GBP.getUniqueId());
    final ValueSpecification valueSpec = new ValueSpecification("SpotRate", targetSpec, properties);
    ABSOLUTE_SHIFT.execute(2d, valueSpec, new FunctionExecutionContext());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedCurrencyPair() {
    ABSOLUTE_SHIFT.execute(2d, valueSpec("GBP/USD"), new FunctionExecutionContext());
  }

  @Test
  public void dsl() {
    final Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/SpotRateTest.groovy", null);
    final ScenarioDefinition definition = scenario.createDefinition();

    assertEquals("spot rate test", definition.getName());
    final Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();

    final FunctionParameters eurUsdParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/USD"))));
    assertNotNull(eurUsdParams);
    final Object eurUsdValue = ((SimpleFunctionParameters) eurUsdParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator eurUsdManipulator = (CompositeStructureManipulator) eurUsdValue;
    final List eurUsdManipulators = eurUsdManipulator.getManipulators();
    assertEquals(1, eurUsdManipulators.size());
    final SpotRateShift eurUsdShift = new SpotRateShift(ScenarioShiftType.ABSOLUTE, 0.1, ImmutableSet.of(CurrencyPair.parse("EUR/USD")));
    assertEquals(eurUsdShift, eurUsdManipulators.get(0));

    final FunctionParameters gbpAudParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("GBP/AUD"))));
    assertNotNull(gbpAudParams);
    final Object gbpAudValue = ((SimpleFunctionParameters) gbpAudParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator gbpAudManipulator = (CompositeStructureManipulator) gbpAudValue;
    final List gbpAudManipulators = gbpAudManipulator.getManipulators();
    assertEquals(1, gbpAudManipulators.size());
    final SpotRateScaling gbpAudScaling = new SpotRateScaling(1.2, ImmutableSet.of(CurrencyPair.parse("GBP/AUD")));
    assertEquals(gbpAudScaling, gbpAudManipulators.get(0));

    final FunctionParameters eurCadParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/CAD"))));
    assertNotNull(eurCadParams);
    final Object eurCadValue = ((SimpleFunctionParameters) eurCadParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator eurCadManipulator = (CompositeStructureManipulator) eurCadValue;
    final List eurCadManipulators = eurCadManipulator.getManipulators();
    assertEquals(1, eurCadManipulators.size());
    final SpotRateReplace eurCadReplace = new SpotRateReplace(1.5);
    assertEquals(eurCadReplace, eurCadManipulators.get(0));
  }

  @Test
  public void javaApi() {
    final Scenario scenario = new Scenario("Java API test");
    scenario.spotRate().currencyPair("EURUSD").apply().shift(ScenarioShiftType.ABSOLUTE, 0.1);
    scenario.spotRate().currencyPair("GBPAUD").apply().scaling(1.2);
    scenario.spotRate().currencyPair("EURCAD").apply().replace(1.5);
    final ScenarioDefinition definition = scenario.createDefinition();

    assertEquals("Java API test", definition.getName());
    final Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();

    final FunctionParameters eurUsdParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/USD"))));
    assertNotNull(eurUsdParams);
    final Object eurUsdValue = ((SimpleFunctionParameters) eurUsdParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator eurUsdManipulator = (CompositeStructureManipulator) eurUsdValue;
    final List eurUsdManipulators = eurUsdManipulator.getManipulators();
    assertEquals(1, eurUsdManipulators.size());
    final SpotRateShift eurUsdShift = new SpotRateShift(ScenarioShiftType.ABSOLUTE, 0.1, ImmutableSet.of(CurrencyPair.parse("EUR/USD")));
    assertEquals(eurUsdShift, eurUsdManipulators.get(0));

    final FunctionParameters gbpAudParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("GBP/AUD"))));
    assertNotNull(gbpAudParams);
    final Object gbpAudValue = ((SimpleFunctionParameters) gbpAudParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator gbpAudManipulator = (CompositeStructureManipulator) gbpAudValue;
    final List gbpAudManipulators = gbpAudManipulator.getManipulators();
    assertEquals(1, gbpAudManipulators.size());
    final SpotRateScaling gbpAudScaling = new SpotRateScaling(1.2, ImmutableSet.of(CurrencyPair.parse("GBP/AUD")));
    assertEquals(gbpAudScaling, gbpAudManipulators.get(0));

    final FunctionParameters eurCadParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/CAD"))));
    assertNotNull(eurCadParams);
    final Object eurCadValue = ((SimpleFunctionParameters) eurCadParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator eurCadManipulator = (CompositeStructureManipulator) eurCadValue;
    final List eurCadManipulators = eurCadManipulator.getManipulators();
    assertEquals(1, eurCadManipulators.size());
    final SpotRateReplace eurCadReplace = new SpotRateReplace(1.5);
    assertEquals(eurCadReplace, eurCadManipulators.get(0));
  }
}
