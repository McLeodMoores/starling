/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda.util;

import java.util.Collection;

import org.threeten.bp.DayOfWeek;

import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.financial.analytics.isda.credit.FlatSpreadQuote;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.financial.analytics.isda.credit.PointsUpFrontQuote;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for pricing credit instruments using the ISDA model.
 */
public final class IsdaFunctionUtils {
  private static final MarketQuoteConverter PUF_CONVERTER = new MarketQuoteConverter();

  /**
   * Gets a working day calendar for a particular currency from holidays obtained from a {@link HolidaySource}.
   *
   * @param currency
   *          the currency, not null
   * @param holidays
   *          the holiday dates from a source, not null or empty
   * @return the calendar
   */
  public static WorkingDayCalendar getCalendar(final Currency currency, final Collection<Holiday> holidays) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(holidays, "holidays");
    if (holidays.size() != 1) {
      throw new OpenGammaRuntimeException("Could not get currency holiday for " + currency);
    }
    final Holiday holiday = holidays.iterator().next();
    if (holiday instanceof WeekendTypeProvider) {
      final WeekendType weekendType = ((WeekendTypeProvider) holiday).getWeekendType();
      return new SimpleWorkingDayCalendar(currency.getCode(), holiday.getHolidayDates(), weekendType.getFirstDay(), weekendType.getSecondDay());
    }
    return new SimpleWorkingDayCalendar(currency.getCode(), holiday.getHolidayDates(), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  /**
   * Converts a CDS quote to the type used by the analytics library.
   *
   * @param coupon
   *          the coupon, can be null if the quote type is PAR_SPREAD
   * @param quote
   *          the quote, assumed to be normalized to a decimal
   * @param quoteType
   *          the quote type, not null
   * @return a CDS quote
   */
  public static CDSQuoteConvention getQuote(final Double coupon, final double quote, final String quoteType) {
    ArgumentChecker.notNull(quoteType, "quoteType");
    switch (quoteType) {
      case FlatSpreadQuote.TYPE:
        return new QuotedSpread(coupon, quote);
      case PointsUpFrontQuote.TYPE:
        return new PointsUpFront(coupon, quote);
      case ParSpreadQuote.TYPE:
        return new ParSpread(quote);
      default:
        throw new IllegalArgumentException("Unsupported quote type " + quoteType);
    }
  }

  /**
   * Converts a points-upfront quote to a quoted spread.
   *
   * @param puf
   *          the points up front, not null
   * @param buySellProtection
   *          whether protection is bought or sold, not null
   * @param yieldCurve
   *          the discounting curve, not null
   * @param cds
   *          the CDS, not null
   * @return the quoted spread
   */
  public static QuotedSpread getQuotedSpread(final PointsUpFront puf, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve,
      final CDSAnalytic cds) {
    ArgumentChecker.notNull(puf, "puf");
    ArgumentChecker.notNull(buySellProtection, "buySellProtection");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(cds, "cds");
    final double quotedSpread = PUF_CONVERTER.pufToQuotedSpread(cds, puf.getCoupon(), yieldCurve, puf.getPointsUpFront());
    // SELL protection reverses directions of legs
    return new QuotedSpread(puf.getCoupon(), buySellProtection == BuySellProtection.SELL ? -quotedSpread : quotedSpread);

  }

  /**
   * Converts a quoted spread quote to points upfront.
   *
   * @param quote
   *          the quoted spread, not null
   * @param buySellProtection
   *          whether protection is bought or sold, not null
   * @param yieldCurve
   *          the discounting curve, not null
   * @param cds
   *          the CDS, not null
   * @param creditCurve
   *          the hazard rate curve, not null
   * @return the points upfront
   */
  public static PointsUpFront getPointsUpfront(final QuotedSpread quote, final BuySellProtection buySellProtection, final ISDACompliantYieldCurve yieldCurve,
      final CDSAnalytic cds, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(quote, "quote");
    ArgumentChecker.notNull(buySellProtection, "buySellProtection");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(creditCurve, "creditCurve");
    final double puf = PUF_CONVERTER.pointsUpFront(cds, quote.getCoupon(), yieldCurve, creditCurve);
    // SELL protection reverses directions of legs
    return new PointsUpFront(quote.getCoupon(), buySellProtection == BuySellProtection.SELL ? -puf : puf);
  }

  /**
   * Gets the upfront amount to be paid for a CDS.
   *
   * @param cds
   *          the CDS, not null
   * @param puf
   *          the points upfront, not null
   * @param notional
   *          the notional
   * @param buySellProtection
   *          whether protection is bought or sold, not null
   * @return the up-front amount
   */
  public static double getUpfrontAmount(final CDSAnalytic cds, final PointsUpFront puf, final double notional, final BuySellProtection buySellProtection) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(puf, "puf");
    ArgumentChecker.notNull(buySellProtection, "buySellProtection");
    final double cash = (puf.getPointsUpFront() - cds.getAccruedPremium(puf.getCoupon())) * notional;
    return buySellProtection == BuySellProtection.SELL ? -cash : cash;
  }

  private IsdaFunctionUtils() {
  }
}
