/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
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
 * Builds and tests a EUR discounting and 6m EURIBOR curves. The discounting curve has nodes that are shifted
 * to ECB meeting dates. The discounting curve is constructed first, then the EURIBOR curve.
 */
@Test(groups = TestGroup.UNIT)
public class EurDiscounting6mLiborWithCommitteeMeeting1Test extends CurveBuildingTests {
  /** The interpolator used for the EURIBOR curve */
  private static final Interpolator1D LINEAR_INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);
  /** The interpolator used for the discounting curve */
  private static final Interpolator1D LOG_LINEAR_INTERPOLATOR = NamedInterpolator1dFactory.of(LogLinearInterpolator1dAdapter.NAME,
      ExponentialExtrapolator1dAdapter.NAME); // Log-linear on the discount factor = step on the instantaneous rates
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);
  /** Generates OIS for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  /** An EONIA index */
  private static final IndexON EONIA_INDEX = GENERATOR_OIS_EUR.getIndex();
  /** Generates 6m / 1Y swaps for the EURIBOR curve */
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", TARGET);
  /** A 6M EURIBOR index */
  private static final IborIndex EURIBOR_6M_INDEX = EUR1YEURIBOR6M.getIborIndex();
  /** Generates 6m FRAs for the EURIBOR curve */
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR_6M_INDEX, TARGET);
  /** Generates the 6m EURIBOR index */
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EURIBOR_6M_INDEX, TARGET);
  /** The curve construction date, selected such that ECB dates are in the same OIS month: 7-Mar and 4-Apr */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 2, 4);
  /** The previous week date */
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  /** ECB meeting dates */
  private static final ZonedDateTime[] MEETING_ECB_DATE = new ZonedDateTime[] {
      DateUtils.getUTCDate(2013, 3, 7), DateUtils.getUTCDate(2013, 4, 4), DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 6, 6),
      DateUtils.getUTCDate(2013, 7, 4), DateUtils.getUTCDate(2013, 8, 1), DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 10, 2),
      DateUtils.getUTCDate(2013, 11, 7), DateUtils.getUTCDate(2013, 12, 5), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 2, 6)};
  /** Overnight rate fixing time series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.07, 0.08 });
  /** Overnight rate fixing time series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.07 });
  /** EURIBOR fixing time series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_EURIBOR_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  /** EURIBOR fixing time series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_EURIBOR_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EURIBOR_6M_INDEX, TS_EURIBOR_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EURIBOR_6M_INDEX, TS_EURIBOR_WITH_TODAY);
  }
  /** The discounting curve name */
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  /** The EURIBOR curve name */
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";
  /** Market values for the dsc EUR curve */
  private static final double[] DSC_EUR_MARKET_QUOTES =
      new double[] {0.0060, 0.0050, 0.0055, 0.0070, 0.0080, 0.0075, 0.0070, 0.0075, 0.0080, 0.0075, 0.0080, 0.0075 };
  /** Vanilla instrument generators for the discounting curve  */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Attribute generators for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5),
        Period.ofMonths(6), Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10), Period.ofMonths(11), Period.ofYears(1) };
    DSC_EUR_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0100, 0.0150, 0.0175, 0.0175, 0.0200, 0.00175, 0.0200, 0.00175 };
  /** Vanilla instrument generators for the EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR6M, GENERATOR_FRA_6M, GENERATOR_FRA_6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Attribute generators for the EURIBOR curve */
  private static final GeneratorAttributeIR[] FWD6_EUR_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12), Period.ofYears(2), Period.ofYears(3),
        Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD6_EUR_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD6_EUR_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Already known data - contains only the FX matrix */
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** The curve builder */
  private static final DiscountingMethodCurveSetUp BUILDER_FOR_TEST = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(LOG_LINEAR_INTERPOLATOR)
            .usingNodeDates(MEETING_ECB_DATE)
      .thenBuilding(CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EURIBOR_6M_INDEX).withInterpolator(LINEAR_INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_EUR_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_EUR_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
    }
  }
  /** Curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_TODAYS_FIXING;
  /** Curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_TODAYS_FIXING;
  static {
    BEFORE_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
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

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    Map<String, InstrumentDefinition<?>[]> definitions = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), BEFORE_TODAYS_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), BEFORE_TODAYS_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), AFTER_TODAYS_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), AFTER_TODAYS_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    // before today's fixing
    // discounting sensitivities to discounting
    assertFiniteDifferenceSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_FOR_TEST, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // discounting sensitivities to 6m EURIBOR should not have been calculated
    assertNoSensitivities(BEFORE_TODAYS_FIXING.getSecond(), CURVE_NAME_DSC_EUR, CURVE_NAME_FWD6_EUR);
    // EURIBOR sensitivities to discounting
    assertFiniteDifferenceSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_FOR_TEST, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // EURIBOR sensitivities to 6m EURIBOR
    assertFiniteDifferenceSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_FOR_TEST, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_FWD6_EUR, NOW, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR, FWD6_EUR_MARKET_QUOTES, false);
    // after today's fixing
    // discounting sensitivities to discounting
    assertFiniteDifferenceSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_FOR_TEST, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // discounting sensitivities to 6m EURIBOR should not have been calculated
    assertNoSensitivities(AFTER_TODAYS_FIXING.getSecond(), CURVE_NAME_DSC_EUR, CURVE_NAME_FWD6_EUR);
    // EURIBOR sensitivities to discounting
    assertFiniteDifferenceSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_FOR_TEST, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // EURIBOR sensitivities to 6m EURIBOR
    assertFiniteDifferenceSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_FOR_TEST, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_FWD6_EUR, NOW, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR, FWD6_EUR_MARKET_QUOTES, false);
  }

  /**
   * Only one set of curves is constructed, so no tests are possible.
   */
  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    return;
  }

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
