/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import java.util.Collection;

import org.threeten.bp.DayOfWeek;

import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
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
 *
 */
public final class IsdaFunctionUtils {

  /**
   * Gets a working day calendar for a particular currency from holidays obtained from a {@link HolidaySource}.
   *
   * @param currency
   *          the currency, not null
   * @param holidays
   *          the holidays as sourced from a source, not null or empty
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

  private IsdaFunctionUtils() {
  }
}
