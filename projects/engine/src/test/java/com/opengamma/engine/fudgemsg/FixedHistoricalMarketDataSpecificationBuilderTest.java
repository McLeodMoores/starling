/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link FixedHistoricalMarketDataSpecificationFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class FixedHistoricalMarketDataSpecificationBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmptyConstructor() {
    assertEncodeDecodeCycle(FixedHistoricalMarketDataSpecification.class, new FixedHistoricalMarketDataSpecification(LocalDate.of(2015, 3, 31)));
  }

  public void testResolutionKey() {
    assertEncodeDecodeCycle(FixedHistoricalMarketDataSpecification.class, new FixedHistoricalMarketDataSpecification("TEST", LocalDate.of(2015, 3, 31)));
  }
  
  public void testNullResolutionKey() {
    assertEncodeDecodeCycle(FixedHistoricalMarketDataSpecification.class, new FixedHistoricalMarketDataSpecification(null, LocalDate.of(2017, 3, 31)));
  }

}
