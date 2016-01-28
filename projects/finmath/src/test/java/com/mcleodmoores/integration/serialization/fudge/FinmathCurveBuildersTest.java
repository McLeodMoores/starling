/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertCurveEquals;
import static org.testng.Assert.fail;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.mcleodmoores.integration.adapter.ActActAfbFinmathDayCount;
import com.mcleodmoores.integration.adapter.TargetBusinessDayCalendar;
import com.mcleodmoores.integration.testutils.FinancialTestBase;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromProductOfCurves;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurveNelsonSiegelSvensson;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve.InterpolationEntityForward;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurveNelsonSiegelSvensson;
import net.finmath.marketdata.model.curves.ForwardCurveWithFixings;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface.DateRollConvention;
import net.finmath.time.daycount.DayCountConventionInterface;

/**
 * Unit tests for {@link FinmathCurveBuilders}.
 */
public class FinmathCurveBuildersTest extends FinancialTestBase {

  /**
   * Tests a cycle of {@link DiscountCurve}.
   */
  @Test
  public void testDiscountCurve() {
    final String name = "discount-curve";
    final double[] times = new double[] {1, 2, 3, 4, 5, 6};
    final double[] df = new double[] {0.9, 0.85, 0.8, 0.75, 0.7, 0.65};
    final boolean[] parameters = new boolean[] {true, true, true, true, true, true};
    DiscountCurve curve = DiscountCurve.createDiscountCurveFromDiscountFactors(name, times, df);
    DiscountCurve cycled = cycleObject(DiscountCurve.class, curve);
    assertCurveEquals(curve, cycled);
    curve = DiscountCurve.createDiscountCurveFromDiscountFactors(name, times, df, parameters, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
        InterpolationEntity.LOG_OF_VALUE_PER_TIME);
    cycled = cycleObject(DiscountCurve.class, curve);
    assertCurveEquals(curve, cycled);
    curve = DiscountCurve.createDiscountCurveFromDiscountFactors(name, times, df, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
        InterpolationEntity.LOG_OF_VALUE_PER_TIME);
    assertCurveEquals(curve, cycled);
    cycled = cycleObject(DiscountCurve.class, curve);
    assertCurveEquals(curve, cycled);
    final LocalDate date = new LocalDate(2015, 1, 1);
    curve = DiscountCurve.createDiscountCurveFromZeroRates(name, date, times, df, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
        InterpolationEntity.LOG_OF_VALUE);
    cycled = cycleObject(DiscountCurve.class, curve);
    assertCurveEquals(curve, cycled);
  }

  /**
   * Tests a cycle of {@link DiscountCurveFromProductOfCurves}.
   */
  @Test
  public void testDiscountCurveProductCurve() {
    final String name = "product-curve";
    final LocalDate date = new LocalDate(2015, 1, 1);
    final DiscountCurveInterface[] curves = new DiscountCurveInterface[] {
        DiscountCurve.createDiscountCurveFromDiscountFactors("one", new double[] {1, 2, 3}, new double[] {0.99, 0.98, 0.97}),
        DiscountCurve.createDiscountCurveFromDiscountFactors("one", new double[] {1, 2, 3}, new double[] {0.96, 0.95, 0.94})};
    DiscountCurveFromProductOfCurves curve = new DiscountCurveFromProductOfCurves(name, null, curves);
    DiscountCurveFromProductOfCurves cycled = cycleObject(DiscountCurveFromProductOfCurves.class, curve);
    assertCurveEquals(curve, cycled);
    curve = new DiscountCurveFromProductOfCurves(name, date, curves);
    cycled = cycleObject(DiscountCurveFromProductOfCurves.class, curve);
    assertCurveEquals(curve, cycled);
  }

  /**
   * Tests a cycle of {@link DiscountCurveNelsonSiegelSvensson}.
   */
  @Test
  public void testDiscountCurveNelsonSiegelSvennson() {
    final String name = "nss-curve";
    final LocalDate date = new LocalDate(2015, 1, 1);
    final double[] parameters = new double[] {1, 2, 3, 4, 5, 6};
    DiscountCurveNelsonSiegelSvensson curve = new DiscountCurveNelsonSiegelSvensson(name, null, parameters, 0.1);
    DiscountCurveNelsonSiegelSvensson cycled = cycleObject(DiscountCurveNelsonSiegelSvensson.class, curve);
    assertCurveEquals(curve, cycled);
    curve = new DiscountCurveNelsonSiegelSvensson(name, date, parameters, 0.1);
    cycled = cycleObject(DiscountCurveNelsonSiegelSvensson.class, curve);
    assertCurveEquals(curve, cycled);
  }

  /**
   * Tests a cycle of {@link ForwardCurve}.
   */
  @Test
  public void testForwardCurve() {
    final String name = "forward-curve";
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
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
    final ForwardCurve curve = ForwardCurve.createForwardCurveFromForwards(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar,
        paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity, interpolationEntityForward, discountCurveName,
        model, times, givenForwards);
    final ForwardCurve cycled = cycleObject(ForwardCurve.class, curve);
    assertCurveEquals(curve, cycled);
  }

