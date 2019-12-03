/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests a cycle.
   */
  public void test() {
    MultipleCurrencyAmount object = MultipleCurrencyAmount.of(Currency.AUD, 101);
    object = object.plus(Currency.GBP, 300);
    object = object.plus(Currency.USD, 400);
    assertEncodeDecodeCycle(MultipleCurrencyAmount.class, object);
  }

  /**
   * Tests conversion to a Fudge message.
   */
  public void testToFudgeMsg() {
    final MultipleCurrencyAmount sample = MultipleCurrencyAmount.of(CurrencyAmount.parse("USD 0"));
    assertNull(MultipleCurrencyAmountFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(MultipleCurrencyAmountFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  /**
   * Tests conversion from a Fudge message.
   */
  public void testFromFudgeMsg() {
    assertNull(MultipleCurrencyAmountFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
