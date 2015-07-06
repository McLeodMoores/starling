/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.curve.exposure.ContractCategoryExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.CounterpartyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.RegionExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityAndCurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityAndRegionExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityAndSettlementExchangeExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityAndTradingExchangeExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityCurrencyAndRegionExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.SecurityTypeExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.TradeAttributeExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.UnderlyingExposureFunction;

/**
 * Unit tests for {@link NamedExposureFunctionFactory}.
 */
public class NamedExposureFunctionFactoryTest {

  /**
   * Tests the behaviour when an unknown exposure function is requested.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnknown() {
    NamedExposureFunctionFactory.of("Unknown");
  }

  /**
   * Tests that the factory returns the expected exposure functions.
   */
  @Test
  public void test() {
    assertEquals(NamedExposureFunctionFactory.of("Contract Category").getName(), ContractCategoryExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Counterparty").getName(), CounterpartyExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Currency").getName(), CurrencyExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Region").getName(), RegionExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security").getName(), SecurityExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security / Currency").getName(), SecurityAndCurrencyExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security / Currency / Region").getName(), SecurityCurrencyAndRegionExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security / Region").getName(), SecurityAndRegionExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security / Settlement Exchange").getName(), SecurityAndSettlementExchangeExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security / Trading Exchange").getName(), SecurityAndTradingExchangeExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Security Type").getName(), SecurityTypeExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Trade Attribute").getName(), TradeAttributeExposureFunction.NAME);
    assertEquals(NamedExposureFunctionFactory.of("Underlying").getName(), UnderlyingExposureFunction.NAME);
  }
}
