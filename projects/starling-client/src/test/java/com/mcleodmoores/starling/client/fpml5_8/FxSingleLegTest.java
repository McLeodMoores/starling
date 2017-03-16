/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangeRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangedCurrency;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSingleLeg;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link FxSingleLeg}.
 */
@Test(groups = TestGroup.UNIT)
public class FxSingleLegTest {
  /** The counterparty */
  private static final Counterparty CTPTY = new SimpleCounterparty(ExternalId.of("CTPTY", "A"));
  /** A payment */
  private static final PaymentAmount EUR_PAYMENT = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(1000.)).build();
  /** A payment */
  private static final PaymentAmount JPY_PAYMENT = PaymentAmount.builder().currency(Currency.JPY).amount(BigDecimal.valueOf(100000.)).build();
  /** EUR payment */
  private static final ExchangedCurrency EUR_EXCHANGED_CURRENCY = ExchangedCurrency.builder().paymentAmount(EUR_PAYMENT).payerPartyReference(CTPTY).build();
  /** JPY payment */
  private static final ExchangedCurrency JPY_EXCHANGED_CURRENCY = ExchangedCurrency.builder().paymentAmount(JPY_PAYMENT).receiverPartyReference(CTPTY).build();
  /** The payment date */
  private static final LocalDate PAYMENT_DATE = LocalDate.of(2016, 1, 1);
  /** The exchange rate */
  private static final ExchangeRate EXCHANGE_RATE = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.EUR)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
      .rate(BigDecimal.valueOf(100.))
      .build();
  /** The inverse exchange rate */
  private static final ExchangeRate INVERSE_EXCHANGE_RATE = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.EUR)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
      .rate(BigDecimal.valueOf(0.01))
      .build();

  /**
   * Tests that the counterparties must be consistent.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testConsistentCounterparties1() {
    final ExchangedCurrency secondPayment = ExchangedCurrency.builder()
        .paymentAmount(JPY_PAYMENT)
        .receiverPartyReference(new SimpleCounterparty(ExternalId.of("CTPTY", "B")))
        .build();
    FxSingleLeg.builder()
      .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
      .exchangedCurrency2(secondPayment)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the counterparties must be consistent.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testConsistentCounterparties2() {
    final ExchangedCurrency firstPayment = ExchangedCurrency.builder()
        .paymentAmount(EUR_PAYMENT)
        .receiverPartyReference(new SimpleCounterparty(ExternalId.of("CTPTY", "A")))
        .payerPartyReference(new SimpleCounterparty(ExternalId.of("CTPTY", "B")))
        .build();
    final ExchangedCurrency secondPayment = ExchangedCurrency.builder()
        .paymentAmount(JPY_PAYMENT)
        .receiverPartyReference(new SimpleCounterparty(ExternalId.of("CTPTY", "B")))
        .payerPartyReference(new SimpleCounterparty(ExternalId.of("CTPTY", "C")))
        .build();
    FxSingleLeg.builder()
      .exchangedCurrency1(firstPayment)
      .exchangedCurrency2(secondPayment)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the first exchanged currency must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFirstExchangedCurrencyNotNull() {
    FxSingleLeg.builder()
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .valueDate(PAYMENT_DATE)
      .exchangeRate(EXCHANGE_RATE)
      .build();
  }

  /**
   * Tests that the value date must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValueDateNotNull() {
    FxSingleLeg.builder()
      .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .exchangeRate(EXCHANGE_RATE)
      .build();
  }

  /**
   * Tests that either the exchange rate or second exchanged currency must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testExchangeRateOrSecondPaymentSet() {
    FxSingleLeg.builder()
      .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate, if set, must have currencies that are consistent with the payments.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCurrenciesAreInconsistent1() {
    FxSingleLeg.builder()
      .exchangedCurrency1(ExchangedCurrency.builder()
          .paymentAmount(PaymentAmount.builder().currency(Currency.USD).amount(BigDecimal.valueOf(1000)).build())
          .payerPartyReference(CTPTY)
          .build())
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate, if set, must have currencies that are consistent with the payments.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCurrenciesAreInconsistent2() {
    FxSingleLeg.builder()
      .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
      .exchangedCurrency2(ExchangedCurrency.builder()
          .paymentAmount(PaymentAmount.builder().currency(Currency.USD).amount(BigDecimal.valueOf(1000)).build())
          .payerPartyReference(CTPTY)
          .build())
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate, if set, must have currencies that are consistent with the first payment.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCurrenciesAreInconsistent3() {
    FxSingleLeg.builder()
      .exchangedCurrency1(ExchangedCurrency.builder()
          .paymentAmount(PaymentAmount.builder().currency(Currency.GBP).amount(BigDecimal.valueOf(1000)).build())
          .payerPartyReference(CTPTY)
          .build())
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate must be equivalent to the implied rate.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInconsistentRate1() {
    final PaymentAmount eurPayment = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(900)).build();
    FxSingleLeg.builder()
      .exchangedCurrency1(ExchangedCurrency.builder().paymentAmount(eurPayment).payerPartyReference(CTPTY).build())
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate must be equivalent to the implied rate.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInconsistentRate2() {
    final PaymentAmount eurPayment = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(900)).build();
    FxSingleLeg.builder()
      .exchangedCurrency1(ExchangedCurrency.builder().paymentAmount(eurPayment).payerPartyReference(CTPTY).build())
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .exchangeRate(INVERSE_EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate must be equivalent to the implied rate.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInconsistentRate3() {
    final PaymentAmount eurPayment = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(900)).build();
    FxSingleLeg.builder()
      .exchangedCurrency1(JPY_EXCHANGED_CURRENCY)
      .exchangedCurrency2(ExchangedCurrency.builder().paymentAmount(eurPayment).payerPartyReference(CTPTY).build())
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate must be equivalent to the implied rate.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInconsistentRate4() {
    final PaymentAmount eurPayment = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(900)).build();
    FxSingleLeg.builder()
      .exchangedCurrency1(JPY_EXCHANGED_CURRENCY)
      .exchangedCurrency2(ExchangedCurrency.builder().paymentAmount(eurPayment).payerPartyReference(CTPTY).build())
      .exchangeRate(INVERSE_EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests that the exchange rate can be different from the implied rate by 7dp.
   */
  @Test
  public void testExchangeRateEqualToWithinTolerance() {
    final PaymentAmount eurPayment = PaymentAmount.builder().currency(Currency.EUR).amount(BigDecimal.valueOf(1000 - 1e-7)).build();
    FxSingleLeg.builder()
      .exchangedCurrency1(ExchangedCurrency.builder().paymentAmount(eurPayment).payerPartyReference(CTPTY).build())
      .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
      .exchangeRate(EXCHANGE_RATE)
      .valueDate(PAYMENT_DATE)
      .build();
  }

  /**
   * Tests the constructed exchange rate.
   */
  @Test
  public void testConstructedExchangeRate1() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
        .exchangedCurrency2(JPY_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .build();
    assertEquals(leg.getExchangeRate(), EXCHANGE_RATE);
  }

  /**
   * Tests the constructed exchange rate.
   */
  @Test
  public void testConstructedExchangeRate2() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(JPY_EXCHANGED_CURRENCY)
        .exchangedCurrency2(EUR_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .build();
    final ExchangeRate expectedRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.EUR)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(BigDecimal.valueOf(0.01))
        .build();
    assertEquals(leg.getExchangeRate(), expectedRate);
  }

  /**
   * Tests the constructed second payment.
   */
  @Test
  public void testConstructedPayment1() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .exchangeRate(EXCHANGE_RATE)
        .build();
    assertEquals(leg.getExchangedCurrency2(), JPY_EXCHANGED_CURRENCY);
  }

  /**
   * Tests the constructed second payment.
   */
  @Test
  public void testConstructedPayment2() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(JPY_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .exchangeRate(EXCHANGE_RATE)
        .build();
    assertEquals(leg.getExchangedCurrency2(), EUR_EXCHANGED_CURRENCY);
  }

  /**
   * Tests the constructed second payment.
   */
  @Test
  public void testConstructedPayment3() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(EUR_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .exchangeRate(INVERSE_EXCHANGE_RATE)
        .build();
    assertEquals(leg.getExchangedCurrency2(), JPY_EXCHANGED_CURRENCY);
  }

  /**
   * Tests the constructed second payment.
   */
  @Test
  public void testConstructedPayment4() {
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(JPY_EXCHANGED_CURRENCY)
        .valueDate(PAYMENT_DATE)
        .exchangeRate(INVERSE_EXCHANGE_RATE)
        .build();
    assertEquals(leg.getExchangedCurrency2(), EUR_EXCHANGED_CURRENCY);
  }
}
