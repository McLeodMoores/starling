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

  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Currency.class, REF);
  }

  @Test
  public void testFromString() {
    assertEquals(REF, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getCode())));
  }

  @Test
  public void testFromUniqueId() {
    assertEquals(REF, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getUniqueId().toString())));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueId_bad1() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, "Rubbish~ID"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueId_bad2() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, Currency.OBJECT_SCHEME + "~Rubbish"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toFudgeMsg() {
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    final MutableFudgeMsg msg = bld.buildMessage(getFudgeSerializer(), REF);
    assertEquals(ImmutableSet.of(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME), msg.getAllFieldNames());
    assertEquals("USD", msg.getString(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_fromFudgeMsg() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME, "USD");
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromFudgeMsg_badMessage1() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    final CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

}
