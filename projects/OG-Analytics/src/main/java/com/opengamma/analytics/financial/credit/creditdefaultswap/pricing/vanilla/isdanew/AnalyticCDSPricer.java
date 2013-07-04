/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.DoublesScheduleGenerator.truncateSetInclusive;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnalyticCDSPricer {

  private static final double[] COEFF1 = new double[] {1 / 24., 1 / 6., 0.5, 1};
  private static final double[] COEFF2 = new double[] {1 / 30., 1 / 8., 1 / 3., 0.5};
  private static final double[] COEFF3 = new double[] {1 / 48., 1 / 10., 1 / 4., 1 / 3.};

  private static final boolean DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA = false;

  private final boolean _useCorrectAccOnDefaultFormula;

  public AnalyticCDSPricer() {
    _useCorrectAccOnDefaultFormula = DEFAULT_USE_CORRECT_ACC_ON_DEFAULT_FORMULA;
  }

  public AnalyticCDSPricer(final boolean useCorrectAccOnDefaultFormula) {
    _useCorrectAccOnDefaultFormula = useCorrectAccOnDefaultFormula;
  }

  /**
   * Present value for the payer of premiums (i.e. the buyer of protection) 
  * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve The yield (or discount) curve  
   * @param creditCurve the credit (or survival) curve 
   * @param fractionalSpread The <b>fraction</b> spread 
   * @return The PV 
   */
  public double pv(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double fractionalSpread) {
    // TODO check for any repeat calculations
    final double rpv01 = rpv01(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double proLeg = protectionLeg(cds, yieldCurve, creditCurve);
    return proLeg - fractionalSpread * rpv01;
  }

  public double rpv01(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final PriceType cleanOrDirty) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    final int n = cds.getNumPayments();
    double pv = 0.0;
    for (int i = 0; i < n; i++) {
      final double q = creditCurve.getDiscountFactor(cds.getCreditObservationTime(i));
      final double p = yieldCurve.getDiscountFactor(cds.getPaymentTime(i));
      pv += cds.getAccrualFraction(i) * p * q;
    }

    if (cds.isPayAccOnDefault()) {
      final double offset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;
      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + offset;

      double accPV = 0.0;
      for (int i = 0; i < n; i++) {
        double offsetAccStart = cds.getAccStart(i) + offset;
        double offsetAccEnd = cds.getAccEnd(i) + offset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPV += calculateSinglePeriodAccrualOnDefault(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve);
      }
      pv += accPV;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    if (cleanOrDirty == PriceType.CLEAN) {
      pv -= cds.getAccrued();
    }
    return pv;
  }

  /**
   * The sensitivity of the RPV01 to the zero hazard rate of a given node (knot) of the credit curve. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve The yield (or discount) curve  
   * @param creditCurve the credit (or survival) curve 
   * @param creditCurveNode The credit curve node 
   * @return  sensitivity (on a unit notional) 
   * @deprecated not tested 
   */
  @Deprecated
  public double rpv01CreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    final int n = cds.getNumPayments();
    double pvSense = 0.0;
    for (int i = 0; i < n; i++) {
      final double dqdh = creditCurve.getSingleNodeDiscountFactorSensitivity(cds.getCreditObservationTime(i), creditCurveNode);
      final double p = yieldCurve.getDiscountFactor(cds.getPaymentTime(i));
      pvSense += cds.getAccrualFraction(i) * p * dqdh;
    }

    if (cds.isPayAccOnDefault()) {
      final double offset = cds.isProtectionFromStartOfDay() ? -cds.getCurveOneDay() : 0.0;
      final double[] integrationSchedule = getIntegrationsPoints(cds.getAccStart(0), cds.getAccEnd(n - 1), yieldCurve, creditCurve);
      final double offsetStepin = cds.getStepin() + offset;

      double accPVSense = 0.0;
      for (int i = 0; i < n; i++) {
        double offsetAccStart = cds.getAccStart(i) + offset;
        double offsetAccEnd = cds.getAccEnd(i) + offset;
        final double accRate = cds.getAccrualFraction(i) / (offsetAccEnd - offsetAccStart);
        accPVSense += calculateSinglePeriodAccrualOnDefaultSensitivity(accRate, offsetStepin, offsetAccStart, offsetAccEnd, integrationSchedule, yieldCurve, creditCurve, creditCurveNode);
      }
      pvSense += accPVSense;
    }

    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pvSense /= df;
    return pvSense;
  }

  private double calculateSinglePeriodAccrualOnDefault(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {

    double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double b0 = Math.exp(-rt0 - ht0); // this is the risky discount factor

    double t0 = _useCorrectAccOnDefaultFormula ? 0.0 : t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pv = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      double ht1 = creditCurve.getRT(t);
      double rt1 = yieldCurve.getRT(t);
      double b1 = Math.exp(-rt1 - ht1);

      final double dt = knots[j] - knots[j - 1];

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt + 1e-50; // to keep consistent with ISDA c code

      double tPV;
      if (_useCorrectAccOnDefaultFormula) {
        if (Math.abs(dhrt) < 1e-5) {
          tPV = dht * dt * b0 * epsilonP(-dhrt);
        } else {
          tPV = dht * dt / dhrt * ((b0 - b1) / dhrt - b1);
        }
      } else {
        // This is a know bug - a fix is proposed by Markit (and appears commented out in ISDA v.1.8.2)
        // This is the correct term plus dht*t0/dhrt*(b0-b1) which is an error
        final double t1 = t - accStart + 1 / 730.0;
        if (Math.abs(dhrt) < 1e-5) {
          tPV = dht * b0 * (t0 * epsilon(-dhrt) + dt * epsilonP(-dhrt));
        } else {
          tPV = dht / dhrt * ((t0 + dt / dhrt) * b0 - (t1 + dt / dhrt) * b1);
        }
        t0 = t1;
      }
      // TODO the Taylor expansions

      pv += tPV;
      ht0 = ht1;
      rt0 = rt1;
      b0 = b1;
    }
    return accRate * pv;
  }

  private double calculateSinglePeriodAccrualOnDefaultSensitivity(final double accRate, final double stepin, final double accStart, final double accEnd, final double[] integrationPoints,
      final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    double start = Math.max(accStart, stepin);
    if (start >= accEnd) {
      return 0.0;
    }
    double[] knots = truncateSetInclusive(start, accEnd, integrationPoints);

    double t = knots[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double p0 = Math.exp(-rt0);
    double q0 = Math.exp(-ht0);
    double b0 = p0 * q0; // this is the risky discount factor
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);

    double t0 = _useCorrectAccOnDefaultFormula ? 0.0 : t - accStart + 1 / 730.0; // TODO not entirely clear why ISDA adds half a day
    double pvSense = 0.0;
    final int nItems = knots.length;
    for (int j = 1; j < nItems; ++j) {
      t = knots[j];
      final double ht1 = creditCurve.getRT(t);
      final double rt1 = yieldCurve.getRT(t);
      final double p1 = Math.exp(-rt1);
      final double q1 = Math.exp(-ht1);
      final double b1 = p1 * q1;
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);

      final double dt = knots[j] - knots[j - 1];

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt + 1e-50; // to keep consistent with ISDA c code

      double tPV;
      double tPvSense;
      // TODO once the maths is written up in a white paper, check these formula again, since tests again finite difference
      // could miss some subtle error
      if (_useCorrectAccOnDefaultFormula) {
        if (Math.abs(dhrt) < 1e-5) {
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          final double dPVdq0 = dht * p0 * dt * (eP - ePP);
          final double dPVdq1 = dht * b0 * dt / q1 * ePP;
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        } else {
          tPV = dht * dt / dhrt * ((b0 - b1) / dhrt - b1);
          final double dPVdq0 = tPV / q0 * (1 / dht - 1 / dhrt) + dht * dt / dhrt / dhrt * (p0 - (p0 - b1 / q0) / dhrt);
          final double dPVdq1 = tPV / q1 * (-1 / dht + 1 / dhrt) + dht * dt / dhrt * ((b0 - b1) / q1 / dhrt / dhrt - p1 * (1 + 1 / dhrt));
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        }
      } else {
        // this is a know bug - a fix is proposed by Markit (and appears commented out in ISDA v.1.8.2)
        final double t1 = t - accStart + 1 / 730.0;
        if (Math.abs(dhrt) < 1e-5) {
    //       final double e1 = epsilon(-dhrt);
          final double eP = epsilonP(-dhrt);
          final double ePP = epsilonPP(-dhrt);
          final double dPVdq0 = dht * p0 * dt * (eP - ePP);
          final double dPVdq1 = dht * p0 * q0 / q1 * (t0 * eP + dt * ePP);
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        } else {
          tPV = dht / dhrt * ((t0 + dt / dhrt) * b0 - (t1 + dt / dhrt) * b1);
          final double dPVdq0 = tPV / q0 * (1 / dht - 1 / dhrt) + dht / dhrt * ((t0 + dt / dhrt) * p0 - dt * (b0 - b1) / q0 / dhrt / dhrt);
          final double dPVdq1 = -tPV / q1 * (1 / dht - 1 / dhrt) - dht / dhrt * ((t1 + dt / dhrt) * p1 - dt * (b0 - b1) / q1 / dhrt / dhrt);
          tPvSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
        }
        t0 = t1;
      }
      // TODO the Taylor expansions

      pvSense += tPvSense;
      ht0 = ht1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;
      b0 = b1;
      dqdr0 = dqdr1;
    }
    return accRate * pvSense;
  }

  /**
   * Compute the present value of the protection leg with a notional of 1, which is given by the integral 
   * $\frac{1-R}{P(T_{v})} \int_{T_a} ^{T_b} P(t) \frac{dQ(t)}{dt} dt$ where $P(t)$ and $Q(t)$ are the discount and survival curves 
   * respectively, $T_a$ and $T_b$ are the start and end of the protection respectively, $T_v$ is the valuation time (all measured 
   * from $t = 0$, 'today') and $R$ is the recovery rate. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve The yield (or discount) curve  
   * @param creditCurve the credit (or survival) curve 
   * @return The value of the protection leg (on a unit notional) 
   */
  public double protectionLeg(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double ht0 = creditCurve.getRT(integrationSchedule[0]);
    double rt0 = yieldCurve.getRT(integrationSchedule[0]);
    double b0 = Math.exp(-ht0 - rt0); // risky discount factor

    double pv = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      final double ht1 = creditCurve.getRT(integrationSchedule[i]);
      final double rt1 = yieldCurve.getRT(integrationSchedule[i]);
      final double b1 = Math.exp(-ht1 - rt1);

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      // The formula has been modified from ISDA (but is equivalent) to avoid log(exp(x)) and explicitly calculating the time
      // step - it also handles the limit
      double dPV;
      if (Math.abs(dhrt) < 1e-5) {
        dPV = dht * b0 * epsilon(-dhrt);
      } else {
        dPV = (b0 - b1) * dht / dhrt;
      }

      pv += dPV;
      ht0 = ht1;
      rt0 = rt1;
      b0 = b1;
    }
    pv *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());
    pv /= df;

    return pv;
  }

  /**
   * The sensitivity of the PV of the protection leg to the zero hazard rate of a given node (knot) of the credit curve. 
   * @param cds analytic description of a CDS traded at a certain time 
   * @param yieldCurve The yield (or discount) curve  
   * @param creditCurve the credit (or survival) curve 
   * @param creditCurveNode The credit curve node 
   * @return  sensitivity (on a unit notional) 
   * @deprecated not tested 
   */
  @Deprecated
  public double protectionLegCreditSensitivity(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(creditCurve, "null creditCurve");
    ArgumentChecker.isTrue(creditCurveNode >= 0 && creditCurveNode < creditCurve.getNumberOfKnots(), "creditCurveNode out of range");
    if ((creditCurveNode != 0 && cds.getProtectionEnd() <= creditCurve.getTimeAtIndex(creditCurveNode - 1))
        || (creditCurveNode != creditCurve.getNumberOfKnots() - 1 && cds.getProtectionStart() >= creditCurve.getTimeAtIndex(creditCurveNode + 1))) {
      return 0.0; // can't have any sensitivity in this case
    }

    final double[] integrationSchedule = getIntegrationsPoints(cds.getProtectionStart(), cds.getProtectionEnd(), yieldCurve, creditCurve);

    double t = integrationSchedule[0];
    double ht0 = creditCurve.getRT(t);
    double rt0 = yieldCurve.getRT(t);
    double dqdr0 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
    double q0 = Math.exp(-ht0);
    double p0 = Math.exp(-rt0);
    // double pv = 0.0;
    double pvSense = 0.0;
    final int n = integrationSchedule.length;
    for (int i = 1; i < n; ++i) {

      t = integrationSchedule[i];
      final double ht1 = creditCurve.getRT(t);
      final double dqdr1 = creditCurve.getSingleNodeDiscountFactorSensitivity(t, creditCurveNode);
      final double rt1 = yieldCurve.getRT(t);
      final double q1 = Math.exp(-ht1);
      final double p1 = Math.exp(-rt1);

      if (dqdr0 == 0.0 && dqdr1 == 0.0) {
        continue;
      }

      final double dht = ht1 - ht0;
      final double drt = rt1 - rt0;
      final double dhrt = dht + drt;

      double dPVSense;
      if (Math.abs(dhrt) < 1e-5) {
        final double theta = epsilon(-dhrt); // 1 - dhrt * (0.5 - dhrt / 6);
        final double thetaPrime = epsilonP(-dhrt);
        final double dPVdq0 = p0 * ((1 + dht) * theta + dht * thetaPrime);
        final double dPVdq1 = -p0 * q0 / q1 * (theta + dht * thetaPrime);
        dPVSense = dPVdq0 * dqdr0 + dPVdq1 * dqdr1;
      } else {
        final double temp1 = drt / dhrt * (p0 * q0 - p1 * q1);
        dPVSense = ((p0 * dht + temp1 / q0) * dqdr0 - (p1 * dht + temp1 / q1) * dqdr1) / dhrt;
      }

      // pv += dPV;
      pvSense += dPVSense;

      ht0 = ht1;
      dqdr0 = dqdr1;
      rt0 = rt1;
      p0 = p1;
      q0 = q1;

    }
    pvSense *= cds.getLGD();

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double df = yieldCurve.getDiscountFactor(cds.getValuationTime());

    pvSense /= df;

    return pvSense;
  }

  private static double epsilon(final double x) {
    if (Math.abs(x) > 1e-10) {
      return Math.expm1(x) / x;
    }
    double sum = COEFF1[0];
    final int n = COEFF1.length;
    for (int i = 1; i < n; i++) {
      sum = COEFF1[i] + x * sum;
    }
    return sum;
  }

  private static double epsilonP(final double x) {

    if (Math.abs(x) > 1e-10) {
      return ((x - 1) * Math.expm1(x) + x) / x / x;
    }

    double sum = COEFF2[0];
    final int n = COEFF2.length;
    for (int i = 1; i < n; i++) {
      sum = COEFF2[i] + x * sum;
    }
    return sum;
  }

  private static double epsilonPP(final double x) {

    if (Math.abs(x) > 1e-10) {
      final double x2 = x * x;
      final double x3 = x * x2;
      return (Math.expm1(x) * (x2 - 2 * x + 2) + x2 - 2 * x) / x3;
    }

    double sum = COEFF3[0];
    final int n = COEFF3.length;
    for (int i = 1; i < n; i++) {
      sum = COEFF3[i] + x * sum;
    }
    return sum;
  }

}
