/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuotedCurrencyPair}.
 */
@Test(groups = TestGroup.UNIT)
public class QuotedCurrencyPairTest {

  /**
   * Checks that the first currency must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoFirstCurrency() {
    QuotedCurrencyPair.builder()
      .currency2(Currency.USD)
      .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Checks that the second currency must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSecondCurrency() {
    QuotedCurrencyPair.builder()
      .currency1(Currency.USD)
      .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Checks that the quote basis must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoQuoteBasis() {
    QuotedCurrencyPair.builder()
      .currency1(Currency.USD)
      .currency2(Currency.JPY)
      .build();
  }

  /**
   * Checks that the two currencies must be different.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSameCurrencies() {
    QuotedCurrencyPair.builder()
      .currency1(Currency.USD)
      .currency2(Currency.USD)
      .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final QuotedCurrencyPair pair =
        QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.BRL).quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2).build();
    QuotedCurrencyPair other =
        QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.BRL).quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2).build();
    assertEquals(pair, pair);
    assertNotEquals(new Object(), pair);
    assertEquals(pair, other);
    assertEquals(pair.hashCode(), other.hashCode());
    other = QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.BRL).quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1).build();
    assertNotEquals(pair, other);
    other = QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.CAD).quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2).build();
    assertNotEquals(pair, other);
    other = QuotedCurrencyPair.builder().currency1(Currency.BRL).currency2(Currency.AUD).quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2).build();
    assertNotEquals(pair, other);
  }
}
