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
import com.mcleodmoores.starling.client.portfolio.fpml5_8.NonDeliverableSettlement;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PrimaryRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link NonDeliverableSettlement}.
 */
@Test(groups = TestGroup.UNIT)
public class NonDeliverableSettlementTest {
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
  /** The FX fixing */
  private static final FxFixing FX_FIXING = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE).fxSpotRateSource(SOURCE).build();

  /**
   * Tests that the settlement currency must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCurrencyNotNull() {
    NonDeliverableSettlement.builder().fixing(FX_FIXING).build();
  }

  /**
   * Tests that the fixing must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingNotNull() {
    NonDeliverableSettlement.builder().settlementCurrency(Currency.AUD).build();
  }

  /**
   * Tests that the settlement currency must be referenced in the fixing.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSettlementCurrencyIsReferenced() {
    NonDeliverableSettlement.builder().settlementCurrency(Currency.CAD).fixing(FX_FIXING).build();
  }

  /**
   * Tests that the settlement currency can be either of the currencies in the fixing.
   */
  @Test
  public void testSettlementCurrency() {
    NonDeliverableSettlement.builder().settlementCurrency(Currency.AUD).fixing(FX_FIXING).build();
    NonDeliverableSettlement.builder().settlementCurrency(Currency.BRL).fixing(FX_FIXING).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final NonDeliverableSettlement nds = NonDeliverableSettlement.builder().settlementCurrency(Currency.AUD).fixing(FX_FIXING).build();
    NonDeliverableSettlement other = NonDeliverableSettlement.builder().settlementCurrency(Currency.AUD).fixing(FX_FIXING).build();
    assertEquals(nds, nds);
    assertEquals(nds, other);
    assertEquals(nds.hashCode(), other.hashCode());
    assertNotEquals(new Object(), nds);
    other = NonDeliverableSettlement.builder().settlementCurrency(Currency.BRL).fixing(FX_FIXING).build();
    assertNotEquals(nds, other);
    final FxFixing fixing = FxFixing.builder().quotedCurrencyPair(QUOTE).fixingDate(FIXING_DATE.plusDays(1)).fxSpotRateSource(SOURCE).build();
    other = NonDeliverableSettlement.builder().settlementCurrency(Currency.AUD).fixing(fixing).build();
    assertNotEquals(nds, other);
  }
}
