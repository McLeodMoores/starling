/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantScheduleGenerator.getIntegrationNodesAsDates;
import static com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantScheduleGenerator.truncateList;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.MathException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * This prices a CDS using the ISDA methodology. The API of the public functions mimic as far a possible the ISDA high level ISDA c functions. However this is
 * NOT a line-by-line translation of the ISDA code. We find agreement with ISDA to better than 1 part in 10^12 on a test suit of 200 example.
 */
public class ISDACompliantPresentValueCreditDefaultSwap {

  @SuppressWarnings("unused")
  private static final int DEFAULT_CASH_SETTLEMENT_DAYS = 3;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final DayCount ACT_360 = DayCounts.ACT_360;

  private final BusinessDayConvention _businessdayAdjustmentConvention;
  private final Calendar _calandar;
  private final DayCount _accuralDayCount;
  private final DayCount _curveDayCount;

  public ISDACompliantPresentValueCreditDefaultSwap() {
    _businessdayAdjustmentConvention = FOLLOWING;
    _calandar = DEFAULT_CALENDAR;
    _accuralDayCount = ACT_360;
    _curveDayCount = ACT_365;
  }

  /**
   * This is the present value of the premium leg per unit of fractional spread - hence it is equal to 10,000 times the RPV01 (Risky PV01). The actual PV of the
   * leg is this multiplied by the notional and the fractional spread (i.e. spread in basis points divided by 10,000)
   * <p>
   * This mimics the ISDA c function <b>JpmcdsCdsFeeLegPV</b>
   *
   * @param today
   *          The 'current' date
   * @param stepinDate
   *          Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate
   *          The valuation date. The date that values are PVed to. Is is normally today + 3 business days. Aka cash-settle date.
   * @param startDate
   *          The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param endDate
   *          The protection end date (the protection ends at end of day)
   * @param payAccOnDefault
   *          Is the accrued premium paid in the event of a default
   * @param tenor
   *          The nominal step between premium payments (e.g. 3 months, 6 months).
   * @param stubType
   *          stubType Options are FRONTSHORT, FRONTLONG, BACKSHORT, BACKLONG or NONE - <b>Note</b> in this code NONE is not allowed
   * @param yieldCurve
   *          Curve from which payments are discounted
   * @param hazardRateCurve
   *          Curve giving survival probability
   * @param protectStart
   *          Does protection start at the beginning of the day
   * @param priceType
   *          Clean or Dirty price. The clean price removes the accrued premium if the trade is between payment times.
   * @return 10,000 times the RPV01 (on a notional of 1)
   */
  public double pvPremiumLegPerUnitSpread(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate,
      final LocalDate endDate, final boolean payAccOnDefault, final Period tenor, final StubType stubType, final ISDACompliantDateYieldCurve yieldCurve,
      final ISDACompliantDateCreditCurve hazardRateCurve, final boolean protectStart, final PriceType priceType) {
    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(stepinDate, "null stepinDate");
    ArgumentChecker.notNull(valueDate, "null valueDate");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(endDate, "null endDate");
    ArgumentChecker.notNull(tenor, "null tenor");
    ArgumentChecker.notNull(stubType, "null stubType");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "null hazardRateCurve");
    ArgumentChecker.notNull(priceType, "null priceType");
    ArgumentChecker.isFalse(valueDate.isBefore(today), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "Require stepin >= today");

    final ISDAPremiumLegSchedule paymentSchedule = new ISDAPremiumLegSchedule(startDate, endDate, tenor, stubType, _businessdayAdjustmentConvention, _calandar,
        protectStart);
    final int nPayments = paymentSchedule.getNumPayments();

    // these are potentially different from startDate and endDate
    final LocalDate globalAccStart = paymentSchedule.getAccStartDate(0);
    final LocalDate golobalAccEnd = paymentSchedule.getAccEndDate(nPayments - 1);

