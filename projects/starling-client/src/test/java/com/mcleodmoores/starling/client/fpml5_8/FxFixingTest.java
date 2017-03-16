/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;

import com.mcleodmoores.starling.client.marketdata.DataSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxFixing;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSpotRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PrimaryRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link FxFixing}.
 */
@Test(groups = TestGroup.UNIT)
public class FxFixingTest {
  /** The quoted currency pair */
  private static final QuotedCurrencyPair QUOTE = QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.BRL)
      .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2).build();
  /** The fixing date */
  private static final LocalDate FIXING_DATE = LocalDate.of(2016, 1, 1);
  /** The primary rate source */
  private static final PrimaryRateSource PRIMARY_SOURCE = PrimaryRateSource.builder().dataSource(DataSource.DEFAULT).rateSourcePage("FX FIX").build();
  /** The rate source */
  private static final FxSpotRateSource SOURCE = FxSpotRateSource.builder()
      .businessCenterZone(ZoneOffset.UTC).fixingTime(LocalTime.of(11, 0)).primaryRateSource(PRIMARY_SOURCE).build();

  /**
   * Tests that the quoted currency pair must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testQuotedCurrencyPairNotNull() {
    FxFixing.builder().fixingDate(FIXING_DATE).fxSpotRateSource(SOURCE).build();
  }

  /**
   * Tests that the fixing date must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingDateNotNull() {
    FxFixing.builder().quotedCurrencyPair(QUOTE).fxSpotRateSource(SOURCE).build();
  }

  /**
   * Tests that the rate source must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRateSourceNotNull() {
    FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final FxFixing fixing = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE).fxSpotRateSource(SOURCE).build();
    FxFixing other = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE).fxSpotRateSource(SOURCE).build();
    assertEquals(fixing, fixing);
    assertEquals(fixing, other);
    assertEquals(fixing.hashCode(), other.hashCode());
    assertNotEquals(new Object(), fixing);
    other = FxFixing.builder().quotedCurrencyPair(QuotedCurrencyPair.builder().currency1(Currency.AUD).currency2(Currency.BRL)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1).build()).fixingDate(FIXING_DATE).fxSpotRateSource(SOURCE).build();
    assertNotEquals(fixing, other);
    other = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE.plusDays(1)).fxSpotRateSource(SOURCE).build();
    assertNotEquals(fixing, other);
    other = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE).fxSpotRateSource(FxSpotRateSource.builder()
        .businessCenterZone(ZoneOffset.UTC).fixingTime(LocalTime.of(12, 0)).primaryRateSource(PRIMARY_SOURCE).build()).build();
    assertNotEquals(fixing, other);
  }
}
