/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests a cycle.
   */
  @Test
  public void test() {
    final CurrencyAmount object = CurrencyAmount.of(Currency.AUD, 101);
    assertEncodeDecodeCycle(CurrencyAmount.class, object);
  }

  /**
   * Tests an un-decodable message.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromFudgeMsgBadMessage1() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyAmountFudgeBuilder.AMOUNT_FIELD_NAME, "100");
    final CurrencyAmountFudgeBuilder bld = new CurrencyAmountFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

  /**
   * Tests an un-decodable message.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromFudgeMsgBadMessage2() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyAmountFudgeBuilder.CURRENCY_FIELD_NAME, "USD");
    final CurrencyAmountFudgeBuilder bld = new CurrencyAmountFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

}
