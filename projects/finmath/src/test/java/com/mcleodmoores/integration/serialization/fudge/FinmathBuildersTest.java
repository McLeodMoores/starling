/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertCurveEquals;
import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertSurfaceEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.mcleodmoores.integration.adapter.ActActAfbFinmathDayCount;
import com.mcleodmoores.integration.testutils.FinancialTestBase;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveNelsonSiegelSvensson;
import net.finmath.marketdata.model.volatilities.CapletVolatilitiesParametric;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface.QuotingConvention;
import net.finmath.time.Period;
import net.finmath.time.RegularSchedule;
import net.finmath.time.Schedule;
import net.finmath.time.Tenor;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.daycount.DayCountConventionInterface;

/**
 * Unit tests for {@link FinmathBuilders}.
 */
public class FinmathBuildersTest extends FinancialTestBase {

  /**
   * Tests a cycle of {@link Tenor}.
   */
  @Test
  public void testTenor() {
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
    final int n = 10;
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = referenceDate.plusMonths(i + 1);
    }
    final Tenor tenor = new Tenor(dates, referenceDate);
    final Tenor cycled = cycleObject(Tenor.class, tenor);
    assertArrayEquals(tenor.getAsDoubleArray(), cycled.getAsDoubleArray(), 1e-15);
  }

  /**
   * Tests a cycle of {@link TimeDiscretization}.
   */
  @Test
  public void testTimeDiscretization() {
    final double[] times = new double[] {0.1, 0.3, 0.6, 1, 5};
    final TimeDiscretization timeDiscretization = new TimeDiscretization(times);
    final TimeDiscretization cycled = cycleObject(TimeDiscretization.class, timeDiscretization);
    assertArrayEquals(timeDiscretization.getAsDoubleArray(), cycled.getAsDoubleArray(), 1e-15);
  }

  /**
   * Tests a cycle of {@link AnalyticModel}.
   */
  @Test
  public void testAnalyticModel() {
    final DiscountCurve curve1 = DiscountCurve.createDiscountCurveFromDiscountFactors("discount-curve", new double[] {1, 2, 3, 4, 5, 6},
        new double[] {0.9, 0.85, 0.8, 0.75, 0.7, 0.65});
    final DiscountCurveNelsonSiegelSvensson curve2 = new DiscountCurveNelsonSiegelSvensson("nss-curve", null,  new double[] {1, 2, 3, 4, 5, 6}, 0.1);
    final LocalDate date = new LocalDate(2015, 1, 1);
    final CapletVolatilitiesParametric surface1 = new CapletVolatilitiesParametric("surface1", date, 0.1, 0.2, 0.3, 0.4, 1.1);
    final CapletVolatilitiesParametric surface2 = new CapletVolatilitiesParametric("surface2", date, 1.1, 1.2, 1.3, 1.4, 2.1);
    AnalyticModelInterface model = new AnalyticModel();
    model = model.addCurves(curve1);
    model = model.addCurve("new-name", curve2);
    model = model.addVolatilitySurfaces(surface1, surface2);
    final AnalyticModelInterface cycled = cycleObject(AnalyticModelInterface.class, model);
    assertCurveEquals(model.getCurve("discount-curve"), cycled.getCurve("discount-curve"));
    assertCurveEquals(model.getCurve("new-name"), cycled.getCurve("new-name"));
    assertSurfaceEquals(model.getVolatilitySurface("surface1"), cycled.getVolatilitySurface("surface1"), QuotingConvention.VOLATILITYLOGNORMAL);
    assertSurfaceEquals(model.getVolatilitySurface("surface2"), cycled.getVolatilitySurface("surface2"), QuotingConvention.VOLATILITYLOGNORMAL);
  }

  /**
   * Tests a cycle of {@link RegularSchedule}.
   */
  @Test
  public void testRegularSchedule() {
    final double[] times = new double[] {1, 2, 3, 4, 5};
    final RegularSchedule schedule = new RegularSchedule(new TimeDiscretization(times));
    final RegularSchedule cycled = cycleObject(RegularSchedule.class, schedule);
    assertEquals(schedule.getNumberOfPeriods(), cycled.getNumberOfPeriods());
    for (int i = 0; i < times.length; i++) {
      assertEquals(schedule.getPeriod(i), cycled.getPeriod(i));
      if (i != times.length - 1) {
        assertEquals(schedule.getPeriodLength(i), cycled.getPeriodLength(i));
      }
    }
  }

  /**
   * Tests a cycle of {@link Schedule}.
   */
  @Test
  public void testSchedule() {
    final LocalDate referenceDate = new LocalDate(2015, 1, 1);
    final List<Period> periods = Arrays.asList(
        new Period(new LocalDate(2015, 1, 2), new LocalDate(2015, 1, 3), new LocalDate(2015, 1, 4), new LocalDate(2015, 1, 5)),
        new Period(new LocalDate(2015, 1, 6), new LocalDate(2015, 1, 7), new LocalDate(2015, 1, 8), new LocalDate(2015, 1, 9)));
    final DayCountConventionInterface dayCount = new ActActAfbFinmathDayCount();
    final Schedule schedule = new Schedule(referenceDate, periods, dayCount);
    final Schedule cycled = cycleObject(Schedule.class, schedule);
    assertEquals(schedule.getReferenceDate(), cycled.getReferenceDate());
    final List<Period> cycledPeriods = cycled.getPeriods();
    assertEquals(periods.size(), cycledPeriods.size());
    for (int i = 0; i < periods.size(); i++) {
      final Period expectedPeriod = periods.get(i);
      final Period actualPeriod = cycledPeriods.get(i);
      assertEquals(expectedPeriod.getFixing(), actualPeriod.getFixing());
      assertEquals(expectedPeriod.getPayment(), actualPeriod.getPayment());
      assertEquals(expectedPeriod.getPeriodStart(), actualPeriod.getPeriodStart());
      assertEquals(expectedPeriod.getPeriodEnd(), actualPeriod.getPeriodEnd());
    }
  }

  /**
   * Tests a cycle of {@link Period}.
   */
  @Test
  public void testPeriod() {
    final LocalDate fixing = new LocalDate(2015, 1, 1);
    final LocalDate payment = new LocalDate(2015, 1, 2);
    final LocalDate periodStart = new LocalDate(2015, 1, 3);
    final LocalDate periodEnd = new LocalDate(2015, 1, 4);
    final Period period = new Period(fixing, payment, periodStart, periodEnd);
    final Period cycled = cycleObject(Period.class, period);
    assertEquals(period.getFixing(), cycled.getFixing());
    assertEquals(period.getPayment(), cycled.getPayment());
    assertEquals(period.getPeriodStart(), cycled.getPeriodStart());
    assertEquals(period.getPeriodEnd(), cycled.getPeriodEnd());
  }
}
