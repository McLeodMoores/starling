/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.testutils.FinancialTestBase;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuandlFedFundsFutureCurveInstrumentProviderBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlFedFundsFutureCurveInstrumentProviderBuilderTest extends FinancialTestBase {

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final QuandlFedFundsFutureCurveInstrumentProvider provider = new QuandlFedFundsFutureCurveInstrumentProvider("CME/FF",
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT, QuandlConstants.ofCode("ON"), MarketDataRequirementNames.LAST);
    assertEquals(cycleObject(QuandlFedFundsFutureCurveInstrumentProvider.class, provider), provider);
  }
}
