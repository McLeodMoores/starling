/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertMatrixEquals;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertYieldCurvesEqual;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

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
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
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
 * Builds and tests EUR discounting and 3m and 6m EURIBOR curves. Curves are constructed in two ways:
 * <ul>
 *  <li> Discounting, then 3m EURIBOR, then 6m EURIBOR curves;
 *  <li> Discounting and the two EURIBOR curves simultaneously.
 * </ul>
 * In the first case, the discounting curve only has sensitivities to the market data used in its construction. In the second case, each curve
 * does have sensitivities to the other curves. The sensitivities of the discounting curve to the EURIBOR curves are
 * zero, and the sensitivities of the 3m and 6m EURIBOR curve to the market data in the other curve are zero.
 * <p>
 * The discounting curve contains the EONIA rate and OIS. The 3m EURIBOR curve contains the 3m EURIBOR rate, 3m FRAs
 * and 3m floating / 1Y fixed swaps. The 6m EURIBOR curve contains the 6m EURIBOR rate, 6m FRAs and 6m floating / 1Y fixed swaps.
 */
@Test(groups = TestGroup.UNIT)
public class EurDiscounting3mLibor6mLiborTest extends CurveBuildingTests {
  /** The interpolator used for all curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);
  /** Generates OIS for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  /** EONIA */
  private static final IndexON EONIA_INDEX = GENERATOR_OIS_EUR.getIndex();
  /** Generates the EONIA deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", Currency.EUR, TARGET, EONIA_INDEX.getDayCount());
  /** Generates 3m EURIBOR / 1Y fixed swaps */
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  /** Generates 6m EURIBOR / 1Y fixed swaps */
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", TARGET);
  /** A 3m EURIBOR index */
  private static final IborIndex EUR_3M_EURIBOR_INDEX = EUR1YEURIBOR3M.getIborIndex();
  /** A 6m EURIBOR index */
  private static final IborIndex EUR_6M_EURIBOR_INDEX = EUR1YEURIBOR6M.getIborIndex();
  /** Generates 3m FRAs */
  private static final GeneratorFRA GENERATOR_FRA_3M = new GeneratorFRA("GENERATOR_FRA_3M", EUR_3M_EURIBOR_INDEX, TARGET);
  /** Generates 6m FRAs */
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EUR_6M_EURIBOR_INDEX, TARGET);
  /** Generates the 3m EURIBOR index */
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EUR_3M_EURIBOR_INDEX, TARGET);
  /** Generates the 6m EURIBOR index */
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EUR_6M_EURIBOR_INDEX, TARGET);
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous week day */
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  /** Fixing time series of overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.07, 0.08});
  /** Fixing time series of overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.07});
  /** Fixing time series of 3m EURIBOR rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.0035, 0.0036});
  /** Fixing time series of 3m EURIBOR rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.0035});
  /** Fixing time series of 6m EURIBOR rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.0036, 0.0037 });
  /** Fixing time series of 6m EURIBOR rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {NOW}, new double[] {0.0036 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_6M_EURIBOR_INDEX, TS_IBOR_EUR6M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_6M_EURIBOR_INDEX, TS_IBOR_EUR6M_WITH_TODAY);
  }
  /** The discounting curve name */
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  /** The 3m EURIBOR curve name */
  private static final String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
  /** The 6m EURIBOR curve name */
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";
  /** Market values for the discounting curve */
  private static final double[] DSC_EUR_MARKET_QUOTES =
      new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Vanilla instrument generators for the discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Attributes for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
        Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4),
        Period.ofYears(5), Period.ofYears(10) };
    DSC_EUR_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Market values for the 3m EURIBOR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Vanilla instrument generators for the 3m EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR3M, GENERATOR_FRA_3M, GENERATOR_FRA_3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Attributes for the 3m EURIBOR curve */
  private static final GeneratorAttributeIR[] FWD3_EUR_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
        Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD3_EUR_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD3_EUR_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Market values for the 6m EURIBOR curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0440, 0.0440, 0.0440, 0.0445, 0.0485, 0.0555, 0.0580, 0.0610 };
  /** Generators for the 6m EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR6M, GENERATOR_FRA_6M, GENERATOR_FRA_6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Attributes for the 6m EURIBOR curve */
  private static final GeneratorAttributeIR[] FWD6_EUR_ATTR;
  static {
    final Period[] periods = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD6_EUR_ATTR = new GeneratorAttributeIR[periods.length];
    for (int i = 0; i < periods.length; i++) {
      FWD6_EUR_ATTR[i] = new GeneratorAttributeIR(periods[i]);
    }
  }
  /** Already known market data - contains only an empty FX matrix */
  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builder that constructs the curves one at a time */
  private static final DiscountingMethodCurveSetUp CONSECUTIVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_EUR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EUR_6M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  /** Builder that constructs the curves simultaneously */
  private static final DiscountingMethodCurveSetUp SIMULTANEOUS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR, CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EUR_6M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_EUR_MARKET_QUOTES.length; i++) {
      CONSECUTIVE_BUILDER.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
      SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_EUR_MARKET_QUOTES.length; i++) {
      CONSECUTIVE_BUILDER.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i]);
      SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_EUR_MARKET_QUOTES.length; i++) {
      CONSECUTIVE_BUILDER.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
      SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
    }
  }
  /** Simultaneous curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> SIMULTANEOUS_BEFORE_FIXING;
  /** Consecutive curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> CONSECUTIVE_BEFORE_FIXING;
  /** Simultaneous curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> SIMULTANEOUS_AFTER_FIXING;
  /** Consecutive curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> CONSECUTIVE_AFTER_FIXING;
  /** Calculation tolerance */
  private static final double EPS = 1.0e-9;

  static {
    SIMULTANEOUS_BEFORE_FIXING = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    SIMULTANEOUS_AFTER_FIXING = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    CONSECUTIVE_BEFORE_FIXING = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    CONSECUTIVE_AFTER_FIXING = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    final int allQuotes = DSC_EUR_MARKET_QUOTES.length + FWD3_EUR_MARKET_QUOTES.length + FWD6_EUR_MARKET_QUOTES.length;
    // discounting curve first, then 3m, then 6m
    CurveBuildingBlockBundle fullJacobian = CONSECUTIVE_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_EUR).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_EUR_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_EUR_MARKET_QUOTES.length);
    DoubleMatrix2D euribor3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_EUR).getSecond();
    assertEquals(euribor3mJacobianMatrix.getNumberOfRows(), FWD3_EUR_MARKET_QUOTES.length);
    assertEquals(euribor3mJacobianMatrix.getNumberOfColumns(), DSC_EUR_MARKET_QUOTES.length + FWD3_EUR_MARKET_QUOTES.length);
    DoubleMatrix2D euribor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_EUR).getSecond();
    assertEquals(euribor6mJacobianMatrix.getNumberOfRows(), FWD6_EUR_MARKET_QUOTES.length);
    assertEquals(euribor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
    // three curves fitted at the same time
    fullJacobian = SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_EUR).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_EUR_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), allQuotes);
    euribor3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_EUR).getSecond();
    assertEquals(euribor3mJacobianMatrix.getNumberOfRows(), FWD3_EUR_MARKET_QUOTES.length);
    assertEquals(euribor3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    euribor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_EUR).getSecond();
    assertEquals(euribor6mJacobianMatrix.getNumberOfRows(), FWD6_EUR_MARKET_QUOTES.length);
    assertEquals(euribor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    Map<String, InstrumentDefinition<?>[]> definitions;
    // discounting then 3m then 6m
    // before fixing
    definitions = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), CONSECUTIVE_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_EUR), CONSECUTIVE_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), CONSECUTIVE_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // after fixing
    definitions = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), CONSECUTIVE_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_EUR), CONSECUTIVE_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), CONSECUTIVE_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // discounting and euribors
    // before fixing
    definitions = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_EUR), SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // after fixing
    definitions = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_EUR), SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_EUR), SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_EUR), SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testDiscountingCurveSensitivities1(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, CONSECUTIVE_BUILDER);
    testDiscountingCurveSensitivities1(CONSECUTIVE_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, CONSECUTIVE_BUILDER);
    testDiscountingCurveSensitivities2(SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, SIMULTANEOUS_BUILDER);
    testDiscountingCurveSensitivities2(SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, SIMULTANEOUS_BUILDER);
    test3mEuriborCurveSensitivities1(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, CONSECUTIVE_BUILDER);
    test3mEuriborCurveSensitivities1(CONSECUTIVE_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, CONSECUTIVE_BUILDER);
    test3mEuriborCurveSensitivities2(SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, SIMULTANEOUS_BUILDER);
    test3mEuriborCurveSensitivities2(SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, SIMULTANEOUS_BUILDER);
    test6mEuriborCurveSensitivities(SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, SIMULTANEOUS_BUILDER);
    test6mEuriborCurveSensitivities(SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, SIMULTANEOUS_BUILDER);
    test6mEuriborCurveSensitivities(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, CONSECUTIVE_BUILDER);
    test6mEuriborCurveSensitivities(CONSECUTIVE_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, CONSECUTIVE_BUILDER);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * discounting curve. The sensitivities of the discounting curve to the EURIBOR curves are not calculated
   * when the curves are constructed consecutively.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the builder
   */
  private static void testDiscountingCurveSensitivities1(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // sensitivities to 3m EURIBOR should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR);
    // sensitivities to 6m EURIBOR should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_EUR, CURVE_NAME_FWD6_EUR);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * three curves. The sensitivities of the discounting curve to the EURIBOR curves should be zero.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the builder
   */
  private static void testDiscountingCurveSensitivities2(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // sensitivities to 3m EURIBOR should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR, FWD3_EUR_MARKET_QUOTES, true);
    // sensitivities to 6m EURIBOR should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD6_EUR, NOW, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR, FWD6_EUR_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the 3m EURIBOR curve to changes in the market data points used in the
   * discounting curve and 3m EURIBOR curve.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the builder
   */
  private static void test3mEuriborCurveSensitivities1(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // sensitivities to 3m EURIBOR
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR, FWD3_EUR_MARKET_QUOTES, false);
    // sensitivities to 6m EURIBOR should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_FWD3_EUR, CURVE_NAME_FWD6_EUR);
  }

  /**
   * Tests the sensitivities of the 3m EURIBOR curve to changes in the market data points used in the
   * three curves. The sensitivities of the 3m curve to the 6m curve should be zero.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the builder
   */
  private static void test3mEuriborCurveSensitivities2(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // sensitivities to 3m EURIBOR
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR, FWD3_EUR_MARKET_QUOTES, false);
    // sensitivities to 6m EURIBOR, should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD6_EUR, NOW, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR, FWD6_EUR_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the 6m EURIBOR curve to changes in the market data points used in the
   * discounting curve and EURIBOR curves.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the builder
   */
  private static void test6mEuriborCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // sensitivities to 3m EURIBOR, should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR, FWD3_EUR_MARKET_QUOTES, true);
    // sensitivities to 6m EURIBOR
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_EUR,
        CURVE_NAME_FWD6_EUR, NOW, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR, FWD6_EUR_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // discounting curves
    YieldAndDiscountCurve curveBefore1 = CONSECUTIVE_BEFORE_FIXING.getFirst().getCurve(Currency.EUR);
    YieldAndDiscountCurve curveBefore2 = SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(Currency.EUR);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    YieldAndDiscountCurve curveAfter1 = CONSECUTIVE_AFTER_FIXING.getFirst().getCurve(Currency.EUR);
    YieldAndDiscountCurve curveAfter2 = SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(Currency.EUR);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // 3m EURIBOR curves
    curveBefore1 = CONSECUTIVE_BEFORE_FIXING.getFirst().getCurve(EUR_3M_EURIBOR_INDEX);
    curveBefore2 = SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(EUR_3M_EURIBOR_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = CONSECUTIVE_AFTER_FIXING.getFirst().getCurve(EUR_3M_EURIBOR_INDEX);
    curveAfter2 = SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(EUR_3M_EURIBOR_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // 6m EURIBOR curves
    curveBefore1 = CONSECUTIVE_BEFORE_FIXING.getFirst().getCurve(EUR_6M_EURIBOR_INDEX);
    curveBefore2 = SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(EUR_6M_EURIBOR_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = CONSECUTIVE_AFTER_FIXING.getFirst().getCurve(EUR_6M_EURIBOR_INDEX);
    curveAfter2 = SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(EUR_6M_EURIBOR_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // discounting and 3m sensitivities are not the same, but the 6m matrices should be the same for both construction methods
    final DoubleMatrix2D matrixBefore1 = CONSECUTIVE_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_EUR).getSecond();
    final DoubleMatrix2D matrixBefore2 = SIMULTANEOUS_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_EUR).getSecond();
    assertMatrixEquals(matrixBefore1, matrixBefore2, EPS);
    final DoubleMatrix2D matrixAfter1 = CONSECUTIVE_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_EUR).getSecond();
    final DoubleMatrix2D matrixAfter2 = SIMULTANEOUS_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_EUR).getSecond();
    assertMatrixEquals(matrixAfter1, matrixAfter2, EPS);
  }

  /**
   * Performance test.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    DiscountingMethodCurveBuilder builder = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 3 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 3 units: 07-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 655 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 07-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 635 ms for 100 sets.

  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = CONSECUTIVE_BEFORE_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, EUR_3M_EURIBOR_INDEX.getSpotLag() + startIndex * jump, TARGET);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, EUR_3M_EURIBOR_INDEX, TARGET);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = EUR_3M_EURIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(EUR_3M_EURIBOR_INDEX, startTime[i], endTime, accrualFactor);
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
