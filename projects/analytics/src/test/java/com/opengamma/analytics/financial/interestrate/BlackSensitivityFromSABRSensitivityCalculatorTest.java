/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertTrue;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitterTest;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Tests related to the sensitivity of swaptions to the Black volatility when SABR fitting and interpolation is used.
 */
@Test(groups = TestGroup.UNIT)
public class BlackSensitivityFromSABRSensitivityCalculatorTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(SABRModelFitterTest.class);
  private static final BitSet FIXED = new BitSet();
  static {
    FIXED.set(1);
  }
  private static final double ERROR = 0.0001;
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final DoubleMatrix1D SABR_INITIAL_VALUES = new DoubleMatrix1D(new double[] { 0.05, 0.50, 0.70, 0.30 });
  private static final Interpolator1D LINEAR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);
  private static final WorkingDayCalendar NYC = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);

  private static final Period[] EXPIRY_TENOR = new Period[] { Period.ofMonths(6), Period.ofYears(1), Period.ofYears(5) };
  private static final int NB_EXPIRY = EXPIRY_TENOR.length;
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXPIRY];
  private static final double[] EXPIRY_TIME = new double[NB_EXPIRY];
  private static final Period[] MATURITY_TENOR = new Period[] { Period.ofYears(1), Period.ofYears(2), Period.ofYears(5),
      Period.ofYears(10) };
  private static final double[] MATURITY_TIME = new double[] { 1.0, 2.0, 5.0, 10.0 };
  private static final int NB_MATURITY = MATURITY_TENOR.length;
  private static final double[] STRIKE_RELATIVE = new double[] { -0.0100, -0.0050, -0.0025, 0.0000, 0.0025, 0.0050, 0.0100 };
  private static final int NB_STRIKE = STRIKE_RELATIVE.length;
  private static final double[][][] VOLATILITIES_BLACK = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];
  static {
    VOLATILITIES_BLACK[0] = new double[][] { { 0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 }, { 0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 },
        { 0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        { 0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 6M
    VOLATILITIES_BLACK[1] = new double[][] { { 0.30, 0.27, 0.25, 0.24, 0.24, 0.25, 0.27 }, { 0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 },
        { 0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        { 0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 1Y
    VOLATILITIES_BLACK[2] = new double[][] { { 0.33, 0.29, 0.27, 0.25, 0.24, 0.24, 0.24 }, { 0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 },
        { 0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        { 0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 5Y
    for (int i = 0; i < NB_EXPIRY; i++) {
      EXPIRY_DATE[i] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR[i], USD6MLIBOR3M.getIborIndex(),
          NYC);
      EXPIRY_TIME[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[i]);
    }
  }

  private static final double NOTIONAL = 1000000;
  private static final Period EXPIRY_1_SWPT = Period.ofYears(2);
  private static final ZonedDateTime EXPIRY_1_SWPT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_1_SWPT,
      USD6MLIBOR3M.getIborIndex(), NYC);
  private static final ZonedDateTime SETTLE_1_SWPT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_1_SWPT_DATE, USD6MLIBOR3M.getSpotLag(),
      NYC);
  private static final Period MATURITY_1_SWPT = Period.ofYears(6);
  private static final double STRIKE_1 = 0.0250;
  private static final SwapFixedIborDefinition SWAP_1_DEFINITION = SwapFixedIborDefinition.from(SETTLE_1_SWPT_DATE, MATURITY_1_SWPT,
      USD6MLIBOR3M, NOTIONAL, STRIKE_1, true);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_1_DEFINITION = SwaptionPhysicalFixedIborDefinition
      .from(EXPIRY_1_SWPT_DATE, SWAP_1_DEFINITION, true, true);

  private static final Period EXPIRY_2_SWPT = Period.ofMonths(9);
  private static final ZonedDateTime EXPIRY_2_SWPT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_2_SWPT,
      USD6MLIBOR3M.getIborIndex(), NYC);
  private static final ZonedDateTime SETTLE_2_SWPT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_1_SWPT_DATE, USD6MLIBOR3M.getSpotLag(),
      NYC);
  private static final Period MATURITY_2_SWPT = Period.ofYears(4);
  private static final double STRIKE_2 = 0.0300;
  private static final SwapFixedIborDefinition SWAP_2_DEFINITION = SwapFixedIborDefinition.from(SETTLE_2_SWPT_DATE, MATURITY_2_SWPT,
      USD6MLIBOR3M, NOTIONAL, STRIKE_2, true);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_2_DEFINITION = SwaptionPhysicalFixedIborDefinition
      .from(EXPIRY_2_SWPT_DATE, SWAP_2_DEFINITION, true, true);

  private static final SABRSwaptionProviderDiscount DATA = new SABRSwaptionProviderDiscount(
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd(), SABRDataSets.createSABR2(), USD6MLIBOR3M);

  private static final int NB_INS = 2;
  private static final SwaptionPhysicalFixedIbor[] INSTRUMENTS = new SwaptionPhysicalFixedIbor[NB_INS];
  static {
    INSTRUMENTS[0] = SWAPTION_1_DEFINITION.toDerivative(REFERENCE_DATE);
    INSTRUMENTS[1] = SWAPTION_2_DEFINITION.toDerivative(REFERENCE_DATE);
  }

  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWAPTION_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRSwaptionCalculator
      .getInstance();

  private ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> calibration(final double[][][] volBlack) {
    final double[] expiryTimeVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] maturityTimeVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] alphaVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] betaVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] rhoVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] nuVector = new double[NB_EXPIRY * NB_MATURITY];
    int ii = 0;
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = new HashMap<>();
    for (int i = 0; i < NB_EXPIRY; i++) {
      final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE[i], USD6MLIBOR3M.getSpotLag(), NYC);
      for (int j = 0; j < NB_MATURITY; j++) {
        final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate, MATURITY_TENOR[j], USD6MLIBOR3M, 1.0,
            0.0, true); // used to compute atm
        final SwapFixedCoupon<Coupon> swap = swapDefinition.toDerivative(REFERENCE_DATE);
        final double atm = swap.accept(PRC, DATA.getMulticurveProvider());
        final double[] strikeAbs = new double[NB_STRIKE];
        final double[] errors = new double[NB_STRIKE];
        for (int k = 0; k < NB_STRIKE; k++) {
          strikeAbs[k] = atm + STRIKE_RELATIVE[k];
          errors[k] = ERROR;
        }
        final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(atm, strikeAbs, EXPIRY_TIME[i],
            volBlack[i][j], errors, SABR_FUNCTION).solve(
                SABR_INITIAL_VALUES, FIXED);
        inverseJacobianMap.put(DoublesPair.of(EXPIRY_TIME[i], MATURITY_TIME[j]),
            fittedResult.getModelParameterSensitivityToData());
        expiryTimeVector[ii] = EXPIRY_TIME[i];
        maturityTimeVector[ii] = MATURITY_TIME[j];
        alphaVector[ii] = fittedResult.getModelParameters().getEntry(0);
        betaVector[ii] = fittedResult.getModelParameters().getEntry(1);
        rhoVector[ii] = fittedResult.getModelParameters().getEntry(2);
        nuVector[ii] = fittedResult.getModelParameters().getEntry(3);
        ii++;
      }
    }
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, alphaVector,
        INTERPOLATOR, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, betaVector,
        INTERPOLATOR, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, nuVector,
        INTERPOLATOR, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, rhoVector,
        INTERPOLATOR, "SABR rho surface");
    final SABRInterestRateParameters sabrParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface,
        SABR_FUNCTION);
    return ObjectsPair.of(sabrParameters, inverseJacobianMap);
  }

  /**
   *
   */
  @Test
  public void blackNodeSensitivity() {
    final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> result = calibration(VOLATILITIES_BLACK);
    final SABRInterestRateParameters sabrParameters = result.getFirst();
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = result.getSecond();
    final SABRSwaptionProviderDiscount sabrBundle = new SABRSwaptionProviderDiscount(DATA.getMulticurveProvider(), sabrParameters,
        USD6MLIBOR3M);

    final double bump = 0.001;
    final double[][][] sensiBlackFD = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];

    final double[] pv = new double[NB_INS];

    final OperationTimer timer = new OperationTimer(LOGGER,
        "Calibrating {}x{}x{}x{} SABR and computing Black sensitivities for {} instruments", NB_INS, NB_EXPIRY, NB_MATURITY, NB_STRIKE,
        NB_INS);
    for (int i = 0; i < NB_INS; i++) {
      pv[i] = METHOD_SWAPTION_SABR.presentValue(INSTRUMENTS[i], sabrBundle).getAmount(Currency.USD);
      final PresentValueSABRSensitivityDataBundle sensiPoint = INSTRUMENTS[i].accept(PVSSC_SABR, sabrBundle);
      final PresentValueSABRSensitivityDataBundle sensiNode = SABRSensitivityNodeCalculator.calculateNodeSensitivities(sensiPoint,
          sabrParameters);
      final Map<DoublesPair, DoubleMatrix1D> sensiBlack = BlackSensitivityFromSABRSensitivityCalculator.blackSensitivity(sensiNode,
          inverseJacobianMap);
      for (int j = 0; j < NB_EXPIRY; j++) {
        for (int k = 0; k < NB_MATURITY; k++) {
          for (int l = 0; l < NB_STRIKE; l++) {
            final double[][][] bumpedVol = bump(j, k, l, bump);
            final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> bumpedResult = calibration(bumpedVol);
            final SABRSwaptionProviderDiscount bumpedSabrBundle = new SABRSwaptionProviderDiscount(DATA.getMulticurveProvider(),
                bumpedResult.getFirst(), USD6MLIBOR3M);
            final double bumpedPv = METHOD_SWAPTION_SABR.presentValue(INSTRUMENTS[i], bumpedSabrBundle).getAmount(Currency.USD);
            sensiBlackFD[j][k][l] = (bumpedPv - pv[i]) / bump;
            final double sensiCalc = sensiBlack.get(DoublesPair.of(EXPIRY_TIME[j], MATURITY_TIME[k])).getEntry(l);
            if (i != 0 && j != 1 && k != 2 && l != 0) { // The first point has a larger error wrt FD.
              assertTrue("Black Node Sensitivity: FD [" + j + ", " + k + ", " + l + "]",
                  Math.abs(sensiBlackFD[j][k][l] - sensiCalc) < 25.0
                      || Math.abs(sensiBlackFD[j][k][l] / sensiCalc - 1) < 0.05);
            }
          } // end strike
        } // end maturity
      } // end expiry
    } // end n
    timer.finished();
  }

  private double[][][] bump(final int exp, final int mat, final int str, final double bump) {
    final double[][][] vol = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];
    for (int i = 0; i < NB_EXPIRY; i++) {
      for (int j = 0; j < NB_MATURITY; j++) {
        for (int k = 0; k < NB_STRIKE; k++) {
          vol[i][j][k] = VOLATILITIES_BLACK[i][j][k];
        }
      }
    }
    vol[exp][mat][str] += bump;
    return vol;
  }

  /**
   * Analyzes the smoothness of the Black sensitivities to change in strike.
   */
  @Test(enabled = false)
  public void analysisSensitivitySmoothness() {

    final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> result = calibration(VOLATILITIES_BLACK);
    final SABRInterestRateParameters sabrParameters = result.getFirst();
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = result.getSecond();
    final SABRSwaptionProviderDiscount sabrBundle = new SABRSwaptionProviderDiscount(DATA.getMulticurveProvider(), sabrParameters,
        USD6MLIBOR3M);

    final double strikeRange = 0.0600;
    final double strikeStart = 0.0050;
    final int nbStrikeSwapt = 100;
    final double[] strikes = new double[nbStrikeSwapt + 1];
    final SwaptionPhysicalFixedIbor[] swaptions = new SwaptionPhysicalFixedIbor[nbStrikeSwapt + 1];
    final double[] pv = new double[nbStrikeSwapt + 1];
    final double[][][][] blackSensi1 = new double[2][2][NB_STRIKE][nbStrikeSwapt + 1];
    for (int i = 0; i <= nbStrikeSwapt; i++) {
      strikes[i] = strikeStart + i * strikeRange / nbStrikeSwapt;
      final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLE_1_SWPT_DATE, MATURITY_1_SWPT, USD6MLIBOR3M,
          NOTIONAL, strikes[i], true);
      final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_SWPT_DATE,
          swapDefinition, true, true);
      swaptions[i] = swaptionDefinition.toDerivative(REFERENCE_DATE);
      pv[i] = METHOD_SWAPTION_SABR.presentValue(swaptions[i], sabrBundle).getAmount(Currency.USD);
      final PresentValueSABRSensitivityDataBundle sensiPoint = swaptions[i].accept(PVSSC_SABR, sabrBundle);
      final PresentValueSABRSensitivityDataBundle sensiNode = SABRSensitivityNodeCalculator.calculateNodeSensitivities(sensiPoint,
          sabrParameters);
      final Map<DoublesPair, DoubleMatrix1D> sensiBlack = BlackSensitivityFromSABRSensitivityCalculator.blackSensitivity(sensiNode,
          inverseJacobianMap);
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          for (int l = 0; l < NB_STRIKE; l++) {
            blackSensi1[j][k][l][i] = sensiBlack
                .get(DoublesPair.of(EXPIRY_TIME[j + 1], MATURITY_TIME[k + 2])).getEntry(l);
          }
        }
      }
    }
    @SuppressWarnings("unused")
    final double atm = swaptions[0].getUnderlyingSwap().accept(PRC, sabrBundle.getMulticurveProvider());
  }

}
