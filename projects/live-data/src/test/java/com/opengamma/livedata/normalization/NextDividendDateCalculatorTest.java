/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link NextDividendDateCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class NextDividendDateCalculatorTest {
  private static final NextDividendDateCalculator CALCULATOR = new NextDividendDateCalculator();

  /**
   * Tests the case where neither the message nor the history contains any
   * information about the next dividend date.
   */
  public void testNoDividendInformation() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    final MutableFudgeMsg historyMsg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 120);
    historyMsg.add(MarketDataRequirementNames.LAST, 117);
    final MutableFudgeMsg updated = CALCULATOR.apply(msg, "uid", new FieldHistoryStore(historyMsg));
    assertEquals(updated.getAllFields().size(), 1);
    assertEquals(((Number) updated.getByName(MarketDataRequirementNames.LAST).getValue()).doubleValue(), 120.);
    assertNull(updated.getByName(MarketDataRequirementNames.NEXT_DIVIDEND_DATE));
  }

  /**
   * Tests the case where only the history contains next dividend date
   * information.
   */
  public void testHistoryDividendInformation() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    final MutableFudgeMsg historyMsg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 120);
    historyMsg.add(MarketDataRequirementNames.LAST, 117);
    historyMsg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, LocalDate.of(2020, 10, 1));
    final MutableFudgeMsg updated = CALCULATOR.apply(msg, "uid", new FieldHistoryStore(historyMsg));
    assertEquals(updated.getAllFields().size(), 2);
    assertEquals(((Number) updated.getByName(MarketDataRequirementNames.LAST).getValue()).doubleValue(), 120.);
    assertEquals(updated.getByName(MarketDataRequirementNames.NEXT_DIVIDEND_DATE).getValue(), LocalDate.of(2020, 10, 1));
  }

  /**
   * Tests the case where only the live data contains next dividend date
   * information.
   */
  public void testLiveDividendInformation() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    final MutableFudgeMsg historyMsg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 120);
    msg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, LocalDate.of(2020, 10, 2));
    historyMsg.add(MarketDataRequirementNames.LAST, 117);
    final MutableFudgeMsg updated = CALCULATOR.apply(msg, "uid", new FieldHistoryStore(historyMsg));
    assertEquals(updated.getAllFields().size(), 2);
    assertEquals(((Number) updated.getByName(MarketDataRequirementNames.LAST).getValue()).doubleValue(), 120.);
    assertEquals(updated.getByName(MarketDataRequirementNames.NEXT_DIVIDEND_DATE).getValue(), LocalDate.of(2020, 10, 2));
  }

  /**
   * Tests the case where both the live data and history contains next dividend
   * date information.
   */
  public void testLiveAndHistoryDividendInformation() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    final MutableFudgeMsg historyMsg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 120);
    msg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, LocalDate.of(2020, 10, 2));
    historyMsg.add(MarketDataRequirementNames.LAST, 117);
    historyMsg.add(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, LocalDate.of(2020, 10, 1));
    final MutableFudgeMsg updated = CALCULATOR.apply(msg, "uid", new FieldHistoryStore(historyMsg));
    assertEquals(updated.getAllFields().size(), 2);
    assertEquals(((Number) updated.getByName(MarketDataRequirementNames.LAST).getValue()).doubleValue(), 120.);
    assertEquals(updated.getByName(MarketDataRequirementNames.NEXT_DIVIDEND_DATE).getValue(), LocalDate.of(2020, 10, 2));
  }
}
