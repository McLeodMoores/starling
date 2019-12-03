/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PriceIndexCurveTest {

  private static final double[] INDEX_VALUE = new double[] { 108.23, 108.64, 111.0, 115.0 };
  private static final double[] TIME_VALUE = new double[] { -3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE = new PriceIndexCurve(CURVE);

  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new PriceIndexCurve(null);
  }

  /**
   * Tests the getter.
   */
  @Test
  public void getter() {
    assertEquals(CURVE, PRICE_INDEX_CURVE.getCurve());
  }

  /**
   * Tests price index.
   */
  @Test
  public void priceIndex() {
    assertEquals(INDEX_VALUE[0], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[0]), 1.0E-10);
    assertEquals(INDEX_VALUE[2], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[2]), 1.0E-10);
    assertEquals((INDEX_VALUE[2] + INDEX_VALUE[3]) / 2.0, PRICE_INDEX_CURVE.getPriceIndex((TIME_VALUE[2] + TIME_VALUE[3]) / 2.0), 1.0E-10);
  }

  /**
   * Tests price index builder from zero-coupon swap rates with start of the month convention.
   */
  @Test
  public void fromStartOfMonth() {
    final ZonedDateTime constructionDate = DateUtils.getUTCDate(2011, 8, 18);
    final ZonedDateTime[] indexKnownDate = new ZonedDateTime[] { DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1) };
    final double[] nodeTimeKnown = new double[indexKnownDate.length];
    for (int i = 0; i < indexKnownDate.length; i++) {
      nodeTimeKnown[i] = -ACT_ACT.getDayCountFraction(indexKnownDate[i], constructionDate);
    }
    final int[] swapTenor = new int[] { 1, 2, 3, 4, 5, 7, 10, 15, 20, 30 };
    final double[] swapRate = new double[] { 0.02, 0.021, 0.02, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025 };
    final double[] indexKnown = new double[] { 113.11, 113.10 }; // May / June 2011.
    final int monthLag = 3;
    final double[] nodeTimeOther = new double[swapTenor.length];
    final ZonedDateTime[] referenceDate = new ZonedDateTime[swapTenor.length];
    for (int i = 0; i < swapTenor.length; i++) {
      final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(constructionDate, Period.ofYears(swapTenor[i]),
          BUSINESS_DAY, CALENDAR);
      referenceDate[i] = paymentDate.minusMonths(monthLag).withDayOfMonth(1);
      nodeTimeOther[i] = ACT_ACT.getDayCountFraction(constructionDate, referenceDate[i]);
    }
    final PriceIndexCurve priceIndexCurve = PriceIndexCurve.fromStartOfMonth(nodeTimeKnown, indexKnown, nodeTimeOther, swapRate);
    for (int loopswap = 0; loopswap < swapTenor.length; loopswap++) {
      assertEquals("Simple price curve", indexKnown[0] * Math.pow(1 + swapRate[loopswap], swapTenor[loopswap]),
          priceIndexCurve.getPriceIndex(nodeTimeOther[loopswap]));
    }
  }
}
