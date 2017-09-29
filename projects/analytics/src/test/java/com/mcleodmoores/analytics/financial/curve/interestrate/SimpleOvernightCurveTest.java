/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.OvernightDepositConvention;
import com.mcleodmoores.analytics.financial.convention.interestrate.VanillaOisConvention;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the construction of a discounting / overnight curve containing cash and OIS.
 */
public class SimpleOvernightCurveTest {
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2017, 1, 3);
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  private static final Tenor[] OIS_TENORS = new Tenor[] {Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS,
      Tenor.NINE_YEARS, Tenor.TEN_YEARS};
  private static final double OVERNIGHT_QUOTE = 0.0005;
  private static final double[] OIS_QUOTES = new double[] {0.002, 0.0021, 0.0022,
      0.0025, 0.004, 0.005, 0.0071, 0.0098, 0.012,
      0.0146, 0.0153, 0.0169, 0.0171, 0.025, 0.0276,
      0.0295, 0.031};
  private static final OvernightDepositConvention DEPOSIT = OvernightDepositConvention.builder()
      .withCurrency(Currency.USD)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .build();
  private static final OvernightIndex INDEX = new OvernightIndex("FED FUNDS", Currency.USD, DayCounts.ACT_360, 1);
  private static final VanillaOisConvention OIS = VanillaOisConvention.builder()
      .withUnderlyingIndex(INDEX)
      .withPaymentTenor(Tenor.ONE_YEAR)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
      .withPaymentLag(2)
      .withSpotLag(2)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withStubType(StubType.SHORT_START)
      .build();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {VALUATION_DATE.minusDays(1), VALUATION_DATE}, new double[] {OVERNIGHT_QUOTE, OVERNIGHT_QUOTE});
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXINGS = new HashMap<>();

  static {
    FIXINGS.put(IndexConverter.toIndexOn(INDEX), TS_ON_USD_WITH_TODAY);
  }

  private static final String CURVE_NAME = "USD OIS";

  private static final DiscountingMethodCurveSetUp CURVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME)
      .using(CURVE_NAME).forDiscounting(Currency.USD).forOvernightIndex(INDEX).withInterpolator(INTERPOLATOR);

  static {
    final Tenor startTenor = Tenor.of(Period.ZERO);
    CURVE_BUILDER.addNode(CURVE_NAME, DEPOSIT.toCurveInstrument(VALUATION_DATE, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE));
    for (int i = 0; i < OIS_TENORS.length; i++) {
      CURVE_BUILDER.addNode(CURVE_NAME, OIS.toCurveInstrument(VALUATION_DATE, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i]));
    }
  }

  private static final double EPS = 1e-15;

  /**
   * Tests that the discounting and overnight curves are the same curve, containing the name number of nodes as instruments.
   */
  @Test
  public void testDataInBundle() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves(VALUATION_DATE, FIXINGS);
    final MulticurveProviderDiscount curves = result.getFirst();
    assertEquals(curves.getDiscountingCurves().size(), 1);
    assertTrue(curves.getForwardIborCurves().isEmpty());
    assertEquals(curves.getForwardONCurves().size(), 1);
    final YieldAndDiscountCurve discounting = curves.getCurve(Currency.USD);
    assertEquals(discounting.getNumberOfParameters(), OIS_TENORS.length + 1);
    assertEquals(discounting, curves.getCurve(IndexConverter.toIndexOn(INDEX)));
  }

  /**
   * Tests that the present value of each instrument used to price the curve is zero.
   */
  @Test
  public void testCurveInstrumentsPriceToZero() {
    final MulticurveProviderDiscount curves = CURVE_BUILDER.getBuilder().buildCurves(VALUATION_DATE, FIXINGS).getFirst();
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final CashDefinition deposit = DEPOSIT.toCurveInstrument(VALUATION_DATE, startTenor, Tenor.ON, 1, OVERNIGHT_QUOTE);
    assertEquals(deposit.toDerivative(VALUATION_DATE).accept(PresentValueDiscountingCalculator.getInstance(), curves).getAmount(Currency.USD), 0, EPS);
    for (int i = 0; i < OIS_TENORS.length; i++) {
      final SwapFixedONDefinition instrument = OIS.toCurveInstrument(VALUATION_DATE, startTenor, OIS_TENORS[i], 1, OIS_QUOTES[i]);
      assertEquals(instrument.toDerivative(VALUATION_DATE).accept(PresentValueDiscountingCalculator.getInstance(), curves).getAmount(Currency.USD), 0, EPS);
    }
  }

  /**
   * Detects regressions.
   */
  @Test
  public void regression() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves(VALUATION_DATE, FIXINGS);
    final MulticurveProviderDiscount curves = result.getFirst();
    final DoublesCurve discounting = ((YieldCurve) curves.getCurve(Currency.USD)).getCurve();
    assertTrue(discounting instanceof InterpolatedDoublesCurve);
    InterpolatedDoublesCurve interpolated = (InterpolatedDoublesCurve) discounting;
    double[] xs = {
        0.0027397260273972603, 0.09863013698630137, 0.17534246575342466, 0.25753424657534246, 0.3452054794520548, 0.4246575342465753,
        0.5068493150684932, 0.7643835616438356, 1.0164383561643835, 2.0164383561643833, 3.013646231005315, 4.010958904109589,
        5.010958904109589, 6.016438356164383, 7.016378471442473, 8.013698630136986, 9.01095890410959, 10.01095890410959
    };
    double[] ys = {
        5.069440923725009E-4, 0.001963151552692394, 0.0020862845878990254, 0.0022050037351620784, 0.0025722931697927938,
        0.004094423296710267, 0.005068273350377301, 0.00723207771729943, 0.009883166934284508, 0.012088050497133587, 0.014724649501476696,
        0.015444714270769539, 0.017088151414733955, 0.01728341885816411, 0.025837207158120174, 0.02863597530702982, 0.03071389917451138,
        0.032368430068847945
    };
    assertArrayEquals(interpolated.getXDataAsPrimitive(), xs, EPS);
    assertArrayEquals(interpolated.getYDataAsPrimitive(), ys, EPS);
    final DoublesCurve overnight = ((YieldCurve) curves.getCurve(IndexConverter.toIndexOn(INDEX))).getCurve();
    assertTrue(overnight instanceof InterpolatedDoublesCurve);
    interpolated = (InterpolatedDoublesCurve) overnight;
    xs = new double[] {
        0.0027397260273972603, 0.09863013698630137, 0.17534246575342466, 0.25753424657534246, 0.3452054794520548, 0.4246575342465753,
        0.5068493150684932, 0.7643835616438356, 1.0164383561643835, 2.0164383561643833, 3.013646231005315, 4.010958904109589,
        5.010958904109589, 6.016438356164383, 7.016378471442473, 8.013698630136986, 9.01095890410959, 10.01095890410959
    };
    ys = new double[] {
        5.069440923725009E-4, 0.001963151552692394, 0.0020862845878990254, 0.0022050037351620784, 0.0025722931697927938,
        0.004094423296710267, 0.005068273350377301, 0.00723207771729943, 0.009883166934284508, 0.012088050497133587, 0.014724649501476696,
        0.015444714270769539, 0.017088151414733955, 0.01728341885816411, 0.025837207158120174, 0.02863597530702982, 0.03071389917451138,
        0.032368430068847945
    };
    assertArrayEquals(interpolated.getXDataAsPrimitive(), xs, EPS);
    assertArrayEquals(interpolated.getYDataAsPrimitive(), ys, EPS);
  }
}
