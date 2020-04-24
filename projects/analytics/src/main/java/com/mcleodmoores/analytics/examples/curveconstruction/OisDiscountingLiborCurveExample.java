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
import com.mcleodmoores.analytics.financial.generator.interestrate.IborGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.OvernightDepositGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaFixedIborSwapGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaOisGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.CurveInstrumentGenerator.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
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
public class OisDiscountingLiborCurveExample {
  // valuation date/time
  private static final LocalDate VALUATION_DATE = LocalDate.now();
  private static final LocalTime VALUATION_TIME = LocalTime.of(9, 0);
  private static final ZoneId VALUATION_ZONE = ZoneId.of("Europe/London");

  // get the interpolator that will be used for both curves
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(
      MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);

  // the underlying index objects for the swaps
  private static final OvernightIndex FED_FUNDS_INDEX = new OvernightIndex("FED FUNDS", Currency.USD, DayCounts.ACT_360, 1);
  private static final IborTypeIndex LIBOR_INDEX = new IborTypeIndex("3M USD LIBOR", Currency.USD, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360,
      BusinessDayConventions.MODIFIED_FOLLOWING, true);

  // create the conventions for overnight and IBOR-type deposits, OIS swaps and IBOR swaps
  private static final OvernightDepositGenerator OVERNIGHT = OvernightDepositGenerator.builder()
      .withCurrency(Currency.USD)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .build();
  private static final IborGenerator LIBOR = IborGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(LIBOR_INDEX)
      .build();
  private static final VanillaOisGenerator OIS = VanillaOisGenerator.builder()
      .withUnderlyingIndex(FED_FUNDS_INDEX)
      .withPaymentTenor(Tenor.ONE_YEAR)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
      .withPaymentLag(2)
      .withSpotLag(2)
      .withStubType(StubType.SHORT_START)
      .withEndOfMonth(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .build();
  private static final VanillaFixedIborSwapGenerator FIXED_LIBOR = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withFixedLegDayCount(DayCounts.THIRTY_U_360)
      .withFixedLegPaymentTenor(Tenor.SIX_MONTHS)
      .withStub(StubType.SHORT_START)
      .withUnderlyingIndex(LIBOR_INDEX)
      .build();

  // the tenors of the OIS swaps
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
      Tenor.SIX_YEARS,
      Tenor.SEVEN_YEARS,
      Tenor.EIGHT_YEARS,
      Tenor.NINE_YEARS,
      Tenor.TEN_YEARS };
  // the tenors of the IBOR swaps
  private static final Tenor[] LIBOR_SWAP_TENORS = new Tenor[] {
      Tenor.SIX_MONTHS,
      Tenor.ONE_YEAR,
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FOUR_YEARS,
      Tenor.FIVE_YEARS,
      Tenor.SIX_YEARS,
      Tenor.SEVEN_YEARS,
      Tenor.EIGHT_YEARS,
      Tenor.NINE_YEARS,
      Tenor.TEN_YEARS,
      Tenor.ofYears(12),
      Tenor.ofYears(15),
      Tenor.ofYears(20),
      Tenor.ofYears(25),
      Tenor.ofYears(30),
      Tenor.ofYears(40),
      Tenor.ofYears(50) };

  // market quotes for the curve instruments
  private static final double OVERNIGHT_QUOTE = 0.0005;
  private static final double LIBOR_3M_QUOTE = 0.001;
  private static final double[] OIS_QUOTES = new double[] {
      0.002,
      0.0021,
      0.0022,
      0.0025,
      0.004,
      0.005,
      0.0071,
      0.0098,
      0.012,
      0.0146,
      0.0153,
      0.0169,
      0.0171,
      0.025,
      0.0276,
      0.0295,
      0.031 };
  private static final double[] LIBOR_SWAP_QUOTES = new double[] {
      0.003,
      0.005,
      0.022,
      0.025,
      0.0287,
      0.03,
      0.0334,
      0.0361,
      0.0391,
      0.0414,
      0.0467,
      0.05,
      0.052,
      0.054,
      0.058,
      0.06,
      0.066,
      0.07 };

  private static final String OIS_CURVE_NAME = "USD OIS";
  private static final String LIBOR_CURVE_NAME = "USD 3M LIBOR";

  public static void constructSimultaneousCurves(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    // an interpolated OIS curve is used to discount USD payments and to calculate forward rates for any
    // floating payments linked to the Fed Funds rate
    // an interpolated LIBOR curve is used to calculate any forward rates linked to the 3 month LIBOR rate
    // both curves are built at the same time
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .building(OIS_CURVE_NAME, LIBOR_CURVE_NAME)
        .using(OIS_CURVE_NAME).forDiscounting(Currency.USD).forIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
        .using(LIBOR_CURVE_NAME).forIndex(LIBOR_INDEX).withInterpolator(INTERPOLATOR);
    // add the cash nodes to the builder
    final Tenor startTenor = Tenor.of(Period.ZERO);
    curveBuilder.addNode(OIS_CURVE_NAME, OVERNIGHT.toCurveInstrument(valuationDate, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    curveBuilder.addNode(LIBOR_CURVE_NAME, LIBOR.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, LIBOR_3M_QUOTE));
    // add the OIS nodes
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> curveBuilder.addNode(OIS_CURVE_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));
    IntStream.range(0, LIBOR_SWAP_TENORS.length).forEach(
        i -> curveBuilder.addNode(LIBOR_CURVE_NAME, FIXED_LIBOR.toCurveInstrument(valuationDate, startTenor, LIBOR_SWAP_TENORS[i], 1, LIBOR_SWAP_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = curveBuilder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n\nSimultaneous construction for: " + curves.getAllNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, curveBuilder.getBuilder());
  }

  public static void constructConsecutiveCurves(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    // an interpolated OIS curve is constructed first, which is used to discount USD payments and to calculate forward rates for any
    // floating payments linked to the Fed Funds rate
    // an interpolated LIBOR curve is then built, which is used to calculate any forward rates linked to the 3 month LIBOR rate
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .building(OIS_CURVE_NAME)
        .using(OIS_CURVE_NAME).forDiscounting(Currency.USD).forIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
        .thenBuilding(LIBOR_CURVE_NAME)
        .using(LIBOR_CURVE_NAME).forIndex(LIBOR_INDEX).withInterpolator(INTERPOLATOR);
    // add the cash nodes to the builder
    final Tenor startTenor = Tenor.of(Period.ZERO);
    curveBuilder.addNode(OIS_CURVE_NAME, OVERNIGHT.toCurveInstrument(valuationDate, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    curveBuilder.addNode(LIBOR_CURVE_NAME, LIBOR.toCurveInstrument(valuationDate, startTenor, Tenor.THREE_MONTHS, 1, LIBOR_3M_QUOTE));
    // add the OIS nodes
    IntStream.range(0, OIS_TENORS.length).forEach(
        i -> curveBuilder.addNode(OIS_CURVE_NAME, OIS.toCurveInstrument(valuationDate, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i])));
    IntStream.range(0, LIBOR_SWAP_TENORS.length).forEach(
        i -> curveBuilder.addNode(LIBOR_CURVE_NAME, FIXED_LIBOR.toCurveInstrument(valuationDate, startTenor, LIBOR_SWAP_TENORS[i], 1, LIBOR_SWAP_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = curveBuilder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n\nConsecutive construction for: " + curves.getAllNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, curveBuilder.getBuilder());
  }

  public static void main(final String[] args) {
    constructSimultaneousCurves(System.out);
    constructConsecutiveCurves(System.out);
    System.exit(0);
  }

}
