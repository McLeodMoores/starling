/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.DefaultDateAdjustmentCalculator;
import com.opengamma.analytics.date.DefaultSettlementDateCalculator;
import com.opengamma.analytics.date.EndOfMonthDateAdjustmentCalculator;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.calendar.HolidaySourceWorkingDayCalendarAdapter;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Convert a cash node into an {@link InstrumentDefinition} for use in the analytics library. This class supports
 * deposit, overnight index and ibor index. In all cases, the convention id in the node is assumed to be an identifier
 * for either a {@link Convention} or {@link Security}. In most cases, either a convention or security can be used
 * to construct the correct instrument. However, if the index is compounded (e.g. a 3 month term on a 6m index) then
 * the security must be used.
 * <p>
 * For deposits and ibor indices, the dates are calculated as:
 * <ul>
 *  <li> The spot date is computed from the valuation date adding the number of settlement days (i.e. the number of business days) of the convention.
 *  <li> The start date is computed from the spot date by adding the start tenor of the node and using the business day convention, calendar and EOM
 *   of the convention to adjust to the appropriate business day.
 *  <li> The end date is computed from the start date by adding the maturity tenor of the node and using the business day convention, calendar
 *   and EOM of the convention to adjust to the appropriate business day.
 * </ul>
 * <p>
 * For overnight indices, the dates are calculated as:
 * <ul>
 *  <li> The spot and start dates are set to the valuation date.
 *  <li> The end date is computed from the start date by adding the maturity tenor (overnight or tom/next) of the node and using the following
 *  business day convention and calendar to adjust to the appropriate business day.
 * </ul>
 * <p>
 * <p>
 * The notional in all cases is one.
 */
