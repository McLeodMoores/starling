/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.DiscountingMethodCurveBuilder2;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.ExponentialExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LogLinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 * Two curves in EUR; no futures; EONIA curve with ECB meeting dates.
 */
@Test(groups = TestGroup.UNIT)
public class EurDiscounting6mLiborWithCommitteeMeeting1Test {

  private static final Interpolator1D LINEAR_INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);
  private static final Interpolator1D LOG_LINEAR_INTERPOLATOR = NamedInterpolator1dFactory.of(LogLinearInterpolator1dAdapter.NAME,
      ExponentialExtrapolator1dAdapter.NAME); // Log-linear on the discount factor = step on the instantaneous rates

  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  /** An EONIA index */
  private static final IndexON EONIA_INDEX = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  /** A 6M EURIBOR index */
  private static final IborIndex EURIBOR_6M_INDEX = EUR1YEURIBOR6M.getIborIndex();
  /** A 6M EUROLIBOR index */
  private static final IborIndex EUROLIBOR_6M_INDEX = new IborIndex(Currency.EUR, Period.ofMonths(6), 2, EURIBOR_6M_INDEX.getDayCount(), EURIBOR_6M_INDEX.getBusinessDayConvention(), true, "EUROLIBOR6M");
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR_6M_INDEX, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EURIBOR_6M_INDEX, TARGET);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 2, 4);
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  // Curve building date selected such that ECB dates are in the same OIS month: 7-Mar and 4-Apr
  private static final ZonedDateTime[] MEETING_ECB_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 7), DateUtils.getUTCDate(2013, 4, 4), DateUtils.getUTCDate(2013, 5, 2),
    DateUtils.getUTCDate(2013, 6, 6), DateUtils.getUTCDate(2013, 7, 4), DateUtils.getUTCDate(2013, 8, 1), DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 10, 2),
    DateUtils.getUTCDate(2013, 11, 7), DateUtils.getUTCDate(2013, 12, 5), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 2, 6) };
  private static final double[] MEETING_ECB_TIME = new double[MEETING_ECB_DATE.length];
  static {
    for (int i = 0; i < MEETING_ECB_DATE.length; i++) {
      MEETING_ECB_TIME[i] = TimeCalculator.getTimeBetween(NOW, MEETING_ECB_DATE[i]);
    }
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.07 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUROLIBOR_6M_INDEX, TS_IBOR_EUR6M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EURIBOR_6M_INDEX, TS_IBOR_EUR6M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUROLIBOR_6M_INDEX, TS_IBOR_EUR6M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EURIBOR_6M_INDEX, TS_IBOR_EUR6M_WITH_TODAY);
  }
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";

  /** Market values for the dsc EUR curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0060, 0.0050, 0.0055, 0.0070, 0.0080, 0.0075, 0.0070, 0.0075, 0.0080, 0.0075, 0.0080, 0.0075 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5),
    Period.ofMonths(6), Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10), Period.ofMonths(11), Period.ofYears(1) };
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR = new GeneratorAttributeIR[DSC_EUR_TENOR.length];
  static {
    for (int i = 0; i < DSC_EUR_TENOR.length; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeIR(DSC_EUR_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0100, 0.0150, 0.0175, 0.0175, 0.0200, 0.00175, 0.0200, 0.00175 };
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR6M, GENERATOR_FRA_6M, GENERATOR_FRA_6M, EUR1YEURIBOR6M,
    EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD6_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD6_EUR_ATTR = new GeneratorAttributeIR[FWD6_EUR_TENOR.length];
  static {
    for (int i = 0; i < FWD6_EUR_TENOR.length; i++) {
      FWD6_EUR_ATTR[i] = new GeneratorAttributeIR(FWD6_EUR_TENOR[i]);
    }
  }
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final DiscountingMethodCurveBuilder2.ConfigBuilder2 BUILDER_FOR_TEST = DiscountingMethodCurveBuilder2.setUp()
      .buildingFirst(CURVE_NAME_DSC_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(LOG_LINEAR_INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EURIBOR_6M_INDEX, EUROLIBOR_6M_INDEX).withInterpolator(LINEAR_INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_EUR_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_EUR_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
    }
  }
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_TODAYS_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_TODAYS_FIXING;
  static {
    BEFORE_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW, MEETING_ECB_DATE);
    AFTER_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW, MEETING_ECB_DATE);
  }

  @Test
  public void testJacobianSizes() {
    final CurveBuildingBlockBundle fullJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_EUR).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_EUR_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_EUR_MARKET_QUOTES.length);
    final DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_EUR).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD6_EUR_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), FWD6_EUR_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES.length);
  }

  @Test
  public void testInstrumentsInCurvePriceToZero() {
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurvesBeforeFixing = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurvesAfterFixing = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_DSC_EUR),
        BEFORE_TODAYS_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_DSC_EUR),
        AFTER_TODAYS_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_FWD6_EUR),
        BEFORE_TODAYS_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_FWD6_EUR),
        AFTER_TODAYS_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
  }

//  @Test
//  public void blockBundleDscFiniteDifferenceTest() {
//    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
//    final CurveBuildingBlockBundle fullInverseJacobian = BEFORE_TODAYS_FIXING.getSecond();
//    final double bump = 1e-6;
//    for (int i = 0; i < discountingCurveSize; i++) {
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
//          .buildCurves(NOW);
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
//          .buildCurves(NOW);
//      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
//      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
//      for (int j = 0; j < discountingCurveSize; j++) {
//        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
//        // note columns then rows tested
//        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_EUR).getSecond().getData()[j][i];
//        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_EUR + ": column=" + i + " row=" + j,
//            expectedSensitivity, dYielddQuote, bump);
//      }
//    }
//  }
//
//  @Test
//  public void blockBundleFwd3MFiniteDifferenceTest() {
//    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
//    final int liborCurveSize = FWD6_EUR_MARKET_QUOTES.length;
//    final CurveBuildingBlockBundle fullInverseJacobian = BEFORE_TODAYS_FIXING.getSecond();
//    final double bump = 1e-6;
//    for (int i = 0; i < discountingCurveSize; i++) {
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
//          .buildCurves(NOW);
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
//          .buildCurves(NOW);
//      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
//      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
//      for (int j = 0; j < liborCurveSize; j++) {
//        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
//        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][i];
//        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + i + " row=" + j,
//            expectedSensitivity, dYielddQuote, bump);
//      }
//    }
//    for (int i = 0; i < liborCurveSize; i++) {
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] + bump)
//          .buildCurves(NOW);
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] - bump)
//          .buildCurves(NOW);
//      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
//      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
//      final int offset = i + discountingCurveSize;
//      for (int j = 0; j < liborCurveSize; j++) {
//        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
//        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][offset];
//        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + offset + " row=" + j,
//            expectedSensitivity, dYielddQuote, bump);
//      }
//    }
//  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = BEFORE_TODAYS_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, EURIBOR_6M_INDEX.getSpotLag() + startIndex * jump, TARGET);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, EURIBOR_6M_INDEX, TARGET);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = EURIBOR_6M_INDEX.getDayCount().getDayCountFraction(startDate, endDate, TARGET);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(EURIBOR_6M_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, TARGET);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
