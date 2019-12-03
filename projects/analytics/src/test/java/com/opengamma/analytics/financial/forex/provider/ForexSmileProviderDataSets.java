/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;

/**
 * Sets of market data used in Forex tests.
 */
public class ForexSmileProviderDataSets {

  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Period[] EXPIRY_PERIOD = new Period[] { Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(5) };
  private static final int NB_EXP = EXPIRY_PERIOD.length;
  private static final double[] ATM = { 0.185, 0.18, 0.17, 0.16, 0.16 };

  private static final double[] DELTA_2 = new double[] { 0.10, 0.25 };
  private static final double[][] RISK_REVERSAL_2 = new double[][] { { -0.011, -0.0060 }, { -0.012, -0.0070 }, { -0.013, -0.0080 },
      { -0.014, -0.0090 }, { -0.014, -0.0090 } };
  private static final double[][] STRANGLE_2 = new double[][] { { 0.0310, 0.0110 }, { 0.0320, 0.0120 }, { 0.0330, 0.0130 },
      { 0.0340, 0.0140 }, { 0.0340, 0.0140 } };

  private static final double[] DELTA_1 = new double[] { 0.25 };
  private static final double[][] RISK_REVERSAL_1 = new double[][] { { -0.0060 }, { -0.0070 }, { -0.0080 }, { -0.0090 }, { -0.0090 } };
  private static final double[][] STRANGLE_1 = new double[][] { { 0.0110 }, { 0.0120 }, { 0.0130 }, { 0.0140 }, { 0.0140 } };

  private static final double[][] RISK_REVERSAL_FLAT = new double[][] { { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 },
      { 0.0, 0.0 } };
  private static final double[][] STRANGLE_FLAT = new double[][] { { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 }, { 0.0, 0.0 } };

  /**
   * Gets a volatility surface with a term structure that has five points in each smile.
   *
   * @param referenceDate
   *          the reference data for the data
   * @return a volatility surface
   */
  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int i = 0; i < NB_EXP; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[i] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[i]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2);
  }

  /**
   * Gets a volatility surface with a term structure that has five points in each smile.
   *
   * @param referenceDate
   *          the reference data for the data
   * @param interpolator
   *          the strike interpolator
   * @return a volatility surface
   */
  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate,
      final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int i = 0; i < NB_EXP; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[i] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[i]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_2, STRANGLE_2, interpolator);
  }

  /**
   * Gets a volatility surface with a term structure that has three points in each smile.
   *
   * @param referenceDate
   *          the reference data for the data
   * @param interpolator
   *          the strike interpolator
   * @return a volatility surface
   */
  public static SmileDeltaTermStructureParametersStrikeInterpolation smile3points(final ZonedDateTime referenceDate,
      final Interpolator1D interpolator) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int i = 0; i < NB_EXP; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[i] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[i]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_1, ATM, RISK_REVERSAL_1, STRANGLE_1, interpolator);
  }

  /**
   * Gets a shifted volatility surface with a term structure that has five points in each smile.
   *
   * @param referenceDate
   *          the reference data for the data
   * @param shift
   *          the shift
   * @return a volatility surface
   */
  public static SmileDeltaTermStructureParametersStrikeInterpolation smile5points(final ZonedDateTime referenceDate, final double shift) {
    final double[] atmShift = ATM.clone();
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int i = 0; i < NB_EXP; i++) {
      atmShift[i] += shift;
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[i] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[i]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, atmShift, RISK_REVERSAL_2, STRANGLE_2);
  }

  /**
   * Gets a flat volatility surface with a term structure.
   *
   * @param referenceDate
   *          the reference data for the data
   * @return a volatility surface
   */
  public static SmileDeltaTermStructureParametersStrikeInterpolation smileFlat(final ZonedDateTime referenceDate) {
    final ZonedDateTime[] expiryDate = new ZonedDateTime[NB_EXP];
    final double[] timeToExpiry = new double[NB_EXP];
    for (int i = 0; i < NB_EXP; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(referenceDate, EXPIRY_PERIOD[i], BUSINESS_DAY, CALENDAR, true);
      timeToExpiry[i] = TimeCalculator.getTimeBetween(referenceDate, expiryDate[i]);
    }
    return new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiry, DELTA_2, ATM, RISK_REVERSAL_FLAT, STRANGLE_FLAT);
  }

}
