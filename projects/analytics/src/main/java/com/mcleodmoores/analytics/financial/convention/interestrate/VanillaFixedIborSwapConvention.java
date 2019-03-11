/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import java.util.Objects;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * A convention for vanilla fixed / IBOR swaps. The required fields are:
 * <ul>
 *  <li> A working day calendar that contains information about weekends and holidays ({@link WorkingDayCalendar}).
 *  <li> The fixed leg accrual / payment tenor.
 *  <li> The day count used to calculate the fixed leg accrual year fraction.
 *  <li> Fields indicating the stub type and if the leg schedules are generated from the start or end
 *       of the swap.
 *  <li> The underlying index - this is used to generate the floating leg.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class VanillaFixedIborSwapConvention implements CurveDataConvention<SwapFixedIborDefinition> {

  /**
   * Gets the convention builder.
   * @return  the builder
   */
  public static VanillaFixedIborSwapConvention.Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this convention.
   */
  public static class Builder {
    private WorkingDayCalendar _calendar;
    private Tenor _fixedLegPaymentTenor;
    private DayCount _fixedLegDayCount;
    private StubType _stubType;
    private IborTypeIndex _underlyingIndex;

    /**
     * Constructs the builder.
     */
    /* package */ Builder() {
    }

    /**
     * Sets the working day calendar.
     * @param calendar  the calendar, not null
     * @return  the builder
     */
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = ArgumentChecker.notNull(calendar, "calendar");
      return this;
    }

    /**
     * Sets the stub type.
     * @param stubType  the stub type, not null
     * @return  the builder
     */
    public Builder withStub(final StubType stubType) {
      _stubType = ArgumentChecker.notNull(stubType, "stubType");
      return this;
    }

    /**
     * Sets the floating leg index.
     * @param underlyingIndex  the floating leg index, not null
     * @return  the builder
     */
    public Builder withUnderlyingIndex(final IborTypeIndex underlyingIndex) {
      _underlyingIndex = ArgumentChecker.notNull(underlyingIndex, "underlyingIndex");
      return this;
    }

    /**
     * Sets the fixed leg payment tenor.
     * @param fixedLegPaymentTenor  the fixed leg payment tenor, not null
     * @return  the builder
     */
    public Builder withFixedLegPaymentTenor(final Tenor fixedLegPaymentTenor) {
      _fixedLegPaymentTenor = ArgumentChecker.notNull(fixedLegPaymentTenor, "fixedLegPaymenTenor");
      return this;
    }

    /**
     * Sets the fixed leg day count.
     * @param fixedLegDayCount  the fixed leg day count, not null
     * @return  the builder
     */
    public Builder withFixedLegDayCount(final DayCount fixedLegDayCount) {
      _fixedLegDayCount = ArgumentChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
      return this;
    }

    /**
     * Builds the convention.
     * @return  the convention
     */
    public VanillaFixedIborSwapConvention build() {
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      if (_fixedLegDayCount == null) {
        throw new IllegalStateException("The fixed leg day count must be supplied");
      }
      if (_fixedLegPaymentTenor == null) {
        throw new IllegalStateException("The fixed leg payment tenor must be supplied");
      }
      if (_stubType == null) {
        throw new IllegalStateException("The stub type must be supplied");
      }
      if (_underlyingIndex == null) {
        throw new IllegalStateException("The underlying index must be supplied");
      }
      return new VanillaFixedIborSwapConvention(_calendar, _stubType, _underlyingIndex, _fixedLegPaymentTenor, _fixedLegDayCount);
    }
  }

  private final WorkingDayCalendar _calendar;
  private final DayCount _fixedLegDayCount;
  private final StubType _stubType;
  private final Tenor _fixedLegPaymentTenor;
  private final IborTypeIndex _index;

  /**
   * Constructs the convention.
   * @param calendar  the working day calendar
   * @param stubType  the stub type
   * @param index  the index
   * @param fixedLegPaymentTenor  the fixed leg payment tenor
   * @param fixedLegDayCount  the fixed leg day count
   */
  /* package */VanillaFixedIborSwapConvention(final WorkingDayCalendar calendar, final StubType stubType, final IborTypeIndex index,
      final Tenor fixedLegPaymentTenor, final DayCount fixedLegDayCount) {
    _calendar = calendar;
    _stubType = stubType;
    _index = index;
    _fixedLegPaymentTenor = fixedLegPaymentTenor;
    _fixedLegDayCount = fixedLegDayCount;
  }

  @Override
  public SwapFixedIborDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    final IborIndex index = IndexConverter.toIborIndex(_index);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _index.getSpotLag(), _calendar);
    final ZonedDateTime settlementDate =
        ScheduleCalculator.getAdjustedDate(spot, startTenor, _index.getBusinessDayConvention(), _calendar, _index.isEndOfMonth());
    final ZonedDateTime maturityDate = TenorUtils.adjustDateByTenor(settlementDate, endTenor);
    final Calendar holidays = CalendarAdapter.of(_calendar);
    final AnnuityCouponFixedDefinition fixedLeg =
        AnnuityCouponFixedDefinition.from(_index.getCurrency(), settlementDate, maturityDate, _fixedLegPaymentTenor.getPeriod(),
        holidays, _fixedLegDayCount, _index.getBusinessDayConvention(), _index.isEndOfMonth(), notional, fixedRate, true, _stubType);
    final AnnuityCouponIborDefinition iborLeg = AnnuityCouponIborDefinition.from(settlementDate, maturityDate, _index.getTenor().getPeriod(),
        notional, index, false, _index.getBusinessDayConvention(), _index.isEndOfMonth(), _index.getDayCount(), holidays, _stubType);
    return new SwapFixedIborDefinition(fixedLeg, iborLeg);
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_calendar == null ? 0 : _calendar.hashCode());
    result = prime * result + (_fixedLegDayCount == null ? 0 : _fixedLegDayCount.hashCode());
    result = prime * result + (_fixedLegPaymentTenor == null ? 0 : _fixedLegPaymentTenor.hashCode());
    result = prime * result + (_index == null ? 0 : _index.hashCode());
    result = prime * result + (_stubType == null ? 0 : _stubType.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VanillaFixedIborSwapConvention other = (VanillaFixedIborSwapConvention) obj;
    if (_stubType != other._stubType) {
      return false;
    }
    if (!Objects.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (!Objects.equals(_fixedLegPaymentTenor, other._fixedLegPaymentTenor)) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (!Objects.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("VanillaFixedIborSwapConvention [index=");
    builder.append(_index);
    builder.append(", fixedLegPaymentTenor=");
    builder.append(_fixedLegPaymentTenor);
    builder.append(", fixedLegDayCount=");
    builder.append(_fixedLegDayCount.getName());
    builder.append(", calendar=");
    builder.append(_calendar);
    builder.append(", stubType=");
    builder.append(_stubType);
    builder.append("]");
    return builder.toString();
  }
}
