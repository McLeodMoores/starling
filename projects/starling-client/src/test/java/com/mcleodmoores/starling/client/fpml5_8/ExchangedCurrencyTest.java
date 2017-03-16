/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangedCurrency;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ExchangedCurrency}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangedCurrencyTest {
  /** The payer */
  private static final Counterparty PAYER = new SimpleCounterparty(ExternalId.of("TEST", "1"));
  /** The receiver */
  private static final Counterparty RECEIVER = new SimpleCounterparty(ExternalId.of("TEST", "2"));
  /** The payment */
  private static final PaymentAmount PAYMENT = PaymentAmount.builder().amount(BigDecimal.ONE).currency(Currency.USD).build();

  /**
   * Tests that the payer reference can be null if the receiver is set.
   */
  @Test
  public void testPayerCanBeNull() {
    ExchangedCurrency.builder().receiverPartyReference(RECEIVER).paymentAmount(PAYMENT).build();
  }

  /**
   * Tests that the receiver reference can be null if the payer is set.
   */
  @Test
  public void testReceiverCanBeNull() {
    ExchangedCurrency.builder().payerPartyReference(PAYER).paymentAmount(PAYMENT).build();
  }

  /**
   * Tests that either the payer or receiver must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPayerAndReceiverMustNotBeNull() {
    ExchangedCurrency.builder().paymentAmount(PAYMENT).build();
  }

  /**
   * Tests that the payer and receiver reference must be different.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPayerAndReceiverMustBeDifferent() {
    ExchangedCurrency.builder().payerPartyReference(PAYER).receiverPartyReference(PAYER).paymentAmount(PAYMENT).build();
  }

  /**
   * Tests that the payment amount cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPaymentAmountNotNull() {
    ExchangedCurrency.builder().payerPartyReference(PAYER).receiverPartyReference(RECEIVER).build();
  }
}
