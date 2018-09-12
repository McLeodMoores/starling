/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
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

  /**
   * Removes Joda bean cycle.
   */
  @Override
  protected <T> void assertEncodeDecodeCycle(final Class<T> clazz, final T object) {
    assertEquals(object, cycleObjectProxy(clazz, object));
    assertEquals(object, cycleObjectBytes(clazz, object));
    assertEquals(object, cycleObjectXml(clazz, object));
  }

}
