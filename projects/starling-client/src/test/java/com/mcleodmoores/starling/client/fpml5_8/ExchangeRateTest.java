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
 * Unit tests for {@link ExchangeRate}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeRateTest {
  /** GBP/USD rate */
  private static final BigDecimal GBPUSD = BigDecimal.valueOf(1.25);
  /** USD/JPY rate */
  private static final BigDecimal USDJPY = BigDecimal.valueOf(100.);
  /** JPY/USD rate */
  private static final BigDecimal JPYUSD = BigDecimal.valueOf(0.01);
  /** GBP/JPY rate */
  private static final BigDecimal GBPJPY = BigDecimal.valueOf(125.);
  /** JPY/GBP rate */
  private static final BigDecimal JPYGBP = BigDecimal.valueOf(0.008);
  /** USD/JPY cross rate */
  private static final CrossRate USDJPY_CROSS = CrossRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.USD)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
      .rate(USDJPY)
      .build();
  /** GBP/USD cross rate */
  private static final CrossRate GBPUSD_CROSS = CrossRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
      .rate(GBPUSD)
      .build();

  /**
   * Tests that the rate can either be constructed from spot + forward or cross rates, not both.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfSpotAndFirstCrossRateSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(USDJPY)
      .forwardPoints(BigDecimal.ZERO)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .crossRate1(USDJPY_CROSS) // this should not be set
      .build();
  }

  /**
   * Tests that the rate can either be constructed from spot + forward or cross rates, not both.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfSpotAndSecondCrossRateSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(USDJPY)
      .forwardPoints(BigDecimal.ZERO)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .crossRate2(USDJPY_CROSS) // this should not be set
      .build();
  }

  /**
   * Tests that the forward and forward quote basis must be set if the spot is.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfForwardPointsAndQuoteBasisNotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(USDJPY)
      .build();
  }

  /**
   * Tests that the forward quote basis must be set if the spot and forward points are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfFxForwardQuoteBasisNotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(USDJPY)
      .forwardPoints(BigDecimal.ZERO)
      .build();
  }

  /**
   * Tests that the forward points must be set if the spot and forward quote basis are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfFxForwardNotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(USDJPY)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Tests that the spot must be set if the forward points and quote basis are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfSpotNotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .forwardPoints(BigDecimal.ZERO)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Tests that both cross rates must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfSecondCrossNotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.GBP)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .crossRate1(USDJPY_CROSS)
      .build();
  }

  /**
   * Tests that the spot cannot be set if the cross rates are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCrossAndSpotSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.GBP)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .crossRate1(USDJPY_CROSS)
      .crossRate2(GBPUSD_CROSS)
      .spotRate(GBPJPY) // this should not be set
      .build();
  }

  /**
   * Tests that the forward points cannot be set if the cross rates are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCrossAndForwardSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
      .crossRate1(USDJPY_CROSS)
      .crossRate2(GBPUSD_CROSS)
      .forwardPoints(BigDecimal.ZERO) // this should not be set
      .build();
  }

  /**
   * Tests that the forward quote basis cannot be set if the cross rates are.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCrossAndForwardQuoteBasisSet() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
      .crossRate1(USDJPY_CROSS)
      .crossRate2(GBPUSD_CROSS)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2) // this should not be set
      .build();
  }

  /**
   * Tests that the currency pair must match those in the cross rates.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testErrorIfCrossRateCurrenciesDontMatch() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.EUR)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
      .crossRate1(USDJPY_CROSS)
      .crossRate2(GBPUSD_CROSS)
      .build();
  }

  /**
   * Tests the exchange rate object that is constructed from a rate and quoted currency pair.
   */
  @Test
  public void testBuildDirect() {
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.USD)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .rate(USDJPY)
        .build();
    // all the fields that should not be set
    assertNull(exchangeRate.getCrossRate1());
    assertNull(exchangeRate.getCrossRate2());
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getRate(), USDJPY);
  }

  /**
   * Tests the exchange rate object that is constructed from cross rates, including the case where
   * the order of the currencies in the quoted currency pair is the reverse of that in calculated
   * from the cross rates.
   */
  @Test
  public void testBuildFromCrossRatesNoRateSet() {
    // currency1 and currency2 are in the same order as the exchange rate calculated from the cross rates
    ExchangeRate exchangeRate = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.GBP)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .crossRate1(GBPUSD_CROSS)
      .crossRate2(USDJPY_CROSS)
      .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate(), GBPJPY);
    // currency1 and currency2 are in inverse order as the exchange rate calculated from the cross rates
    exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.JPY)
          .currency2(Currency.GBP)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .crossRate1(GBPUSD_CROSS)
        .crossRate2(USDJPY_CROSS)
        .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate().doubleValue(), 1 / GBPJPY.doubleValue());
  }

  /**
   * Tests the exchange rate object that is constructed from cross rates, including the case where
   * the order of the currencies in the quoted currency pair is the reverse of that in calculated
   * from the cross rates. In this case, the rate is also set, so the calculated rate is compared to
   * this value.
   */
  @Test
  public void testBuildFromCrossRatesRateAndQuoteBasisSet() {
    // currency1 and currency2 are in the same order as the exchange rate calculated from the cross rates
    ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
      .crossRate1(GBPUSD_CROSS)
      .crossRate2(USDJPY_CROSS)
      .rate(GBPJPY)
      .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate(), GBPJPY);
    // currency1 and currency2 are in the same order as the exchange rate calculated from the cross rates
    // but the quote basis is different
    exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.JPY)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .crossRate1(GBPUSD_CROSS)
        .crossRate2(USDJPY_CROSS)
        .rate(JPYGBP)
        .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getRate(), JPYGBP);
    // currency1 and currency2 are in inverse order as the exchange rate calculated from the cross rates
    exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .crossRate1(GBPUSD_CROSS)
        .crossRate2(USDJPY_CROSS)
        .rate(GBPJPY)
        .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getRate(), GBPJPY);
    // currency1 and currency2 are in inverse order as the exchange rate calculated from the cross rates
    // but the quote basis is different
    exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.JPY)
            .currency2(Currency.GBP)
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build())
        .crossRate1(GBPUSD_CROSS)
        .crossRate2(USDJPY_CROSS)
        .rate(JPYGBP)
        .build();
    assertNull(exchangeRate.getForwardPoints());
    assertNull(exchangeRate.getFxForwardQuoteBasis());
    assertNull(exchangeRate.getSpotRate());
    assertEquals(exchangeRate.getCrossRate1(), GBPUSD_CROSS);
    assertEquals(exchangeRate.getCrossRate2(), USDJPY_CROSS);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.GBP);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate(), JPYGBP);
  }

  /**
   * Tests that the rate and the calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromCrossRatesRateMismatch1() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.GBP)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .crossRate1(GBPUSD_CROSS)
      .crossRate2(USDJPY_CROSS)
      .rate(GBPJPY.add(BigDecimal.ONE))
      .build();
  }

  /**
   * Tests that the rate and the calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromCrossRatesRateMismatch2() {
    ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.JPY)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .crossRate1(GBPUSD_CROSS)
        .crossRate2(USDJPY_CROSS)
        .rate(JPYGBP.add(BigDecimal.ONE))
        .build();
  }

  /**
   * Tests that the rate and the calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromCrossRatesRateMismatch3() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.JPY)
        .currency2(Currency.GBP)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .crossRate1(GBPUSD_CROSS)
      .crossRate2(USDJPY_CROSS)
      .rate(JPYGBP.add(BigDecimal.ONE))
      .build();
  }

  /**
   * Tests that the rate and the calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromCrossRatesRateMismatch4() {
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.JPY)
        .currency2(Currency.GBP)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build())
      .crossRate1(GBPUSD_CROSS)
      .crossRate2(USDJPY_CROSS)
      .rate(GBPJPY.add(BigDecimal.ONE))
      .build();
  }

  /**
   * Tests the exchange rate object that is constructed from spot and forward points, including the case where
   * the order of the currencies in the quoted currency pair is the reverse of that the forward quote basis.
   */
  @Test
  public void testBuildFromSpotAndForwardNoRateSet() {
    final BigDecimal forwardPoints = BigDecimal.valueOf(10);
    final BigDecimal spot = USDJPY.subtract(forwardPoints);
    // forward quote basis is the same as the rate quote basis
    ExchangeRate exchangeRate = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
    assertNull(exchangeRate.getCrossRate1());
    assertNull(exchangeRate.getCrossRate2());
    assertEquals(exchangeRate.getSpotRate(), spot);
    assertEquals(exchangeRate.getForwardPoints(), forwardPoints);
    assertEquals(exchangeRate.getFxForwardQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getRate(), USDJPY);
    // forward quote basis is the inverse of the rate quote basis
    exchangeRate = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build())
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
    assertNull(exchangeRate.getCrossRate1());
    assertNull(exchangeRate.getCrossRate2());
    assertEquals(exchangeRate.getSpotRate(), spot);
    assertEquals(exchangeRate.getForwardPoints(), forwardPoints);
    assertEquals(exchangeRate.getFxForwardQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate().doubleValue(), 1 / USDJPY.doubleValue());
  }

  /**
   * Tests the exchange rate object that is constructed from spot and forward points, including the case where
   * the order of the currencies in the quoted currency pair is the reverse of that the forward quote basis.
   * In this case, the rate is also set, so the calculated rate is compared to this value.
   */
  @Test
  public void testBuildFromSpotAndForwardRateAndQuoteBasisSet() {
    final BigDecimal forwardPoints = BigDecimal.valueOf(10);
    final BigDecimal spot = USDJPY.subtract(forwardPoints);
    // forward quote basis is the same as the rate quote basis
    ExchangeRate exchangeRate = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .rate(USDJPY)
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
    assertNull(exchangeRate.getCrossRate1());
    assertNull(exchangeRate.getCrossRate2());
    assertEquals(exchangeRate.getSpotRate(), spot);
    assertEquals(exchangeRate.getForwardPoints(), forwardPoints);
    assertEquals(exchangeRate.getFxForwardQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getRate(), USDJPY);
    // forward quote basis is the inverse of the rate quote basis
    exchangeRate = ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build())
      .rate(JPYUSD)
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
    assertNull(exchangeRate.getCrossRate1());
    assertNull(exchangeRate.getCrossRate2());
    assertEquals(exchangeRate.getSpotRate(), spot);
    assertEquals(exchangeRate.getForwardPoints(), forwardPoints);
    assertEquals(exchangeRate.getFxForwardQuoteBasis(), QuoteBasis.CURRENCY1_PER_CURRENCY2);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency1(), Currency.USD);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getCurrency2(), Currency.JPY);
    assertEquals(exchangeRate.getQuotedCurrencyPair().getQuoteBasis(), QuoteBasis.CURRENCY2_PER_CURRENCY1);
    assertEquals(exchangeRate.getRate().doubleValue(), 1 / USDJPY.doubleValue());
  }

  /**
   * Tests that the rate and calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromSpotAndForwardRateMismatch1() {
    final BigDecimal forwardPoints = BigDecimal.valueOf(10);
    final BigDecimal spot = USDJPY.subtract(forwardPoints).add(BigDecimal.ONE);
    // forward quote basis is the same as the rate quote basis
    ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .rate(USDJPY)
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }

  /**
   * Tests that the rate and calculated rate must match.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildFromSpotAndForwardRateMismatch2() {
  final BigDecimal forwardPoints = BigDecimal.valueOf(10);
  final BigDecimal spot = USDJPY.subtract(forwardPoints).add(BigDecimal.ONE);
    // forward quote basis is the inverse of the rate quote basis
  ExchangeRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.JPY)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build())
      .rate(JPYUSD)
      .spotRate(spot)
      .forwardPoints(forwardPoints)
      .fxForwardQuoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
      .build();
  }
}
