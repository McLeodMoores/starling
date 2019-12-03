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
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class BucketedShiftTest {

  @Test
  public void yieldCurve() {
    final Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/YieldCurveBucketedShiftTest.groovy", null);
    final ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("bucketed shift test", definition.getName());
    final Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    final FunctionParameters params = map.get(new YieldCurveSelector(null, null, null, null, null));
    assertNotNull(params);
    final Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final CompositeStructureManipulator<?> manipulator = (CompositeStructureManipulator<?>) value;
    final List<?> manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    final List<YieldCurveBucketedShift> shifts = ImmutableList.of(new YieldCurveBucketedShift(Period.ofMonths(3), Period.ofMonths(6), 0.001),
        new YieldCurveBucketedShift(Period.ofYears(1), Period.ofYears(2), 0.002));
    final YieldCurveBucketedShiftManipulator expected = new YieldCurveBucketedShiftManipulator(ScenarioShiftType.ABSOLUTE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

}
