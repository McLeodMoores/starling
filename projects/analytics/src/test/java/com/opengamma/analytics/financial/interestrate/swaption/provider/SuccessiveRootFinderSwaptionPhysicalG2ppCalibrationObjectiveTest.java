/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderG2ppCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderG2ppCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for Hull-White one factor calibration to European swaptions.
 */
@Test(groups = TestGroup.UNIT)
public class SuccessiveRootFinderSwaptionPhysicalG2ppCalibrationObjectiveTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final WorkingDayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M",
      CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER,
      EUR1YEURIBOR3M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  // Swaption description
  private static final boolean IS_LONG = true;
  // Swap 5Y description
  private static final double NOTIONAL = 100000000; // 100m
  // Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  // Ibor leg: quarterly money
  private static final int SWAP_TENOR_YEAR = 9;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR3M,
      Period.ofYears(SWAP_TENOR_YEAR), CalendarAdapter.of(CALENDAR));
  private static final int[] EXPIRY_TENOR = new int[] { 1, 2, 3, 4, 5 };
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final ZonedDateTime[] SETTLEMENT_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final SwapFixedIborDefinition[] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[EXPIRY_TENOR.length];
  private static final SwaptionPhysicalFixedIborDefinition[] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[EXPIRY_TENOR.length];
  static {
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      EXPIRY_DATE[i] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR[i]), EURIBOR3M, CALENDAR);
      SETTLEMENT_DATE[i] = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE[i], EURIBOR3M.getSpotLag(), CALENDAR);
      SWAP_PAYER_DEFINITION[i] = SwapFixedIborDefinition.from(SETTLEMENT_DATE[i], CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER,
          CalendarAdapter.of(CALENDAR));
      SWAPTION_LONG_PAYER_DEFINITION[i] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[i],
          SWAP_PAYER_DEFINITION[i], FIXED_IS_PAYER, IS_LONG);
    }
  }
  // to derivatives
  private static final SwaptionPhysicalFixedIbor[] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[EXPIRY_TENOR.length];
  static {
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      SWAPTION_LONG_PAYER[i] = SWAPTION_LONG_PAYER_DEFINITION[i].toDerivative(REFERENCE_DATE);
    }
  }
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP = SwaptionPhysicalFixedIborG2ppApproximationMethod
      .getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  /**
   * Tests the correctness of G2++ calibration to swaptions with SABR price.
   */
  @Test(enabled = false)
  public void calibration() {
    final double[] meanReversion = new double[] { 0.01, 0.30 };
    final double ratio = 4.0;
    final double correlation = -0.50;
    final G2ppPiecewiseConstantParameters g2Parameters = new G2ppPiecewiseConstantParameters(meanReversion,
        new double[][] { { 0.01 }, { 0.01 / ratio } }, new double[0], correlation);
    final SuccessiveRootFinderG2ppCalibrationObjective objective = new SuccessiveRootFinderG2ppCalibrationObjective(g2Parameters, EUR,
        ratio);
    final SuccessiveRootFinderG2ppCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderG2ppCalibrationEngine<>(
        objective);

    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[i], PVSSC);
    }
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final MultipleCurrencyAmount[] pvSabr = new MultipleCurrencyAmount[EXPIRY_TENOR.length];
    final MultipleCurrencyAmount[] pvHw = new MultipleCurrencyAmount[EXPIRY_TENOR.length];
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      pvSabr[i] = SWAPTION_LONG_PAYER[i].accept(PVSSC, SABR_MULTICURVES);
      pvHw[i] = METHOD_G2PP.presentValue(SWAPTION_LONG_PAYER[i], objective.getG2Provider());
      assertEquals("G2++ calibration: swaption " + i, pvSabr[i].getAmount(EUR), pvHw[i].getAmount(EUR), TOLERANCE_PV);
    }
  }

  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  @Test(enabled = false)
  public void performance() {
    final double[] meanReversion = new double[] { 0.01, 0.30 };
    final double ratio = 4.0;
    final double correlation = -0.50;
    long startTime, endTime;
    final int nbTest = 100;
    final MultipleCurrencyAmount[] pv = new MultipleCurrencyAmount[nbTest];

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      final G2ppPiecewiseConstantParameters g2Parameters = new G2ppPiecewiseConstantParameters(meanReversion,
          new double[][] { { 0.01 }, { 0.01 / ratio } }, new double[0], correlation);
      final SuccessiveRootFinderG2ppCalibrationObjective objective = new SuccessiveRootFinderG2ppCalibrationObjective(g2Parameters, EUR,
          ratio);
      final SuccessiveRootFinderG2ppCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderG2ppCalibrationEngine<>(
          objective);
      for (int j = 0; j < EXPIRY_TENOR.length; j++) {
        calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[j], PVSSC);
      }
      calibrationEngine.calibrate(SABR_MULTICURVES);
      pv[i] = METHOD_G2PP.presentValue(SWAPTION_LONG_PAYER[EXPIRY_TENOR.length - 1], objective.getG2Provider());
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " G2++ calibration to swaption (5 swaptions) + price: " + (endTime - startTime) + " ms");
    // Performance note: calibration: 12-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 550 ms for 100 calibration with 5 swaptions.
    // TODO: Why is the time 4x the one with "CurveBundle"?
  }

}
