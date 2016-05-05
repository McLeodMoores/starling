/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Month;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the FX forward trade object.
 */
@Test(groups = TestGroup.UNIT)
public class FXForwardTradeTest {
  /** The current date */
  private static final LocalDate NOW = LocalDate.of(2015, Month.JUNE, 12);
  /** The forward date */
  private static final LocalDate FORWARD = LocalDate.of(2015, Month.JULY, 12);

  /**
   * Tests conversion of the trade to a position.
   */
  @Test
  public void testToPosition() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    final LocalDate tradeDate = NOW;
    final LocalDate forwardDate = FORWARD;
    builder.tradeDate(tradeDate);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(forwardDate);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade = builder.build();
    final Position position = fxForwardTrade.toPosition();
    Assert.assertEquals(position.getQuantity(), BigDecimal.ONE);
    Assert.assertNull(position.getUniqueId());
    Assert.assertEquals(position.getTrades().size(), 1);
    Assert.assertEquals(position.getAttributes().size(), 1);
    Assert.assertEquals(position.getAttributes().get(ManageableTrade.meta().providerId().name()), "A~B");
    final Security security = position.getSecurity();
    Assert.assertTrue(security instanceof FXForwardSecurity);
    FXForwardSecurity fxForwardSecurity = (FXForwardSecurity) security;
    Assert.assertEquals(fxForwardSecurity.getForwardDate(), forwardDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
    Assert.assertNull(fxForwardSecurity.getUniqueId());
    Assert.assertEquals(fxForwardSecurity.getPayAmount(), 1100000d);
    Assert.assertEquals(fxForwardSecurity.getPayCurrency(), Currency.AUD);
    Assert.assertEquals(fxForwardSecurity.getReceiveAmount(), 1000000d);
    Assert.assertEquals(fxForwardSecurity.getReceiveCurrency(), Currency.NZD);
    Assert.assertEquals(fxForwardSecurity.getRegionId(), ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, "GB"));
    Assert.assertTrue(fxForwardSecurity.getAttributes().isEmpty());
    final com.opengamma.core.position.Trade trade = position.getTrades().iterator().next();
    Assert.assertEquals(trade.getQuantity(), BigDecimal.ONE);
    Assert.assertEquals(trade.getAttributes().size(), 1);
    Assert.assertEquals(trade.getAttributes().get(ManageableTrade.meta().providerId().name()), "A~B");
    Assert.assertEquals(trade.getCounterparty().getExternalId(), ExternalId.of("Cpty", "MyBroker"));
    Assert.assertEquals(trade.getTradeTime(), OffsetTime.MAX);
    Assert.assertNull(trade.getUniqueId());
    Assert.assertTrue(trade.getSecurity() instanceof FXForwardSecurity);
    fxForwardSecurity = (FXForwardSecurity) trade.getSecurity();
    Assert.assertEquals(fxForwardSecurity.getForwardDate(), forwardDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
    Assert.assertNull(fxForwardSecurity.getUniqueId());
    Assert.assertEquals(fxForwardSecurity.getPayAmount(), 1100000d);
    Assert.assertEquals(fxForwardSecurity.getPayCurrency(), Currency.AUD);
    Assert.assertEquals(fxForwardSecurity.getReceiveAmount(), 1000000d);
    Assert.assertEquals(fxForwardSecurity.getReceiveCurrency(), Currency.NZD);
    Assert.assertEquals(fxForwardSecurity.getRegionId(), ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, "GB"));
    Assert.assertEquals(fxForwardSecurity.getAttributes().size(), 0);
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderEmpty() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTradeDate() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPC() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFD() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRA() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACI() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACICP() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.build();
  }

  /**
   * Tests that fields must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACICPPA() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.build();
  }

  /**
   * Tests that a trade can be built with a minimal set of fields - the trade date, pay  receive currencies,
   * pay and / or receive amounts, forward date, counterparty and correlation id.
   */
  @Test
  public void testBuilderTDPCFDRACICPPARC() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Test that the trade date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDNull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(null);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the pay currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPCNull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(null);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the forward date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderFDNull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(null);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the receive amount can be null.
   */
  //TODO why is this allowed?
  @Test
  public void testBuilderRAMissing() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the correlation id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderCINull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(null);
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the counterparty cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderCPNull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(null);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty(null);
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the pay amount can be null.
   */
  //TODO why is this allowed?
  @Test
  public void testBuilderPAMissing() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.receiveCurrency(Currency.NZD);
    builder.build();
  }

  /**
   * Tests that the receive currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderRCNull() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(null);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(null);
    builder.build();
  }

  /**
   * Tests the conversion of the trade to its builder and vice versa.
   */
  @Test
  public void testToBuilder() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade.Builder builder1 = builder.build().toBuilder();
    final FXForwardTrade fxForwardTrade = builder.build();
    final FXForwardTrade fxForwardTrade1 = builder1.build();
    Assert.assertEquals(fxForwardTrade, fxForwardTrade1);
    final FXForwardTrade usdFXForward = fxForwardTrade1.toBuilder().receiveCurrency(Currency.USD).build();
    Assert.assertNotEquals(fxForwardTrade1, usdFXForward);
    Assert.assertEquals(Currency.USD, usdFXForward.getReceiveCurrency());
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade = builder.build();
    final FXForwardTrade.Builder builder1 = FXForwardTrade.builder();
    builder1.tradeDate(NOW);
    builder1.payCurrency(Currency.AUD);
    builder1.forwardDate(FORWARD);
    builder1.receiveAmount(1000000d);
    builder1.correlationId(ExternalId.of("A", "B"));
    builder1.counterparty("MyBroker");
    builder1.payAmount(1100000d);
    builder1.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade1 = builder1.build();
    Assert.assertEquals(fxForwardTrade, fxForwardTrade1);
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().tradeDate(NOW.minusDays(2)).build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().payCurrency(Currency.USD).build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().receiveAmount(999999d).build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().correlationId(ExternalId.of("A", "C")).build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().counterparty("YourBroker").build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().payAmount(999999d).build());
    Assert.assertNotEquals(fxForwardTrade, fxForwardTrade1.toBuilder().receiveCurrency(Currency.BRL).build());
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade = builder.build();
    Assert.assertEquals(fxForwardTrade.hashCode(), fxForwardTrade.toBuilder().build().hashCode());

    final FXForwardTrade.Builder builder1 = FXForwardTrade.builder();
    builder1.tradeDate(NOW.minusMonths(1));
    builder1.payCurrency(Currency.USD);
    builder1.forwardDate(NOW.plusMonths(2));
    builder1.receiveAmount(100000000d);
    builder1.correlationId(ExternalId.of("A", "C"));
    builder1.counterparty("YourBroker");
    builder1.payAmount(1200000d);
    builder1.receiveCurrency(Currency.BRL);
    final FXForwardTrade fxForwardTrade1 = builder1.build();
    Assert.assertEquals(fxForwardTrade1.hashCode(), fxForwardTrade1.toBuilder().build().hashCode());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade = builder.build();
    Assert.assertEquals("FXForwardTrade{tradeDate=2015-06-12, payCurrency=AUD, " 
        + "receiveCurrency=NZD, payAmount=1100000.0, receiveAmount=1000000.0," 
        + " forwardDate=2015-07-12, correlationId=A~B, counterparty=MyBroker}", fxForwardTrade.toString());
  }
}