/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import java.util.Objects;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A convention for vanilla OIS that contains sufficient information to build a {@link SwapFixedONDefinition}
 * when the fixing rate and start and end tenors are supplied:
 * <ul>
 *  <li> The business day convention used for date adjustments.
 *  <li> A working day calendar that contains information about weekends and holidays.
 *  <li> The leg payment tenor.
 *  <li> Fields indicating the stub type and if the leg schedules are generated from the start or end of the
 *       swap.
 *  <li> The payment and spot lag.
 *  <li> The underlying index.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class VanillaOisConvention implements CurveDataConvention<SwapFixedONDefinition> {

  /**
   * Gets the convention builder.
   * @return  the builder
   */
  public static VanillaOisConvention.Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this convention.
   */
  public static class Builder {
    private BusinessDayConvention _businessDayConvention;
    private WorkingDayCalendar _calendar;
    private Tenor _paymentTenor;
    private StubType _stubType;
    private int _paymentLag;
    private int _spotLag;
    private OvernightIndex _underlyingIndex;
    private EndOfMonthConvention _endOfMonthConvention;

    /**
     * Constructs the builder.
     */
    /* package */ Builder() {
    }

    /**
     * Sets the business day convention.
     * @param businessDayConvention  the business day convention, not null
     * @return  the builder
     */
    public Builder withBusinessDayConvention(final BusinessDayConvention businessDayConvention) {
      _businessDayConvention = ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
      return this;
    }

    /**
     * Sets the end of month convention.
     * @param endOfMonthConvention  the convention, not null
     * @return  the builder
     */
    public Builder withEndOfMonth(final EndOfMonthConvention endOfMonthConvention) {
      _endOfMonthConvention = ArgumentChecker.notNull(endOfMonthConvention, "endOfMonthConvention");
      return this;
    }

    /**
     * Sets the working day calendar.
     * @param calendar  the working day calendar, not null
     * @return  the builder
     */
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = ArgumentChecker.notNull(calendar, "calendar");
      return this;
    }

    /**
     * Sets the payment tenor.
     * @param paymentTenor  the payment tenor, not null
     * @return  the builder
     */
    public Builder withPaymentTenor(final Tenor paymentTenor) {
      _paymentTenor = ArgumentChecker.notNull(paymentTenor, "paymentTenor");
      return this;
    }

    /**
     * Sets the stub type.
     * @param stubType  the stub type, not null
     * @return  the builder
     */
    public Builder withStubType(final StubType stubType) {
      _stubType = ArgumentChecker.notNull(stubType, "stubType");
      return this;
    }

    /**
     * Sets the payment lag.
     * @param paymentLag  the payment lag, not negative
     * @return  the builder
     */
    public Builder withPaymentLag(final int paymentLag) {
      _paymentLag = ArgumentChecker.notNegative(paymentLag, "paymentLag");
      return this;
    }

    /**
     * Sets the spot lag.
     * @param spotLag  the spot lag, not negative
     * @return  the builder
     */
    public Builder withSpotLag(final int spotLag) {
      _spotLag = ArgumentChecker.notNegative(spotLag, "spotLag");
      return this;
    }

    /**
     * Sets the overnight index.
     * @param underlyingIndex  the overnight index, not null
     * @return  the builder
     */
    public Builder withUnderlyingIndex(final OvernightIndex underlyingIndex) {
      _underlyingIndex = ArgumentChecker.notNull(underlyingIndex, "underlyingIndex");
      return this;
    }

    /**
     * Builds the convention.
     * @return  the convention
     */
    public VanillaOisConvention build() {
      if (_businessDayConvention == null) {
        throw new IllegalStateException("The business day convention must be supplied");
      }
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      if (_endOfMonthConvention == null) {
        throw new IllegalStateException("The end of month convention must be supplied");
      }
      if (_paymentTenor == null) {
        throw new IllegalStateException("The payment tenor must be supplied");
      }
      if (_stubType == null) {
        throw new IllegalStateException("The stub type must be supplied");
      }
      if (_underlyingIndex == null) {
        throw new IllegalStateException("The underlying index must be supplied");
      }
      return new VanillaOisConvention(_businessDayConvention, _calendar, _paymentTenor, _paymentLag,
          _spotLag, _underlyingIndex, _stubType, _endOfMonthConvention);
    }
  }

  private final BusinessDayConvention _businessDayConvention;
  private final boolean _endOfMonth;
  private final WorkingDayCalendar _calendar;
  private final Tenor _paymentTenor;
  private final boolean _isShortStub;
  private final boolean _isLegGeneratedFromEnd;
  private final int _paymentLag;
  private final int _spotLag;
  private final OvernightIndex _index;

  /**
   * @param businessDayConvention  the swap leg business day convention
   * @param calendar  the swap leg holiday calendar
   * @param paymentTenor  the swap leg payment tenor
   * @param paymentLag  the payment lag of the swap leg
   * @param spotLag  the lag to the settlement date
   * @param index  the underlying overnight index
   * @param stubType  the payment schedule stub type
   * @param endOfMonth  how dates at the end of months are handled
   */
  /* package */VanillaOisConvention(final BusinessDayConvention businessDayConvention, final WorkingDayCalendar calendar,
      final Tenor paymentTenor, final int paymentLag, final int spotLag,
      final OvernightIndex index, final StubType stubType, final EndOfMonthConvention endOfMonth) {
    _businessDayConvention = businessDayConvention;
    _calendar = calendar;
    _paymentTenor = paymentTenor;
    _paymentLag = paymentLag;
    _spotLag = spotLag;
    _index = index;
    switch (stubType) {
      case NONE:
        _isLegGeneratedFromEnd = false;
        _isShortStub = false;
        break;
      case LONG_START:
        _isLegGeneratedFromEnd = true;
        _isShortStub = false;
        break;
      case LONG_END:
        _isLegGeneratedFromEnd = false;
        _isShortStub = false;
        break;
      case SHORT_START:
        _isLegGeneratedFromEnd = true;
        _isShortStub = true;
        break;
      case SHORT_END:
        _isLegGeneratedFromEnd = false;
        _isShortStub = true;
        break;
      default:
        throw new IllegalArgumentException("Unsupported stub type " + stubType);
    }
    switch (endOfMonth) {
      case ADJUST_FOR_END_OF_MONTH:
        _endOfMonth = true;
        break;
      case IGNORE_END_OF_MONTH:
        _endOfMonth = false;
        break;
      default:
        throw new IllegalArgumentException("Unsupported end of month convention " + endOfMonth);
    }
  }

  @Override
  public SwapFixedONDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(startTenor, "startTenor");
    ArgumentChecker.notNull(endTenor, "endTenor");
    final Currency currency = _index.getCurrency();
    final Calendar holidays = CalendarAdapter.of(_calendar);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime[] fixingPeriodEndDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endTenor, _paymentTenor,
        _isShortStub, _isLegGeneratedFromEnd, _businessDayConvention, _calendar, _endOfMonth);
    final CouponONDefinition[] overnightCoupons = new CouponONDefinition[fixingPeriodEndDates.length];
    final CouponFixedDefinition[] fixedCoupons = new CouponFixedDefinition[fixingPeriodEndDates.length];
    final IndexON index = IndexConverter.toIndexOn(_index);
    overnightCoupons[0] = CouponONDefinition.from(index, settlementDate, fixingPeriodEndDates[0], notional, _paymentLag, holidays);
    fixedCoupons[0] = new CouponFixedDefinition(currency, overnightCoupons[0].getPaymentDate(), overnightCoupons[0].getAccrualStartDate(),
        overnightCoupons[0].getAccrualEndDate(), overnightCoupons[0].getPaymentYearFraction(), -notional, fixedRate);
    for (int i = 1; i < fixingPeriodEndDates.length; i++) {
      overnightCoupons[i] = CouponONDefinition.from(index, fixingPeriodEndDates[i - 1], fixingPeriodEndDates[i], notional, _paymentLag, holidays);
      fixedCoupons[i] = new CouponFixedDefinition(currency, overnightCoupons[i].getPaymentDate(), overnightCoupons[i].getAccrualStartDate(),
          overnightCoupons[i].getAccrualEndDate(), overnightCoupons[i].getPaymentYearFraction(), -notional, fixedRate);
    }
    return new SwapFixedONDefinition(new AnnuityCouponFixedDefinition(fixedCoupons, holidays),
        new AnnuityCouponONDefinition(overnightCoupons, index, holidays));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_businessDayConvention == null ? 0 : _businessDayConvention.hashCode());
    result = prime * result + (_calendar == null ? 0 : _calendar.hashCode());
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + (_index == null ? 0 : _index.hashCode());
    result = prime * result + (_isLegGeneratedFromEnd ? 1231 : 1237);
    result = prime * result + (_isShortStub ? 1231 : 1237);
    result = prime * result + _paymentLag;
    result = prime * result + (_paymentTenor == null ? 0 : _paymentTenor.hashCode());
    result = prime * result + _spotLag;
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
    final VanillaOisConvention other = (VanillaOisConvention) obj;
    if (_isLegGeneratedFromEnd != other._isLegGeneratedFromEnd) {
      return false;
    }
    if (_isShortStub != other._isShortStub) {
      return false;
    }
    if (_paymentLag != other._paymentLag) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    if (!Objects.equals(_businessDayConvention.getName(), other._businessDayConvention.getName())) {
      return false;
    }
    if (!Objects.equals(_paymentTenor, other._paymentTenor)) {
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
    builder.append("VanillaOisConvention [index=");
    builder.append(_index);
    builder.append(", paymentTenor=");
    builder.append(_paymentTenor);
    builder.append(", endOfMonth=");
    builder.append(_endOfMonth);
    builder.append(", calendar=");
    builder.append(_calendar);
    builder.append(", businessDayConvention=");
    builder.append(_businessDayConvention);
    builder.append(", isShortStub=");
    builder.append(_isShortStub);
    builder.append(", isLegGeneratedFromEnd=");
    builder.append(_isLegGeneratedFromEnd);
    builder.append(", paymentLag=");
    builder.append(_paymentLag);
    builder.append(", spotLag=");
    builder.append(_spotLag);
    builder.append("]");
    return builder.toString();
  }

}
