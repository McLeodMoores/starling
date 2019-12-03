/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class InflationBondSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionSource _conventionSource;
  private final RegionSource _regionSource;

  public InflationBondSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitInflationBondSecurity(final InflationBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    if (domicile == null) {
      throw new OpenGammaRuntimeException("bond security domicile cannot be null");
    }
    final BondConvention convention = _conventionSource.getSingle(ExternalSchemes.financialRegionId(domicile), BondConvention.class);
    return visitBondSecurity(security, convention);
  }

  @Override
  public InstrumentDefinition<?> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final String domicile = security.getIssuerDomicile();
    if (domicile == null) {
      throw new OpenGammaRuntimeException("bond security domicile cannot be null");
    }
    final BondConvention convention = _conventionSource.getSingle(ExternalSchemes.financialRegionId(domicile), BondConvention.class);
    return visitBondSecurity(security, convention);
  }

  public InstrumentDefinition<?> visitBondSecurity(final BondSecurity security, final BondConvention convention) {
    final ExternalId regionId = ExternalSchemes.financialRegionId(security.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + security.getIssuerDomicile());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = security.getCurrency();
    final ZoneId zone = security.getInterestAccrualDate().getZone();
    final ZonedDateTime firstAccrualDate = ZonedDateTime.of(security.getInterestAccrualDate().toLocalDate().atStartOfDay(), zone);
    final ZonedDateTime maturityDate = ZonedDateTime.of(security.getLastTradeDate().getExpiry().toLocalDate().atStartOfDay(), zone);
    final double rate = security.getCouponRate() / 100;
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    final boolean isEOM = convention.isIsEOM();
    final YieldConvention yieldConvention = security.getYieldConvention();
    if (security.getCouponType().equals("NONE") || security.getCouponType().equals("ZERO COUPON")) { // TODO find where string is
      return new PaymentFixedDefinition(currency, maturityDate, 1);
    }
    final int settlementDays = convention.getSettlementDays();
    final Period paymentPeriod = ConversionUtils.getTenor(security.getCouponFrequency());
    final ZonedDateTime firstCouponDate = ZonedDateTime.of(security.getFirstCouponDate().toLocalDate().atStartOfDay(), zone);
    return BondFixedSecurityDefinition.from(currency, firstAccrualDate, firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, calendar, dayCount,
        businessDay,
        yieldConvention, isEOM, security.getIssuerName());
  }

  @Override
  public InstrumentDefinition<?> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    throw new NotImplementedException();
  }

}
