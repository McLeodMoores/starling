/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.generator.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.generator.interestrate.VanillaFixedIborSwapGenerator;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link VanillaFixedIborSwapGenerator}.
 */
public class VanillaFixedIborSwapGeneratorTest {
  private static final Tenor FIXED_LEG_TENOR = Tenor.SIX_MONTHS;
  private static final DayCount FIXED_LEG_DAYCOUNT = DayCounts.THIRTY_U_360;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final IborTypeIndex INDEX = new IborTypeIndex("NAME", Currency.USD, Tenor.THREE_MONTHS, 2,
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final VanillaFixedIborSwapGenerator CONVENTION = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(CALENDAR)
      .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
      .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
      .withStub(STUB_TYPE)
      .withUnderlyingIndex(INDEX)
      .build();

  /**
   * Tests that the calendar cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(null)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX);
  }

  /**
   * Tests that the calendar must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCalendarIsSet() {
    VanillaFixedIborSwapGenerator.builder()
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the fixed leg day count cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLegDayCount() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(null)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX);
  }

  /**
   * Tests that the fixed leg day count must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixedLegDayCountIsSet() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the fixed leg payment tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLegPaymentTenor() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(null)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX);
  }

  /**
   * Tests that the fixed leg payment tenor must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixedLegPaymentTenorIsSet() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the stub type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStubType() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(null)
    .withUnderlyingIndex(INDEX);
  }

  /**
   * Tests that the stub type must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testStubTypeIsSet() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .withUnderlyingIndex(null);
  }

  /**
   * Tests that the index must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIndexIsSet() {
    VanillaFixedIborSwapGenerator.builder()
    .withCalendar(CALENDAR)
    .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
    .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
    .withStub(STUB_TYPE)
    .build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    VanillaFixedIborSwapGenerator other = VanillaFixedIborSwapGenerator.builder()
        .withCalendar(CALENDAR)
        .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
        .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
        .withStub(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    assertEquals(CONVENTION.toString(), "VanillaFixedIborSwapConvention [index=IborIndex[NAME, currency=USD, tenor=P3M, day count=Actual/360, "
        + "business day convention=Modified Following, spot lag=2, end-of-month], fixedLegPaymentTenor=Tenor[P6M], "
        + "fixedLegDayCount=30U/360, calendar=Saturday / Sunday: [SATURDAY, SUNDAY], stubType=LONG_END]");
    other = VanillaFixedIborSwapGenerator.builder()
        .withCalendar(WeekendWorkingDayCalendar.FRIDAY_SATURDAY)
        .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
        .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
        .withStub(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(CALENDAR)
      .withFixedLegDayCount(DayCounts.ACT_365)
      .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
      .withStub(STUB_TYPE)
      .withUnderlyingIndex(INDEX)
      .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(CALENDAR)
      .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
      .withFixedLegPaymentTenor(Tenor.ONE_YEAR)
      .withStub(STUB_TYPE)
      .withUnderlyingIndex(INDEX)
      .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(CALENDAR)
      .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
      .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
      .withStub(StubType.SHORT_END)
      .withUnderlyingIndex(INDEX)
      .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaFixedIborSwapGenerator.builder()
      .withCalendar(CALENDAR)
      .withFixedLegDayCount(FIXED_LEG_DAYCOUNT)
      .withFixedLegPaymentTenor(FIXED_LEG_TENOR)
      .withStub(STUB_TYPE)
      .withUnderlyingIndex(new IborTypeIndex("NAME", Currency.USD, Tenor.THREE_MONTHS, 1,
          DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true))
      .build();
    assertNotEquals(CONVENTION, other);
  }

  /**
   * Tests that the definition is the same as that produced using the generator.
   */
  @Test
  public void testGeneratorEquivalence() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 8, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.FIVE_YEARS;
    final double rate = 0.01;
    final GeneratorSwapFixedIbor generator =
        new GeneratorSwapFixedIbor("", FIXED_LEG_TENOR.getPeriod(), FIXED_LEG_DAYCOUNT, IndexConverter.toIborIndex(INDEX), CalendarAdapter.of(CALENDAR));
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(startTenor.getPeriod(), endTenor.getPeriod());
    assertEquals(CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate), generator.generateInstrument(date, rate, 1, attribute));
  }

  /**
   * Tests the definition.
   */
  @Test
  public void testDefinition() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 8, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.FIVE_YEARS;
    final double rate = 0.01;
    final SwapFixedIborDefinition swap = CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate);
    assertTrue(swap.getFirstLeg() instanceof AnnuityCouponFixedDefinition);
    assertTrue(swap.getSecondLeg() instanceof AnnuityCouponIborDefinition);
  }
}
