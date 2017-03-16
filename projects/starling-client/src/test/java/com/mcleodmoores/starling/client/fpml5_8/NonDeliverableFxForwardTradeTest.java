/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.marketdata.DataSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangeRate;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.ExchangedCurrency;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxFixing;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSingleLeg;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSpotRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.NonDeliverableFxForwardTrade;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.NonDeliverableSettlement;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PartyTradeIdentifier;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PaymentAmount;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PrimaryRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuoteBasis;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.QuotedCurrencyPair;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.TradeHeader;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class NonDeliverableFxForwardTradeTest {
  private static final ExternalId DEFAULT_REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final LocalDate TRADE_DATE = LocalDate.of(2001, 11, 19);
  private static final LocalDate VALUE_DATE = LocalDate.of(2001, 12, 21);
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "A"));
  private static final TradeHeader HEADER = TradeHeader.builder().party1(PartyTradeIdentifier.builder().party(COUNTERPARTY)
      .tradeId(ExternalId.of("TRADE", "1")).build()).tradeDate(TRADE_DATE).build();
  private static final BigDecimal USD_AMOUNT = BigDecimal.valueOf(10000000);
  private static final PaymentAmount USD_PAYMENT = PaymentAmount.builder().amount(USD_AMOUNT).currency(Currency.USD).build();
  private static final BigDecimal EUR_AMOUNT = BigDecimal.valueOf(9175000);
  private static final PaymentAmount EUR_PAYMENT = PaymentAmount.builder().amount(EUR_AMOUNT).currency(Currency.EUR).build();
  private static final ExternalId CORRELATION_ID = ExternalId.of("ID", "123456");
  private static final FxSpotRateSource FX_RATE_SOURCE = FxSpotRateSource.builder()
      .fixingTime(LocalTime.of(16, 0))
      .primaryRateSource(PrimaryRateSource.builder()
          .dataSource(DataSource.DEFAULT)
          .rateSourcePage("FX FIX")
          .build())
      .businessCenterZone(ZoneId.of("America/New_York"))
      .build();

  @Test
  public void testPayUsdReceiveEurSettleInReceiveCurrency() {
    final NonDeliverableFXForwardSecurity expectedSecurity = new NonDeliverableFXForwardSecurity(Currency.USD, USD_AMOUNT.doubleValue(),
        Currency.EUR, EUR_AMOUNT.doubleValue(), VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION, true);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive EUR, pay USD so counterparty pays EUR, receives USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final QuotedCurrencyPair usdEur = QuotedCurrencyPair.builder()
      .currency1(Currency.USD)
      .currency2(Currency.EUR)
      .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
      .build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(usdEur)
        .rate(BigDecimal.valueOf(0.9175))
        .spotRate(BigDecimal.valueOf(0.9130))
        .forwardPoints(BigDecimal.valueOf(0.0045))
        .fxForwardQuoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final NonDeliverableFxForwardTrade trade = NonDeliverableFxForwardTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(ZonedDateTime.now())
        .nonDeliverableSettlement(NonDeliverableSettlement.builder()
            .fixing(FxFixing.builder()
                .fixingDate(VALUE_DATE)
                .fxSpotRateSource(FX_RATE_SOURCE)
                .quotedCurrencyPair(usdEur)
                .build())
            .settlementCurrency(Currency.EUR)
            .build())
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof NonDeliverableFXForwardSecurity);
    final NonDeliverableFXForwardSecurity security = (NonDeliverableFXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayUsdReceiveEurSettleInPayCurrency() {
    final NonDeliverableFXForwardSecurity expectedSecurity = new NonDeliverableFXForwardSecurity(Currency.USD, USD_AMOUNT.doubleValue(),
        Currency.EUR, EUR_AMOUNT.doubleValue(), VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION, false);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we receive EUR, pay USD so counterparty pays EUR, receives USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final QuotedCurrencyPair usdEur = QuotedCurrencyPair.builder()
      .currency1(Currency.USD)
      .currency2(Currency.EUR)
      .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
      .build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(usdEur)
        .rate(BigDecimal.valueOf(0.9175))
        .spotRate(BigDecimal.valueOf(0.9130))
        .forwardPoints(BigDecimal.valueOf(0.0045))
        .fxForwardQuoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final NonDeliverableFxForwardTrade trade = NonDeliverableFxForwardTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(ZonedDateTime.now())
        .nonDeliverableSettlement(NonDeliverableSettlement.builder()
            .fixing(FxFixing.builder()
                .fixingDate(VALUE_DATE)
                .fxSpotRateSource(FX_RATE_SOURCE)
                .quotedCurrencyPair(usdEur)
                .build())
            .settlementCurrency(Currency.USD)
            .build())
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof NonDeliverableFXForwardSecurity);
    final NonDeliverableFXForwardSecurity security = (NonDeliverableFXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayEurReceiveUsdSettleInReceiveCurrency() {
    // pay EUR, receive USD
    final NonDeliverableFXForwardSecurity expectedSecurity = new NonDeliverableFXForwardSecurity(Currency.EUR, EUR_AMOUNT.doubleValue(),
        Currency.USD, USD_AMOUNT.doubleValue(), VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION, true);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay EUR, receive USD so counterparty receives EUR, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final QuotedCurrencyPair usdEur = QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.EUR)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(usdEur)
        .rate(BigDecimal.valueOf(0.9175))
        .spotRate(BigDecimal.valueOf(0.9130))
        .forwardPoints(BigDecimal.valueOf(0.0045))
        .fxForwardQuoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final NonDeliverableFxForwardTrade trade = NonDeliverableFxForwardTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .nonDeliverableSettlement(NonDeliverableSettlement.builder()
            .fixing(FxFixing.builder()
                .fixingDate(VALUE_DATE)
                .fxSpotRateSource(FX_RATE_SOURCE)
                .quotedCurrencyPair(usdEur)
                .build())
            .settlementCurrency(Currency.USD)
            .build())
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof NonDeliverableFXForwardSecurity);
    final NonDeliverableFXForwardSecurity security = (NonDeliverableFXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }

  @Test
  public void testPayEurReceiveUsdSettleInPayCurrency() {
    // pay EUR, receive USD
    final NonDeliverableFXForwardSecurity expectedSecurity = new NonDeliverableFXForwardSecurity(Currency.EUR, EUR_AMOUNT.doubleValue(),
        Currency.USD, USD_AMOUNT.doubleValue(), VALUE_DATE.atStartOfDay(ZoneId.systemDefault()), DEFAULT_REGION, false);
    expectedSecurity.setExternalIdBundle(CORRELATION_ID.toBundle());
    // we pay EUR, receive USD so counterparty receives EUR, pays USD
    final ExchangedCurrency exchangedCurrency1 = ExchangedCurrency.builder().payerPartyReference(COUNTERPARTY).paymentAmount(USD_PAYMENT).build();
    final ExchangedCurrency exchangedCurrency2 = ExchangedCurrency.builder().receiverPartyReference(COUNTERPARTY).paymentAmount(EUR_PAYMENT).build();
    final QuotedCurrencyPair usdEur = QuotedCurrencyPair.builder()
        .currency1(Currency.USD)
        .currency2(Currency.EUR)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final ExchangeRate exchangeRate = ExchangeRate.builder()
        .quotedCurrencyPair(usdEur)
        .rate(BigDecimal.valueOf(0.9175))
        .spotRate(BigDecimal.valueOf(0.9130))
        .forwardPoints(BigDecimal.valueOf(0.0045))
        .fxForwardQuoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    final FxSingleLeg leg = FxSingleLeg.builder()
        .exchangedCurrency1(exchangedCurrency1)
        .exchangedCurrency2(exchangedCurrency2)
        .exchangeRate(exchangeRate)
        .valueDate(VALUE_DATE)
        .build();
    final NonDeliverableFxForwardTrade trade = NonDeliverableFxForwardTrade.builder()
        .correlationId(CORRELATION_ID)
        .tradeHeader(HEADER)
        .fxSingleLeg(leg)
        .party1Id(COUNTERPARTY.getExternalId())
        .creationTimestamp(TRADE_DATE.atStartOfDay(ZoneId.systemDefault()))
        .nonDeliverableSettlement(NonDeliverableSettlement.builder()
            .fixing(FxFixing.builder()
                .fixingDate(VALUE_DATE)
                .fxSpotRateSource(FX_RATE_SOURCE)
                .quotedCurrencyPair(usdEur)
                .build())
            .settlementCurrency(Currency.EUR)
            .build())
        .build();
    final Position position = trade.toPosition();
    assertEquals(position.getTrades().size(), 1);
    final Trade ogTrade = position.getTrades().iterator().next();
    assertTrue(ogTrade.getSecurity() instanceof NonDeliverableFXForwardSecurity);
    final NonDeliverableFXForwardSecurity security = (NonDeliverableFXForwardSecurity) ogTrade.getSecurity();
    assertEquals(security, expectedSecurity);
  }
}
