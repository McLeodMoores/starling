/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Currency REF = Currency.USD;

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Currency.class, REF);
  }

  /**
   * Tests secondary type encoding, which uses a string.
   */
  @Test
  public void testFromString() {
    assertEquals(REF, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getCode())));
  }

  /**
   * Tests encoding from the currency unique id.
   */
  @Test
  public void testFromUniqueId() {
    assertEquals(REF, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getUniqueId().toString())));
  }

  /**
   * Tests the behaviour when the unique id is not a currency uid.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueIdBad1() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, "Rubbish~ID"));
  }

  /**
   * Tests the behaviour when the unique id is not a currency uid.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueIdBad2() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, Currency.OBJECT_SCHEME + "~Rubbish"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion to a Fudge message.
   */
  @Test
  public void testToFudgeMsg() {
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    final MutableFudgeMsg msg = bld.buildMessage(getFudgeSerializer(), REF);
    assertEquals(ImmutableSet.of(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME), msg.getAllFieldNames());
    assertEquals("USD", msg.getString(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests conversion from a Fudge message.
   */
  @Test
  public void testFromFudgeMsg() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME, "USD");
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

  /**
   * Tests the behaviour when the Fudge message cannot be converted to a currency.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromFudgeMsgBadMessage1() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

}
