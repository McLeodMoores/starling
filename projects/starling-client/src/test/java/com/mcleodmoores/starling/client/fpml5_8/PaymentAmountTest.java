/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PaymentAmount}.
 */
@Test(groups = TestGroup.UNIT)
public class PaymentAmountTest {

  /**
   * Tests that the currency must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCurrencyNotNull() {
    PaymentAmount.builder().amount(BigDecimal.ONE).build();
  }

  /**
   * Tests that the amount must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAmountNotNull() {
    PaymentAmount.builder().currency(Currency.USD).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final PaymentAmount pa = PaymentAmount.builder().currency(Currency.USD).amount(BigDecimal.ONE).build();
    PaymentAmount other = PaymentAmount.builder().currency(Currency.USD).amount(BigDecimal.ONE).build();
    assertEquals(pa, pa);
    assertEquals(pa, other);
    assertEquals(pa.hashCode(), other.hashCode());
    assertNotEquals(new Object(), pa);
    other = PaymentAmount.builder().currency(Currency.AUD).amount(BigDecimal.ONE).build();
    assertNotEquals(pa, other);
    other = PaymentAmount.builder().currency(Currency.USD).amount(BigDecimal.valueOf(-100)).build();
    assertNotEquals(pa, other);
  }
}
