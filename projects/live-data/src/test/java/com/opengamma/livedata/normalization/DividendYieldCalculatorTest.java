/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DividendYieldCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class DividendYieldCalculatorTest {
  private static final DividendYieldCalculator CALCULATOR = new DividendYieldCalculator();

  /**
   * Tests that the original message is returned if there is no annual dividend
   * data in either the live data or last known value.
   */
  public void testReturnSameIfNoAnnualDividend() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.MARKET_VALUE, 100);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", new FieldHistoryStore());
    assertSame(msg, normalized);
  }

  /**
   * Tests that the annual dividend and market value from the live data are used
   * first.
   */
  public void testUseLiveFirst() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 20);
    msg.add(MarketDataRequirementNames.MARKET_VALUE, 100);
    final MutableFudgeMsg history = OpenGammaFudgeContext.getInstance().newMessage();
    history.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 21);
    history.add(MarketDataRequirementNames.MARKET_VALUE, 101);
    final FieldHistoryStore lkv = new FieldHistoryStore();
    lkv.liveDataReceived(history);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", lkv);
    assertEquals(normalized.getAllFields().size(), 3);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.ANNUAL_DIVIDEND), 20., 1e-15);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 100., 1e-15);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.DIVIDEND_YIELD), 0.2, 1e-15);
  }

  /**
   * Tests that the annual dividend and market value from the last known value
   * store is used if there are no live values.
   */
  public void testUseLkvNoLive() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    final MutableFudgeMsg history = OpenGammaFudgeContext.getInstance().newMessage();
    history.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 21);
    history.add(MarketDataRequirementNames.MARKET_VALUE, 101);
    final FieldHistoryStore lkv = new FieldHistoryStore();
    lkv.liveDataReceived(history);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", lkv);
    assertEquals(normalized.getAllFields().size(), 1);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.DIVIDEND_YIELD), 21 / 101., 1e-15);
  }

  /**
   * Tests that the message is returned unchanged if there is no market value
   * and no historical dividend yield value
   */
  public void testReturnSameNoMarketValueNoDividendYield() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 21);
    final MutableFudgeMsg history = OpenGammaFudgeContext.getInstance().newMessage();
    final FieldHistoryStore lkv = new FieldHistoryStore();
    lkv.liveDataReceived(history);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", lkv);
    assertSame(msg, normalized);
  }

  /**
   * Tests that the last known dividend yield is used if there is no market
   * value.
   */
  public void testUseLkvDividendYieldNoMarketValue() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 21);
    final MutableFudgeMsg history = OpenGammaFudgeContext.getInstance().newMessage();
    history.add(MarketDataRequirementNames.DIVIDEND_YIELD, 0.23);
    final FieldHistoryStore lkv = new FieldHistoryStore();
    lkv.liveDataReceived(history);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", lkv);
    assertEquals(normalized.getAllFields().size(), 2);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.ANNUAL_DIVIDEND), 21., 1e-15);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.DIVIDEND_YIELD), 0.23, 1e-15);
  }

  /**
   * Tests that the last known dividend yield is used if the market value is
   * zero.
   */
  public void testUseLkvDividendYieldZeroMarketValue() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.ANNUAL_DIVIDEND, 21);
    msg.add(MarketDataRequirementNames.MARKET_VALUE, 0);
    final MutableFudgeMsg history = OpenGammaFudgeContext.getInstance().newMessage();
    history.add(MarketDataRequirementNames.DIVIDEND_YIELD, 0.23);
    final FieldHistoryStore lkv = new FieldHistoryStore();
    lkv.liveDataReceived(history);
    final MutableFudgeMsg normalized = CALCULATOR.apply(msg, "uid", lkv);
    assertEquals(normalized.getAllFields().size(), 3);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.ANNUAL_DIVIDEND), 21., 1e-15);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0., 1e-15);
    assertEquals(normalized.getDouble(MarketDataRequirementNames.DIVIDEND_YIELD), 0.23, 1e-15);
  }
}
