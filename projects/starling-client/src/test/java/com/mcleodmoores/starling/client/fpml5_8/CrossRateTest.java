/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.CrossRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangeRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link CrossRate}.
 */
@Test(groups = TestGroup.UNIT)
public class CrossRateTest {
  /** GBPUSD rate */
  private static final BigDecimal GBPUSD = BigDecimal.valueOf(1.25);
  /** USDJPY rate */
  private static final BigDecimal USDJPY = BigDecimal.valueOf(100);
  /** USDGBP rate */
  private static final BigDecimal USDGBP = BigDecimal.valueOf(0.8);
  /** JPYUSD rate */
  private static final BigDecimal JPYUSD = BigDecimal.valueOf(0.01);

  /**
   * Tests that the quoted currency pair must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testQuotedCurrencyPairNotSet() {
    CrossRate.builder()
      .rate(BigDecimal.ONE)
      .build();
  }

  /**
   * Tests that the rate must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRateNotSet() {
    CrossRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.EUR)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
      .build();
  }

  /**
   * Tests that trying to create an exchange rate from incompatible cross rates (USD/GBP and EUR/JPY) fails.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFourCurrencies() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate eurJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.EUR)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(BigDecimal.valueOf(110))
        .build();
    gbpUsd.toExchangeRate(eurJpy);
  }

  /**
   * Tests that the exchange rate from USD/JPY and USD/JPY is USD/JPY.
   */
  @Test
  public void testUsdJpyUsdJpy() {
    final QuotedCurrencyPair quotedCurrencyPair = QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(quotedCurrencyPair)
        .rate(USDJPY)
        .build();
    final ExchangeRate unchanged = usdJpy.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(unchanged.getCrossRate1());
    assertNull(unchanged.getCrossRate2());
    assertNull(unchanged.getForwardPoints());
    assertNull(unchanged.getFxForwardQuoteBasis());
    assertNull(unchanged.getSpotRate());
    assertEquals(unchanged.getQuotedCurrencyPair(), quotedCurrencyPair);
    assertEquals(unchanged.getRate().doubleValue(), USDJPY.doubleValue());
  }