public class CashNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The security source */
  private final SecuritySource _securitySource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;
  /** The cash rate */
  private final Double _rate;

  /**
   * @param securitySource  the security source, not null
   * @param holidaySource  the holiday source, not null
   * @param regionSource  the region source, not null
   * @param marketData  the market data, not null
   * @param dataId  the id of the market data, not null
   * @param valuationTime  the valuation time, not null
   */
  public CashNodeConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.notNull(dataId, "dataId");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
    _rate = _marketData.getDataPoint(_dataId);
    if (_rate == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
  }

  /**
   * @param securitySource  the security source, not null
   * @param conventionSource  the convention source, not used
   * @param holidaySource  the holiday source, not null
   * @param regionSource  the region source, not null
   * @param marketData  the market data, not null
   * @param dataId  the id of the market data, not null
   * @param valuationTime  the valuation time, not null
   * @deprecated  Use the constructor that does not take a convention source
   */
  @Deprecated
  public CashNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource,
      final RegionSource regionSource, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    this(securitySource, holidaySource, regionSource, marketData, dataId, valuationTime);
  }

  @Override
  public InstrumentDefinition<?> visitCashNode(final CashNode cashNode) {
    final Tenor startTenor = cashNode.getStartTenor();
    final Tenor maturityTenor = cashNode.getMaturityTenor();
    try {
      final Convention convention =
          ConventionLink.resolvable(cashNode.getConvention().toBundle(), Convention.class).resolve();
      if (convention == null) {
        return getFromSecurity(cashNode, startTenor, maturityTenor);
      }
      final LocalTime time = _valuationTime.toLocalTime();
      final ZoneOffset zone = _valuationTime.getOffset();
      if (convention instanceof DepositConvention) {
        final DepositConvention depositConvention = (DepositConvention) convention;
        final Currency currency = depositConvention.getCurrency();
        final Region region = _regionSource.getHighestLevelRegion(depositConvention.getRegionCalendar());
        final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
        final BusinessDayConvention businessDayConvention = depositConvention.getBusinessDayConvention();
        final boolean isEOM = depositConvention.isIsEOM();
        final DayCount dayCount = depositConvention.getDayCount();
        final int settlementDays = depositConvention.getSettlementDays();
        final LocalDate spotLocalDate = DefaultSettlementDateCalculator.getInstance().getSettlementDate(_valuationTime.toLocalDate(), settlementDays, calendar);
        final LocalDate startLocalDate, endLocalDate;
        if (isEOM) {
          startLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        } else {
          startLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        }
        final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(startLocalDate, time), zone);
        final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(endLocalDate, time), zone);
        final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
        return new CashDefinition(currency, startDate, endDate, 1, _rate, accrualFactor);
      } else if (convention instanceof OvernightIndexConvention) {
        final OvernightIndexConvention overnightIndexConvention = (OvernightIndexConvention) convention;
        final Currency currency = overnightIndexConvention.getCurrency();
        final Region region = _regionSource.getHighestLevelRegion(overnightIndexConvention.getRegionCalendar());
        final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
        final BusinessDayConvention businessDayConvention = BusinessDayConventions.FOLLOWING; // by definition for overnight
        final boolean isEOM = false; // by definition for overnight
        final DayCount dayCount = overnightIndexConvention.getDayCount();
        final int settlementDays = 0; // by definition for overnight
        final LocalDate spotLocalDate = DefaultSettlementDateCalculator.getInstance().getSettlementDate(_valuationTime.toLocalDate(), settlementDays, calendar);
        final LocalDate startLocalDate, endLocalDate;
        if (isEOM) {
          startLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        } else {
          startLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        }
        final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(startLocalDate, time), zone);
        final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(endLocalDate, time), zone);
        final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
        return new CashDefinition(currency, startDate, endDate, 1, _rate, accrualFactor);
      } else if (convention instanceof IborIndexConvention) {
        final IborIndexConvention iborIndexConvention = (IborIndexConvention) convention;
        final Currency currency = iborIndexConvention.getCurrency();
        final Region region = _regionSource.getHighestLevelRegion(iborIndexConvention.getRegionCalendar());
        final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
        final BusinessDayConvention businessDayConvention = iborIndexConvention.getBusinessDayConvention();
        final boolean isEOM = iborIndexConvention.isIsEOM();
        final DayCount dayCount = iborIndexConvention.getDayCount();
        final int settlementDays = iborIndexConvention.getSettlementDays();
        final LocalDate spotLocalDate = DefaultSettlementDateCalculator.getInstance().getSettlementDate(_valuationTime.toLocalDate(), settlementDays, calendar);
        final LocalDate startLocalDate, endLocalDate;
        if (isEOM) {
          startLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        } else {
          startLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
          endLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
        }
        final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(startLocalDate, time), zone);
        final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(endLocalDate, time), zone);
        final Period period = Period.between(startDate.toLocalDate(), endDate.toLocalDate());
        final IborIndex index;
        if (period.getDays() != 0) {
          //TODO won't work for business day tenors
          final Tenor daysPeriod = Tenor.of(maturityTenor.getPeriod().minus(startTenor.getPeriod()));
          index = ConverterUtils.indexIbor(iborIndexConvention.getName(), iborIndexConvention, daysPeriod);
        } else {
          final Tenor monthPeriod = Tenor.of(Period.ofMonths(Long.valueOf(period.toTotalMonths()).intValue()));
          index = ConverterUtils.indexIbor(iborIndexConvention.getName(), iborIndexConvention, monthPeriod);
        }
        final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
        return new DepositIborDefinition(currency, startDate, endDate, 1, _rate, accrualFactor, index);
      }
    } catch (final Exception e) {
      // An exception could be thrown from the convention link resolver
      // If the convention is not found, try with the security.
      return getFromSecurity(cashNode, startTenor, maturityTenor);
    }
    throw new OpenGammaRuntimeException("Could not handle cash node with convention " + cashNode.getConvention());
  }

  /**
   * Creates an instrument from a security if it is available from the security master.
   * @param cashNode  the cash node
   * @param startTenor  the start tenor
   * @param maturityTenor  the maturity tenor
   * @return  the instrument
   */
  private InstrumentDefinition<?> getFromSecurity(final CashNode cashNode, final Tenor startTenor, final Tenor maturityTenor) {
    final Security security = _securitySource.getSingle(cashNode.getConvention().toBundle());
    if (security == null) {
      throw new OpenGammaRuntimeException("Could not get security with id " + cashNode.getConvention());
    }
    final LocalTime time = _valuationTime.toLocalTime();
    final ZoneOffset zone = _valuationTime.getOffset();
    if (security instanceof com.opengamma.financial.security.index.IborIndex) {
      final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) security;
      final IborIndexConvention indexConvention = ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("Convention with id " + indexSecurity.getConventionId() + " was null");
      }
      final Currency currency = indexConvention.getCurrency();
      final Region region = _regionSource.getHighestLevelRegion(indexConvention.getRegionCalendar());
      final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
      final BusinessDayConvention businessDayConvention = indexConvention.getBusinessDayConvention();
      final boolean isEOM = indexConvention.isIsEOM();
      final DayCount dayCount = indexConvention.getDayCount();
      final int settlementDays = indexConvention.getSettlementDays();
      final LocalDate spotLocalDate = DefaultSettlementDateCalculator.getInstance().getSettlementDate(_valuationTime.toLocalDate(), settlementDays, calendar);
      final LocalDate startLocalDate, endLocalDate;
      if (isEOM) {
        startLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
        endLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
      } else {
        startLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
        endLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
      }
      final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(startLocalDate, time), zone);
      final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(endLocalDate, time), zone);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      final IborIndex index = ConverterUtils.indexIbor(indexConvention.getName(), indexConvention, indexSecurity.getTenor());
      return new DepositIborDefinition(currency, startDate, endDate, 1, _rate, accrualFactor, index);
    } else if (security instanceof OvernightIndex) {
      final OvernightIndex indexSecurity = (OvernightIndex) security;
      final OvernightIndexConvention indexConvention = ConventionLink.resolvable(indexSecurity.getConventionId(), OvernightIndexConvention.class).resolve();
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("Convention with id " + indexSecurity.getConventionId() + " was null");
      }
      final Currency currency = indexConvention.getCurrency();
      final Region region = _regionSource.getHighestLevelRegion(indexConvention.getRegionCalendar());
      final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
      final BusinessDayConvention businessDayConvention = BusinessDayConventions.FOLLOWING; // by definition for overnight
      final boolean isEOM = false; // by definition for overnight
      final DayCount dayCount = indexConvention.getDayCount();
      final int settlementDays = 0; // by definition for overnight
      final LocalDate spotLocalDate = DefaultSettlementDateCalculator.getInstance().getSettlementDate(_valuationTime.toLocalDate(), settlementDays, calendar);
      final LocalDate startLocalDate, endLocalDate;
      if (isEOM) {
        startLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
        endLocalDate = EndOfMonthDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
      } else {
        startLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(spotLocalDate, startTenor, businessDayConvention, calendar);
        endLocalDate = DefaultDateAdjustmentCalculator.getInstance().getSettlementDate(startLocalDate, maturityTenor, businessDayConvention, calendar);
      }
      final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(startLocalDate, time), zone);
      final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(endLocalDate, time), zone);
      final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate);
      return new CashDefinition(currency, startDate, endDate, 1, _rate, accrualFactor);
    }
    throw new OpenGammaRuntimeException("Could not create cash definition from " + security);
  }
}
