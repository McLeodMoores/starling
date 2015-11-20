/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link LatestHistoricalMarketDataSpecificationFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class LatestHistoricalMarketDataSpecificationBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmptyConstructor() {
    assertEncodeDecodeCycle(LatestHistoricalMarketDataSpecification.class, new LatestHistoricalMarketDataSpecification());
  }

  public void testResolutionKey() {
    assertEncodeDecodeCycle(LatestHistoricalMarketDataSpecification.class, new LatestHistoricalMarketDataSpecification("TEST"));
  }
  
  public void testNullResolutionKey() {
    assertEncodeDecodeCycle(LatestHistoricalMarketDataSpecification.class, new LatestHistoricalMarketDataSpecification(null));
  }

}