  /**
   * Tests that incompatible rates are detected when the currency pairs are the same. The comparison is to 7 decimal places.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUsdJpyUsdJpyWithDifferentRates() {
    final QuotedCurrencyPair quotedCurrencyPair = QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final CrossRate usdJpy1 = CrossRate.builder()
        .quotedCurrencyPair(quotedCurrencyPair)
        .rate(USDJPY)
        .build();
    final CrossRate usdJpy2 = CrossRate.builder()
        .quotedCurrencyPair(quotedCurrencyPair)
        .rate(BigDecimal.valueOf(USDJPY.doubleValue() + 1e-7))
        .build();
    usdJpy1.toExchangeRate(usdJpy2);
  }

  /**
   * Tests that the exchange rates from USD/JPY and JPY/USD is USD/JPY.
   */
  @Test
  public void testUsdJpyJpyUsd() {
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDJPY)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate unchanged = usdJpy.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(unchanged.getCrossRate1());
    assertNull(unchanged.getCrossRate2());
    assertNull(unchanged.getForwardPoints());
    assertNull(unchanged.getFxForwardQuoteBasis());
    assertNull(unchanged.getSpotRate());
    assertEquals(unchanged.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(unchanged.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(unchanged.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(unchanged.getRate().doubleValue(), USDJPY.doubleValue());
    unchanged = jpyUsd.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(unchanged.getCrossRate1());
    assertNull(unchanged.getCrossRate2());
    assertNull(unchanged.getForwardPoints());
    assertNull(unchanged.getFxForwardQuoteBasis());
    assertNull(unchanged.getSpotRate());
    assertEquals(unchanged.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(unchanged.getQuotedCurrencyPair().getCurrency2(), Currency.USD);
    assertEquals(unchanged.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(unchanged.getRate().doubleValue(), JPYUSD.doubleValue());
  }

  /**
   * Tests that incompatible rates are detected when one currency pair is the inverse of the other. The comparison is to 7 decimal places.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUsdJpyJpyUsdWithDifferentRates() {
    final CrossRate usdJpy1 = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDJPY)
        .build();
    final CrossRate usdJpy2 = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(BigDecimal.valueOf(1 / USDJPY.doubleValue() + 1e-5))
        .build();
    usdJpy1.toExchangeRate(usdJpy2);
  }

  /**
   * Checks that GBP/USD * USD/JPY = GBP/JPY and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testGbpUsdUsdJpy() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(GBPUSD)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that GBP/USD * USD/JPY = GBP/JPY and vice versa when the quotes are USDGBP and USDJPY.
   */
  @Test
  public void testGbpUsdUsdJpyInverseFirstQuote() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that GBP/USD * USD/JPY = GBP/JPY and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testGbpUsdUsdJpyInverseSecondQuote() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(GBPUSD)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that GBP/USD * USD/JPY = GBP/JPY and vice versa when the quotes are USDGBP and JPYUSD.
   */
  @Test
  public void testGbpUsdUsdJpyInverseBothQuotes() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that USD/GBP * JPY/USD = JPY/GBP and vice versa when the quotes are USDGBP and JPYUSD.
   */
  @Test
  public void testUsdGbpJpyUsd() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = jpyUsd.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that USD/GBP * JPY/USD = JPY/GBP and vice versa when the quotes are GBPUSD and JPYUSD.
   */
  @Test
  public void testUsdGbpJpyUsdInverseFirstQuote() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(GBPUSD)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = jpyUsd.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that USD/GBP * JPY/USD = JPY/GBP and vice versa when the quotes are USDGBP and USDJPY.
   */
  @Test
  public void testUsdGbpJpyUsdInverseSecondQuote() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.USD)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = jpyUsd.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that USD/GBP * JPY/USD = JPY/GBP and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testUsdGbpJpyUsdInverseBothQuotes() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.GBP)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(GBPUSD)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = jpyUsd.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that GBP/USD / JPY/USD = GBP/JPY and vice versa when the quotes are GBPUSD and JPYUSD.
   */
  @Test
  public void testGbpUsdJpyUsd() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(GBPUSD)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should have the quote direction reversed
    gbpJpy = jpyUsd.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that GBP/USD / JPY/USD = GBP/JPY and vice versa when the quotes are USDGBP and JPYUSD.
   */
  @Test
  public void testGbpUsdJpyUsdInverseFirstQuote() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(USDGBP)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should have the quote direction reversed
    gbpJpy = jpyUsd.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that GBP/USD / JPY/USD = GBP/JPY and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testGbpUsdJpyUsdInverseSecondQuote() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(GBPUSD)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should have the quote direction reversed
    gbpJpy = jpyUsd.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that GBP/USD / JPY/USD = GBP/JPY and vice versa when the quotes are USDGBP and USDJPY.
   */
  @Test
  public void testGbpUsdJpyUsdInverseBothQuotes() {
    final CrossRate gbpUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(USDGBP)
        .build();
    final CrossRate jpyUsd = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = gbpUsd.toExchangeRate(jpyUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
    // reverse calculation should have the quote direction reversed
    gbpJpy = jpyUsd.toExchangeRate(gbpUsd);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
  }

  /**
   * Checks that USD/GBP / USD/JPY = JPY/GBP and vice versa when the quotes are USDGBP and JPYUSD.
   */
  @Test
  public void testUsdGbpUsdJpy() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.GBP)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(USDGBP)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that USD/GBP / USD/JPY = JPY/GBP and vice versa when the quotes are GBPUSD and JPYUSD.
   */
  @Test
  public void testUsdGbpUsdJpyInverseFirstQuote() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.GBP)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .rate(GBPUSD)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(USDJPY)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that USD/GBP / USD/JPY = JPY/GBP and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testUsdGbpUsdJpyInverseSecondQuote() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .rate(USDGBP)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }

  /**
   * Checks that USD/GBP / USD/JPY = JPY/GBP and vice versa when the quotes are GBPUSD and USDJPY.
   */
  @Test
  public void testUsdGbpUsdJpyInverseBothQuotes() {
    final CrossRate usdGbp = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(GBPUSD)
        .build();
    final CrossRate usdJpy = CrossRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
        .rate(JPYUSD)
        .build();
    ExchangeRate gbpJpy = usdGbp.toExchangeRate(usdJpy);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 0.008);
    // reverse calculation should be identical
    gbpJpy = usdJpy.toExchangeRate(usdGbp);
    // all the fields that should not be set
    assertNull(gbpJpy.getCrossRate1());
    assertNull(gbpJpy.getCrossRate2());
    assertNull(gbpJpy.getForwardPoints());
    assertNull(gbpJpy.getFxForwardQuoteBasis());
    assertNull(gbpJpy.getSpotRate());
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(gbpJpy.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(gbpJpy.getRate().doubleValue(), 125.);
  }
}
