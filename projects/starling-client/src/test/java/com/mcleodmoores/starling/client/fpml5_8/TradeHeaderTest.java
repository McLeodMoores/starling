/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.PartyTradeIdentifier;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.TradeHeader;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.id.ExternalId;

/**
 * Unit tests for {@link TradeHeader}.
 */
public class TradeHeaderTest {
  /** The trade id */
  private static final ExternalId TRADE_ID = ExternalId.of("TRADE", "1");
  /** The first party in the trade */
  private static final PartyTradeIdentifier PARTY_1 =
      PartyTradeIdentifier.builder().party(new SimpleCounterparty(ExternalId.of("CTPTY", "A"))).tradeId(TRADE_ID).build();
  /** The second party in the trade */
  private static final PartyTradeIdentifier PARTY_2 =
      PartyTradeIdentifier.builder().party(new SimpleCounterparty(ExternalId.of("CTPTY", "B"))).tradeId(TRADE_ID).build();
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2016, 1, 1);

  /**
   * Tests that the first party must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFirstPartyNotNull() {
    TradeHeader.builder().party2(PARTY_2).tradeDate(TRADE_DATE).build();
  }

  /**
   * Tests that the second party need not be set.
   */
  @Test
  public void testSecondPartyCanBeNull() {
    TradeHeader.builder().party1(PARTY_1).tradeDate(TRADE_DATE).build();
  }

  /**
   * Tests that the trade date must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTradeDateNotNull() {
    TradeHeader.builder().party1(PARTY_1).party2(PARTY_2).build();
  }

  /**
   * Tests that the trade ids in the party identifiers can be different.
   */
  @Test
  public void testTradeIdsCanBeDifferent() {
    final PartyTradeIdentifier party2 =
        PartyTradeIdentifier.builder().party(new SimpleCounterparty(ExternalId.of("CTPTY", "A"))).tradeId(TRADE_ID).build();
    TradeHeader.builder().party1(PARTY_1).party2(party2).tradeDate(TRADE_DATE).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final TradeHeader th = TradeHeader.builder().party1(PARTY_1).party2(PARTY_2).tradeDate(TRADE_DATE).build();
    TradeHeader other = TradeHeader.builder().party1(PARTY_1).party2(PARTY_2).tradeDate(TRADE_DATE).build();
    assertEquals(th, th);
    assertEquals(th, other);
    assertEquals(th.hashCode(), other.hashCode());
    assertNotEquals(new Object(), th);
    other = TradeHeader.builder().party1(PARTY_1).party2(PARTY_1).tradeDate(TRADE_DATE).build();
    assertNotEquals(th, other);
    other = TradeHeader.builder().party1(PARTY_2).party2(PARTY_2).tradeDate(TRADE_DATE).build();
    assertNotEquals(th, other);
    other = TradeHeader.builder().party1(PARTY_1).party2(PARTY_2).tradeDate(TRADE_DATE.plusDays(1)).build();
    assertNotEquals(th, other);
  }
}
