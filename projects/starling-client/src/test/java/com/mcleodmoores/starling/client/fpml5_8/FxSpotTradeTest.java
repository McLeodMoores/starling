/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangeRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangedCurrency;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSingleLeg;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSpotTrade;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PartyTradeIdentifier;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.SimpleDeal;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.TradeHeader;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link FxSpotTrade}.
 */
@Test(groups = TestGroup.UNIT)
public class FxSpotTradeTest {
  /** The default region */
  private static final ExternalId DEFAULT_REGION = ExternalSchemes.countryRegionId(Country.US);
  /** The trade creation time stamp */
  private static final ZonedDateTime TIME_STAMP = ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2001, 10, 1), LocalTime.of(8, 57)), ZoneOffset.UTC);
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2001, 10, 23);
  /** The value date (spot date) */
  private static final LocalDate VALUE_DATE = LocalDate.of(2001, 10, 25);
  /** The counterparty */
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "A"));
  /** The trade header */
  private static final TradeHeader HEADER = TradeHeader.builder().party1(PartyTradeIdentifier.builder().party(COUNTERPARTY)
      .tradeId(ExternalId.of("TRADE", "1")).build()).tradeDate(TRADE_DATE).build();
  /** USD amount */
  private static final BigDecimal USD_AMOUNT = BigDecimal.valueOf(14800000);
  /** USD payment */
  private static final PaymentAmount USD_PAYMENT = PaymentAmount.builder().amount(USD_AMOUNT).currency(Currency.USD).build();
  /** GBP amount */
  private static final BigDecimal GBP_AMOUNT = BigDecimal.valueOf(10000000);
  /** GBP payment */
  private static final PaymentAmount GBP_PAYMENT = PaymentAmount.builder().amount(GBP_AMOUNT).currency(Currency.GBP).build();
  /** The correlation id */
  private static final ExternalId CORRELATION_ID = ExternalId.of("ID", "123456");

  /**
   * Tests that the trade header cannot contain information about the second party. This will change in the future.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testTradeHeader2CannotBeSetWhenConverting() {
    final TradeHeader header = TradeHeader.builder()
        .party1(PartyTradeIdentifier.builder()
            .party(COUNTERPARTY)
            .tradeId(ExternalId.of("TRADE", "1"))
            .build())
        .party2(PartyTradeIdentifier.builder()
            .party(COUNTERPARTY)
            .tradeId(ExternalId.of("TRADE", "2"))
            .build())
        .tradeDate(TRADE_DATE)
        .build();
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    FxSpotTrade.builder()
      .creationTimestamp(TIME_STAMP)
      .correlationId(CORRELATION_ID)
      .tradeHeader(header)
      .fxSingleLeg(leg)
      .party1Id(COUNTERPARTY.getExternalId())
      .build()
      .toPosition();
  }

  /**
   * Tests the conversion of a spot trade to the equivalent position.
   */
  @Test
  public void testPayUsdReceiveGbp1() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.USD, USD_AMOUNT.doubleValue(), Currency.GBP, GBP_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive GBP, pay USD so counterparty pays GBP, receives USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertTrue(JodaBeanUtils.equalIgnoring(security, expectedSecurity, FXForwardSecurity.meta().name()));
  }

  /**
   * Tests the conversion of a spot trade to the equivalent position.
   */
  @Test
  public void testPayUsdReceiveGbp2() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.USD, USD_AMOUNT.doubleValue(), Currency.GBP, GBP_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive GBP, pay USD so counterparty pays GBP, receives USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertTrue(JodaBeanUtils.equalIgnoring(security, expectedSecurity, FXForwardSecurity.meta().name()));
  }

  /**
   * Tests the conversion of a spot trade to the equivalent position.
   */
  @Test
  public void testPayGbpReceiveUsd1() {
    // pay GBP, receive USD
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.USD, USD_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay GBP, receive USD so counterparty receives GBP, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertTrue(JodaBeanUtils.equalIgnoring(security, expectedSecurity, FXForwardSecurity.meta().name()));
  }

  /**
   * Tests the conversion of a spot trade to the equivalent position.
   */
  @Test
  public void testPayGbpReceiveUsd2() {
    // pay GBP, receive USD
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.USD, USD_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay GBP, receive USD so counterparty receives GBP, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertTrue(JodaBeanUtils.equalIgnoring(security, expectedSecurity, FXForwardSecurity.meta().name()));
  }

  /**
   * Tests creation of the trade.
   */
  @Test
  public void testTradeCreation() {
    // pay GBP, receive USD
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.USD, USD_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay GBP, receive USD so counterparty receives GBP, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final SimpleTrade ogTrade = (SimpleTrade) position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final SimpleTrade expectedTrade = new SimpleTrade(expectedSecurity, BigDecimal.ONE, COUNTERPARTY, TRADE_DATE, OffsetTime.MAX);
    final SimpleDeal deal = new SimpleDeal();
    deal.setCreationTimestamp(TIME_STAMP);
    expectedTrade.addAttribute("providerId", CORRELATION_ID.toString());
    expectedTrade.addAttribute("deal", deal.toString());
    assertTrue(JodaBeanUtils.equalIgnoring(ogTrade, expectedTrade, SimpleTrade.meta().securityLink()));
  }

  /**
   * Tests creation of the position.
   */
  @Test
  public void testPositionCreation() {
    // pay GBP, receive USD
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.USD, USD_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay GBP, receive USD so counterparty receives GBP, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.USD)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .rate(BigDecimal.valueOf(1.48))
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final FxSpotTrade trade = FxSpotTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TIME_STAMP)
        .build();
    final SimplePosition position = (SimplePosition) trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    // not testing position or security so no need to add
    final SimplePosition expectedPosition = new SimplePosition();
    expectedPosition.addAttribute("providerId", CORRELATION_ID.toString());
    expectedPosition.setQuantity(BigDecimal.ONE);
    assertTrue(JodaBeanUtils.equalIgnoring(position, expectedPosition, SimplePosition.meta().securityLink(), SimplePosition.meta().trades()));
  }
}
