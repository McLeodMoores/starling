/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
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
 * Builds and tests discounting and 3m USD LIBOR curves. The discounting curve is used as an external input when
 * constructing the LIBOR curve.
 * <p>
 * The discounting curve contains the overnight deposit rate and OIS swaps. The LIBOR curve contains the 3m LIBOR rate and
 * 3m LIBOR / 6m fixed swaps.
 */
@Test(groups = TestGroup.UNIT)
public class Usd3mLiborKnownDiscountingTest extends CurveBuildingTests {
  /** The interpolator used for both curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  /** Generates OIS swaps for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD =
      new GeneratorDepositON("Overnight USD Deposit", Currency.USD, NYC, FED_FUNDS_INDEX.getDayCount());
  /** Generates the 3m LIBOR / 6m fixed swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  /** A 3m LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
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
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITH_TODAY);
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
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Attribute generators for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] discountingTenors = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
        Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
        Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[discountingTenors.length];
    for (int i = 0; i < discountingTenors.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(discountingTenors[i]);
    }
  }
  /** Market values for the 3m LIBOR curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Vanilla instrument generators for the 3m LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M };
  /** Attribute generators for the 3m LIBOR curve */
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR;
  static {
    final Period[] libor3mTenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD3_USD_ATTR = new GeneratorAttributeIR[libor3mTenors.length];
    for (int i = 0; i < libor3mTenors.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(libor3mTenors[i]);
    }
  }
  /** Builds the discounting curve */
  private static final DiscountingMethodCurveSetUp DSC_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(new MulticurveProviderDiscount(FX_MATRIX));
  static {
    for (int i = 0; i < DSC_USD_MARKET_QUOTES.length; i++) {
      DSC_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
  }
  /** Discounting curve before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BEFORE_FIXING;
  /** Discounting curve after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_AFTER_FIXING;
  static {
    DSC_BEFORE_FIXING = DSC_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_AFTER_FIXING = DSC_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }
  /** Builds the LIBOR curve */
  private static final DiscountingMethodCurveSetUp LIBOR_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR);
  /** LIBOR curve before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> LIBOR_BEFORE_FIXING;
  /** LIBOR curve after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> LIBOR_AFTER_FIXING;
  static {
    for (int i = 0; i < FWD3_USD_MARKET_QUOTES.length; i++) {
      LIBOR_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
    }
    LIBOR_BEFORE_FIXING = LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).withKnownData(DSC_BEFORE_FIXING.getFirst()).getBuilder().buildCurves(NOW);
    LIBOR_AFTER_FIXING = LIBOR_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).withKnownData(DSC_AFTER_FIXING.getFirst()).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    // discounting curve fitted first
    CurveBuildingBlockBundle fullJacobian = DSC_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 1);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
    // instruments have sensitivities to the discounting curve, so there are entries for both curves
    fullJacobian = LIBOR_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    final DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    Map<String, InstrumentDefinition<?>[]> definitionsForCurvesBeforeFixing = DSC_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    Map<String, InstrumentDefinition<?>[]> definitionsForCurvesAfterFixing = DSC_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_DSC_USD),
        DSC_BEFORE_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_DSC_USD),
        DSC_AFTER_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
    definitionsForCurvesBeforeFixing = LIBOR_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    definitionsForCurvesAfterFixing = LIBOR_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_FWD3_USD),
        LIBOR_BEFORE_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_FWD3_USD),
        LIBOR_AFTER_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
  }


  @Override
  public void testFiniteDifferenceSensitivities() {
    // discounting curve has sensitivities to its market data
    assertFiniteDifferenceSensitivities(DSC_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DSC_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(DSC_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DSC_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // discounting curve has no sensitivity to the LIBOR curve
    assertNoSensitivities(DSC_BEFORE_FIXING.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(DSC_AFTER_FIXING.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    // LIBOR curve has no sensitivity to the discounting curve
    assertNoSensitivities(LIBOR_BEFORE_FIXING.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD);
    assertNoSensitivities(LIBOR_AFTER_FIXING.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD);
    // LIBOR curve has sensitivities to its market data
    assertFiniteDifferenceSensitivities(LIBOR_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY,
        LIBOR_BUILDER.copy().withKnownData(DSC_BEFORE_FIXING.getFirst()), CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(LIBOR_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY,
        LIBOR_BUILDER.copy().withKnownData(DSC_AFTER_FIXING.getFirst()), CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
  }

  /**
   * Only one set of curves is constructed, so no tests are possible.
   */
  @Override
  public void testSameCurvesDifferentMethods() {
    return;
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = LIBOR_BEFORE_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USD_3M_LIBOR_INDEX.getSpotLag() + startIndex * jump, NYC);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USD_3M_LIBOR_INDEX, NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = USD_3M_LIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate, NYC);
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