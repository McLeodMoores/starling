/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class OrderIdConfigFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  @SuppressWarnings("deprecation")
  public void testFudgeBuilder() {
    Map<ExternalScheme, Integer> scoreMap = Maps.newHashMap();
    scoreMap.put(ExternalSchemes.BLOOMBERG_TCM, 20); // beacuse if there's both ticker and tcm, you want to see tcm.
    scoreMap.put(ExternalSchemes.BLOOMBERG_TICKER, 19);
    scoreMap.put(ExternalSchemes.RIC, 17);
    scoreMap.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, 16);
    scoreMap.put(ExternalSchemes.ACTIVFEED_TICKER, 15);
    scoreMap.put(ExternalSchemes.SURF, 14);
    scoreMap.put(ExternalSchemes.ISIN, 13);
    scoreMap.put(ExternalSchemes.CUSIP, 12);
    scoreMap.put(ExternalSchemes.SEDOL1, 11);
    scoreMap.put(ExternalSchemes.OG_SYNTHETIC_TICKER, 10);
    scoreMap.put(ExternalSchemes.BLOOMBERG_BUID, 5);
    scoreMap.put(ExternalSchemes.BLOOMBERG_BUID_WEAK, 4);
    ExternalIdOrderConfig externalIdOrderConfig = new ExternalIdOrderConfig();
    externalIdOrderConfig.setRateMap(scoreMap);
    cycleObject(ExternalIdOrderConfig.class, externalIdOrderConfig);
  }
}
