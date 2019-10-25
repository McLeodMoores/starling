/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackstirfutures;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderDiscount;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link UnderlyingMarketPriceSTIRFutureOptionCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class UnderlyingMarketPriceSTIRFutureOptionCalculatorTest {
  private static final InterestRateFutureSecurity UNDERLYING = new InterestRateFutureSecurity(1,
      StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL3()[0], 1, 1.25, 0.25, 100000, 0.25, "name");
  private static final InterestRateFutureOptionMarginSecurity MARGINED_CALL = new InterestRateFutureOptionMarginSecurity(UNDERLYING, 1,
      0.99, true);
  private static final InterestRateFutureOptionMarginTransaction MARGINED_CALL_T = new InterestRateFutureOptionMarginTransaction(
      MARGINED_CALL, 123, 0.995);
  private static final InterestRateFutureOptionMarginSecurity MARGINED_PUT = new InterestRateFutureOptionMarginSecurity(UNDERLYING, 1,
      0.99, false);
  private static final InterestRateFutureOptionMarginTransaction MARGINED_PUT_T = new InterestRateFutureOptionMarginTransaction(
      MARGINED_PUT, 1231, 0.995);
  private static final InterestRateFutureOptionPremiumSecurity PREMIUM_CALL = new InterestRateFutureOptionPremiumSecurity(UNDERLYING, 1,
      0.99, true);
  private static final InterestRateFutureOptionPremiumTransaction PREMIUM_CALL_T = new InterestRateFutureOptionPremiumTransaction(
      PREMIUM_CALL, 1234, 0.002, 0.995);
  private static final InterestRateFutureOptionPremiumSecurity PREMIUM_PUT = new InterestRateFutureOptionPremiumSecurity(UNDERLYING, 1,
      0.99, false);
  private static final InterestRateFutureOptionPremiumTransaction PREMIUM_PUT_T = new InterestRateFutureOptionPremiumTransaction(
      PREMIUM_PUT, 1239, 0.002, 0.995);

  /**
   * Tests that the underlying price is the same for all options.
   */
  @Test
  public void testUnderlyingPrice() {
    final BlackSTIRFuturesProviderInterface data = new BlackSTIRFuturesSmileProviderDiscount(
        StandardDataSetsMulticurveUSD.getCurvesUSDOisL3().getFirst(), ConstantDoublesSurface.from(0.2), UNDERLYING.getIborIndex());
    final double expected = InterestRateFutureSecurityDiscountingMethod.getInstance().price(UNDERLYING, data);
    assertEquals(MARGINED_CALL.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(MARGINED_CALL_T.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(MARGINED_PUT.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(MARGINED_PUT_T.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(PREMIUM_CALL.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(PREMIUM_CALL_T.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(PREMIUM_PUT.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
    assertEquals(PREMIUM_PUT_T.accept(UnderlyingMarketPriceSTIRFutureOptionCalculator.getInstance(), data), expected, 1e-12);
  }

}
