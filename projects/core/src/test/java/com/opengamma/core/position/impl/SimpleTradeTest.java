/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimpleTrade}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleTradeTest {

  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of("CPARTY", "C100"));
  private static final UniqueId POSITION_UID = UniqueId.of("P", "A");
  private static final Position POSITION = new SimplePosition(POSITION_UID, BigDecimal.ONE, ExternalId.of("A", "B"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();
  private static final ExternalIdBundle BUNDLE = POSITION.getSecurityLink().getExternalId();

  /**
   * Tests construction.
   */
  public void testConstructionExternalIdBundleBigDecimalCounterpartyLocalDateOffsetTime() {
    final SimpleTrade test = new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, COUNTERPARTY,
        TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurityLink().getTarget());
    assertEquals(TRADE_OFFSET_DATETIME.toLocalDate(), test.getTradeDate());
    assertEquals(TRADE_OFFSET_DATETIME.toOffsetTime(), test.getTradeTime());
  }

  /**
   * Tests that the security link cannot be null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionExternalIdBundleBigDecimalCounterpartyLocalDateOffsetTimeNullLink() {
    new SimpleTrade((SecurityLink) null, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  /**
   * Tests that the quantity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionExternalIdBundleBigDecimalCounterpartyLocalDateOffsetTimeNullBigDecimal() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), null, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  /**
   * Tests that the counterparty cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionExternalIdBundleBigDecimalCounterpartyLocalDateOffsetTimeNullCounterparty() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, null, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  /**
   * Test sthat the trade date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionExternalIdBundleBigDecimalCounterpartyLocalDateOffsetTimeNullLocalDate() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, COUNTERPARTY, null, TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  /**
   * Tests construction.
   */
  public void testConstructionSecurityBigDecimalCounterpartyInstant() {
    final ExternalIdBundle securityKey = ExternalIdBundle.of(ExternalId.of("A", "B"));
    final SimpleSecurity security = new SimpleSecurity("A");
    security.setExternalIdBundle(securityKey);

    final SimpleTrade test = new SimpleTrade(security, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertEquals(security, test.getSecurityLink().getTarget());
  }

  /**
   * Tests the copy constructor.
   */
  public void testConstructionCopyFromTrade() {
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");

    final SimpleTrade copy = new SimpleTrade(trade);
    assertEquals(copy, trade);
  }

  /**
   * Tests equality.
   */
  public void testCollectionsOfTradesWithDifferentFields() {
    final Set<SimpleTrade> trades = Sets.newHashSet();

    final SimpleTrade trade1 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade1);

    final SimpleTrade trade2 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("C", "D")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trade2.setPremium(100.00);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(TRADE_OFFSET_DATETIME.toLocalDate().plusDays(1));
    trade2.setPremiumTime(TRADE_OFFSET_DATETIME.toOffsetTime().plusHours(1));
    trades.add(trade2);

    final SimpleTrade trade3 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("E", "F")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade3);

    trades.add(new SimpleTrade(trade3));

    final SimpleTrade trade4 = new SimpleTrade(trade1);
    trade4.addAttribute("key1", "value1");
    trade4.addAttribute("key2", "value2");
    trades.add(trade4);

    assertEquals(4, trades.size());
    assertTrue(trades.contains(trade1));
    assertTrue(trades.contains(trade2));
    assertTrue(trades.contains(trade3));
    assertTrue(trades.contains(trade4));

    trades.remove(trade1);
    assertEquals(3, trades.size());
    assertFalse(trades.contains(trade1));

    trades.remove(trade2);
    assertEquals(2, trades.size());
    assertFalse(trades.contains(trade2));

    trades.remove(trade3);
    assertEquals(1, trades.size());
    assertFalse(trades.contains(trade3));

    trades.remove(trade4);
    assertTrue(trades.isEmpty());
  }

  //------------------------------------------------------------------------
  /**
   * Tests that attribute keys cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddAttributeNullKey() {
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute(null, "B");
  }

  /**
   * Tests the attribute values cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddAttributeNullValue() {
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", null);
  }

  /**
   * Tests the addition of an attribute.
   */
  public void testAddAttribute() {
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", "B");
    assertEquals(1, trade.getAttributes().size());
    assertEquals("B", trade.getAttributes().get("A"));
    trade.addAttribute("C", "D");
    assertEquals(2, trade.getAttributes().size());
    assertEquals("D", trade.getAttributes().get("C"));
  }

  /**
   * Tests the removal of attribute.
   */
  public void testRemoveAttribute() {
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE,
        COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");
    assertEquals(2, trade.getAttributes().size());
    trade.removeAttribute("A");
    assertEquals(1, trade.getAttributes().size());
    assertNull(trade.getAttributes().get("A"));
  }

}
