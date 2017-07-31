/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class VanillaOisConvention implements CurveDataConvention<SwapFixedONDefinition> {

  public static VanillaOisConvention.Builder builder() {
    return new Builder();
  }

  public static class Builder {
    BusinessDayConvention _businessDayConvention;
    EndOfMonthConvention _endOfMonth;
    WorkingDayCalendar _calendar;
    Tenor _paymentPeriod;
    boolean _isShortStub;
    boolean _isFromEnd;
    int _paymentLag;
    int _spotLag;
    IndexON _underlyingIndex;

    /* package */ Builder() {
    }

    public Builder withBusinessDayConvention(final BusinessDayConvention businessDayConvention) {
      _businessDayConvention = businessDayConvention;
      return this;
    }

    public Builder withEndOfMonth(final EndOfMonthConvention endOfMonth) {
      _endOfMonth = endOfMonth;
      return this;
    }

    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = calendar;
      return this;
    }

    public Builder withPaymentPeriod(final Tenor paymentPeriod) {
      _paymentPeriod = paymentPeriod;
      return this;
    }

    public Builder withShortStub(final boolean isShortStub) {
      _isShortStub = isShortStub;
      return this;
    }

    public Builder withFromEnd(final boolean isFromEnd) {
      _isFromEnd = isFromEnd;
      return this;
    }

    public Builder withPaymentLag(final int paymentLag) {
      _paymentLag = paymentLag;
      return this;
    }

    public Builder withSpotLag(final int spotLag) {
      _spotLag = spotLag;
      return this;
    }

    public Builder withUnderlyingIndex(final IndexON underlyingIndex) {
      _underlyingIndex = underlyingIndex;
      return this;
    }

    public VanillaOisConvention build() {
      return new VanillaOisConvention(this);
    }
  }

  private final BusinessDayConvention _businessDayConvention;
  private final boolean _endOfMonth;
  private final WorkingDayCalendar _calendar;
  private final Tenor _paymentPeriod;
  private final boolean _isShortStub;
  private final boolean _isFromEnd;
  private final int _paymentLag;
  private final int _spotLag;
  private final IndexON _index;

  VanillaOisConvention(final VanillaOisConvention.Builder builder) {
    _businessDayConvention = builder._businessDayConvention;
    _calendar = builder._calendar;
    _paymentPeriod = builder._paymentPeriod;
    _isShortStub = builder._isShortStub;
    _isFromEnd = builder._isFromEnd;
    _paymentLag = builder._paymentLag;
    _spotLag = builder._spotLag;
    _index = builder._underlyingIndex;
    switch (builder._endOfMonth) {
      case ADJUST_FOR_END_OF_MONTH:
        _endOfMonth = true;
        break;
      case IGNORE_END_OF_MONTH:
        _endOfMonth = false;
        break;
      default:
        throw new IllegalArgumentException("Unsupported end of month convention " + builder._endOfMonth);
    }
  }

  @Override
  public SwapFixedONDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional, final double fixedRate) {
    ArgumentChecker.notNull(date, "date");
    final Calendar holidays = new CalendarAdapter(_calendar);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime[] endFixingPeriodDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endTenor, _paymentPeriod.getPeriod(), _isShortStub, _isFromEnd,
        _businessDayConvention, _calendar, _endOfMonth);
    final CouponONDefinition[] coupons = new CouponONDefinition[endFixingPeriodDates.length];
    coupons[0] = CouponONDefinition.from(_index, settlementDate, endFixingPeriodDates[0], notional, _paymentLag, holidays);
    for (int i = 1; i < endFixingPeriodDates.length; i++) {
      coupons[i] = CouponONDefinition.from(_index, endFixingPeriodDates[i - 1], endFixingPeriodDates[i], notional, _paymentLag,
          holidays);
    }
    final AnnuityCouponONDefinition oisLeg = new AnnuityCouponONDefinition(coupons, _index, holidays);
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[oisLeg.getNumberOfPayments()];
    for (int i = 0; i < oisLeg.getNumberOfPayments(); i++) {
      cpnFixed[i] = new CouponFixedDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(i).getPaymentDate(), oisLeg.getNthPayment(i).getAccrualStartDate(), oisLeg.getNthPayment(
          i).getAccrualEndDate(), oisLeg.getNthPayment(i).getPaymentYearFraction(), -notional, fixedRate);
    }
    return new SwapFixedONDefinition(new AnnuityCouponFixedDefinition(cpnFixed, holidays), oisLeg);
  }

}
