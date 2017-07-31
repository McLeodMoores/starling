/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IborTypeIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class VanillaFixedIborSwapConvention implements CurveDataConvention<SwapFixedIborDefinition> {

  public static VanillaFixedIborSwapConvention.Builder builder() {
    return new Builder();
  }

  public static class Builder {
    WorkingDayCalendar _calendar;
    Tenor _fixedLegPaymentTenor;
    DayCount _fixedLegDayCount;
    boolean _isShortStub;
    boolean _isFromEnd;
    IborTypeIndex _underlyingIndex;

    /* package */ Builder() {
    }

    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = calendar;
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

    public Builder withUnderlyingIndex(final IborTypeIndex underlyingIndex) {
      _underlyingIndex = underlyingIndex;
      return this;
    }

    public Builder withFixedLegPaymentPeriod(final Tenor fixedLegPaymentTenor) {
      _fixedLegPaymentTenor = fixedLegPaymentTenor;
      return this;
    }

    public Builder withFixedLegDayCount(final DayCount fixedLegDayCount) {
      _fixedLegDayCount = fixedLegDayCount;
      return this;
    }

    public VanillaFixedIborSwapConvention build() {
      return new VanillaFixedIborSwapConvention(this);
    }
  }

  private final WorkingDayCalendar _calendar;
  private final DayCount _fixedLegDayCount;
  private final boolean _isShortStub;
  private final boolean _isFromEnd;
  private final Tenor _fixedLegPaymentTenor;
  private final IborTypeIndex _index;

  VanillaFixedIborSwapConvention(final VanillaFixedIborSwapConvention.Builder builder) {
    _calendar = builder._calendar;
    _isShortStub = builder._isShortStub;
    _isFromEnd = builder._isFromEnd;
    _index = builder._underlyingIndex;
    _fixedLegPaymentTenor = builder._fixedLegPaymentTenor;
    _fixedLegDayCount = builder._fixedLegDayCount;
  }

  @Override
  public SwapFixedIborDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional, final double fixedRate) {
    //TODO deal with stubs - need an enem for fromEnd as well
    final IborIndex index = IndexConverter.toIborIndex(_index);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _index.getSpotLag(), _calendar);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(spot, startTenor, _index.getBusinessDayConvention(), _calendar, _index.isEndOfMonth());
    final Calendar holidays = new CalendarAdapter(_calendar);
    final AnnuityCouponFixedDefinition fixedLeg = AnnuityCouponFixedDefinition.from(_index.getCurrency(), settlementDate, endTenor.getPeriod(), _fixedLegPaymentTenor.getPeriod(),
        holidays, _fixedLegDayCount, _index.getBusinessDayConvention(), _index.isEndOfMonth(), notional, fixedRate, true);
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(settlementDate, endTenor.getPeriod(), notional, index, false, holidays);
    return new SwapFixedIborDefinition(fixedLeg, iborLeg);
  }

}
