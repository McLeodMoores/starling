/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

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
 *  <li>
 *
 * </ul>
 */
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
    private Tenor _swapLegPaymentTenor;
    private StubType _stubType;
    private int _swapLegPaymentLag;
    private int _swapLegSpotLag;
    private OvernightIndex _underlyingIndex;
    private EndOfMonthConvention _endOfMonthConvention;

    /**
     * Constructs the builder.
     */
    /* package */ Builder() {
    }

    public Builder withBusinessDayConvention(final BusinessDayConvention businessDayConvention) {
      _businessDayConvention = businessDayConvention;
      return this;
    }

    public Builder withEndOfMonth(final EndOfMonthConvention endOfMonthConvention) {
      _endOfMonthConvention = endOfMonthConvention;
      return this;
    }

    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = calendar;
      return this;
    }

    public Builder withPaymentPeriod(final Tenor paymentPeriod) {
      _swapLegPaymentTenor = paymentPeriod;
      return this;
    }

    public Builder withStubType(final StubType stubType) {
      _stubType = stubType;
      return this;
    }

    public Builder withPaymentLag(final int paymentLag) {
      _swapLegPaymentLag = paymentLag;
      return this;
    }

    public Builder withSpotLag(final int spotLag) {
      _swapLegSpotLag = spotLag;
      return this;
    }

    public Builder withUnderlyingIndex(final OvernightIndex underlyingIndex) {
      _underlyingIndex = underlyingIndex;
      return this;
    }

    public VanillaOisConvention build() {
      return new VanillaOisConvention(_businessDayConvention, _calendar, _swapLegPaymentTenor, _swapLegPaymentLag,
          _swapLegSpotLag, _underlyingIndex, _stubType, _endOfMonthConvention);
    }
  }

  private final BusinessDayConvention _businessDayConvention;
  private final boolean _endOfMonth;
  private final WorkingDayCalendar _swapLegCalendar;
  private final Tenor _swapLegPaymentTenor;
  private final boolean _isShortStub;
  private final boolean _isLegGeneratedFromEnd;
  private final int _swapLegPaymentLag;
  private final int _swapLegSpotLag;
  private final OvernightIndex _underlyingIndex;

  /**
   * @param businessDayConvention  the swap leg business day convention
   * @param swapLegCalendar  the swap leg holiday calendar
   * @param swapLegPaymentTenor  the swap leg payment tenor
   * @param swapLegPaymentLag  the payment lag of the swap leg
   * @param swapLegSpotLag  the lag to the settlement date
   * @param underlyingIndex  the underlying overnight index
   * @param stubType  the payment schedule stub type
   * @param endOfMonth  how dates at the end of months are handled
   */
  /* package */VanillaOisConvention(final BusinessDayConvention businessDayConvention, final WorkingDayCalendar swapLegCalendar,
      final Tenor swapLegPaymentTenor, final int swapLegPaymentLag, final int swapLegSpotLag,
      final OvernightIndex underlyingIndex, final StubType stubType, final EndOfMonthConvention endOfMonth) {
    _businessDayConvention = businessDayConvention;
    _swapLegCalendar = swapLegCalendar;
    _swapLegPaymentTenor = swapLegPaymentTenor;
    _swapLegPaymentLag = swapLegPaymentLag;
    _swapLegSpotLag = swapLegSpotLag;
    _underlyingIndex = underlyingIndex;
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
    final Currency currency = _underlyingIndex.getCurrency();
    final Calendar holidays = new CalendarAdapter(_swapLegCalendar);
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(date, _swapLegSpotLag, _swapLegCalendar);
    final ZonedDateTime[] fixingPeriodEndDates = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, endTenor, _swapLegPaymentTenor,
        _isShortStub, _isLegGeneratedFromEnd, _businessDayConvention, _swapLegCalendar, _endOfMonth);
    final CouponONDefinition[] coupons = new CouponONDefinition[fixingPeriodEndDates.length];
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[fixingPeriodEndDates.length];
    final IndexON index = IndexConverter.toIndexOn(_underlyingIndex);
    coupons[0] = CouponONDefinition.from(index, settlementDate, fixingPeriodEndDates[0], notional, _swapLegPaymentLag, holidays);
    cpnFixed[0] = new CouponFixedDefinition(currency, coupons[0].getPaymentDate(), coupons[0].getAccrualStartDate(),
        coupons[0].getAccrualEndDate(), coupons[0].getPaymentYearFraction(), -notional, fixedRate);
    for (int i = 1; i < fixingPeriodEndDates.length; i++) {
      coupons[i] = CouponONDefinition.from(index, fixingPeriodEndDates[i - 1], fixingPeriodEndDates[i], notional, _swapLegPaymentLag, holidays);
      cpnFixed[i] = new CouponFixedDefinition(currency, coupons[i].getPaymentDate(), coupons[i].getAccrualStartDate(),
          coupons[i].getAccrualEndDate(), coupons[i].getPaymentYearFraction(), -notional, fixedRate);
    }
    return new SwapFixedONDefinition(new AnnuityCouponFixedDefinition(cpnFixed, holidays), new AnnuityCouponONDefinition(coupons, index, holidays));
  }

}
