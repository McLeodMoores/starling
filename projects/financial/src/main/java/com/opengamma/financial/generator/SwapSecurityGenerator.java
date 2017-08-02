/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of random, but reasonable, swap security instances.
 */
public class SwapSecurityGenerator extends SecurityGenerator<SwapSecurity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SwapSecurityGenerator.class);
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS,
      Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20) };
  private static final Map<Currency, ExternalId> FIXED_LEG_CONVENTION_FOR_CCY = new HashMap<>();
  private static final Map<Currency, ExternalId> IBOR_LEG_CONVENTION_FOR_CCY = new HashMap<>();

  static {
    FIXED_LEG_CONVENTION_FOR_CCY.put(Currency.USD, ExternalId.of("CONVENTION", "USD IBOR Fixed"));
    FIXED_LEG_CONVENTION_FOR_CCY.put(Currency.GBP, ExternalId.of("CONVENTION", "GBP IBOR Fixed"));
    FIXED_LEG_CONVENTION_FOR_CCY.put(Currency.CHF, ExternalId.of("CONVENTION", "CHF IBOR Fixed"));
    FIXED_LEG_CONVENTION_FOR_CCY.put(Currency.EUR, ExternalId.of("CONVENTION", "EUR IBOR Fixed"));
    FIXED_LEG_CONVENTION_FOR_CCY.put(Currency.JPY, ExternalId.of("CONVENTION", "JPY IBOR Fixed"));
    IBOR_LEG_CONVENTION_FOR_CCY.put(Currency.USD, ExternalId.of("CONVENTION", "USD 3M IBOR"));
    IBOR_LEG_CONVENTION_FOR_CCY.put(Currency.GBP, ExternalId.of("CONVENTION", "GBP 6M IBOR"));
    IBOR_LEG_CONVENTION_FOR_CCY.put(Currency.CHF, ExternalId.of("CONVENTION", "CHF 6M IBOR"));
    IBOR_LEG_CONVENTION_FOR_CCY.put(Currency.EUR, ExternalId.of("CONVENTION", "EUR 6M IBOR"));
    IBOR_LEG_CONVENTION_FOR_CCY.put(Currency.JPY, ExternalId.of("CONVENTION", "JPY 6M IBOR"));
  }

  private int _daysTrading = 60;
  private LocalDate _swaptionExpiry;

  public void setDaysTrading(final int daysTrading) {
    _daysTrading = daysTrading;
  }

  public int getDaysTrading() {
    return _daysTrading;
  }

  public void setSwationExpiry(final LocalDate swaptionExpiry) {
    _swaptionExpiry = swaptionExpiry;
  }

  public LocalDate getSwaptionExpiry() {
    return _swaptionExpiry;
  }

  /**
   * Return the time series identifier.
   *
   * @param liborConvention the convention bundle, not null
   * @return the time series identifier
   */
  protected ExternalId getTimeSeriesIdentifier(final ConventionBundle liborConvention) {
    return liborConvention.getIdentifiers().getExternalId(getPreferredScheme());
  }

  @Override
  public SwapSecurity createSecurity() {
    final Currency ccy = getRandomCurrency();
    final Collection<Holiday> holidays = getHolidaySource().get(ccy);
    if (holidays == null || holidays.size() != 1) {
      LOGGER.error("Could not get currency holidays for {}", ccy);
      return null;
    }
    final List<LocalDate> holidayDates = holidays.iterator().next().getHolidayDates();
    LocalDate tradeDate;
    int i = 0;
    do {
      if (getSwaptionExpiry() == null) { // just a normal swap
        tradeDate = LocalDate.now().minusDays(getRandom(getDaysTrading()));
      } else {
        tradeDate = getSwaptionExpiry().plusDays(2 + i++); // effective date should be at least two days after expiry of swaption.
      }
    } while (holidayDates.contains(tradeDate));

    final Tenor maturity = getRandom(TENORS);
    ExternalId tsIdentifier = null;
    DayCount fixedLegDayCount = null;
    DayCount floatingLegDayCount = null;
    Frequency fixedLegFrequency = null;
    Frequency floatingLegFrequency = null;
    BusinessDayConvention fixedLegBusinessDayConvention = null;
    BusinessDayConvention floatingLegBusinessDayConvention = null;
    ExternalId fixedLegRegion = null;
    ExternalId floatingLegRegion = null;
    ExternalId indexId = null;
    // try the convention master - see if there's a convention of the appropriate type with the currency as the identifier
    final ConventionSource conventionSource = getConventionSource();
    if (conventionSource != null) {
      SwapFixedLegConvention fixedLegConvention = null;
      VanillaIborLegConvention iborLegConvention = null;
      try {
        fixedLegConvention = conventionSource.getSingle(FIXED_LEG_CONVENTION_FOR_CCY.get(ccy), SwapFixedLegConvention.class);
      } catch (final DataNotFoundException e) {
        LOGGER.error("Could not get SwapFixedLegConvention with id {}", FIXED_LEG_CONVENTION_FOR_CCY.get(ccy));
      }
      try {
        iborLegConvention = conventionSource.getSingle(IBOR_LEG_CONVENTION_FOR_CCY.get(ccy), VanillaIborLegConvention.class);
      } catch (final DataNotFoundException e) {
        LOGGER.error("Could not get VanillaIborLegConvention with id {}", IBOR_LEG_CONVENTION_FOR_CCY.get(ccy));
      }
      if (fixedLegConvention != null) {
        fixedLegDayCount = fixedLegConvention.getDayCount();
        fixedLegFrequency = PeriodFrequency.of(fixedLegConvention.getPaymentTenor().getPeriod());
        fixedLegBusinessDayConvention = fixedLegConvention.getBusinessDayConvention();
        fixedLegRegion = fixedLegConvention.getRegionCalendar();
      } else {
        LOGGER.error("Could not get SwapFixedLegConvention for {}", ccy);
      }
      if (iborLegConvention != null) {
        IborIndexConvention indexConvention = null;
        try {
          indexConvention = conventionSource.getSingle(iborLegConvention.getIborIndexConvention().toBundle(), IborIndexConvention.class);
        } catch (final DataNotFoundException e) {
        }
        if (indexConvention != null) {
          floatingLegDayCount = indexConvention.getDayCount();
          floatingLegBusinessDayConvention = indexConvention.getBusinessDayConvention();
          floatingLegRegion = indexConvention.getRegionCalendar();
          final SecuritySearchRequest indexRequest = new SecuritySearchRequest(indexConvention.getExternalIdBundle());
          try {
            final SecuritySearchResult securities = getSecurityMaster().search(indexRequest);
            for (final ManageableSecurity security : securities.getSecurities()) {
              if (security instanceof IborIndex) {
                final IborIndex indexSecurity = (IborIndex) security;
                floatingLegFrequency = PeriodFrequency.of(indexSecurity.getTenor().getPeriod());
                indexId = indexSecurity.getExternalIdBundle().getExternalId(getPreferredScheme());
                tsIdentifier = indexId;
                break;
              }
            }
          } catch (final Exception e) {
            LOGGER.error("Could not get any securities with id {}", indexConvention.getExternalIdBundle());
          }
        } else {
          LOGGER.error("Could not get IborIndexConvention with identifier {}", indexConvention);
        }
      } else {
        LOGGER.error("Could not get VanillaIborLegConvention for {}", ccy);
      }
    }
    // just need to test that either the fixed or floating leg was missing
    if (fixedLegDayCount == null || floatingLegFrequency == null || tsIdentifier == null || indexId == null) {
      // couldn't find information from convention, try the old way
      // discouraged use of ConventionBundleSource
      final ConventionBundle swapConvention =
          getConventionBundleSource().getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
      if (swapConvention == null) {
        LOGGER.error("Couldn't get swap convention for {}", ccy.getCode());
        return null;
      }
      // get the convention of the identifier of the initial rate
      final ConventionBundle liborConvention = getConventionBundleSource().getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
      if (liborConvention == null) {
        LOGGER.error("Couldn't get libor convention for {}", swapConvention.getSwapFloatingLegInitialRate());
        return null;
      }
      if (tsIdentifier == null) {
        // look up the rate timeseries identifier out of the bundle
        tsIdentifier = getTimeSeriesIdentifier(liborConvention);
      }
      if (tsIdentifier == null) {
        LOGGER.error("Could not get time series identifier for {}", liborConvention);
        return null;
      }
      if (fixedLegDayCount == null) {
        fixedLegDayCount = swapConvention.getSwapFixedLegDayCount();
        fixedLegFrequency = swapConvention.getSwapFixedLegFrequency();
        fixedLegBusinessDayConvention = swapConvention.getSwapFixedLegBusinessDayConvention();
        fixedLegRegion = swapConvention.getSwapFixedLegRegion();
      }
      if (floatingLegDayCount == null) {
        floatingLegDayCount = swapConvention.getSwapFloatingLegDayCount();
        floatingLegFrequency = swapConvention.getSwapFloatingLegFrequency();
        floatingLegBusinessDayConvention = swapConvention.getSwapFloatingLegBusinessDayConvention();
        floatingLegRegion = swapConvention.getSwapFloatingLegRegion();
      }
      if (indexId == null) {
        indexId = swapConvention.getSwapFloatingLegInitialRate();
      }
      if (indexId == null) {
        LOGGER.error("Could not get index id for {}", swapConvention);
        return null;
      }
    }
    // look up the value on our chosen trade date
    final HistoricalTimeSeries rateSeries =
        getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, tsIdentifier.toBundle(), null, tradeDate,
        true, tradeDate, true);
    if (rateSeries == null || rateSeries.getTimeSeries().isEmpty()) {
      LOGGER.error("couldn't get series for {} on {}", tsIdentifier, tradeDate);
      return null;
    }
    final Double initialRate = rateSeries.getTimeSeries().getLatestValue();
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    final Double fixedRate = initialRate + (getRandom().nextDouble() - 0.5) / 200.;
    return createSwap(ccy, tradeDate, maturity, tsIdentifier, fixedLegDayCount,
        floatingLegDayCount, fixedLegFrequency, floatingLegFrequency,
        fixedLegBusinessDayConvention, floatingLegBusinessDayConvention,
        fixedLegRegion, floatingLegRegion, indexId, initialRate, fixedRate);
  }

  private SwapSecurity createSwap(final Currency ccy, final LocalDate tradeDate, final Tenor maturity, final ExternalId tsIdentifier,
      final DayCount fixedLegDayCount, final DayCount floatingLegDayCount, final Frequency fixedLegFrequency, final Frequency floatingLegFrequency,
      final BusinessDayConvention fixedLegBusinessDayConvention, final BusinessDayConvention floatingLegBusinessDayConvention,
      final ExternalId fixedLegRegion, final ExternalId floatingLegRegion, final ExternalId indexId, final Double initialRate, final Double fixedRate) {
    final Double notional = (double) (getRandom(99999) + 1) * 1000;
    final ZonedDateTime tradeDateTime = tradeDate.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturityDateTime = tradeDate.plus(maturity.getPeriod()).atStartOfDay(ZoneOffset.UTC);
    final String counterparty = "CParty";
    final SwapLeg fixedLeg = new FixedInterestRateLeg(fixedLegDayCount,
        fixedLegFrequency,
        fixedLegRegion,
        fixedLegBusinessDayConvention,
        new InterestRateNotional(ccy, notional),
        false, fixedRate);
    final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(floatingLegDayCount,
        floatingLegFrequency,
        floatingLegRegion,
        floatingLegBusinessDayConvention,
        new InterestRateNotional(ccy, notional),
        false, tsIdentifier,
        FloatingRateType.IBOR);
    floatingLeg.setInitialFloatingRate(initialRate);
    final String fixedLegDescription = RATE_FORMATTER.format(fixedRate);
    final String floatingLegDescription = indexId.getValue();
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
    final SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, counterparty, payLeg, receiveLeg);
    swap.setName("IR Swap " + ccy + " "
        + NOTIONAL_FORMATTER.format(notional) + " " + maturityDateTime.format(DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    ManageableTrade trade = null;
    final SwapSecurity swap = createSecurity();
    if (swap != null) {
      trade = new ManageableTrade(quantity.createQuantity(), persister.storeSecurity(swap), swap.getTradeDate().toLocalDate(),
          swap.getTradeDate().toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
    }
    return trade;
  }

}
