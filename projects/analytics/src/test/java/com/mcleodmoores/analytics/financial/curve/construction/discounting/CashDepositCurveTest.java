/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.construction.discounting;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.convention.interestrate.CashConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
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
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class CashDepositCurveTest {
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2017, 1, 3);
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  private static final CashConvention US_CONVENTION = CashConvention.builder()
      .withCurrency(Currency.USD)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withSpotLag(2)
      .build();
  private static final Tenor[] CURVE_TENORS = new Tenor[] {Tenor.ON,
      Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS, Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS};
  private static final double[] MARKET_QUOTES = new double[] {0.002,
      0.003, 0.0034, 0.0036, 0.004, 0.0047, 0.005,
      0.0052, 0.0058, 0.006, 0.0079, 0.01, 0.013,
      0.017, 0.02, 0.026};
  private static final String CURVE_NAME = "USD DEPOSIT";

  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(new FXMatrix());
  private static final DiscountingMethodCurveSetUp CURVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME)
      .using(CURVE_NAME).forDiscounting(Currency.USD).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);

  static {
    final Tenor startTenor = Tenor.of(Period.ZERO);
    for (int i = 0; i < CURVE_TENORS.length; i++) {
      CURVE_BUILDER.withNode(CURVE_NAME, US_CONVENTION.toCurveInstrument(VALUATION_DATE, startTenor, CURVE_TENORS[i], 1, MARKET_QUOTES[i]));
    }
  }

  @Test
  public void testNodeTimes() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves1(VALUATION_DATE);
    final MulticurveProviderDiscount curves = result.getFirst();
    assertEquals(curves.getDiscountingCurves().size(), 1);
    assertTrue(curves.getForwardIborCurves().isEmpty());
    assertTrue(curves.getForwardONCurves().isEmpty());
  }

  @Test
  public void testCurveInstrumentsPriceToZero() {

  }

  @Test
  public void integration() {

  }
}
