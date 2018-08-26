/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test.unittest.dealstest;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.analytics.test.IRCurveParser;
import com.opengamma.financial.analytics.test.IRSwapSecurity;
import com.opengamma.financial.analytics.test.IRSwapTradeParser;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for CZK deals.
 */
@Test(groups = TestGroup.UNIT)
public class CZKTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(CZKTest.class);
  private static final String CURRENCY = "CZK";

  private static final String ON_NAME = "CZK_PRIBOR_6M_ERS";
  private static final String THREE_MONTH_NAME = "CZK_PRIBOR_6M_ERS";
  private static final String SIX_MONTH_NAME = "CZK_PRIBOR_6M_ERS";
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_3M_CURVE_NAME = "Forward 3M";
  private static final String FORWARD_6M_CURVE_NAME = "Forward 6M";

  private static final Currency CCY = Currency.CZK;

  private static final String PAY_CURRENCY = "LEG1_CCY";

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  public void test() throws Exception {

    // Build the clean list of swap
    final IRSwapTradeParser tradeParser = new IRSwapTradeParser();
    final Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Trades14Oct.csv");
    final List<IRSwapSecurity> trades = tradeParser.parseCSVFile(resource.getURL());
    final List<IRSwapSecurity> tradesClean = Lists.newArrayList();
    for (final IRSwapSecurity irSwapSecurity : trades) {

      final String currency = irSwapSecurity.getRawInput().getString(PAY_CURRENCY);
      if (currency.equals(CURRENCY)) {
        tradesClean.add(irSwapSecurity);
      }

    }
    // Build the curve bundle
    final HashMap<String, Currency> ccyMap = new HashMap<>();
    ccyMap.put(DISCOUNTING_CURVE_NAME, CCY);
    ccyMap.put(FORWARD_3M_CURVE_NAME, CCY);
    ccyMap.put(FORWARD_6M_CURVE_NAME, CCY);
    final FXMatrix fx = new FXMatrix(CCY);
    final YieldCurveBundle curvesClean = new YieldCurveBundle(fx, ccyMap);

    final IRCurveParser curveParser = new IRCurveParser();
    final Resource resourceCurve = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");
    final List<InterpolatedDoublesCurve> curves = curveParser.parseCSVFile(resourceCurve.getURL());

    for (final InterpolatedDoublesCurve interpolatedDoublesCurve : curves) {

      final String name = interpolatedDoublesCurve.getName();
      if (name.equals(ON_NAME)) {
        curvesClean.setCurve(DISCOUNTING_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(THREE_MONTH_NAME)) {
        curvesClean.setCurve(FORWARD_3M_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(SIX_MONTH_NAME)) {
        curvesClean.setCurve(FORWARD_6M_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
    }

    // Convert the swap security into a swap definition
    //TODO
    LOGGER.warn("Got {} trades", trades.size());
  }

}
