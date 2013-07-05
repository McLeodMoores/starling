/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.StubType;

/**
 * 
 */
public class AnalyticCDSPricerTest {

  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final AnalyticCDSPricer PRICER_CORRECT = new AnalyticCDSPricer(true);

  @Test(enabled = false)
  public void timingTest() {

    final int warmup = 1000;
    final int benchmark = 10000;

    final double fracSpred = 0.01;

    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0};
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(1);
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today.plusMonths(1); // protection starts in a month
    LocalDate endDate1 = LocalDate.of(2018, 6, 20);
    LocalDate endDate2 = LocalDate.of(2023, 6, 20);

    CDSAnalytic cds1 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    CDSAnalytic cds2 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int i = 0; i < warmup; i++) {
      double p1 = PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
      double p2 = PRICER.pv(cds2, yieldCurve, creditCurve, fracSpred);
    }
    long timer = System.nanoTime();
    double p1 = 0;
    for (int i = 0; i < benchmark; i++) {
      p1 += PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p1);
    double time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 5 year CDS: " + time + "ms");

    timer = System.nanoTime();
    double p2 = 0;
    for (int i = 0; i < benchmark; i++) {
      p2 += PRICER.pv(cds2, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p2);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 10 year CDS: " + time + "ms");

    // now do the date logic
    for (int i = 0; i < warmup; i++) {
      CDSAnalytic cds1temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      CDSAnalytic cds2temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    }

    timer = System.nanoTime();
    double p3 = 0;
    for (int i = 0; i < benchmark; i++) {
      CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p3 += PRICER.pv(cds, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 5 year CDS with date logic: " + time + "ms");

    timer = System.nanoTime();
    double p4 = 0;
    for (int i = 0; i < benchmark; i++) {
      CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p4 += PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 10 year CDS with date logic: " + time + "ms");

  }

  @Test
  // (enabled=false)
  public void sensitivityTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccNormalRates = new double[] {0.05, 0.06, 0.07, 0.08, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ccLowRates = new double[] {0.00, 0.00, 1e-6, 2e-4, 5e-4, 0.001, 0.0015, 0.002, 0.0015};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.0, 11.0, 30.0};
    final double[] ycNormalRates = new double[] {0.004, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};
    final double[] ycLowRates = new double[] {0.00, 0.00, 0.00, 0.0, 0.00, 0.0005, 0.001, 0.0015, 0.002, 0.0015};

    final ISDACompliantCreditCurve creditCurveLow = new ISDACompliantCreditCurve(ccTimes, ccLowRates);
    final ISDACompliantCreditCurve creditCurveNorm = new ISDACompliantCreditCurve(ccTimes, ccNormalRates);
    final ISDACompliantYieldCurve yieldCurveLow = new ISDACompliantYieldCurve(ycTimes, ycLowRates);
    final ISDACompliantYieldCurve yieldCurveNorm = new ISDACompliantYieldCurve(ycTimes, ycNormalRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(1); // this is usually 1
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today; // protection starts now
    LocalDate endDate = LocalDate.of(2017, 9, 20);

    final boolean payAccOnDefault = true;

    CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, payAccOnDefault, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int count = 0; count < 1; count++) {
      final AnalyticCDSPricer pricer = count == 0 ? PRICER : PRICER_CORRECT;
      creditCurveSenseTest(pricer, cds, yieldCurveLow, creditCurveLow);
      creditCurveSenseTest(pricer, cds, yieldCurveLow, creditCurveNorm);
      creditCurveSenseTest(pricer, cds, yieldCurveNorm, creditCurveLow);
      creditCurveSenseTest(pricer, cds, yieldCurveNorm, creditCurveNorm);
    }

    // final int n = ccTimes.length;
    // for (int i = 0; i < n; i++) {
    // final double fdProSenseLL = fdProtectionLegSense(cds, yieldCurveLow, creditCurveLow, i);
    // final double fdProSenseLN = fdProtectionLegSense(cds, yieldCurveLow, creditCurveNorm, i);
    // final double fdProSenseNL = fdProtectionLegSense(cds, yieldCurveNorm, creditCurveLow, i);
    // final double fdProSenseNN = fdProtectionLegSense(cds, yieldCurveNorm, creditCurveNorm, i);
    // final double analProSenseLL = PRICER.protectionLegCreditSensitivity(cds, yieldCurveLow, creditCurveLow, i);
    // final double analProSenseLN = PRICER.protectionLegCreditSensitivity(cds, yieldCurveLow, creditCurveNorm, i);
    // final double analProSenseNL = PRICER.protectionLegCreditSensitivity(cds, yieldCurveNorm, creditCurveLow, i);
    // final double analProSenseNN = PRICER.protectionLegCreditSensitivity(cds, yieldCurveNorm, creditCurveNorm, i);
    //
    // final double fdRPV01SenseLL = fdRPV01Sense(cds, yieldCurveLow, creditCurveLow, i,PRICER);
    // final double fdRPV01SenseLN = fdRPV01Sense(cds, yieldCurveLow, creditCurveNorm, i,PRICER);
    // final double fdRPV01SenseNL = fdRPV01Sense(cds, yieldCurveNorm, creditCurveLow, i,PRICER);
    // final double fdRPV01SenseNN = fdRPV01Sense(cds, yieldCurveNorm, creditCurveNorm, i,PRICER);
    // final double analRPV01SenseLL = PRICER.rpv01CreditSensitivity(cds, yieldCurveLow, creditCurveLow, i);
    // final double analRPV01SenseLN = PRICER.rpv01CreditSensitivity(cds, yieldCurveLow, creditCurveNorm, i);
    // final double analRPV01SenseNL = PRICER.rpv01CreditSensitivity(cds, yieldCurveNorm, creditCurveLow, i);
    // final double analRPV01SenseNN = PRICER.rpv01CreditSensitivity(cds, yieldCurveNorm, creditCurveNorm, i);
    // //
    // assertEquals("ProSenseLL " + i, fdProSenseLL, analProSenseLL, 1e-10);
    // assertEquals("ProSenseLN " + i, fdProSenseLN, analProSenseLN, 1e-9);
    // assertEquals("ProSenseNL " + i, fdProSenseNL, analProSenseNL, 1e-9);
    // assertEquals("ProSenseNN " + i, fdProSenseNN, analProSenseNN, 1e-9);
    //
    // // these errors are consistent with the accuracy for finite difference
    // assertEquals("RPV01SenseLL " + i, fdRPV01SenseLL, analRPV01SenseLL, 1e-8);
    // assertEquals("RPV01SenseLN " + i, fdRPV01SenseLN, analRPV01SenseLN, 5e-8);
    // assertEquals("RPV01SenseNL " + i, fdRPV01SenseNL, analRPV01SenseNL, 1e-8);
    // assertEquals("RPV01SenseNN " + i, fdRPV01SenseNN, analRPV01SenseNN, 2e-8);
    //
    // // System.out.println(fdRPV01SenseNN + "\t" + analRPV01SenseNN);
    // // System.out.println(fdProSenseLL + "\t" + analProSenseLL);
    // // System.out.println(fdProSenseNL + "\t" + analProSenseNL);

  }

  private void creditCurveSenseTest(final AnalyticCDSPricer pricer, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    final int n = creditCurve.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      final double fdProSense = fdProtectionLegSense(cds, yieldCurve, creditCurve, i);
      final double analProSense = pricer.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
      final double fdRPV01Sense = fdRPV01Sense(cds, yieldCurve, creditCurve, i, pricer);
      final double analRPV01Sense = pricer.rpv01CreditSensitivity(cds, yieldCurve, creditCurve, i);
      assertEquals("ProSense " + i, fdProSense, analProSense, 1e-9);
      assertEquals("RPV01Sense " + i, fdRPV01Sense, analRPV01Sense, 5e-8);
    }
  }

  @Test
  // (enabled=false)
  public void sensitivityParallelShiftTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0};
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(2); // this is usually 1
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today.plusMonths(1); // protection starts in a month
    LocalDate endDate = LocalDate.of(2023, 6, 20);

    CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    final double fd = fdProtectionLegSense(cds, yieldCurve, creditCurve);

    final int n = creditCurve.getNumberOfKnots();
    double anal = 0.0;
    for (int i = 0; i < n; i++) {
      anal += PRICER.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
    }
    assertEquals(fd, anal, 1e-8);
  }

  private double fdRPV01Sense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve, final int creditCurveNode, final AnalyticCDSPricer pricer) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-3 * Math.max(1e-3, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = pricer.rpv01(cds, yieldCurve, ccUp, PriceType.DIRTY); // clean or dirty has no effect on sensitivity
    final double down = pricer.rpv01(cds, yieldCurve, ccDown, PriceType.DIRTY);
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegSense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve) {

    final int n = creditCurve.getNumberOfKnots();
    final double h = 0.5 * (creditCurve.getZeroRateAtIndex(0) + creditCurve.getZeroRateAtIndex(n - 1));
    final double eps = 1e-4 * h;

    final double[] rUp = creditCurve.getKnotZeroRates();
    final double[] rDown = creditCurve.getKnotZeroRates();
    for (int i = 0; i < n; i++) {
      rUp[i] += eps;
      rDown[i] -= eps;
    }
    final double up = PRICER.protectionLeg(cds, yieldCurve, creditCurve.withRates(rUp));
    final double down = PRICER.protectionLeg(cds, yieldCurve, creditCurve.withRates(rDown));
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegSense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-4 * Math.max(1e-3, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = PRICER.protectionLeg(cds, yieldCurve, ccUp);
    final double down = PRICER.protectionLeg(cds, yieldCurve, ccDown);
    return (up - down) / 2 / eps;
  }

}