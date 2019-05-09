/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda.util;

import java.util.Collection;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.IsdaCreditCurveConvention;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility class that for converting credit securities to the form required by the analytics library.
 */
public final class CreditSecurityConverter {

  /**
   * Converts a standard CDS security to the form used by the analytics library.
   *
   * @param holidaySource
   *          a source of holiday data, not null
   * @param security
   *          the security to convert, not null
   * @param recoveryRate
   *          the recovery rate
   * @param convention
   *          the convention to be used, not null
   * @param valuationDate
   *          the valuation date, not null
   * @return the analytics form of the security
   */
  public static CDSAnalytic convertStandardCdsSecurity(final HolidaySource holidaySource, final StandardCDSSecurity security, final double recoveryRate,
      final IsdaCreditCurveConvention convention, final LocalDate valuationDate) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(valuationDate, "valuationDate");
    final Currency currency = security.getNotional().getCurrency();
    final Collection<Holiday> holidays = holidaySource.get(currency);
    final WorkingDayCalendar calendar = IsdaFunctionUtils.getCalendar(currency, holidays);
    final StubType stubType = convention.getStubType().toAnalyticsType();
    final Period period = convention.getCouponInterval();
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(valuationDate, valuationDate.plusDays(convention.getStepIn()),
        BusinessDayDateUtils.addWorkDays(valuationDate, convention.getCashSettle(), calendar), security.getTradeDate(), security.getMaturityDate(),
        convention.isPayAccOnDefault(), period, stubType, convention.isProtectFromStartOfDay(), recoveryRate, convention.getBusinessDayConvention(),
        CalendarAdapter.of(calendar), convention.getAccrualDayCount(), convention.getCurveDayCount());
    return cdsAnalytic;
  }

  /**
   * Converts a curve node to the form used by the analytics library.
   *
   * @param holidaySource
   *          a source of holiday data, not null
   * @param currency
   *          the currency, not null
   * @param curveNode
   *          the curve node to convert, not null
   * @param recoveryRate
   *          the recovery rate
   * @param convention
   *          the convention to be used, not null
   * @param valuationDate
   *          the valuation date, not null
   * @return the analytics form of the security
   */
  public static CDSAnalytic convertStandardCDSSecurity(final HolidaySource holidaySource, final Currency currency, final CurveNodeWithIdentifier curveNode,
      final double recoveryRate, final IsdaCreditCurveConvention convention, final LocalDate valuationDate) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(curveNode, "curveNode");
    ArgumentChecker.notNull(convention, "convention");
    ArgumentChecker.notNull(valuationDate, "valuationDate");
    ArgumentChecker.isTrue(curveNode.getCurveNode() instanceof CreditSpreadNode, "Curve node is not a CreditSpreadNode: have {}", curveNode.getCurveNode());
    final Collection<Holiday> holidays = holidaySource.get(currency);
    final WorkingDayCalendar calendar = IsdaFunctionUtils.getCalendar(currency, holidays);
    final StubType stubType = convention.getStubType().toAnalyticsType();
    final Period period = convention.getCouponInterval();
    final LocalDate effectiveDate = valuationDate.plusDays(convention.getStepIn());
    final LocalDate maturityDate = valuationDate.plus(curveNode.getCurveNode().getResolvedMaturity().getPeriod());
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(valuationDate, effectiveDate,
        BusinessDayDateUtils.addWorkDays(valuationDate, convention.getCashSettle(), calendar), effectiveDate, maturityDate, convention.isPayAccOnDefault(),
        period, stubType, convention.isProtectFromStartOfDay(), recoveryRate, convention.getBusinessDayConvention(), CalendarAdapter.of(calendar),
        convention.getAccrualDayCount(), convention.getCurveDayCount());
    return cdsAnalytic;
  }

  private CreditSecurityConverter() {
  }
}
