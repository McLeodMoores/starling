/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.result;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FailureResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SuccessResultTest extends AbstractFudgeBuilderTestCase {
  private static final CurrencyAmount VALUE = CurrencyAmount.of(Currency.AUD, 10000);
  private static final SuccessResult<CurrencyAmount> SUCCESS = new SuccessResult<>(VALUE);

  /**
   * Tests the object.
   */
  public void testObject() {
    assertEquals(SUCCESS, SUCCESS);
    assertNotEquals(null, SUCCESS);
    assertNotEquals(VALUE, SUCCESS);
    SuccessResult<CurrencyAmount> other = new SuccessResult<>(VALUE);
    assertEquals(SUCCESS, other);
    assertEquals(SUCCESS.hashCode(), other.hashCode());
    other = new SuccessResult<>(CurrencyAmount.of(Currency.AUD, 123));
    assertNotEquals(SUCCESS, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    assertEquals(SUCCESS.metaBean().value().get(SUCCESS), VALUE);
    assertEquals(SUCCESS.property("value").get(), VALUE);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(SuccessResult.class, SUCCESS), SUCCESS);
  }
}
