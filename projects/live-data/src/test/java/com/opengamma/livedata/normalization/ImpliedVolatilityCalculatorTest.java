/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ImpliedVolatilityCalculatorTest {

  private static final ImpliedVolatilityCalculator CALCULATOR = new ImpliedVolatilityCalculator();

  /**
   * Tests using the best implied volatility.
   */
  public void best() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BEST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.81);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

  /**
   * Tests using the mid implied volatility.
   */
  public void mid() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.81);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

  /**
   * Tests using the last implied volatility.
   */
  public void last() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

  /**
   * Tests using the bid/ask implied volatility.
   */
  public void bidAsk() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.815, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

  /**
   * Tests using the historical implied volatility.
   */
  public void history() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, 50.80);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = CALCULATOR.apply(OpenGammaFudgeContext.getInstance().newMessage(), "123", store);
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

  /**
   * Tests the case where there is no implied volatility information.
   */
  public void testNoImpliedVolatility() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 100);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", new FieldHistoryStore());
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(100., normalized.getDouble(MarketDataRequirementNames.LAST));
    assertNull(normalized.getByName(MarketDataRequirementNames.IMPLIED_VOLATILITY));
  }
}
