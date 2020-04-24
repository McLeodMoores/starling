/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.examples.curveconstruction;

import java.io.PrintStream;
import java.util.stream.IntStream;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveSetUp;
import com.mcleodmoores.analytics.financial.generator.interestrate.CurveInstrumentGenerator.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.generator.interestrate.IborGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaFixedIborSwapGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaOisGenerator;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
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
public class EcbMeetingCurveExample {
  // valuation date/time
  private static final LocalDate VALUATION_DATE = LocalDate.of(2020, 4, 4);
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
  private static final IborTypeIndex EURIBOR_6M_INDEX = new IborTypeIndex("EURIBOR 6M", Currency.EUR, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360,
      BusinessDayConventions.MODIFIED_FOLLOWING, true);

  // discounting curve instruments
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
  // 6m curve instruments
  private static final IborGenerator EURIBOR_6M = IborGenerator.builder()
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

  // ECB meeting dates
  private static final LocalDateTime[] ECB_MEETING_DATES = new LocalDateTime[] {
      LocalDateTime.of(2020, 5, 7, 10, 0),
      LocalDateTime.of(2020, 6, 4, 10, 0),
      LocalDateTime.of(2020, 7, 2, 10, 0),
      LocalDateTime.of(2020, 8, 6, 10, 0),
      LocalDateTime.of(2020, 9, 4, 10, 0),
      LocalDateTime.of(2020, 10, 1, 10, 0),
      LocalDateTime.of(2020, 11, 5, 10, 0),
      LocalDateTime.of(2020, 12, 2, 10, 0),
      LocalDateTime.of(2021, 1, 7, 10, 0),
      LocalDateTime.of(2021, 2, 5, 10, 0),
      LocalDateTime.of(2021, 3, 9, 10, 0),
      LocalDateTime.of(2021, 4, 6, 10, 0) };

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
      0.0102,
      0.0105,
      0.0106 };
  private static final double[] EURIBOR_6M_SWAP_QUOTES = new double[] {
      0.0145,
      0.0185,
      0.0255,
      0.0280,
      0.0310
  };
  private static final Tenor[] OIS_TENORS = new Tenor[] {
      Tenor.ONE_MONTH,
      Tenor.TWO_MONTHS,
      Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS,
      Tenor.FIVE_MONTHS,
      Tenor.SIX_MONTHS,
      Tenor.SEVEN_MONTHS,
      Tenor.EIGHT_MONTHS,
      Tenor.NINE_MONTHS,
      Tenor.TEN_MONTHS,
      Tenor.ELEVEN_MONTHS,
      Tenor.ONE_YEAR };
  private static final Tenor[] EURIBOR_6M_SWAP_TENORS = new Tenor[] {
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FIVE_YEARS,
      Tenor.SEVEN_YEARS,
      Tenor.TEN_YEARS };

  // the curve names
  private static final String DISCOUNTING_NAME = "EUR Dsc";
  private static final String FWD6_NAME = "EUR Fwd 6M";

  public static void constructCurvesUsingMeetingDates(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    // build the discounting / overnight curve first, using ECB meeting dates as nodes
    // then build the 6m EURIBOR curve
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .buildingFirst(DISCOUNTING_NAME)
        .using(DISCOUNTING_NAME).forDiscounting(Currency.EUR).forIndex(EONIA).withInterpolator(INTERPOLATOR_1).usingNodeDates(ECB_MEETING_DATES)
        .thenBuilding(FWD6_NAME).using(FWD6_NAME).forIndex(EURIBOR_6M_INDEX).withInterpolator(INTERPOLATOR_2);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    // add the discounting curve nodes
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> curveBuilder.addNode(DISCOUNTING_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));
    // add the EURIBOR curve nodes
    curveBuilder.addNode(FWD6_NAME, EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, Tenor.SIX_MONTHS, 1, EURIBOR_6M_QUOTE));
    IntStream.range(0, EURIBOR_6M_SWAP_QUOTES.length).forEach(
        i -> curveBuilder.addNode(FWD6_NAME,
            FIXED_EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_SWAP_TENORS[i], 1, EURIBOR_6M_SWAP_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = curveBuilder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n" + curves.getAllNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, curveBuilder.getBuilder());
  }

  public static void constructCurves(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    // build the discounting / overnight curve first,
    // then build the 6m EURIBOR curve
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .buildingFirst(DISCOUNTING_NAME).using(DISCOUNTING_NAME).forDiscounting(Currency.EUR).forIndex(EONIA).withInterpolator(INTERPOLATOR_1)
        .thenBuilding(FWD6_NAME).using(FWD6_NAME).forIndex(EURIBOR_6M_INDEX).withInterpolator(INTERPOLATOR_2);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    // add the discounting curve nodes
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> curveBuilder.addNode(DISCOUNTING_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));
    // add the EURIBOR curve nodes
    curveBuilder.addNode(FWD6_NAME, EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, Tenor.SIX_MONTHS, 1, EURIBOR_6M_QUOTE));
    IntStream.range(0, EURIBOR_6M_SWAP_QUOTES.length).forEach(
        i -> curveBuilder.addNode(FWD6_NAME,
            FIXED_EURIBOR_6M.toCurveInstrument(valuationDate, startTenor, EURIBOR_6M_SWAP_TENORS[i], 1, EURIBOR_6M_SWAP_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = curveBuilder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n" + curves.getAllNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, curveBuilder.getBuilder());
  }

  public static void main(final String[] args) {
    constructCurvesUsingMeetingDates(System.out);
    constructCurves(System.out);
    System.exit(0);
  }

}
