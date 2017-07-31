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

import com.mcleodmoores.analytics.financial.curve.interestrate.CurveBuilder;
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
 * Builds and tests USD discounting and 3m LIBOR curves. Curves are constructed in three ways:
 * <ul>
 *  <li> Discounting only;
 *  <li> Discounting, then LIBOR;
 *  <li> Discounting and LIBOR simultaneously.
 * </ul>
 * In the second case, the discounting curve only has sensitivities to the discounting curve market data, while the LIBOR curve
 * has sensitivities to both the discounting and LIBOR market data. In the third case, both curves have sensitivities to the
 * discounting and LIBOR market data, although the discounting curve should have zero sensitivities to the LIBOR data. The
 * discounting and LIBOR curves (where constructed) should be equal in all cases.
 * <p>
 * The discounting curve contains the overnight deposit rate and OIS. The LIBOR curve contains the 3m LIBOR rate, 3m FRAs and
 * 3m LIBOR / 6m fixed swaps.
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscounting3mLibor2Test extends CurveBuildingTests {
  /** The interpolator used for both curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  /** Generates OIS for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON USD_OVERNIGHT_INDEX = GENERATOR_OIS_USD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD =
      new GeneratorDepositON("Overnight USD deposit", Currency.USD, NYC, USD_OVERNIGHT_INDEX.getDayCount());
  /** Generates the 3m LIBOR / 6m fixed swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  /** A 3m LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** Generates the FRAs */
  private static final GeneratorFRA GENERATOR_FRA = new GeneratorFRA("GENERATOR_FRA", USD_3M_LIBOR_INDEX, NYC);
  /** Generates the 3m LIBOR instrument */
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous week day */
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  /** Fixing time series of overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.07, 0.08});
  /** Fixing time series of overnight rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.07});
  /** Fixing time series of 3m LIBOR rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.0035, 0.0036});
  /** Fixing time series of 3m LIBOR rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.0035});
  /** Fixing time series before today's fixing */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after today's fixing */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITH_TODAY);
  }
  /** The name of the discounting curve */
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  /** The name of the LIBOR curve */
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  /** Market values for the discounting curve */
  private static final double[] DSC_USD_MARKET_QUOTES =
      new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Vanilla instrument generators for the discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Attribute generators for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] discountingTenors =
        new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
      Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[discountingTenors.length];
    for (int i = 0; i < discountingTenors.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(discountingTenors[i]);
    }
  }
  /** Market values for the 3m LIBOR curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Vanilla instrument generators for the 3m LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_FRA, GENERATOR_FRA, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Attribute generators for the 3m LIBOR curve */
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR;
  static {
    final Period[] liborTenors =
        new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5),
        Period.ofYears(7), Period.ofYears(10) };
    FWD3_USD_ATTR = new GeneratorAttributeIR[liborTenors.length];
    for (int i = 0; i < liborTenors.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(liborTenors[i]);
    }
  }
  /** Already known curve data - contains only an empty FX matrix */
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builder that constructs single discounting curve */
  private static final DiscountingMethodCurveSetUp DISCOUNTING_ONLY_BUILDER = DiscountingMethodCurveBuilder.setUp()
        .building(CURVE_NAME_DSC_USD)
        .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(USD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
        .withKnownData(KNOWN_DATA);
  /** Builder that constructs the discounting curve, then the LIBOR curve */
  private static final DiscountingMethodCurveSetUp DISCOUNTING_THEN_LIBOR_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(USD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Builder that the constructs the discounting and LIBOR curve simultaneously */
  private static final DiscountingMethodCurveSetUp DISCOUNTING_AND_LIBOR_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(USD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  // initialize the curve builders with market data
  static {
    for (int i = 0; i < DSC_USD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_ONLY_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      DISCOUNTING_THEN_LIBOR_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_LIBOR_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_USD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_LIBOR_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_LIBOR_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
    }
  }
  /** Simultaneous discounting and LIBOR curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING;
  /** Discounting then LIBOR curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_BEFORE_FIXING;
  /** Discounting curve constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BEFORE_FIXING;
  /** Simultaneous discounting and LIBOR curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING;
  /** Discounting then LIBOR curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_AFTER_FIXING;
  /** Discounting curve constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_AFTER_FIXING;
  /** Calculation tolerance */
  private static final double EPS = 1.0E-9;

  static {
    DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_LIBOR_BEFORE_FIXING = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_LIBOR_AFTER_FIXING = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DSC_BEFORE_FIXING = DISCOUNTING_ONLY_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_AFTER_FIXING = DISCOUNTING_ONLY_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    // discounting then LIBOR
    CurveBuildingBlockBundle fullJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
    DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // discounting and LIBOR
    fullJacobian = DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // discounting only
    fullJacobian = DSC_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 1);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    // discounting then LIBOR
    Map<String, InstrumentDefinition<?>[]> definitions;
    // before fixing
    definitions = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    // after fixing
    definitions = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_THEN_LIBOR_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), DSC_THEN_LIBOR_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
    // discounting and LIBOR
    // before fixing
    definitions = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX,
        NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX,
        NOW, Currency.USD);
    // after fixing
    definitions = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX,
        NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX,
        NOW, Currency.USD);
    // discounting only
    definitions = DISCOUNTING_ONLY_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), DSC_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testDiscountingCurveSensitivities1(DSC_THEN_LIBOR_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_THEN_LIBOR_BUILDER);
    testDiscountingCurveSensitivities1(DSC_THEN_LIBOR_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_THEN_LIBOR_BUILDER);
    testDiscountingCurveSensitivities1(DSC_THEN_LIBOR_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_ONLY_BUILDER);
    testDiscountingCurveSensitivities1(DSC_THEN_LIBOR_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_ONLY_BUILDER);
    testDiscountingCurveSensitivities2(DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities2(DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    testLiborCurveSensitivities(DSC_THEN_LIBOR_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_THEN_LIBOR_BUILDER);
    testLiborCurveSensitivities(DSC_THEN_LIBOR_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_THEN_LIBOR_BUILDER);
    testLiborCurveSensitivities(DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_AND_LIBOR_BUILDER);
    testLiborCurveSensitivities(DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_AND_LIBOR_BUILDER);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve has no sensitivity to the LIBOR curve.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the curve builder
   */
  private static void testDiscountingCurveSensitivities1(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve is constructed before the LIBOR curve. Sensitivities to
   * LIBOR market data are calculated, but they should be equal to zero.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   */
  private static void testDiscountingCurveSensitivities2(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_AND_LIBOR_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_AND_LIBOR_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the LIBOR curve to changes in the discounting and LIBOR curve market data.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the curve builder
   */
  private static void testLiborCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // discounting curves
    YieldAndDiscountCurve curveBefore1 = DSC_THEN_LIBOR_BEFORE_FIXING.getFirst().getCurve(Currency.USD);
    YieldAndDiscountCurve curveBefore2 = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(Currency.USD);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    YieldAndDiscountCurve curveAfter1 = DSC_THEN_LIBOR_AFTER_FIXING.getFirst().getCurve(Currency.USD);
    YieldAndDiscountCurve curveAfter2 = DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(Currency.USD);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // LIBOR curves
    curveBefore1 = DSC_THEN_LIBOR_BEFORE_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    curveBefore2 = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = DSC_THEN_LIBOR_AFTER_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    curveAfter2 = DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // discounting sensitivities are not the same, but the LIBOR matrices should be the same for both construction methods
    final DoubleMatrix2D matrixBefore1 = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    final DoubleMatrix2D matrixBefore2 = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    assertMatrixEquals(matrixBefore1, matrixBefore2, EPS);
    final DoubleMatrix2D matrixAfter1 = DSC_THEN_LIBOR_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    final DoubleMatrix2D matrixAfter2 = DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
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
    CurveBuilder<MulticurveProviderDiscount> builder = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 430 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 440 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_ONLY_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 curve: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 curve: 20-May-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 125 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_THEN_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_AND_LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 402 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_ONLY_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 curve: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 curve: 20-May-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 125 ms for 100 sets.
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = DSC_THEN_LIBOR_BEFORE_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USD_3M_LIBOR_INDEX.getSpotLag() + startIndex * jump, NYC);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USD_3M_LIBOR_INDEX, NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = USD_3M_LIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(USD_3M_LIBOR_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, NYC);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
