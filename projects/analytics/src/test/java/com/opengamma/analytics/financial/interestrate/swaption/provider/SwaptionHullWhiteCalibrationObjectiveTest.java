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
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for Hull-White one factor calibration to European swaptions.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionHullWhiteCalibrationObjectiveTest {
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final WorkingDayCalendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M",
      CALENDAR);

  // Swaption description
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.EUR;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
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
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int[] EXPIRY_TENOR = new int[] { 1, 2, 3, 4, 5 };
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final ZonedDateTime[] SETTLEMENT_DATE = new ZonedDateTime[EXPIRY_TENOR.length];
  private static final SwapFixedIborDefinition[] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[EXPIRY_TENOR.length];
  private static final SwaptionPhysicalFixedIborDefinition[] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[EXPIRY_TENOR.length];
  static {
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      EXPIRY_DATE[i] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR[i]), BUSINESS_DAY,
          CALENDAR);
      SETTLEMENT_DATE[i] = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE[i], SETTLEMENT_DAYS, CALENDAR);
      SWAP_PAYER_DEFINITION[i] = SwapFixedIborDefinition.from(SETTLEMENT_DATE[i], CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER,
          CalendarAdapter.of(CALENDAR));
      SWAPTION_LONG_PAYER_DEFINITION[i] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[i],
          SWAP_PAYER_DEFINITION[i], true, IS_LONG);
    }
  }
  // to derivatives
  private static final MulticurveProviderDiscount CURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final SABRSwaptionProviderDiscount SABR_DATA = new SABRSwaptionProviderDiscount(CURVES, SABR_PARAMETER, EUR1YEURIBOR3M);
  private static final SwaptionPhysicalFixedIbor[] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[EXPIRY_TENOR.length];
  static {
    for (int loopexp = 0; loopexp < EXPIRY_TENOR.length; loopexp++) {
      SWAPTION_LONG_PAYER[loopexp] = SWAPTION_LONG_PAYER_DEFINITION[loopexp].toDerivative(REFERENCE_DATE);
    }
  }
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();

  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  @Test
  public void calibration() {
    final double meanReversion = 0.01;
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion,
        new double[] { 0.01 }, new double[0]);
    final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(hwParameters,
        CUR);
    final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(
        objective);
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[i], PresentValueSABRSwaptionCalculator.getInstance());
    }
    calibrationEngine.calibrate(SABR_DATA);
    final CurrencyAmount[] pvSabr = new CurrencyAmount[EXPIRY_TENOR.length];
    final CurrencyAmount[] pvHw = new CurrencyAmount[EXPIRY_TENOR.length];
    for (int i = 0; i < EXPIRY_TENOR.length; i++) {
      pvSabr[i] = METHOD_SABR.presentValue(SWAPTION_LONG_PAYER[i], SABR_DATA).getCurrencyAmount(CUR);
      pvHw[i] = METHOD_HW.presentValue(SWAPTION_LONG_PAYER[i], objective.getHwProvider()).getCurrencyAmount(CUR);
      assertEquals("Hull-White calibration: swaption " + i, pvSabr[i].getAmount(), pvHw[i].getAmount(), 1E-2);
    }
  }

  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  @Test(enabled = false)
  public void performance() {
    final double meanReversion = 0.01;
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion,
          new double[] { 0.01 }, new double[0]);
      final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(
          hwParameters,
          CUR);
      final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(
          objective);
      for (int i = 0; i < EXPIRY_TENOR.length; i++) {
        calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[i], PresentValueSABRSwaptionCalculator.getInstance());
      }
      calibrationEngine.calibrate(SABR_DATA);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " Hull-White calibration to swaption (5 swaptions): " + (endTime - startTime) + " ms");
    // Performance note: calibration: 31-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 410 ms for 100 calibration with 5 swaptions.

  }

}
