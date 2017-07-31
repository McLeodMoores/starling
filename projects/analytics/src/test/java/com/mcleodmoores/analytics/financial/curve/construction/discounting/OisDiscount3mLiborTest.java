/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.construction.discounting;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.IborDepositConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.OvernightDepositConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.VanillaFixedIborSwapConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.VanillaOisConvention;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborTypeIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
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
public class OisDiscount3mLiborTest {
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2017, 1, 3);
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  private static final Tenor[] OIS_TENORS = new Tenor[] {Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS,
      Tenor.NINE_YEARS, Tenor.TEN_YEARS};
  private static final Tenor[] LIBOR_SWAP_TENORS = new Tenor[] {Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS,
      Tenor.NINE_YEARS, Tenor.TEN_YEARS, Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25),
      Tenor.ofYears(30), Tenor.ofYears(50)};
  private static final double OVERNIGHT_QUOTE = 0.0005;
  private static final double[] OIS_QUOTES = new double[] {0.002, 0.0021, 0.0022,
      0.0025, 0.004, 0.005, 0.0071, 0.0098, 0.012,
      0.0146, 0.0153, 0.0169, 0.0171, 0.025, 0.0276,
      0.0295, 0.031};
  private static final double LIBOR_3M_QUOTE = 0.001;
  private static final double[] LIBOR_SWAP_QUOTES = new double[] {0.0015, 0.005, 0.012,
      0.015, 0.0187, 0.02, 0.0234, 0.0261, 0.0291,
      0.0314, 0.0367, 0.04, 0.042, 0.044, 0.048,
      0.05, 0.05};
  //TODO switch to OvernightIndex
  private static final IndexON FED_FUNDS_INDEX = new IndexON("FED FUNDS", Currency.USD, DayCounts.ACT_360, 1);
  private static final IborTypeIndex LIBOR_INDEX = new IborTypeIndex("3M USD LIBOR", Currency.USD, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360,
      BusinessDayConventions.MODIFIED_FOLLOWING, true);
  private static final OvernightDepositConvention DEPOSIT = OvernightDepositConvention.builder()
      .withCurrency(Currency.USD)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .build();
  private static final VanillaOisConvention OIS = VanillaOisConvention.builder()
      .withUnderlyingIndex(FED_FUNDS_INDEX)
      .withPaymentPeriod(Tenor.ONE_YEAR)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
      .withPaymentLag(2)
      .withSpotLag(2)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .build();
  private static final IborDepositConvention LIBOR_DEPOSIT = IborDepositConvention.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withIborIndex(LIBOR_INDEX)
      .build();
  private static final VanillaFixedIborSwapConvention FIXED_LIBOR = VanillaFixedIborSwapConvention.builder()
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withFixedLegDayCount(DayCounts.THIRTY_U_360)
      .withFixedLegPaymentPeriod(Tenor.SIX_MONTHS)
      .withFromEnd(false)
      .withShortStub(false)
      .withUnderlyingIndex(LIBOR_INDEX)
      .build();
  private static final ZonedDateTimeDoubleTimeSeries OVERNIGHT_FIXINGS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {VALUATION_DATE.minusDays(1), VALUATION_DATE}, new double[] {OVERNIGHT_QUOTE, OVERNIGHT_QUOTE});
  private static final ZonedDateTimeDoubleTimeSeries LIBOR_3M_FIXINGS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {VALUATION_DATE.minusDays(1), VALUATION_DATE}, new double[] {LIBOR_3M_QUOTE, LIBOR_3M_QUOTE});
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXINGS = new HashMap<>();

  static {
    FIXINGS.put(FED_FUNDS_INDEX, OVERNIGHT_FIXINGS);
    FIXINGS.put(IndexConverter.toIborIndex(LIBOR_INDEX), LIBOR_3M_FIXINGS);
  }

  private static final String OIS_CURVE_NAME = "USD OIS";
  private static final String LIBOR_CURVE_NAME = "USD 3M LIBOR";

  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(new FXMatrix());
  private static final DiscountingMethodCurveSetUp CURVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(OIS_CURVE_NAME).using(OIS_CURVE_NAME).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(LIBOR_CURVE_NAME).using(LIBOR_CURVE_NAME).forIborIndex(IndexConverter.toIborIndex(LIBOR_INDEX)).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA)
      .withFixingTs(FIXINGS);

  static {
    final Tenor startTenor = Tenor.of(Period.ZERO);
    CURVE_BUILDER.withNode(OIS_CURVE_NAME, DEPOSIT.toCurveInstrument(VALUATION_DATE, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    for (int i = 0; i < OIS_TENORS.length; i++) {
      CURVE_BUILDER.withNode(OIS_CURVE_NAME, OIS.toCurveInstrument(VALUATION_DATE, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i]));
    }
    CURVE_BUILDER.withNode(LIBOR_CURVE_NAME, LIBOR_DEPOSIT.toCurveInstrument(VALUATION_DATE, startTenor, Tenor.THREE_MONTHS, 1, LIBOR_3M_QUOTE));
    for (int i = 0; i < LIBOR_SWAP_TENORS.length; i++) {
      CURVE_BUILDER.withNode(LIBOR_CURVE_NAME, FIXED_LIBOR.toCurveInstrument(VALUATION_DATE, startTenor, LIBOR_SWAP_TENORS[i], 1, LIBOR_SWAP_QUOTES[i]));
    }
  }

  @Test
  public void testNodeTimes() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves1(VALUATION_DATE);
    final MulticurveProviderDiscount curves = result.getFirst();
    assertEquals(curves.getDiscountingCurves().size(), 1);
    assertEquals(curves.getForwardIborCurves().size(), 1);
    assertEquals(curves.getForwardONCurves().size(), 1);
  }

}
