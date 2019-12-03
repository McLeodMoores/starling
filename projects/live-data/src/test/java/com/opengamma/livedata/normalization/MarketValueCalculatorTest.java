/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

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
public class MarketValueCalculatorTest {

  /**
   * Tests that the average of bid and ask is used.
   */
  public void bidAskLast() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.80);
    msg.add(MarketDataRequirementNames.ASK, 50.90);
    msg.add(MarketDataRequirementNames.LAST, 50.89);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the mid value is used.
   */
  public void midLast() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.MID, 50.80);
    msg.add(MarketDataRequirementNames.LAST, 50.89);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the average of bid and ask is used.
   */
  public void bidAskOnly() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.80);
    msg.add(MarketDataRequirementNames.ASK, 50.90);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the last price is used.
   */
  public void lastOnly() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 50.89);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(50.89, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that last is used if the bid/ask spread is too large.
   */
  public void bigSpread() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 55.12);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(55.12, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that last is used if the bid/ask spread is too large and that the
   * history is ignored.
   */
  public void bigSpreadHistory() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.LAST, 45); // Should never use this

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 50.52);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.52, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the bid is used if the last value is below the bid.
   */
  public void bigSpreadLowLast() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 44.50);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.0, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the ask is used if the last value is above the ask.
   */
  public void bigSpreadHighLast() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 120.0);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(100.0, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the average of the historical bid/ask is used.
   */
  public void useHistoricalBidAsk() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.BID, 50.0);
    historicalMsg.add(MarketDataRequirementNames.ASK, 51.0);
    historicalMsg.add(MarketDataRequirementNames.MARKET_VALUE, 50.52);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);

    final MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();
    newMsg.add(MarketDataRequirementNames.LAST, 50.89);

    final MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(50.5, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that historical data is used if there is no data.
   */
  public void useHistoricalMarketValueWithEmptyMsg() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.MARKET_VALUE, 50.52);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);

    final MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();

    final MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(50.52, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that the message is empty if no market data is available.
   */
  public void noMarketValueAvailable() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final FieldHistoryStore store = new FieldHistoryStore();

    final MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();

    final MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(0, normalized.getAllFields().size());
  }

  /**
   * Tests that zero is a valid bid.
   */
  public void zeroBid() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 0.0);
    msg.add(MarketDataRequirementNames.ASK, 1.0);
    msg.add(MarketDataRequirementNames.LAST, 0.57);

    final FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(0.5, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  /**
   * Tests that close is used if there is no other data.
   */
  public void noBidAskLastOrFieldHistory() {
    final MarketValueCalculator calculator = new MarketValueCalculator();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.CLOSING_BID, 0.1);
    msg.add(MarketDataRequirementNames.CLOSING_ASK, 0.2);
    msg.add(MarketDataRequirementNames.CLOSE, 0.14);

    final FieldHistoryStore store = new FieldHistoryStore();

    final MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(0.14, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE));
  }

}