  /**
   * Tests a cycle of {@link DiscountCurveFromForwardCurve}.
   */
  @Test
  public void testDiscountCurveFromForwardCurve() {
    final String name = "forward-curve";
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
    final String paymentOffsetCode = "1W";
    final BusinessdayCalendarInterface paymentBusinessdayCalendar = new TargetBusinessDayCalendar(); // note this is the wrapped type
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
    final ForwardCurve forwardCurve = ForwardCurve.createForwardCurveFromForwards(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar,
        paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity, interpolationEntityForward, discountCurveName,
        model, times, givenForwards);
    final double timeScaling = 0.34;
    DiscountCurveFromForwardCurve curve = new DiscountCurveFromForwardCurve(forwardCurve, timeScaling);
    DiscountCurveFromForwardCurve cycled = cycleObject(DiscountCurveFromForwardCurve.class, curve);
    assertCurveEquals(curve, cycled, model);
    model = new AnalyticModel(new CurveInterface[] {discountCurve, forwardCurve});
    curve = new DiscountCurveFromForwardCurve(name, timeScaling);
    cycled = cycleObject(DiscountCurveFromForwardCurve.class, curve);
    assertCurveEquals(curve, cycled, model);
  }

  /**
   * Tests a cycle of {@link ForwardCurveFromDiscountCurve}.
   */
  @Test
  public void testForwardCurveFromDiscountCurve() {
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
    final String paymentOffsetCode = "1W";
    final String discountCurveName = "discount-curve";
    final double[] times = new double[] {1, 2, 3, 4, 5, 6};
    final double[] df = new double[] {0.9, 0.85, 0.8, 0.75, 0.7, 0.65};
    final DiscountCurve discountCurve = DiscountCurve.createDiscountCurveFromDiscountFactors(discountCurveName, times, df);
    final AnalyticModelInterface model = new AnalyticModel(new CurveInterface[] {discountCurve});
    final ForwardCurveFromDiscountCurve curve = new ForwardCurveFromDiscountCurve(discountCurveName, referenceDate, paymentOffsetCode);
    final ForwardCurveFromDiscountCurve cycled = cycleObject(ForwardCurveFromDiscountCurve.class, curve);
    assertCurveEquals(curve, cycled, model);
  }

  /**
   * Tests a cycle of {@link ForwardCurveWithFixings}.
   */
  @Test
  public void testForwardCurveWithFixings() {
    final String name = "base-curve";
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
    final String paymentOffsetCode = "1W";
    final BusinessdayCalendarInterface paymentBusinessdayCalendar = new TargetBusinessDayCalendar(); // note this is the wrapped type
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
    final ForwardCurve baseCurve = ForwardCurve.createForwardCurveFromForwards(name, referenceDate, paymentOffsetCode, paymentBusinessdayCalendar,
        paymentDateRollConvention, interpolationMethod, extrapolationMethod, interpolationEntity, interpolationEntityForward, discountCurveName,
        model, times, givenForwards);
    final ForwardCurve fixedPartCurve = ForwardCurve.createForwardCurveFromForwards(name, referenceDate, "1D", paymentBusinessdayCalendar,
        paymentDateRollConvention, InterpolationMethod.AKIMA, ExtrapolationMethod.CONSTANT, InterpolationEntity.VALUE, InterpolationEntityForward.ZERO,
        discountCurveName, model, times, givenForwards);
    final ForwardCurveWithFixings curve = new ForwardCurveWithFixings(baseCurve, fixedPartCurve, 0.12, 0.34);
    final ForwardCurveWithFixings cycled = cycleObject(ForwardCurveWithFixings.class, curve);
    assertCurveEquals(curve, cycled);
  }

  /**
   * Tests a cycle of {@link ForwardCurveNelsonSiegelSvensson}.
   */
  @Test
  public void testForwardCurveNelsonSiegelSvennson() {
    final String name = "forward-nss-curve";
    final LocalDate date = new LocalDate(2015, 1, 1);
    final double[] parameters = new double[] {1, 2, 3, 4, 5, 6};
    final String paymentOffsetCode = "1W";
    final BusinessdayCalendarInterface paymentBusinessDayCalendar = new TargetBusinessDayCalendar(); // note this is the wrapped type
    final DateRollConvention paymentRollConvention = DateRollConvention.MODIFIED_FOLLOWING;
    final DayCountConventionInterface dayCountConvention = new ActActAfbFinmathDayCount();
    final double timeScaling = 0.1;
    ForwardCurveNelsonSiegelSvensson curve = new ForwardCurveNelsonSiegelSvensson(name, date, paymentOffsetCode, paymentBusinessDayCalendar,
        paymentRollConvention, dayCountConvention, parameters, timeScaling);
    ForwardCurveNelsonSiegelSvensson cycled = cycleObject(ForwardCurveNelsonSiegelSvensson.class, curve);
    assertCurveEquals(curve, cycled);
    curve = new ForwardCurveNelsonSiegelSvensson(name, null, paymentOffsetCode, paymentBusinessDayCalendar,
        paymentRollConvention, dayCountConvention, parameters, timeScaling);
    cycled = cycleObject(ForwardCurveNelsonSiegelSvensson.class, curve);
    try {
      assertCurveEquals(curve, cycled);
      fail();
    } catch (final NullPointerException e) {
      // reference date is required but not tested for non-null
    }
  }

}
