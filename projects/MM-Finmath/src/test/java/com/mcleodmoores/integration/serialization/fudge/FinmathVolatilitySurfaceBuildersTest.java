/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertSurfaceEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Calendar;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.volatilities.CapletVolatilities;
import net.finmath.marketdata.model.volatilities.CapletVolatilitiesParametric;
import net.finmath.marketdata.model.volatilities.CapletVolatilitiesParametricFourParameterPicewiseConstant;
import net.finmath.marketdata.model.volatilities.SwaptionMarketData;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface.QuotingConvention;
import net.finmath.time.Tenor;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface.DateRollConvention;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.integration.adapter.FinmathDateUtils;
import com.mcleodmoores.integration.serialization.fudge.FinmathVolatilitySurfaceBuilders;
import com.mcleodmoores.integration.testutils.FinancialTestBase;

/**
 * Unit tests for {@link FinmathVolatilitySurfaceBuilders}.
 */
public class FinmathVolatilitySurfaceBuildersTest extends FinancialTestBase {
  /** The accuracy */
  private static final double EPS = 1e-15;

  /**
   * Tests a cycle of {@link CapletVolatilitiesParametric}.
   */
  @Test
  public void testCapletVolatilitiesParametric() {
    final String name = "parametric-caplets";
    final Calendar date = FinmathDateUtils.convertLocalDate(LocalDate.of(2015, 1, 1));
    CapletVolatilitiesParametric surface = new CapletVolatilitiesParametric(name, date, 0.1, 0.2, 0.3, 0.4, 1.1);
    CapletVolatilitiesParametric cycled = cycleObject(CapletVolatilitiesParametric.class, surface);
    try {
      assertSurfaceEquals(surface, cycled);
      fail();
    } catch (final NullPointerException e) {
      // parametric surfaces throws NPE when converting between quoting conventions because the forward curve is not set
    }
    assertSurfaceEquals(surface, cycled, QuotingConvention.VOLATILITYLOGNORMAL);
    surface = new CapletVolatilitiesParametric(name, null, 0.1, 0.2, 0.3, 0.4, 1.1);
    cycled = cycleObject(CapletVolatilitiesParametric.class, surface);
    try {
      assertSurfaceEquals(surface, cycled);
      fail();
    } catch (final NullPointerException e) {
      // parametric surfaces throws NPE when converting between quoting conventions because the forward curve is not set
    }
    assertSurfaceEquals(surface, cycled, QuotingConvention.VOLATILITYLOGNORMAL);
  }

  /**
   * Tests a cycle of {@link CapletVolatilities}.
   */
  @Test
  public void testCapletVolatilities() {
    final String name = "caplet-volatilities";
    final Calendar referenceDate = FinmathDateUtils.convertLocalDate(LocalDate.of(2015, 1, 1));
    final String forwardCurveName = "forward-curve";
    final String paymentOffsetCode = "1W";
    final BusinessdayCalendarInterface paymentBusinessdayCalendar = new BusinessdayCalendarExcludingTARGETHolidays();
    final DateRollConvention paymentDateRollConvention = DateRollConvention.MODIFIED_FOLLOWING;
    final InterpolationMethod interpolationMethod = InterpolationMethod.AKIMA_CONTINUOUS;
    final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.LINEAR;
    final InterpolationEntity interpolationEntity = InterpolationEntity.LOG_OF_VALUE;
    final ForwardCurve.InterpolationEntityForward interpolationEntityForward = ForwardCurve.InterpolationEntityForward.FORWARD_TIMES_DISCOUNTFACTOR;
    final String discountCurveName = "discount-curve";
    final double[] times = new double[] {1, 2, 3, 4, 5, 6};
    final double[] df = new double[] {0.9, 0.85, 0.8, 0.75, 0.7, 0.65};
    final double[] givenForwards = new double[] {1., 2., 3., 4., 5., 6.};
    final DiscountCurve discountCurve = DiscountCurve.createDiscountCurveFromDiscountFactors(discountCurveName, times, df);
    AnalyticModelInterface model = new AnalyticModel(new CurveInterface[] {discountCurve});
    final ForwardCurve forwardCurve = ForwardCurve.createForwardCurveFromForwards(forwardCurveName, referenceDate, paymentOffsetCode,
        paymentBusinessdayCalendar, paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity,
        interpolationEntityForward, discountCurveName, model, times, givenForwards);
    final double[] maturities = new double[] {0.1, 1, 3, 5, 20};
    final double[] strikes = new double[] {0.02, 0.03, 0.04, 0.05, 0.06};
    final double[] volatilities = new double[] {0.22, 0.23, 0.24, 0.25, 0.26};
    final QuotingConvention quotingConvention = QuotingConvention.VOLATILITYLOGNORMAL;
    final CapletVolatilities surface = new CapletVolatilities(name, referenceDate, forwardCurve, maturities, strikes, volatilities,
        quotingConvention, discountCurve);
    final CapletVolatilities cycled = cycleObject(CapletVolatilities.class, surface);
    try {
      assertSurfaceEquals(surface, cycled);
      fail();
    } catch (final NullPointerException e) {
      // requires that the analytic model is not null, as the code uses the stored value for the discount curve
    }
    model = new AnalyticModel(new CurveInterface[] {discountCurve, forwardCurve});
    try {
      assertSurfaceEquals(surface, cycled, model);
    } catch (final NullPointerException e) {
      // surface throws NPE when converting between quoting conventions
    }
    assertSurfaceEquals(surface, cycled, QuotingConvention.VOLATILITYLOGNORMAL);
  }


