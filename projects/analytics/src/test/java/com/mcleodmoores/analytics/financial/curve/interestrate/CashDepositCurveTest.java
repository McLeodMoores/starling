/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveSetUp;
import com.mcleodmoores.analytics.financial.generator.interestrate.CashGenerator;
import com.mcleodmoores.analytics.financial.generator.interestrate.CurveInstrumentGenerator.EndOfMonthConvention;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
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
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the construction of a curve containing only cash instruments.
 */
@Test(groups = TestGroup.UNIT)
public class CashDepositCurveTest {
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2017, 1, 3);
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
  private static final CashGenerator US_CONVENTION = CashGenerator.builder()
      .withCurrency(Currency.USD)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withSpotLag(2)
      .build();
  private static final Tenor[] CURVE_TENORS = new Tenor[] { Tenor.ON,
      Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS, Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
      Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS };
  private static final double[] MARKET_QUOTES = new double[] { 0.002,
      0.003, 0.0034, 0.0036, 0.004, 0.0047, 0.005,
      0.0052, 0.0058, 0.006, 0.0079, 0.01, 0.013,
      0.017, 0.02, 0.026 };
  private static final String CURVE_NAME = "USD DEPOSIT";

  private static final DiscountingMethodCurveSetUp CURVE_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME)
      .using(CURVE_NAME).forDiscounting(Currency.USD).withInterpolator(INTERPOLATOR);

  static {
    final Tenor startTenor = Tenor.of(Period.ZERO);
    for (int i = 0; i < CURVE_TENORS.length; i++) {
      CURVE_BUILDER.addNode(CURVE_NAME, US_CONVENTION.toCurveInstrument(VALUATION_DATE, startTenor, CURVE_TENORS[i], 1, MARKET_QUOTES[i]));
    }
  }

  private static final double EPS = 1e-15;

  /**
   * Tests that a discounting curve is created, with the same number of nodes as instruments.
   */
  @Test
  public void testDataInBundle() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves(VALUATION_DATE,
        Collections.<Index, ZonedDateTimeDoubleTimeSeries> emptyMap());
    final MulticurveProviderDiscount curves = result.getFirst();
    assertEquals(curves.getDiscountingCurves().size(), 1);
    assertTrue(curves.getForwardIborCurves().isEmpty());
    assertTrue(curves.getForwardONCurves().isEmpty());
    assertEquals(curves.getCurve(Currency.USD).getNumberOfParameters(), CURVE_TENORS.length);
  }

  /**
   * Tests that the present value of each instrument used to price the curve is zero.
   */
  @Test
  public void testCurveInstrumentsPriceToZero() {
    final MulticurveProviderDiscount curves = CURVE_BUILDER.getBuilder()
        .buildCurves(VALUATION_DATE, Collections.<Index, ZonedDateTimeDoubleTimeSeries> emptyMap()).getFirst();
    final Tenor startTenor = Tenor.of(Period.ZERO);
    for (int i = 0; i < CURVE_TENORS.length; i++) {
      final CashDefinition instrument = US_CONVENTION.toCurveInstrument(VALUATION_DATE, startTenor, CURVE_TENORS[i], 1, MARKET_QUOTES[i]);
      assertEquals(instrument.toDerivative(VALUATION_DATE).accept(PresentValueDiscountingCalculator.getInstance(), curves).getAmount(Currency.USD), 0, EPS);
    }
  }

  /**
   * Detects regressions.
   */
  @Test
  public void regression() {
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = CURVE_BUILDER.getBuilder().buildCurves(VALUATION_DATE,
        Collections.<Index, ZonedDateTimeDoubleTimeSeries> emptyMap());
    final MulticurveProviderDiscount curves = result.getFirst();
    final DoublesCurve curve = ((YieldCurve) curves.getCurve(Currency.USD)).getCurve();
    assertTrue(curve instanceof InterpolatedDoublesCurve);
    final InterpolatedDoublesCurve interpolated = (InterpolatedDoublesCurve) curve;
    final double[] xs = {
        0.0027397260273972603, 0.024657534246575342, 0.043835616438356165, 0.06301369863013699, 0.09315068493150686,
        0.16986301369863013, 0.25205479452054796, 0.33424657534246577, 0.4191780821917808, 0.5013698630136987, 0.7534246575342466,
        1.0054794520547945, 2.010958904109589, 3.0081817501309978, 4.005479452054795, 5.005479452054795 };
    final double[] ys = {
        0.0020277721451123724, 0.0028414907123070396, 0.0032837682202226234, 0.003518448852325246, 0.003942267128445193,
        0.004678824384064859, 0.005002689694397154, 0.005216404582490975, 0.005824626789509663, 0.006031192545802319,
        0.00794331869023279, 0.010044529157004621, 0.012979749227564343, 0.016778393857520602, 0.019486415661266757, 0.024750823073002855 };
    assertArrayEquals(interpolated.getXDataAsPrimitive(), xs, EPS);
    assertArrayEquals(interpolated.getYDataAsPrimitive(), ys, EPS);
  }
}