    // TODO this logic could be part of ISDAPremiumLegSchdule
    final LocalDate matDate = protectStart ? golobalAccEnd.minusDays(1) : golobalAccEnd;

    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
      return 0.0; // trade has expired
    }

    final LocalDate[] yieldCurveDates = yieldCurve.getCurveDates();
    final LocalDate[] creditCurveDates = hazardRateCurve.getCurveDates();
    // This is common to the protection leg
    final LocalDate[] integrationSchedule = payAccOnDefault ? getIntegrationNodesAsDates(globalAccStart, golobalAccEnd, yieldCurveDates, creditCurveDates)
        : null;
    final int obsOffset = protectStart ? -1 : 0; // protection start at the beginning or end day

    double rpv01 = 0.0;
    for (int i = 0; i < nPayments; i++) {

      final LocalDate accStart = paymentSchedule.getAccStartDate(i);
      final LocalDate accEnd = paymentSchedule.getAccEndDate(i);
      final LocalDate pay = paymentSchedule.getPaymentDate(i);

      if (!accEnd.isAfter(stepinDate)) {
        continue; // this cashflow has already been realised
      }

      final double[] temp = calculateSinglePeriodRPV01(today, accStart, accEnd, pay, obsOffset, yieldCurve, hazardRateCurve);
      rpv01 += temp[0];

      if (payAccOnDefault) {
        final LocalDate offsetStepinDate = stepinDate.plusDays(obsOffset);
        final LocalDate offsetAccStartDate = accStart.plusDays(obsOffset);
        final LocalDate offsetAccEndDate = accEnd.plusDays(obsOffset);
        rpv01 += calculateSinglePeriodAccrualOnDefault(today, offsetStepinDate, offsetAccStartDate, offsetAccEndDate, temp[1], yieldCurve, hazardRateCurve,
            integrationSchedule);
      }
    }

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double t = _curveDayCount.getDayCountFraction(today, valueDate);
    final double df = yieldCurve.getDiscountFactor(t);
    rpv01 /= df;

    // Do we want to calculate the clean price (includes the previously accrued portion of the premium)
    if (priceType == PriceType.CLEAN) {
      rpv01 -= calculateAccruedInterest(paymentSchedule, stepinDate);
    }

    return rpv01;
  }

  /**
   * Computes the risky present value of a premium payment
   * <p>
   * This mimics the ISDA c code function <b>FeePaymentPVWithTimeLine<b>
   *
   * @param today
   * @param valueDate
   * @param stepinDate
   * @param accStartDate
   * @param accEndDate
   * @param payAccOnDefault
   * @return PV
   */
  private double[] calculateSinglePeriodRPV01(final LocalDate today, final LocalDate accStartDate, final LocalDate accEndDate, final LocalDate paymentDate,
      final int obsOffset, final ISDACompliantDateYieldCurve yieldCurve, final ISDACompliantDateCreditCurve hazardRateCurve) {

    final double accTime = _accuralDayCount.getDayCountFraction(accStartDate, accEndDate);
    double t = _curveDayCount.getDayCountFraction(today, paymentDate);
    double tObsOffset = _curveDayCount.getDayCountFraction(today, accEndDate.plusDays(obsOffset));

    // TODO Do we need this?
    // Compensate Java shortcoming
    if (Double.compare(t, -0.0) == 0) {
      t = 0;
    }
    if (Double.compare(tObsOffset, -0.0) == 0) {
      tObsOffset = 0;
    }

    final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
    final double discount = yieldCurve.getDiscountFactor(t);
    return new double[] { accTime * discount * survival, accTime };
  }

  /**
   * this mimics the ISDA c JpmcdsAccrualOnDefaultPVWithTimeLine
   *
   * @param today
   * @param valueDate
   * @param offsetStepinDate
   * @param offsetAccStartDate
   * @param offsetAccEndDate
   * @param obsOffset
   * @param yieldCurve
   * @param hazardRateCurve
   * @param integrationSchedule
   * @return The single period accrual on default
   */
  private static double calculateSinglePeriodAccrualOnDefault(final LocalDate today, final LocalDate offsetStepinDate, final LocalDate offsetAccStartDate,
      final LocalDate offsetAccEndDate, final double accTime, final ISDACompliantDateYieldCurve yieldCurve, final ISDACompliantDateCreditCurve hazardRateCurve,
      final LocalDate[] integrationSchedule) {

    final LocalDate[] truncatedDateList = truncateList(offsetAccStartDate, offsetAccEndDate, integrationSchedule);
    final int nItems = truncatedDateList.length;

    // max(offsetStepinDate,offsetAccStartDate)
    LocalDate subStartDate = offsetStepinDate.isAfter(offsetAccStartDate) ? offsetStepinDate : offsetAccStartDate;

    final double tAcc = ACT_365.getDayCountFraction(offsetAccStartDate, offsetAccEndDate); // This is hardcoded to ACT/365 in ISDA code
    final double accRate = accTime / tAcc;
    double t = ACT_365.getDayCountFraction(today, subStartDate);

    // Compensate Java shortcoming
    if (Double.compare(t, -0.0) == 0) {
      t = 0;
    }
    double s0 = hazardRateCurve.getSurvivalProbability(t);
    double df0 = yieldCurve.getDiscountFactor(t);

    double myPV = 0.0;
    for (int j = 1; j < nItems; ++j) {

      if (!truncatedDateList[j].isAfter(offsetStepinDate)) {
        continue;
      }

      double thisAccPV = 0.0;
      t = ACT_365.getDayCountFraction(today, truncatedDateList[j]);
      final double s1 = hazardRateCurve.getSurvivalProbability(t);
      final double df1 = yieldCurve.getDiscountFactor(t);

      final double t0 = ACT_365.getDayCountFraction(offsetAccStartDate, subStartDate) + 1 / 730.; // add on half a day
      final double t1 = ACT_365.getDayCountFraction(offsetAccStartDate, truncatedDateList[j]) + 1 / 730.;
      t = t1 - t0; // t repurposed

      // TODO check for s0 == s1 -> zero prob of default (and thus zero PV contribution) from this section
      // if (s0 == s1) {
      // continue;
      // }

      final double lambda = Math.log(s0 / s1) / t;
      final double fwdRate = Math.log(df0 / df1) / t;
      final double lambdafwdRate = lambda + fwdRate + 1.0e-50;

      thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / lambdafwdRate) / lambdafwdRate - (t1 + 1.0 / lambdafwdRate) / lambdafwdRate * s1 / s0 * df1 / df0);
      myPV += thisAccPV;
      s0 = s1;
      df0 = df1;
      subStartDate = truncatedDateList[j];
    }
    return myPV;
  }

  /**
   * Calculate the accrued premium at the start of a trade
   *
   * @param premiumLegSchedule
   * @param stepinDate
   *          The trade effective date
   * @return accrued premium
   */
  private double calculateAccruedInterest(final ISDAPremiumLegSchedule premiumLegSchedule, final LocalDate stepinDate) {

    final int n = premiumLegSchedule.getNumPayments();

    // stepinDate is before first accStart or after last accEnd
    if (!stepinDate.isAfter(premiumLegSchedule.getAccStartDate(0)) || !stepinDate.isBefore(premiumLegSchedule.getAccEndDate(n - 1))) {
      return 0.0;
    }

    int index = premiumLegSchedule.getAccStartDateIndex(stepinDate);
    if (index >= 0) {
      return 0.0; // on accrual start date
    }

    index = -(index + 1); // binary search notation
    if (index == 0) {
      throw new MathException("Error in calculateAccruedInterest - check logic"); // this should never be hit
    }

    return _accuralDayCount.getDayCountFraction(premiumLegSchedule.getAccStartDate(index - 1), stepinDate);
  }

  /**
   * Get the value of the protection leg for unit notional.
   * <p>
   * This mimics the ISDA c function <b>JpmcdsCdsContingentLegPV</b>
   *
   * @param today
   *          The 'current' date
   * @param stepinDate
   *          Date when party assumes ownership. This is normally today + 1 (T+1). Aka assignment date or effective date.
   * @param valueDate
   *          The valuation date. The date that values are PVed to. Is is normally today + 3 business days. Aka cash-settle date.
   * @param startDate
   *          The protection start date. If protectStart = true, then protections starts at the beginning of the day, otherwise it is at the end.
   * @param endDate
   *          The protection end date (the protection ends at end of day)
   * @param yieldCurve
   *          Curve from which payments are discounted
   * @param hazardRateCurve
   *          Curve giving survival probability
   * @param recoveryRate
   *          The recovery rate of the protected debt
   * @param protectStart
   *          Does protection start at the beginning of the day
   * @return unit notional PV of protection (or contingent) leg
   */
  public double calculateProtectionLeg(final LocalDate today, final LocalDate stepinDate, final LocalDate valueDate, final LocalDate startDate,
      final LocalDate endDate, final ISDACompliantDateYieldCurve yieldCurve, final ISDACompliantDateCreditCurve hazardRateCurve, final double recoveryRate,
      final boolean protectStart) {
    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(valueDate, "null valueDate");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(endDate, "null endDate");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "null hazardRateCurve");
    ArgumentChecker.isInRangeInclusive(0, 1.0, recoveryRate);
    ArgumentChecker.isFalse(valueDate.isBefore(today), "Require valueDate >= today");
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "Require stepin >= today");

    if (recoveryRate == 1.0) {
      return 0.0;
    }

    final LocalDate temp = stepinDate.isAfter(startDate) ? stepinDate : startDate;
    final LocalDate effectiveStartDate = protectStart ? temp.minusDays(1) : temp;

    if (!endDate.isAfter(effectiveStartDate)) {
      return 0.0; // the protection has expired
    }

    final LocalDate[] yieldCurveDates = yieldCurve.getCurveDates();
    final LocalDate[] creditCurveDates = hazardRateCurve.getCurveDates();
    final double[] integrationSchedule = ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, effectiveStartDate, endDate, yieldCurveDates,
        creditCurveDates);

    // double s1 = hazardRateCurve.getSurvivalProbability(integrationSchedule[0]);
    // double df1 = yieldCurve.getDiscountFactor(integrationSchedule[0]);

    double ht1 = hazardRateCurve.getRT(integrationSchedule[0]);
    double rt1 = yieldCurve.getRT(integrationSchedule[0]);
    double s1 = Math.exp(-ht1);
    double p1 = Math.exp(-rt1);
    double pv = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      final double ht0 = ht1;
      final double rt0 = rt1;
      final double p0 = p1;
      final double s0 = s1;

      ht1 = hazardRateCurve.getRT(integrationSchedule[i]);
      rt1 = yieldCurve.getRT(integrationSchedule[i]);
      s1 = Math.exp(-ht1);
      p1 = Math.exp(-rt1);
      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      // this is equivalent to the ISDA code without explicitly calculating the time step - it also handles the limit
      double dPV;
      if (Math.abs(dhrt) < 1e-5) {
        dPV = dht * (1 - dhrt * (0.5 - dhrt / 6)) * p0 * s0;
      } else {
        dPV = dht / dhrt * (p0 * s0 - p1 * s1);
      }

      // *************
      // ISDA code
      // **************
      // final double dt = integrationSchedule[i] - integrationSchedule[i - 1];
      // final double s0 = s1;
      // final double df0 = df1;
      // s1 = hazardRateCurve.getSurvivalProbability(integrationSchedule[i]);
      // df1 = yieldCurve.getDiscountFactor(integrationSchedule[i]);
      // final double hazardRate = Math.log(s0 / s1) / dt;
      // final double interestRate = Math.log(df0 / df1) / dt;
      // pv += (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * dt)) * s0 * df0;

      pv += dPV;

    }
    pv *= 1.0 - recoveryRate;

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double t = _curveDayCount.getDayCountFraction(today, valueDate);
    final double df = yieldCurve.getDiscountFactor(t);
    pv /= df;

    return pv;
  }

}
