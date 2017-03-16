/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.PartyTradeIdentifier;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PartyTradeIdentifier}.
 */
@Test(groups = TestGroup.UNIT)
public class PartyTradeIdentifierTest {
  /** The counterparty */
  private static final Counterparty CTPTY = new SimpleCounterparty(ExternalId.of("CTPTY", "A"));
  /** The trade identifier */
  private static final ExternalId TRADE_ID = ExternalId.of("TRADE", "1");

  /**
   * Tests that the counterparty must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCounterpartyNotNull() {
    PartyTradeIdentifier.builder().tradeId(TRADE_ID).build();
  }

  /**
   * Tests that the trade id must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTradeIdNotNull() {
    PartyTradeIdentifier.builder().party(CTPTY).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final PartyTradeIdentifier pti = PartyTradeIdentifier.builder().party(CTPTY).tradeId(TRADE_ID).build();
    PartyTradeIdentifier other = PartyTradeIdentifier.builder().party(CTPTY).tradeId(TRADE_ID).build();
    assertEquals(pti, pti);
    assertEquals(pti, other);
    assertEquals(pti.hashCode(), other.hashCode());
    assertNotEquals(new Object(), pti);
    other = PartyTradeIdentifier.builder().party(new SimpleCounterparty(ExternalId.of("CTPTY", "B"))).tradeId(TRADE_ID).build();
    assertNotEquals(pti, other);
    other = PartyTradeIdentifier.builder().party(CTPTY).tradeId(ExternalId.of("TRADE", "2")).build();
    assertNotEquals(pti, other);
  }

}
