/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.loader.config.QuandlCurveConfigurationsGenerator.Configurations;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuandlCurveConfigurationsGenerator}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlCurveConfigurationsGeneratorTest {

  /**
   * Tests that the expected number of conventions are generated. EUR, GBP, CHF and JPY have single-curve
   * configurations, while USD has a two-curve configuration.
   */
  @Test
  public void test() {
    final Configurations configurations = QuandlCurveConfigurationsGenerator.createConfigurations();
    assertEquals(configurations.getAbstractCurveDefinitions().size(), 6);
    assertEquals(configurations.getCurveConstructionConfigurations().size(), 5);
    assertEquals(configurations.getCurveNodeIdMappers().size(), 6);
  }
}
