/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, cash security instances.
 */
public class CashSecurityGenerator extends SecurityGenerator<CashSecurity> {

  protected String createName(final Currency currency, final double amount, final double rate, final ZonedDateTime maturity) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Cash ").append(currency.getCode()).append(" ").append(NOTIONAL_FORMATTER.format(amount));
    sb.append(" @ ").append(RATE_FORMATTER.format(rate)).append(", maturity ").append(maturity.format(DATE_FORMATTER));
    return sb.toString();
  }

  private ExternalId getCashRate(final Currency ccy, final LocalDate tradeDate, final Tenor tenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = getCurrencyCurveConfig(ccy);
    if (curveSpecConfig == null) {
      return null;
    }
    return curveSpecConfig.getCashSecurity(tradeDate, tenor);
  }

  @Override
  public CashSecurity createSecurity() {
    final Currency currency = getRandomCurrency();
    final ExternalId region = ExternalSchemes.currencyRegionId(currency);
    final ZonedDateTime start = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(60) + 7), currency);
    final int length = getRandom(6) + 3;
    final ZonedDateTime maturity = nextWorkingDay(start.plusMonths(length), currency);
    DayCount dayCount;
    try {
      final DepositConvention depositConvention = getConventionSource().getSingle(ExternalId.of(Currency.OBJECT_SCHEME, currency.getCode()),
          DepositConvention.class);
      dayCount = depositConvention.getDayCount();
    } catch (final DataNotFoundException e1) {
      // try ibor
      try {
        final IborIndexConvention iborIndexConvention = getConventionSource().getSingle(ExternalId.of(Currency.OBJECT_SCHEME, currency.getCode()),
            IborIndexConvention.class);
        dayCount = iborIndexConvention.getDayCount();
      } catch (final DataNotFoundException e2) {
        final OvernightIndexConvention overnightIndexConvention = getConventionSource().getSingle(ExternalId.of(Currency.OBJECT_SCHEME, currency.getCode()),
            OvernightIndexConvention.class);
        dayCount = overnightIndexConvention.getDayCount();
      }
    }

    final ExternalId cashRate = getCashRate(currency, start.toLocalDate(), Tenor.ofMonths(length));
    if (cashRate == null) {
      return null;
    }
    final HistoricalTimeSeries timeSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, cashRate.toBundle(), null,
        start.toLocalDate(), true, start.toLocalDate(), true);
    if (timeSeries == null || timeSeries.getTimeSeries().isEmpty()) {
      return null;
    }
    final double rate = timeSeries.getTimeSeries().getEarliestValue() * getRandom(0.8, 1.2);
    final double amount = 10000 * (getRandom(1500) + 200);
    final CashSecurity security = new CashSecurity(currency, region, start, maturity, dayCount, rate, amount);
    security.setName(createName(currency, amount, rate, maturity));
    return security;
  }

}
