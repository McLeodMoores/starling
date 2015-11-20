package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.*;

import java.math.BigDecimal;

/**
 * Created by jim on 10/06/15.
 */
public class FXForwardTradeTest {
  
  private static final LocalDate NOW = LocalDate.of(2015, Month.JUNE, 12);
  private static final LocalDate FORWARD = LocalDate.of(2015, Month.JULY, 12);

  @Test
  public void testToPosition() throws Exception {
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
    Position position = fxForwardTrade.toPosition();
    Assert.assertEquals(position.getQuantity(), BigDecimal.ONE);
    Assert.assertNull(position.getUniqueId());
    Assert.assertEquals(position.getTrades().size(), 1);
    Assert.assertEquals(position.getAttributes().size(), 1);
    Assert.assertEquals(position.getAttributes().get(ManageableTrade.meta().providerId().name()), "A~B");
    Security security = position.getSecurity();
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
    com.opengamma.core.position.Trade trade = position.getTrades().iterator().next();
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderEmpty() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTradeDate() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPC() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFD() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRA() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACI() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACICP() throws Exception {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(NOW);
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(FORWARD);
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", "B"));
    builder.counterparty("MyBroker");
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDPCFDRACICPPA() throws Exception {
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

  @Test
  public void testBuilderTDPCFDRACICPPARC() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderTDNull() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPCNull() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderFDNull() throws Exception {
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

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderRAMissing() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderCINull() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderCPNull() throws Exception {
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

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderPAMissing() throws Exception {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderRCNull() throws Exception {
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

  @Test
  public void testToBuilder() throws Exception {
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

  @Test
  public void testEquals() throws Exception {
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

  @Test
  public void testHashCode() throws Exception {
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

  @Test
  public void testToString() throws Exception {
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
    Assert.assertEquals("FXForwardTrade{tradeDate=2015-06-12, payCurrency=AUD, " +
        "receiveCurrency=NZD, payAmount=1100000.0, receiveAmount=1000000.0," +
        " forwardDate=2015-07-12, correlationId=A~B, counterparty=MyBroker}", fxForwardTrade.toString());
  }
}