  /**
   * Tests a cycle of {@link CapletVolatilitiesParametricFourParameterPicewiseConstant}.
   */
  @Test
  public void testCapletVolatilitiesParametricFourParameterPicewiseConstant() {
    final LocalDate referenceLocalDate = LocalDate.of(2015, 1, 1);
    final Calendar referenceDate = FinmathDateUtils.convertLocalDate(referenceLocalDate);
    final int n = 10;
    final Calendar[] dates = new Calendar[n];
    for (int i = 0; i < n; i++) {
      dates[i] = FinmathDateUtils.convertLocalDate(referenceLocalDate.plusMonths(i + 1));
    }
    final Tenor tenor = new Tenor(dates, referenceDate);
    final String name = "parametric-caplets";
    final Calendar date = FinmathDateUtils.convertLocalDate(LocalDate.of(2015, 1, 1));
    CapletVolatilitiesParametricFourParameterPicewiseConstant surface = new CapletVolatilitiesParametricFourParameterPicewiseConstant(name, date,
        0.1, 0.2, 0.3, 0.4, tenor);
    CapletVolatilitiesParametricFourParameterPicewiseConstant cycled = cycleObject(CapletVolatilitiesParametricFourParameterPicewiseConstant.class, surface);
    try {
      assertSurfaceEquals(surface, cycled);
      fail();
    } catch (final NullPointerException e) {
      // parametric surfaces throws NPE when converting between quoting conventions because the forward curve is not set
    }
    assertSurfaceEquals(surface, cycled, QuotingConvention.VOLATILITYLOGNORMAL);
    surface = new CapletVolatilitiesParametricFourParameterPicewiseConstant(name, null, 0.1, 0.2, 0.3, 0.4, tenor);
    cycled = cycleObject(CapletVolatilitiesParametricFourParameterPicewiseConstant.class, surface);
    try {
      assertSurfaceEquals(surface, cycled);
      fail();
    } catch (final NullPointerException e) {
      // parametric surfaces throws NPE when converting between quoting conventions because the forward curve is not set
    }
    assertSurfaceEquals(surface, cycled, QuotingConvention.VOLATILITYLOGNORMAL);
  }

  /**
   * Tests a cycle of {@link SwaptionMarketData}.
   */
  @Test
  public void testSwaptionMarketData() {
    final Calendar referenceDate = FinmathDateUtils.convertLocalDate(LocalDate.of(2015, 1, 1));
    final String forwardCurveName = "forward-curve";
    final String paymentOffsetCode = "1W";
    final BusinessdayCalendarInterface paymentBusinessdayCalendar = new BusinessdayCalendarExcludingTARGETHolidays();
    final DateRollConvention paymentDateRollConvention = DateRollConvention.MODIFIED_FOLLOWING;
    final InterpolationMethod interpolationMethod = InterpolationMethod.AKIMA_CONTINUOUS;
    final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.LINEAR;
    final InterpolationEntity interpolationEntity = InterpolationEntity.LOG_OF_VALUE;
    final ForwardCurve.InterpolationEntityForward interpolationEntityForward = ForwardCurve.InterpolationEntityForward.FORWARD_TIMES_DISCOUNTFACTOR;
    final String discountCurveName = "discount-curve";
    final double[] times = new double[] {1, 2, 3, 4, 5, 6};
    final double[] df = new double[] {0.9, 0.85, 0.8, 0.75, 0.7, 0.65};
    final double[] givenForwards = new double[] {1., 2., 3., 4., 5., 6.};
    final DiscountCurve discountCurve = DiscountCurve.createDiscountCurveFromDiscountFactors(discountCurveName, times, df);
    final AnalyticModelInterface model = new AnalyticModel(new CurveInterface[] {discountCurve});
    final ForwardCurve forwardCurve = ForwardCurve.createForwardCurveFromForwards(forwardCurveName, referenceDate, paymentOffsetCode,
        paymentBusinessdayCalendar, paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity,
        interpolationEntityForward, discountCurveName, model, times, givenForwards);
    final double[] maturities = new double[] {1, 2, 3};
    final double[] tenors = new double[] {5, 10, 15};
    final double swapPeriodLength = 0.5;
    final double[][] impliedVolatilities = new double[][] {new double[] {0.1, 0.2, 0.3}, new double[] {0.4, 0.5, 0.6}, new double[] {0.7, 0.8, 0.9}};
    final SwaptionMarketData marketData = new SwaptionMarketData(forwardCurve, discountCurve, maturities, tenors, swapPeriodLength, impliedVolatilities);
    final SwaptionMarketData cycled = cycleObject(SwaptionMarketData.class, marketData);
    for (final double maturity : maturities) {
      for (final double tenor : tenors) {
        assertEquals(marketData.getVolatility(maturity, tenor), cycled.getVolatility(maturity, tenor), 1e-15,
            "(" + maturity + ", " + tenor + ")");
      }
    }
  }
}
