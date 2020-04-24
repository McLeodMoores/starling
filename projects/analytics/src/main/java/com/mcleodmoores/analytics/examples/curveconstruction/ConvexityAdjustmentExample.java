/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.examples.curveconstruction;

import java.io.PrintStream;
import java.util.stream.IntStream;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveSetUp;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.HullWhiteMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.HullWhiteMethodCurveSetUp;
import com.mcleodmoores.analytics.financial.generator.interestrate.CurveInstrumentGenerator.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.generator.interestrate.FraGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.IborGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.OvernightDepositGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.QuarterlyStirFutureGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaFixedIborSwapGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaOisGenerator;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class ConvexityAdjustmentExample {
  // valuation date/time
  private static final LocalDate VALUATION_DATE = LocalDate.now();
  private static final LocalTime VALUATION_TIME = LocalTime.of(9, 0);
  private static final ZoneId VALUATION_ZONE = ZoneId.of("Europe/London");

  // get the interpolator that will be used for the discounting curves
  private static final Interpolator1D INTERPOLATOR_1 = NamedInterpolator1dFactory.of(
      DoubleQuadraticInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);
  // get the interpolator that will be used for the forward curves
  private static final Interpolator1D INTERPOLATOR_2 = NamedInterpolator1dFactory.of(
      MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);

  // the underlying index objects for the swaps
  private static final OvernightIndex EONIA = new OvernightIndex("EONIA", Currency.EUR, DayCounts.ACT_360, 0);
  private static final IborTypeIndex EURIBOR_3M_INDEX = new IborTypeIndex("EURIBOR 3M", Currency.EUR, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360,
      BusinessDayConventions.MODIFIED_FOLLOWING, true);
  private static final IborTypeIndex EURIBOR_6M_INDEX = new IborTypeIndex("EURIBOR 6M", Currency.EUR, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360,
      BusinessDayConventions.MODIFIED_FOLLOWING, true);

  // discounting curve instruments
  private static final OvernightDepositGenerator OVERNIGHT = OvernightDepositGenerator.builder()
      .withCurrency(Currency.EUR)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .build();
  private static final VanillaOisGenerator OIS = VanillaOisGenerator.builder()
      .withUnderlyingIndex(EONIA)
      .withPaymentTenor(Tenor.ONE_YEAR)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
      .withPaymentLag(2)
      .withSpotLag(2)
      .withStubType(StubType.SHORT_START)
      .withEndOfMonth(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .build();
  // 3m curve instruments
  private static final IborGenerator EURIBOR_3M = IborGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(EURIBOR_3M_INDEX)
      .build();
  private static final QuarterlyStirFutureGenerator EURIBOR_QUARTERLY_FUT = QuarterlyStirFutureGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(EURIBOR_3M_INDEX)
      .withPaymentAccrualFactor(0.25)
      .build();
  private static final VanillaFixedIborSwapGenerator FIXED_EURIBOR_3M = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withFixedLegDayCount(DayCounts.THIRTY_U_360)
      .withFixedLegPaymentTenor(Tenor.ONE_YEAR)
      .withStub(StubType.SHORT_START)
      .withUnderlyingIndex(EURIBOR_3M_INDEX)
      .build();
  // 6m curve instruments
  private static final IborGenerator EURIBOR_6M = IborGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(EURIBOR_6M_INDEX)
      .build();
  private static final FraGenerator EURIBOR_6M_FRA = FraGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(EURIBOR_6M_INDEX)
      .build();
  private static final VanillaFixedIborSwapGenerator FIXED_EURIBOR_6M = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withFixedLegDayCount(DayCounts.THIRTY_U_360)
      .withFixedLegPaymentTenor(Tenor.ONE_YEAR)
      .withStub(StubType.SHORT_START)
      .withUnderlyingIndex(EURIBOR_6M_INDEX)
      .build();

  private static final double OVERNIGHT_QUOTE = 0.0005;
  private static final double EURIBOR_3M_QUOTE = 0.001;
  private static final double EURIBOR_6M_QUOTE = 0.0015;
  private static final double[] OIS_QUOTES = new double[] {
      0.0010,
      0.0050,
      0.0060,
      0.0054,
      0.0066,
      0.0068,
      0.0075,
      0.0087,
      0.01,
      0.015,
      0.02,
      0.03,
      0.05 };
  private static final double[] EURIBOR_FUT_QUOTES = new double[] {
      0.997,
      0.9865,
      0.9875,
      0.9865,
      0.9860,
      0.9860,
  };
  private static final double[] EURIBOR_3M_SWAP_QUOTES = new double[] {
      0.0220,
      0.0230,
      0.0270,
      0.0340,
      0.0370,
      0.0400 };
  private static final double[] EURIBOR_6M_FRA_QUOTES = new double[] {
      0.024,
      0.024
  };
  private static final double[] EURIBOR_6M_SWAP_QUOTES = new double[] {
      0.0245,
      0.0285,
      0.0355,
      0.0380,
      0.0410
  };
  private static final Tenor[] OIS_TENORS = new Tenor[] {
      Tenor.ONE_MONTH,
      Tenor.TWO_MONTHS,
      Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS,
      Tenor.FIVE_MONTHS,
      Tenor.SIX_MONTHS,
      Tenor.NINE_MONTHS,
      Tenor.ONE_YEAR,
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FOUR_YEARS,
      Tenor.FIVE_YEARS,
      Tenor.TEN_YEARS };
  private static final int[] EURIBOR_N_FUTURE = new int[] {
      2,
      3,
      5,
      6,
      7
  };
  private static final Tenor[] EURIBOR_3M_SWAP_TENORS = new Tenor[] {
      Tenor.ONE_YEAR,
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FIVE_YEARS,
      Tenor.SEVEN_YEARS,
      Tenor.TEN_YEARS };
  private static final Tenor[] EURIBOR_6M_FRA_TENORS = new Tenor[] {
      Tenor.NINE_MONTHS,
      Tenor.TWELVE_MONTHS
  };
  private static final Tenor[] EURIBOR_6M_SWAP_TENORS = new Tenor[] {
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FIVE_YEARS,
      Tenor.SEVEN_YEARS,
      Tenor.TEN_YEARS };

  // the Hull-White model parameters
  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY_LEVELS = new double[] { 0.21, 0.211, 0.212, 0.213, 0.2114 };
  private static final double[] VOLATILITY_TIME = new double[] { 0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(
      MEAN_REVERSION, VOLATILITY_LEVELS, VOLATILITY_TIME);

  // the curve names
  private static final String DISCOUNTING_NAME = "EUR Dsc";
  private static final String FWD3_NAME = "EUR Fwd 3M";
  private static final String FWD6_NAME = "EUR Fwd 6M";

  public static void constructCurvesWithAdjustment(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    final HullWhiteMethodCurveSetUp builder = HullWhiteMethodCurveBuilder.setUp()
        .buildingFirst(DISCOUNTING_NAME)
        .thenBuilding(FWD3_NAME)
        .thenBuilding(FWD6_NAME)
        .using(DISCOUNTING_NAME).forDiscounting(Currency.EUR).forIndex(EONIA).withInterpolator(INTERPOLATOR_1)
        .using(FWD3_NAME).forIndex(EURIBOR_3M_INDEX).withInterpolator(INTERPOLATOR_2)
        .using(FWD6_NAME).forIndex(EURIBOR_6M_INDEX).withInterpolator(INTERPOLATOR_2)
        .addHullWhiteParameters(MODEL_PARAMETERS)
        .forHullWhiteCurrency(Currency.EUR);
    final Tenor startTenor = Tenor.of(Period.ZERO);

    // add nodes to the discounting curve
    builder.addNode(DISCOUNTING_NAME, OVERNIGHT.toCurveInstrument(valuationDate, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> builder.addNode(DISCOUNTING_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));

    // add nodes to the 3m forward curve
    builder.addNode(FWD3_NAME, EURIBOR_3M.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, EURIBOR_3M_QUOTE));
    IntStream.range(0, EURIBOR_N_FUTURE.length).forEach(
        i -> builder.addNode(FWD3_NAME, EURIBOR_QUARTERLY_FUT.toCurveInstrument(valuationDate, EURIBOR_N_FUTURE[i], 1, EURIBOR_FUT_QUOTES[i])));
    IntStream.range(0, EURIBOR_3M_SWAP_TENORS.length).forEach(
        i -> builder.addNode(FWD3_NAME,
            FIXED_EURIBOR_3M.toCurveInstrument(valuationDate, startTenor, EURIBOR_3M_SWAP_TENORS[i], 1, EURIBOR_3M_SWAP_QUOTES[i])));

    // add nodes to the 6m forward curve
    builder.addNode(FWD6_NAME, EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, EURIBOR_6M_QUOTE));
    IntStream.range(0, EURIBOR_6M_FRA_TENORS.length).forEach(
        i -> builder.addNode(FWD6_NAME, EURIBOR_6M_FRA.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_FRA_TENORS[i], 1, EURIBOR_6M_FRA_QUOTES[i])));
    IntStream.range(0, EURIBOR_6M_SWAP_TENORS.length).forEach(
        i -> builder.addNode(FWD6_NAME,
            FIXED_EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_SWAP_TENORS[i], 1, EURIBOR_6M_SWAP_QUOTES[i])));

    // build the curves
    final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> result = builder.getBuilder().buildCurves(valuationDate);
    final HullWhiteOneFactorProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n\nConvexity adjustment for: " + curves.getAllCurveNames());
    curves.getMulticurveProvider().getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, builder.getBuilder());
  }

  public static void constructCurvesWithoutAdjustment(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    final DiscountingMethodCurveSetUp builder = DiscountingMethodCurveBuilder.setUp()
        .buildingFirst(DISCOUNTING_NAME)
        .thenBuilding(FWD3_NAME)
        .thenBuilding(FWD6_NAME)
        .using(DISCOUNTING_NAME).forDiscounting(Currency.EUR).forIndex(EONIA).withInterpolator(INTERPOLATOR_1)
        .using(FWD3_NAME).forIndex(EURIBOR_3M_INDEX).withInterpolator(INTERPOLATOR_2)
        .using(FWD6_NAME).forIndex(EURIBOR_6M_INDEX).withInterpolator(INTERPOLATOR_2);
    final Tenor startTenor = Tenor.of(Period.ZERO);

    // add nodes to the discounting curve
    builder.addNode(DISCOUNTING_NAME, OVERNIGHT.toCurveInstrument(valuationDate, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> builder.addNode(DISCOUNTING_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));

    // add nodes to the 3m forward curve
    builder.addNode(FWD3_NAME, EURIBOR_3M.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, EURIBOR_3M_QUOTE));
    IntStream.range(0, EURIBOR_N_FUTURE.length).forEach(
        i -> builder.addNode(FWD3_NAME, EURIBOR_QUARTERLY_FUT.toCurveInstrument(valuationDate, EURIBOR_N_FUTURE[i], 1, EURIBOR_FUT_QUOTES[i])));
    IntStream.range(0, EURIBOR_3M_SWAP_TENORS.length).forEach(
        i -> builder.addNode(FWD3_NAME,
            FIXED_EURIBOR_3M.toCurveInstrument(valuationDate, startTenor, EURIBOR_3M_SWAP_TENORS[i], 1, EURIBOR_3M_SWAP_QUOTES[i])));

    // add nodes to the 6m forward curve
    builder.addNode(FWD6_NAME, EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, EURIBOR_6M_QUOTE));
    IntStream.range(0, EURIBOR_6M_FRA_TENORS.length).forEach(
        i -> builder.addNode(FWD6_NAME, EURIBOR_6M_FRA.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_FRA_TENORS[i], 1, EURIBOR_6M_FRA_QUOTES[i])));
    IntStream.range(0, EURIBOR_6M_SWAP_TENORS.length).forEach(
        i -> builder.addNode(FWD6_NAME,
            FIXED_EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_SWAP_TENORS[i], 1, EURIBOR_6M_SWAP_QUOTES[i])));

    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = builder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n\nNo convexity adjustment for: " + curves.getAllCurveNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, builder.getBuilder());
  }

  public static void main(final String[] args) {
    constructCurvesWithAdjustment(System.out);
    constructCurvesWithoutAdjustment(System.out);
    System.exit(0);
  }

}
