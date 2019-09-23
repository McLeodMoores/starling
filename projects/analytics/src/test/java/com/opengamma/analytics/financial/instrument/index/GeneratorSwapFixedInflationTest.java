/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapFixedInflationTest {

  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IndexPrice PRICE_INDEX_GPB = PRICE_INDEXES[1];
  private static final Currency CUR = PRICE_INDEX_EUR.getCurrency();
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EOM = true;
  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2008, 8, 14);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final int COUPON_TENOR_YEAR = 10;
  private static final Period COUPON_TENOR = Period.ofYears(COUPON_TENOR_YEAR);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR,
      EOM);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final int SPOT_LAG = 2;
  private static final ZonedDateTime REFERENCE_START_DATE = DateUtils.getUTCDate(2008, 5, 18);
  private static final ZonedDateTime REFERENCE_START_DATE_MONTHLY = DateUtils.getUTCDate(2008, 5, 31);

  private static final boolean IS_LINEAR = true;
  private static final boolean IS_NOT_LINEAR = false;
  private static final ZonedDateTime[] REFERENCE_START_DATES = new ZonedDateTime[2];
  static {
    REFERENCE_START_DATES[0] = REFERENCE_START_DATE.with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_START_DATES[1] = REFERENCE_START_DATE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
  }
  private static final ZonedDateTime[] REFERENCE_END_DATES = new ZonedDateTime[2];
  static {
    REFERENCE_END_DATES[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).with(TemporalAdjusters.lastDayOfMonth());
    REFERENCE_END_DATES[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).with(TemporalAdjusters.lastDayOfMonth());
  }
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_SWAP_INFLATION_LINEAR = new GeneratorSwapFixedInflationZeroCoupon(
      "generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR,
      EOM, MONTH_LAG, SPOT_LAG,
      IS_LINEAR);
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_SWAP_INFLATION_PIECEWISE = new GeneratorSwapFixedInflationZeroCoupon(
      "generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR,
      EOM, MONTH_LAG, SPOT_LAG,
      IS_NOT_LINEAR);
  private static final GeneratorAttributeIR ATTRIBUTE = new GeneratorAttributeIR(COUPON_TENOR);

  /**
   * Tests the getters.
   */
  @Test
  public void getterLinear() {
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY, GENERATOR_SWAP_INFLATION_LINEAR.getBusinessDayConvention());
    assertEquals("GeneratorSwap: getter", CALENDAR, GENERATOR_SWAP_INFLATION_LINEAR.getCalendar());
    assertEquals("GeneratorSwap: getter", PRICE_INDEX_EUR, GENERATOR_SWAP_INFLATION_LINEAR.getIndexPrice());
    final String name = "generator";
    assertTrue(name.equals(GENERATOR_SWAP_INFLATION_LINEAR.getName()));
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR.getName(), GENERATOR_SWAP_INFLATION_LINEAR.toString());
    assertEquals("GeneratorSwap: getter", MONTH_LAG, GENERATOR_SWAP_INFLATION_LINEAR.getMonthLag());
    assertTrue("GeneratorSwap: getter", EOM == GENERATOR_SWAP_INFLATION_LINEAR.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG, GENERATOR_SWAP_INFLATION_LINEAR.getSpotLag());
  }

  /**
   * Tests the getters.
   */
  @Test
  public void getterPiecewise() {
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY, GENERATOR_SWAP_INFLATION_PIECEWISE.getBusinessDayConvention());
    assertEquals("GeneratorSwap: getter", CALENDAR, GENERATOR_SWAP_INFLATION_PIECEWISE.getCalendar());
    assertEquals("GeneratorSwap: getter", PRICE_INDEX_EUR, GENERATOR_SWAP_INFLATION_PIECEWISE.getIndexPrice());
    final String name = "generator";
    assertTrue(name.equals(GENERATOR_SWAP_INFLATION_PIECEWISE.getName()));
    assertEquals(GENERATOR_SWAP_INFLATION_PIECEWISE.getName(), GENERATOR_SWAP_INFLATION_PIECEWISE.toString());
    assertEquals("GeneratorSwap: getter", MONTH_LAG, GENERATOR_SWAP_INFLATION_PIECEWISE.getMonthLag());
    assertTrue("GeneratorSwap: getter", EOM == GENERATOR_SWAP_INFLATION_PIECEWISE.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG, GENERATOR_SWAP_INFLATION_PIECEWISE.getSpotLag());
  }

  /**
   * Tests the equals.
   */
  @Test
  public void equalHash() {
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR, GENERATOR_SWAP_INFLATION_LINEAR);
    final GeneratorSwapFixedInflationZeroCoupon generatorDuplicate = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR,
        BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG, SPOT_LAG,
        IS_LINEAR);
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR, generatorDuplicate);
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR.hashCode(), generatorDuplicate.hashCode());
    GeneratorSwapFixedInflationZeroCoupon generatorModified;
    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_GPB, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    final BusinessDayConvention modifiedBusinessDay = BusinessDayConventions.FOLLOWING;
    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, modifiedBusinessDay, CALENDAR, EOM,
        MONTH_LAG, SPOT_LAG,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY,
        WeekendWorkingDayCalendar.FRIDAY_SATURDAY, EOM, MONTH_LAG, SPOT_LAG,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR, false, MONTH_LAG,
        SPOT_LAG,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR, EOM, 2, SPOT_LAG,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG, 1,
        IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflationZeroCoupon("generator", PRICE_INDEX_EUR, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG,
        false);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(null));
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(CUR));
  }

  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  @Test
  public void swapFixedInflationZeroCouponMonthlyConstructor() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponMonthlyDefinition inflationCpn = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE,
        START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, 3, REFERENCE_START_DATE_MONTHLY, REFERENCE_END_DATES[0], false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL,
        COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn,
        CalendarAdapter.of(CALENDAR));
    final SwapFixedInflationZeroCouponDefinition generateSwap = GENERATOR_SWAP_INFLATION_PIECEWISE.generateInstrument(TODAY, zeroCpnRate,
        NOTIONAL, ATTRIBUTE);
    assertEquals("Swap zero-coupon inflation constructor", swap, generateSwap);
  }

  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  @Test
  public void swapFixedInflationZeroCouponInterpolationConstructor() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponInterpolationDefinition inflationCpn = CouponInflationZeroCouponInterpolationDefinition.from(CUR,
        PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL,
        PRICE_INDEX_EUR, MONTH_LAG, REFERENCE_START_DATES, REFERENCE_END_DATES, false);
    final CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, START_DATE, PAYMENT_DATE, -NOTIONAL,
        COUPON_TENOR_YEAR, zeroCpnRate);
    final SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn,
        CalendarAdapter.of(CALENDAR));
    final SwapFixedInflationZeroCouponDefinition generateSwap = GENERATOR_SWAP_INFLATION_LINEAR.generateInstrument(TODAY, zeroCpnRate,
        NOTIONAL, ATTRIBUTE);
    assertEquals("Swap zero-coupon inflation constructor", swap, generateSwap);
  }

}
