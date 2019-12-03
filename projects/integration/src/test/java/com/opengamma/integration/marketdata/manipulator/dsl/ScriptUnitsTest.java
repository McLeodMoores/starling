/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ScriptUnitsTest {

  @Test
  public void basisPoints() {
    final Simulation sim = SimulationUtils.createSimulationFromDsl("src/test/groovy/ScriptUnitsTest.groovy", null);
    final Map<String, Scenario> scenarios = sim.getScenarios();

    final Scenario s1 = scenarios.get("s1");
    final Scenario expected1 = new Scenario("s1");
    expected1.curve().apply().parallelShift(1d / 10000);
    assertEquals(expected1, s1);

    final Scenario s2 = scenarios.get("s2");
    final Scenario expected2 = new Scenario("s2");
    expected2.curve().apply().parallelShift(1d / 100);
    assertEquals(expected2, s2);

    final Scenario s3 = scenarios.get("s3");
    final Scenario expected3 = new Scenario("s3");
    expected3.curve().apply().parallelShift(5d / 100);
    assertEquals(expected3, s3);
  }
}
