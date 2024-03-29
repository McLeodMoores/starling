/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link BondSecurity}, {@link BillSecurity}, {@link BondFutureSecurity} and {@link FloatingRateNoteSecurity} trades into the appropriate classes in
 * the analytics library.
 */
public class BondAndBondFutureTradeWithEntityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /**
   * Sector name string.
   */
  public static final String SECTOR_STRING = "IndustrySector";
  /**
   * Market type string.
   */
  public static final String MARKET_STRING = "Market";
  /** Excluded coupon types */
  private static final Set<String> EXCLUDED_TYPES = Sets.newHashSet("TOGGLE PIK NOTES", "FLOAT_RATE_NOTE");
  /** Rating agency strings */
  private static final String[] RATING_STRINGS = new String[] { "RatingMoody", "RatingFitch" };
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The security source */
  private final SecuritySource _securitySource;
  /** The legal entity source */
  private final LegalEntitySource _legalEntitySource;

  /**
   * @param holidaySource
   *          The holiday source, not null
   * @param conventionSource
   *          The convention source, not null
   * @param regionSource
   *          The region source, not null
   * @param securitySource
   *          The security source, not null
   * @param legalEntitySource
   *          The legal entity source, not null
   */
  public BondAndBondFutureTradeWithEntityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource, final LegalEntitySource legalEntitySource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _securitySource = securitySource;
    _legalEntitySource = legalEntitySource;
  }

  /**
   * Converts a government bond security into an {@link InstrumentDefinition}.
   *
   * @param security
   *          The government bond security.
   * @return The security definition
   */
  @Override
  public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final LegalEntity legalEntity = getLegalEntityForBond(Collections.<String, String> emptyMap(), security);
    return getFixedCouponBond(security, legalEntity);
  }

  /**
   * Converts a corporate bond security into an {@link InstrumentDefinition}.
   *
   * @param security
   *          The corporate bond security.
   * @return The security definition
   */
  @Override
  public InstrumentDefinition<?> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    final LegalEntity legalEntity = getLegalEntityForBond(Collections.<String, String> emptyMap(), security);
    return getFixedCouponBond(security, legalEntity);
  }

  @Override
  public InstrumentDefinition<?> visitBillSecurity(final BillSecurity security) {
    final LegalEntity legalEntity = getLegalEntityForBill(Collections.<String, String> emptyMap(), security);
    return getBill(security, legalEntity);
  }

  /**
   * Converts a bond or bond future trade into a {@link InstrumentDefinition}.
   *
   * @param trade
   *          The trade, not null. Must be a {@link BondSecurity}, {@link BondFutureSecurity}, {@link BillSecurity} or {@link FloatingRateNoteSecurity}
   * @return The transaction definition
   */
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    final FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
    ArgumentChecker.isTrue(
        security instanceof BondSecurity || security instanceof BondFutureSecurity || security instanceof BillSecurity
            || security instanceof FloatingRateNoteSecurity,
        "Can only handle trades with security type BondSecurity, BondFutureSecurity, BillSecurity or FloatingRateNotSecuritys; have {}" + security);
    final LocalDate tradeDate = trade.getTradeDate();
    if (tradeDate == null) {
      throw new OpenGammaRuntimeException("Trade date should not be null");
    }
    final double quantity = trade.getQuantity().doubleValue(); // MH - 9-May-2013: changed from 1. // TODO REVIEW: The quantity mechanism should be reviewed.
    if (trade.getPremium() == null) {
      throw new OpenGammaRuntimeException("Trade premium should not be null.");
    }
    final double price = trade.getPremium().doubleValue();
    if (security instanceof BondFutureSecurity) {
      final OffsetTime tradeTime = trade.getTradeTime();
      if (tradeTime == null) {
        throw new OpenGammaRuntimeException("Trade time should not be null");
      }
      final ZonedDateTime tradeDateTime = tradeDate.atTime(tradeTime).atZoneSameInstant(ZoneOffset.UTC);
      final BondFutureSecurity bondFutureSecurity = (BondFutureSecurity) security;
      final BondFuturesSecurityDefinition bondFuture = getBondFuture(bondFutureSecurity);
      // FIXME - BondFuturesTransactionDefinition shouldn't take quantity as an int. This could overflow.
      // should be a double, big decimal or long.
      return new BondFuturesTransactionDefinition(bondFuture, Double.valueOf(quantity).intValue(), tradeDateTime, price);
    }

    if (security instanceof InflationBondSecurity) {
      final OffsetTime tradeTime = trade.getTradeTime();
      if (tradeTime == null) {
        throw new OpenGammaRuntimeException("Trade time should not be null");
      }
      final ZonedDateTime tradeDateTime = tradeDate.atTime(tradeTime).atZoneSameInstant(ZoneOffset.UTC);
      final InflationBondSecurity bondSecurity = (InflationBondSecurity) security;
      final WorkingDayCalendar calendar;
      final ExternalId regionId = ExternalSchemes.financialRegionId(bondSecurity.getIssuerDomicile());
      // If the bond is Supranational, we use the calendar derived from the currency of the bond.
      // this may need revisiting.
      if (regionId.getValue().equals("SNAT")) { // Supranational
        calendar = WorkingDayCalendarUtils.getCalendarForCurrency(_holidaySource, bondSecurity.getCurrency());
      } else {
        calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(_regionSource, _holidaySource, regionId, bondSecurity.getCurrency());
      }
      final ZonedDateTime settlementDateTime = ScheduleCalculator.getAdjustedDate(tradeDateTime,
          Integer.parseInt(bondSecurity.attributes().get().get("daysToSettle")), calendar);
      final LegalEntity legalEntity = getLegalEntityForBond(trade.getAttributes(), bondSecurity);
      final BondCapitalIndexedSecurityDefinition<?> bond = (BondCapitalIndexedSecurityDefinition<?>) getInflationBond(bondSecurity, legalEntity);
      return new BondCapitalIndexedTransactionDefinition<>(bond, quantity, settlementDateTime, price);
    }

    OffsetTime settleTime = trade.getPremiumTime();
    if (settleTime == null) {
      settleTime = OffsetTime.of(LocalTime.NOON, ZoneOffset.UTC); // TODO get the real time zone
    }
    if (trade.getPremiumDate() == null) {
      throw new OpenGammaRuntimeException("Trade premium date should not be null");
    }
    final ZonedDateTime settlementDate = trade.getPremiumDate().atTime(settleTime).atZoneSameInstant(ZoneOffset.UTC);
    if (security instanceof BillSecurity) {
      final BillSecurity billSecurity = (BillSecurity) security;
      final LegalEntity legalEntity;
      if (_legalEntitySource != null) {
        final com.opengamma.core.legalentity.LegalEntity legalEntityFromSource = _legalEntitySource.getSingle(billSecurity.getLegalEntityId());
        legalEntity = convertToAnalyticsLegalEntity(legalEntityFromSource, security);
      } else {
        legalEntity = getLegalEntityForBill(Collections.<String, String> emptyMap(), billSecurity);
      }
      final BillSecurityDefinition underlying = getBill(billSecurity, legalEntity);
      final double settlementAmount = -price * quantity * underlying.getNotional();
      return new BillTransactionDefinition(underlying, quantity, settlementDate, settlementAmount);
    }
    if (security instanceof FloatingRateNoteSecurity) {
      final FloatingRateNoteSecurity frn = (FloatingRateNoteSecurity) security;
      final com.opengamma.core.legalentity.LegalEntity legalEntityFromSource = _legalEntitySource.getSingle(frn.getLegalEntityId());
      final LegalEntity legalEntity = convertToAnalyticsLegalEntity(legalEntityFromSource, security);
      final BondIborSecurityDefinition underlying = getIborBond(frn, legalEntity);
      return new BondIborTransactionDefinition(underlying, quantity, settlementDate, price);
    }
    final BondSecurity bondSecurity = (BondSecurity) security;
    final LegalEntity legalEntity = getLegalEntityForBond(trade.getAttributes(), bondSecurity);
    final InstrumentDefinition<?> underlying = getFixedCouponBond(bondSecurity, legalEntity);
    if (underlying instanceof PaymentFixedDefinition) {
      return underlying;
    }
    final BondFixedSecurityDefinition bond = (BondFixedSecurityDefinition) underlying;
    return new BondFixedTransactionDefinition(bond, quantity, settlementDate, price);
  }

  /**
   * Constructs a legal entity for a {@link BondSecurity}.
   *
   * @param tradeAttributes
   *          The trade attributes
   * @param security
   *          The bond security
   * @return A legal entity
   */
  public static LegalEntity getLegalEntityForBond(final Map<String, String> tradeAttributes, final BondSecurity security) {
    final Map<String, String> securityAttributes = security.getAttributes();
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    final String ticker;
    if (identifiers != null) {
      final String isin = identifiers.getValue(ExternalSchemes.ISIN);
      ticker = isin == null ? null : isin;
    } else {
      ticker = null;
    }
    final String shortName = security.getIssuerName();
    Set<CreditRating> creditRatings = null;
    for (final String ratingString : RATING_STRINGS) {
      if (securityAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(securityAttributes.get(ratingString), ratingString, true));
      }
      if (tradeAttributes.containsKey(ratingString)) {
        if (creditRatings == null) {
          creditRatings = new HashSet<>();
        }
        creditRatings.add(CreditRating.of(tradeAttributes.get(ratingString), ratingString, true));
      }
    }
    final String sectorName = security.getIssuerType();
    final FlexiBean classifications = new FlexiBean();
    classifications.put(MARKET_STRING, security.getMarket());
    if (tradeAttributes.containsKey(SECTOR_STRING)) {
      classifications.put(SECTOR_STRING, tradeAttributes.get(SECTOR_STRING));
    }
    final Sector sector = Sector.of(sectorName, classifications);
    final Region region;
    if (security.getIssuerDomicile().equals("SNAT")) { // Supranational
      region = Region.of(security.getIssuerDomicile(), null, security.getCurrency());
    } else {
      region = Region.of(security.getIssuerDomicile(), Country.of(security.getIssuerDomicile()), security.getCurrency());
    }
    final LegalEntity legalEntity = new LegalEntity(ticker, shortName, creditRatings, sector, region);
    return legalEntity;
  }

  /**
   * Constructs a legal entity for a {@link BillSecurity}.
   *
   * @param tradeAttributes
   *          The trade attributes
   * @param security
   *          The bill security
   * @return A legal entity
   */
  public static LegalEntity getLegalEntityForBill(final Map<String, String> tradeAttributes, final BillSecurity security) {
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    final String ticker;
    if (identifiers != null) {
      final String isin = identifiers.getValue(ExternalSchemes.ISIN);
      ticker = isin == null ? null : isin;
    } else {
      ticker = null;
    }
    final String regionName = security.getRegionId().getValue();
    final Region region;
    if (regionName.equals("SNAT")) { // Supranational
      region = Region.of(regionName, null, security.getCurrency());
    } else {
      region = Region.of(regionName, Country.of(regionName), security.getCurrency());
    }
    final LegalEntity legalEntity = new LegalEntity(ticker, ticker, null, null, region);
    return legalEntity;
  }

  /**
   * Creates a fixed coupon bond using the full legal entity information available.
   *
   * @param security
   *          The bond security
   * @param legalEntity
   *          The legal entity
   * @return The fixed coupon bond security definition
   */
  @SuppressWarnings("synthetic-access")
  private InstrumentDefinition<?> getFixedCouponBond(final BondSecurity security, final LegalEntity legalEntity) {
    return security.accept(new FinancialSecurityVisitorAdapter<InstrumentDefinition<?>>() {

      @Override
      public InstrumentDefinition<?> visitCorporateBondSecurity(final CorporateBondSecurity bond) {
        final String domicile = bond.getIssuerDomicile();
        ExternalIdBundle bundle = bond.getExternalIdBundle();
        bundle = bundle.withExternalId(ExternalSchemes.financialRegionId(domicile));
        final BondConvention bondConvention = _conventionSource.getSingle(bundle, BondConvention.class);
        return visitBondSecurity(bond, bondConvention);
      }

      @Override
      public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity bond) {
        final String domicile = bond.getIssuerDomicile();
        ExternalIdBundle bundle = bond.getExternalIdBundle();
        bundle = bundle.withExternalId(ExternalSchemes.financialRegionId(domicile));
        final BondConvention bondConvention = _conventionSource.getSingle(bundle, BondConvention.class);
        return visitBondSecurity(bond, bondConvention);
      }

      /**
       * Creates {@link BondFixedSecurityDefinition} for fixed-coupon bonds or {@link PaymentFixedDefinition} for zero-coupon bonds.
       *
       * @param bond
       *          The security
       * @param convention
       *          The convention
       * @return The definition
       */
      private InstrumentDefinition<?> visitBondSecurity(final BondSecurity bond, final BondConvention convention) {
        if (EXCLUDED_TYPES.contains(bond.getCouponType())) {
          throw new UnsupportedOperationException("Cannot support fixed coupon bonds with coupon of type " + bond.getCouponType());
        }
        final ExternalId regionId = ExternalSchemes.financialRegionId(bond.getIssuerDomicile());
        if (regionId == null) {
          throw new OpenGammaRuntimeException("Could not find region for " + bond.getIssuerDomicile());
        }
        final Currency currency = bond.getCurrency();
        final WorkingDayCalendar calendar;
        // If the bond is Supranational, we use the calendar derived from the currency of the bond.
        // this may need revisiting.
        if (regionId.getValue().equals("SNAT")) { // Supranational
          calendar = WorkingDayCalendarUtils.getCalendarForCurrency(_holidaySource, currency);
        } else {
          calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(_regionSource, _holidaySource, regionId, currency);
        }
        if (bond.getInterestAccrualDate() == null) {
          throw new OpenGammaRuntimeException("Bond first interest accrual date was null");
        }
        final ZoneId zone = bond.getInterestAccrualDate().getZone();
        final ZonedDateTime firstAccrualDate = ZonedDateTime.of(bond.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
        final ZonedDateTime maturityDate = ZonedDateTime.of(bond.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
        final double rate = bond.getCouponRate() / 100;
        final DayCount dayCount = bond.getDayCount();
        final BusinessDayConvention businessDay = convention.getBusinessDayConvention();
        final boolean isEOM = convention.isIsEOM();
        final YieldConvention yieldConvention = bond.getYieldConvention();
        if (bond.getCouponType().equals("NONE") || bond.getCouponType().equals("ZERO COUPON")) { // TODO find where string is
          return new PaymentFixedDefinition(currency, maturityDate, 1);
        }
        final int settlementDays = convention.getSettlementDays();
        final Period paymentPeriod = ConversionUtils.getTenor(bond.getCouponFrequency());
        final ZonedDateTime firstCouponDate = ZonedDateTime.of(bond.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
        return BondFixedSecurityDefinition.from(currency, firstAccrualDate, firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays,
            CalendarAdapter.of(calendar), dayCount, businessDay, yieldConvention, isEOM, legalEntity);
      }

    });
  }

  /**
   * Creates a bill.
   *
   * @param security
   *          The bill security
   * @param legalEntity
   *          The legal entity
   * @return The bill security definition
   */
  private BillSecurityDefinition getBill(final BillSecurity security, final LegalEntity legalEntity) {
    final Currency currency = security.getCurrency();
    final ZonedDateTime maturityDate = security.getMaturityDate().getExpiry();
    final double notional = 1;
    final int settlementDays = security.getDaysToSettle();
    final WorkingDayCalendar calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(_regionSource, _holidaySource, security.getRegionId(), currency);
    final YieldConvention yieldConvention = security.getYieldConvention();
    final DayCount dayCount = security.getDayCount();
    return new BillSecurityDefinition(currency, maturityDate, notional, settlementDays, calendar, yieldConvention, dayCount, legalEntity);
  }

  /**
   * Creates an ibor bond using the full legal entity information available.
   *
   * @param security
   *          The bond security
   * @param legalEntity
   *          The legal entity
   * @return The ibor bond security definition
   */
  @SuppressWarnings("synthetic-access")
  private BondIborSecurityDefinition getIborBond(final FloatingRateNoteSecurity frn, final LegalEntity legalEntity) {
    final Currency currency = frn.getCurrency();
    final WorkingDayCalendar calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(_regionSource, _holidaySource,
        ExternalSchemes.currencyRegionId(currency), currency);
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) _securitySource
        .getSingle(frn.getBenchmarkRateId().toBundle());
    final IborIndexConvention iborConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    final boolean isEOM = iborConvention.isIsEOM();
    final IborIndex index = new IborIndex(currency, indexSecurity.getTenor().getPeriod(), iborConvention.getSettlementDays(), iborConvention.getDayCount(),
        iborConvention.getBusinessDayConvention(), isEOM, iborConvention.getName());
    final ExternalId regionId = frn.getRegionId();
    final DayCount dayCount = frn.getDayCount();
    final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    final int settlementDays = 0; // TODO
    return BondIborSecurityDefinition.from(frn.getMaturityDate().getExpiry(), frn.getIssueDate(), index, settlementDays, dayCount, businessDay, isEOM,
        legalEntity, CalendarAdapter.of(calendar));
  }

  /**
   * Creates a fixed coupon bond using the full legal entity information available.
   *
   * @param security
   *          The bond security
   * @param legalEntity
   *          The legal entity
   * @return The fixed coupon bond security definition
   */
  @SuppressWarnings("synthetic-access")
  private InstrumentDefinition<?> getInflationBond(final InflationBondSecurity security, final LegalEntity legalEntity) {
    return security.accept(new FinancialSecurityVisitorAdapter<InstrumentDefinition<?>>() {

      @Override
      public InstrumentDefinition<?> visitInflationBondSecurity(final InflationBondSecurity bond) {
        final String domicile = bond.getIssuerDomicile();
        ArgumentChecker.notNull(domicile, "bond security domicile cannot be null");
        final BondConvention convention = _conventionSource.getSingle(ExternalSchemes.countryRegionId(Country.of(domicile)), BondConvention.class);
        if (EXCLUDED_TYPES.contains(bond.getCouponType())) {
          throw new UnsupportedOperationException("Cannot support fixed coupon bonds with coupon of type " + bond.getCouponType());
        }
        final ExternalId regionId = ExternalSchemes.financialRegionId(bond.getIssuerDomicile());
        if (regionId == null) {
          throw new OpenGammaRuntimeException("Could not find region for " + bond.getIssuerDomicile());
        }
        final Currency currency = bond.getCurrency();
        final ExternalId indexId = ExternalId.parse(bond.attributes().get().get("ReferenceIndexId"));

        final Security sec = _securitySource.getSingle(indexId.toBundle());
        if (sec == null) {
          throw new OpenGammaRuntimeException("Ibor index with id " + indexId + " was null");
        }
        final com.opengamma.financial.security.index.PriceIndex indexSecurity = (com.opengamma.financial.security.index.PriceIndex) sec;

        final IndexPrice priceIndex = new IndexPrice(indexSecurity.getName(), currency);
        final Calendar calendar;
        // If the bond is Supranational, we use the calendar derived from the currency of the bond.
        // this may need revisiting.
        if (regionId.getValue().equals("SNAT")) { // Supranational
          calendar = CalendarUtils.getCalendar(_holidaySource, currency);
        } else {
          calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
        }
        if (bond.getInterestAccrualDate() == null) {
          throw new OpenGammaRuntimeException("Bond first interest accrual date was null");
        }
        final ZoneId zone = bond.getInterestAccrualDate().getZone();
        final ZonedDateTime firstAccrualDate = ZonedDateTime.of(bond.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
        final ZonedDateTime maturityDate = ZonedDateTime.of(bond.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
        final int monthLag = Integer.parseInt(bond.attributes().get().get("InflationLag"));
        final double rate = bond.getCouponRate() / 100;
        final DayCount dayCount = bond.getDayCount();
        final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING; // bond.getBusinessDayConvention();
        final boolean isEOM = convention.isIsEOM();
        final YieldConvention yieldConvention = bond.getYieldConvention();
        if (bond.getCouponType().equals("NONE") || bond.getCouponType().equals("ZERO COUPON")) { // TODO find where string is
          return new PaymentFixedDefinition(currency, maturityDate, 1);
        }
        final int settlementDays = Integer.parseInt(bond.attributes().get().get("daysToSettle"));
        final Period paymentPeriod = ConversionUtils.getTenor(bond.getCouponFrequency());
        final double baseCPI = Double.parseDouble(bond.attributes().get().get("BaseCPI"));
        final ZonedDateTime firstCouponDate = ZonedDateTime.of(bond.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
        final String interpolationMethod = bond.attributes().get().get("interpolationMethod");
        if ("Monthly".equals(interpolationMethod)) {
          return BondCapitalIndexedSecurityDefinition.fromMonthly(priceIndex, monthLag, firstAccrualDate, baseCPI, firstCouponDate, maturityDate, paymentPeriod,
              rate, businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM, legalEntity);
        } else if ("Daily".equals(interpolationMethod)) {
          return BondCapitalIndexedSecurityDefinition.fromInterpolation(priceIndex, monthLag, firstAccrualDate, baseCPI, maturityDate, paymentPeriod, 1.0, rate,
              businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM, legalEntity);
        } else {
          throw new OpenGammaRuntimeException("Bond interpolation method is not valid");
        }
      }
    });
  }

  /**
   * Constructs a {@link BondFuturesSecurityDefinition} from a {@link BondFutureSecurity}.
   *
   * @param bondFuture
   *          The bond future security
   * @return The bond future definition
   */
  public BondFuturesSecurityDefinition getBondFuture(final BondFutureSecurity bondFuture) {
    final ZonedDateTime lastTradeDate = bondFuture.getExpiry().getExpiry();
    final ZonedDateTime firstNoticeDate = bondFuture.getFirstDeliveryDate();
    final ZonedDateTime lastNoticeDate = bondFuture.getLastDeliveryDate();
    final double notional = bondFuture.getUnitAmount();
    final List<BondFutureDeliverable> basket = bondFuture.getBasket();
    final int n = basket.size();
    final BondFixedSecurityDefinition[] deliveryBasket = new BondFixedSecurityDefinition[n];
    final double[] conversionFactor = new double[n];
    for (int i = 0; i < n; i++) {
      final BondFutureDeliverable deliverable = basket.get(i);
      final BondSecurity bondSecurity = (BondSecurity) _securitySource.getSingle(deliverable.getIdentifiers());
      if (bondSecurity == null) {
        throw new OpenGammaRuntimeException("Security with identifier bundle " + deliverable.getIdentifiers() + " not in security source");
      }
      final LegalEntity issuer = getLegalEntityForBond(new HashMap<String, String>(), bondSecurity);
      final InstrumentDefinition<?> definition = getFixedCouponBond(bondSecurity, issuer);
      if (!(definition instanceof BondFixedSecurityDefinition)) {
        throw new OpenGammaRuntimeException("Could not construct fixed coupon bond from " + bondSecurity);
      }
      deliveryBasket[i] = (BondFixedSecurityDefinition) definition;
      conversionFactor[i] = deliverable.getConversionFactor();
    }
    return new BondFuturesSecurityDefinition(lastTradeDate, firstNoticeDate, lastNoticeDate, notional, deliveryBasket, conversionFactor);
  }

  private static LegalEntity convertToAnalyticsLegalEntity(final com.opengamma.core.legalentity.LegalEntity entity, final FinancialSecurity security) {
    final Collection<Rating> ratings = entity.getRatings();
    final ExternalIdBundle identifiers = security.getExternalIdBundle();
    final String ticker;
    if (identifiers != null) {
      final String isin = identifiers.getValue(ExternalSchemes.ISIN);
      ticker = isin == null ? null : isin;
    } else {
      ticker = null;
    }
    final String shortName = entity.getName();
    Set<CreditRating> creditRatings = null;
    for (final Rating rating : ratings) {
      if (creditRatings == null) {
        creditRatings = new HashSet<>();
      }
      // TODO seniority level needs to go into the credit rating
      creditRatings.add(CreditRating.of(rating.getRater(), rating.getScore().toString(), true));
    }
    if (security instanceof BillSecurity) {
      final BillSecurity bill = (BillSecurity) security;
      final Region region = Region.of(bill.getRegionId().getValue(), Country.of(bill.getRegionId().getValue()), bill.getCurrency());
      return new LegalEntity(ticker, shortName, creditRatings, null, region);
    }
    return new LegalEntity(ticker, shortName, creditRatings, null, null);
  }

}
