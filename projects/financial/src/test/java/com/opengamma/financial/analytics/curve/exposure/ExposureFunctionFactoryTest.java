/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit test for ExposureFunctionFactory.
 */
@Test(groups = TestGroup.UNIT)
public class ExposureFunctionFactoryTest {

  @Test
  public void testContractCategoryExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), ContractCategoryExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected ContractCategoryExposureFunction", function instanceof ContractCategoryExposureFunction);
  }

  @Test
  public void testCounterpartyExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), CounterpartyExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected CounterpartyExposureFunction", function instanceof CounterpartyExposureFunction);
  }

  @Test
  public void testCurrencyExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), CurrencyExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected CurrencyExposureFunction", function instanceof CurrencyExposureFunction);
  }

  @Test
  public void testRegionExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), RegionExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected RegionExposureFunction", function instanceof RegionExposureFunction);
  }

  @Test
  public void testSecurityAndCurrencyExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), SecurityAndCurrencyExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityAndCurrencyExposureFunction", function instanceof SecurityAndCurrencyExposureFunction);
  }

  @Test
  public void testSecurityAndRegionExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), SecurityAndRegionExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityAndRegionExposureFunction", function instanceof SecurityAndRegionExposureFunction);
  }

  @Test
  public void testSecurityAndSettlementExchangeExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), SecurityAndSettlementExchangeExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityAndSettlementExchangeExposureFunction", function instanceof SecurityAndSettlementExchangeExposureFunction);
  }

  @Test
  public void testSecurityAndTradingExchangeExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), SecurityAndTradingExchangeExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityAndTradingExchangeExposureFunction", function instanceof SecurityAndTradingExchangeExposureFunction);
  }

  @Test
  public void testSecurityExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null), SecurityExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityExposureFunction", function instanceof SecurityExposureFunction);
  }

  @Test
  public void testSecurityTypeExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null),
        SecurityTypeExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected SecurityTypeExposureFunction", function instanceof SecurityTypeExposureFunction);
  }

  public void testTradeAttributeExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null),
        TradeAttributeExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected TradeAttributeExposureFunction", function instanceof TradeAttributeExposureFunction);
  }

  @Test
  public void testUnderlyingExposureFunction() {
    final ExposureFunction function = ExposureFunctionFactory.getExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null),
        UnderlyingExposureFunction.NAME);
    assertNotNull("Null exposure function", function);
    assertTrue("Expected UnderlyingExposureFunction", function instanceof UnderlyingExposureFunction);
  }

}
