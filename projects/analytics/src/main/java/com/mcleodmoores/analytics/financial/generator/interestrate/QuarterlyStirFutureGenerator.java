/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.generator.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFuturesUtils;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class QuarterlyStirFutureGenerator implements CurveInstrumentGenerator<InterestRateFutureTransactionDefinition> {

  /**
   * Gets the generator builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this generator.
   */
  public static class Builder {
    private Double _paymentAccrualFactor;
    private IborTypeIndex _index;
    private WorkingDayCalendar _calendar;

    /**
     * Constructs the builder.
     */
    Builder() {
    }

    /**
     * Sets the payment accrual factor.
     *
     * @param paymentAccrualFactor
     *          the accrual factor
     * @return this builder
     */
    public Builder withPaymentAccrualFactor(final Double paymentAccrualFactor) {
      _paymentAccrualFactor = ArgumentChecker.notNegativeOrZero(paymentAccrualFactor, "paymentAccrualFactor");
      return this;
    }

    /**
     * Sets the calendar.
     *
     * @param calendar
     *          the calendar, not null
     * @return the builder
     */
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = ArgumentChecker.notNull(calendar, "calendar");
      return this;
    }

    /**
     * Sets the index.
     *
     * @param index
     *          the index, not null
     * @return the builder
     */
    public Builder withIborIndex(final IborTypeIndex index) {
      _index = ArgumentChecker.notNull(index, "index");
      return this;
    }

    /**
     * Builds the generator.
     *
     * @return the generator
     */
    public QuarterlyStirFutureGenerator build() {
      if (_index == null) {
        throw new IllegalStateException("The ibor index must be supplied");
      }
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      if (_paymentAccrualFactor == null) {
        throw new IllegalStateException("The payment accrual factor must be supplied");
      }
      return new QuarterlyStirFutureGenerator(_index, _calendar, _paymentAccrualFactor);
    }

  }

  private final IborTypeIndex _index;
  private final WorkingDayCalendar _calendar;
  private final Double _paymentAccrualFactor;

  private QuarterlyStirFutureGenerator(final IborTypeIndex index, final WorkingDayCalendar calendar, final Double paymentAccrualFactor) {
    _index = index;
    _calendar = calendar;
    _paymentAccrualFactor = paymentAccrualFactor;
  }

  @Override
  public InterestRateFutureTransactionDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor,
      final double notional, final double marketQuote) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generates the nth quarterly future, assuming that the fixing period start date is an IMM date i.e. the 3rd Wednesday of March, June, September or December.
   *
   * @param date
   *          the date that the instrument is constructed
   * @param nthFuture
   *          the nth future to generate
   * @param notional
   *          the notional
   * @param marketQuote
   *          the market quote as a decimal e.g. 0.995
   * @return the instrument
   */
  public InterestRateFutureTransactionDefinition toCurveInstrument(final ZonedDateTime date, final int nthFuture, final double notional,
      final double marketQuote) {
    final ZonedDateTime fixingPeriodStartDate = InterestRateFuturesUtils.nextQuarterlyDate(nthFuture, date);
    final InterestRateFutureSecurityDefinition security = InterestRateFutureSecurityDefinition.fromFixingPeriodStartDate(fixingPeriodStartDate,
        IndexConverter.toIborIndex(_index), notional, _paymentAccrualFactor, "", _calendar);
    final int quantity = (int) Math.ceil(notional / security.getNotional());
    return new InterestRateFutureTransactionDefinition(security, quantity, date, marketQuote);
  }

}
