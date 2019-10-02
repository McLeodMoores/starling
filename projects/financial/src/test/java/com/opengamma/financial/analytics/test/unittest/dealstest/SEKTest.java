/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test.unittest.dealstest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.financial.analytics.test.IRSwapSecurity;
import com.opengamma.financial.analytics.test.IRSwapTradeParser;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for SEK deals.
 */
@Test(groups = TestGroup.UNIT)
public class SEKTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(SEKTest.class);
  private static final String CURRENCY = "SEK";
  private static final String PAY_CURRENCY = "LEG1_CCY";

  public void test() throws Exception {
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
    LOGGER.warn("Got {} trades", trades.size());
  }

}
