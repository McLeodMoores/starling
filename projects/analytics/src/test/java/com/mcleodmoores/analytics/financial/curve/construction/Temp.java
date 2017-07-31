/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.construction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.OvernightDepositConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.VanillaOisConvention;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class Temp {
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2011, 9, 28);
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final Tenor[] OIS_TENORS = new Tenor[] {Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS,
      Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
  private static final OvernightDepositConvention DEPOSIT = OvernightDepositConvention.builder()
      .withCurrency(Currency.USD)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .build();
  private static final IndexON INDEX = new IndexON("FED FUNDS", Currency.USD, DayCounts.ACT_360, 1);
  private static final VanillaOisConvention OIS = VanillaOisConvention.builder()
      .withUnderlyingIndex(INDEX)
      .withPaymentPeriod(Tenor.ONE_YEAR)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
      .withPaymentLag(2)
      .withSpotLag(2)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .build();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {VALUATION_DATE.minusDays(1), VALUATION_DATE}, new double[] {0.07, 0.08});
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXINGS = new HashMap<>();

  static {
    FIXINGS.put(INDEX, TS_ON_USD_WITH_TODAY);
  }

  private static final String CURVE_NAME = "USD OIS";

  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(new FXMatrix());
  private static final DiscountingMethodCurveSetUp CURVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME)
      .using(CURVE_NAME).forDiscounting(Currency.USD).forOvernightIndex(INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA)
      .withFixingTs(FIXINGS);

  static {
    final Tenor startTenor = Tenor.of(Period.ZERO);
    CURVE_BUILDER.withNode(CURVE_NAME, DEPOSIT.toCurveInstrument(VALUATION_DATE, startTenor, Tenor.ON, 1, 0.04));
    for (final Tenor element : OIS_TENORS) {
      CURVE_BUILDER.withNode(CURVE_NAME, OIS.toCurveInstrument(VALUATION_DATE, startTenor, element, 1, 0.04));
    }
  }

  @Test
  public void testNodeTimes() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves1(VALUATION_DATE);
    final DoublesCurve temp = ((YieldCurve) result.getFirst().getDiscountingCurves().get(Currency.USD)).getCurve();
    for (int i = 0; i < temp.size(); i++) {
      System.out.println(temp.getXData()[i]);
    }
    System.out.println();
    System.out.println();
    System.out.println();
    for (int i = 0; i < temp.size(); i++) {
      System.out.println(temp.getYData()[i]);
    }
    final MulticurveProviderDiscount curves = result.getFirst();
    assertEquals(curves.getDiscountingCurves().size(), 1);
    assertTrue(curves.getForwardIborCurves().isEmpty());
    assertEquals(curves.getForwardONCurves().size(), 1);
  }

}
