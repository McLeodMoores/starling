/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import com.mcleodmoores.starling.client.portfolio.fpml5_8.CrossRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangeRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangedCurrency;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSingleLeg;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSpotTrade;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PartyTradeIdentifier;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.TradeHeader;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class FxSpotCrossTradeTest {
  private static final ExternalId DEFAULT_REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final LocalDate TRADE_DATE = LocalDate.of(2001, 10, 23);
  private static final LocalDate VALUE_DATE = LocalDate.of(2001, 10, 25);
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "A"));
  private static final TradeHeader HEADER = TradeHeader.builder().party1(PartyTradeIdentifier.builder().party(COUNTERPARTY)
      .tradeId(ExternalId.of("TRADE", "1")).build()).tradeDate(TRADE_DATE).build();
  private static final BigDecimal GBP_AMOUNT = BigDecimal.valueOf(10000000);
  private static final PaymentAmount GBP_PAYMENT = PaymentAmount.builder().amount(GBP_AMOUNT).currency(Currency.GBP).build();
  private static final BigDecimal EUR_AMOUNT = BigDecimal.valueOf(6300680);
  private static final PaymentAmount EUR_PAYMENT = PaymentAmount.builder().amount(EUR_AMOUNT).currency(Currency.EUR).build();
  private static final ExternalId CORRELATION_ID = ExternalId.of("ID", "123456");
  private static final CrossRate EURUSD = CrossRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.EUR)
        .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
        .build())
      .rate(BigDecimal.valueOf(0.9325))
      .build();
  private static final CrossRate GBPUSD = CrossRate.builder()
      .quotedCurrencyPair(QuotedCurrencyPair.builder()
        .currency1(Currency.GBP)
        .currency2(Currency.USD)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build())
      .rate(BigDecimal.valueOf(1.48))
      .build();

  @Test
  public void testPayEurReceiveGbp1() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.EUR, EUR_AMOUNT.doubleValue(), Currency.GBP, GBP_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive GBP, pay EUR, so counterparty pays GBP, receives EUR
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.EUR)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
//        .rate(BigDecimal.valueOf(0.630068))
        .crossRate1(EURUSD)
        .crossRate2(GBPUSD)
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
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayEurReceiveGbp2() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.EUR, EUR_AMOUNT.doubleValue(), Currency.GBP, GBP_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive GBP, pay EUR, so counterparty pays GBP, receives EUR
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.EUR)
          .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
          .build())
        .crossRate1(EURUSD)
        .crossRate2(GBPUSD)
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
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayGbpReceiveEur1() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.EUR, EUR_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive EUR, pay GBP, so counterparty pays EUR, receives GBP
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
          .currency1(Currency.GBP)
          .currency2(Currency.EUR)
          .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
          .build())
        .crossRate1(EURUSD)
        .crossRate2(GBPUSD)
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
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayGbpReceiveEur2() {
    final FXForwardSecurity expectedSecurity = new FXForwardSecurity(Currency.GBP, GBP_AMOUNT.doubleValue(), Currency.EUR, EUR_AMOUNT.doubleValue(),
        VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive EUR, pay GBP, so counterparty pays EUR, receives GBP
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(GBP_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(QuotedCurrencyPair.builder()
            .currency1(Currency.GBP)
            .currency2(Currency.EUR)
            .quoteBasis(QuoteBasis.CURRENCY1_PER_CURRENCY2)
            .build())
        .crossRate1(EURUSD)
        .crossRate2(GBPUSD)
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
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof FXForwardSecurity);
    final FXForwardSecurity security = (FXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }
}
