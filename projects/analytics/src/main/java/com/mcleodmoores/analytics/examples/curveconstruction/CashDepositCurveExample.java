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
import com.mcleodmoores.analytics.financial.generator.interestrate.CashGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.CurveInstrumentGenerator.EndOfMonthConvention;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Demonstrates how to construct a single interpolated curve from cash deposit instruments.
 */
public class CashDepositCurveExample {
  // valuation date/time
  private static final LocalDate VALUATION_DATE = LocalDate.now();
  private static final LocalTime VALUATION_TIME = LocalTime.of(9, 0);
  private static final ZoneId VALUATION_ZONE = ZoneId.of("Europe/London");

  // get the interpolator
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(
      MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);

  // contains conventions for USD deposit instruments and will generate instruments
  private static final CashGenerator CASH_CONVENTION = CashGenerator.builder()
      .withCurrency(Currency.USD)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withSpotLag(2)
      .build();

  // the tenors of the deposit instruments that will be used
  private static final Tenor[] CURVE_TENORS = new Tenor[] {
      Tenor.ON,
      Tenor.ONE_WEEK,
      Tenor.TWO_WEEKS,
      Tenor.THREE_WEEKS,
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
      Tenor.FIVE_YEARS };

  // the market quotes of the instruments
  private static final double[] MARKET_QUOTES = new double[] {
      0.002,
      0.003,
      0.0034,
      0.0036,
      0.004,
      0.0047,
      0.005,
      0.0052,
      0.0058,
      0.006,
      0.0079,
      0.01,
      0.013,
      0.017,
      0.02,
      0.026 };

  // the curve name
  private static final String CURVE_NAME = "USD DEPOSIT";

  public static void constructCurve(final PrintStream out) {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    // there is a single curve that will be used for discounting USD payments
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .building(CURVE_NAME)
        .using(CURVE_NAME).forDiscounting(Currency.USD).withInterpolator(INTERPOLATOR);
    // add the nodes to the builder
    IntStream.range(0, CURVE_TENORS.length).forEach(
        i -> curveBuilder.addNode(CURVE_NAME, CASH_CONVENTION.toCurveInstrument(valuationDate, Tenor.of(Period.ZERO), CURVE_TENORS[i], 1, MARKET_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = curveBuilder.getBuilder().buildCurves(valuationDate);
    final MulticurveProviderDiscount curves = result.getFirst();
    final CurveBuildingBlockBundle inverseJacobians = result.getSecond();

    out.println("\n\nCurve names: " + curves.getAllNames());
    curves.getAllCurves().entrySet().stream().forEach(e -> CurvePrintUtils.printAtNodes(out, e.getKey(), e.getValue()));
    CurvePrintUtils.printJacobians(out, inverseJacobians, curveBuilder.getBuilder());
  }

  public static void main(final String[] args) {
    constructCurve(System.out);
    System.exit(0);
  }
}
