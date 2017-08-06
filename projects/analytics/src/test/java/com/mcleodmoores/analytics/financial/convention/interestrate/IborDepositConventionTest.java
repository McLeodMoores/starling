/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.IborTypeIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link IborDepositConvention}.
 */
public class IborDepositConventionTest {
  private static final IborTypeIndex INDEX = new IborTypeIndex("NAME", Currency.USD, Tenor.THREE_MONTHS, 2,
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final IborDepositConvention CONVENTION = IborDepositConvention.builder().withCalendar(CALENDAR).withIborIndex(INDEX).build();

  /**
   * Tests that the calendar cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    IborDepositConvention.builder().withCalendar(null).withIborIndex(INDEX);
  }

  /**
   * Tests that the index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    IborDepositConvention.builder().withCalendar(CALENDAR).withIborIndex(null);
  }

  /**
   * Tests that the calendar must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCalendarSet() {
    IborDepositConvention.builder().withIborIndex(INDEX).build();
  }

  /**
   * Tests that the index must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIndexSet() {
    IborDepositConvention.builder().withCalendar(CALENDAR).build();
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CONVENTION.toCurveInstrument(null, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, 1, 0.03);
  }

  /**
   * Tests that the start tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartTenor() {
    CONVENTION.toCurveInstrument(ZonedDateTime.now(), null, Tenor.ONE_MONTH, 1, 0.02);
  }

  /**
   * Tests that the end tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndTenor() {
    CONVENTION.toCurveInstrument(ZonedDateTime.now(), Tenor.ON, null, 1, 0.02);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    IborDepositConvention other = IborDepositConvention.builder().withCalendar(CALENDAR).withIborIndex(INDEX).build();
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    assertEquals(CONVENTION.toString(), "IborDepositConvention [index=IborIndex[NAME, currency=USD, tenor=P3M, day count=Actual/360, "
        + "business day convention=Modified Following, spot lag=2, end-of-month], tenor=Tenor[P3M], calendar=Saturday / Sunday: [SATURDAY, SUNDAY]]");
    other = IborDepositConvention.builder().withCalendar(WeekendWorkingDayCalendar.FRIDAY_SATURDAY).withIborIndex(INDEX).build();
    assertNotEquals(CONVENTION, other);
    other = IborDepositConvention.builder().withCalendar(CALENDAR)
        .withIborIndex(new IborTypeIndex("NAME", Currency.USD, Tenor.THREE_MONTHS, 1,
            DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true)).build();
    assertNotEquals(CONVENTION, other);
  }

  /**
   * Tests that the definitions is the same as that produced using the generator.
   */
  @Test
  public void testGeneratorEquivalence() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 7, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.ONE_MONTH;
    final double rate = 0.01;
    final GeneratorDepositIbor generator = new GeneratorDepositIbor("", IndexConverter.toIborIndex(INDEX), new CalendarAdapter(CALENDAR));
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(startTenor.getPeriod(), endTenor.getPeriod());
    assertEquals(CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate), generator.generateInstrument(date, rate, 1, attribute));
  }

  /**
   * Tests the definition.
   */
  @Test
  public void testDefinition() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 1, 27);
    final double rate = 0.01;
    final CashDefinition cash = CONVENTION.toCurveInstrument(date, null, null, 1, rate);
    assertEquals(cash.getStartDate(), DateUtils.getUTCDate(2017, 1, 31));
    assertEquals(cash.getEndDate(), DateUtils.getUTCDate(2017, 4, 30));
    assertEquals(cash.getAccrualFactor(), 89 / 360., 1e-15);
    assertEquals(cash.getRate(), rate, 1e-15);
    assertEquals(cash.getCurrency(), INDEX.getCurrency());
    assertEquals(cash.getNotional(), 1, 1e-15);
  }
}
