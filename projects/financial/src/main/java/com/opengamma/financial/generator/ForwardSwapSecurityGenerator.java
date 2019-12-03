/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, forward swap security instances.
 */
public class ForwardSwapSecurityGenerator extends SecurityGenerator<ForwardSwapSecurity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ForwardSwapSecurityGenerator.class);
  private static final Tenor[] TENORS = new Tenor[] { Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS };
  private static final Tenor[] FORWARDS = new Tenor[] { Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS };

  private int _daysTrading = 30;

  public void setDaysTrading(final int daysTrading) {
    _daysTrading = daysTrading;
  }

  public int getDaysTrading() {
    return _daysTrading;
  }

  /**
   * Return the time series identifier.
   *
   * @param iborConvention
   *          the convention, not null
   * @return the time series identifier
   */
  protected ExternalId getTimeSeriesIdentifier(final IborIndexConvention iborConvention) {
    return iborConvention.getExternalIdBundle().getExternalId(getPreferredScheme());
  }

  private ExternalId getSwapRateFor(final Currency ccy, final LocalDate tradeDate, final Tenor maturityTenor, final Tenor forwardTenor) {
    return null;
  }

  @Override
  public ForwardSwapSecurity createSecurity() {
    final Currency ccy = getRandomCurrency();
    final ZonedDateTime now = ZonedDateTime.now();
    final ZonedDateTime tradeDate = previousWorkingDay(now.minusDays(getRandom(getDaysTrading())), ccy);
    final Tenor forward = getRandom(FORWARDS);
    final ZonedDateTime forwardDate = nextWorkingDay(now.plus(forward.getPeriod()), ccy);
    final SwapFixedLegConvention fixedLegConvention = getConventionSource().getSingle(ExternalId.of(Currency.OBJECT_SCHEME, ccy.getCode()),
        SwapFixedLegConvention.class);
    final VanillaIborLegConvention iborLegConvention = getConventionSource().getSingle(ExternalId.of(Currency.OBJECT_SCHEME, ccy.getCode()),
        VanillaIborLegConvention.class);
    if (iborLegConvention == null) {
      LOGGER.error("Couldn't get swap convention for {}", ccy.getCode());
      return null;
    }
    final Tenor maturity = getRandom(TENORS);
    // get the convention of the identifier of the initial rate
    final IborIndexConvention iborConvention = getConventionSource().getSingle(iborLegConvention.getIborIndexConvention(), IborIndexConvention.class);
    if (iborConvention == null) {
      LOGGER.error("Couldn't get ibor convention for {}", iborLegConvention.getIborIndexConvention());
      return null;
    }
    // look up the rate timeseries identifier out of the bundle
    final ExternalId tsIdentifier = getTimeSeriesIdentifier(iborConvention);
    // look up the value on our chosen trade date
    final HistoricalTimeSeries initialRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE,
        tsIdentifier.toBundle(), null, tradeDate.toLocalDate(), true, tradeDate.toLocalDate(), true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      LOGGER.error("couldn't get series for {} on {}", tsIdentifier, tradeDate);
      return null;
    }
    final Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(ccy, tradeDate.toLocalDate(), maturity, forward);
    if (swapRateForMaturityIdentifier == null) {
      LOGGER.error("Couldn't get swap rate identifier for {} [{}] from {}", new Object[] { ccy, maturity, tradeDate });
      return null;
    }
    final HistoricalTimeSeries fixedRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE,
        swapRateForMaturityIdentifier.toBundle(), null, tradeDate.toLocalDate(), true, tradeDate.toLocalDate(), true);
    if (fixedRateSeries == null) {
      LOGGER.error("can't find time series for {} on {}", swapRateForMaturityIdentifier, tradeDate);
      return null;
    }
    final Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + getRandom().nextDouble()) / 100d;
    final Double notional = (double) getRandom(100000) * 1000;
    final ZonedDateTime maturityDate = forwardDate.plus(maturity.getPeriod());
    final String counterparty = "CParty";
    final SwapLeg fixedLeg = new FixedInterestRateLeg(fixedLegConvention.getDayCount(), PeriodFrequency.of(fixedLegConvention.getPaymentTenor().getPeriod()),
        fixedLegConvention.getRegionCalendar(), fixedLegConvention.getBusinessDayConvention(), new InterestRateNotional(ccy, notional), false, fixedRate);
    final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(iborConvention.getDayCount(),
        PeriodFrequency.of(iborLegConvention.getResetTenor().getPeriod()), iborConvention.getRegionCalendar(), iborConvention.getBusinessDayConvention(),
        new InterestRateNotional(ccy, notional), false, tsIdentifier, FloatingRateType.IBOR);
    floatingLeg.setInitialFloatingRate(initialRate);
    final String fixedLegDescription = RATE_FORMATTER.format(fixedRate);
    final String floatingLegDescription = tsIdentifier.getValue();
    final boolean isPayFixed = getRandom().nextBoolean();
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    final ForwardSwapSecurity swap = new ForwardSwapSecurity(tradeDate, tradeDate, maturityDate, counterparty, payLeg, receiveLeg, forwardDate);
    swap.setName("IR Forward Swap " + ccy + " " + NOTIONAL_FORMATTER.format(notional) + " " + maturity.getPeriod() + " from "
        + forwardDate.format(DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    final ForwardSwapSecurity swap = createSecurity();
    if (swap != null) {
      return new ManageableTrade(quantity.createQuantity(), persister.storeSecurity(swap), swap.getTradeDate().toLocalDate(),
          swap.getTradeDate().toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    }
    return null;
  }

}
