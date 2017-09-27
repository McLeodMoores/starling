/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.convention.interestrate.CurveDataConvention.EndOfMonthConvention;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CashConvention}.
 */
public class CashConventionTest {
  private static final Currency CCY = Currency.USD;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final int SPOT_LAG = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final EndOfMonthConvention EOM = EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH;
  private static final CashConvention CONVENTION = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY)
      .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();

  /**
   * Tests that the business day convention cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    CashConvention.builder().withBusinessDayConvention(null);
  }

  /**
   * Tests that the calendar cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    CashConvention.builder().withCalendar(null);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    CashConvention.builder().withCurrency(null);
  }

  /**
   * Tests that the day count cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    CashConvention.builder().withDayCount(null);
  }

  /**
   * Tests that the end of month convention cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndOfMonthConvention() {
    CashConvention.builder().withEndOfMonthConvention(null);
  }

  /**
   * Tests that the business day convention must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBusinessDayConventionSet() {
    CashConvention.builder().withCalendar(CALENDAR).withCurrency(CCY).withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM)
    .withSpotLag(SPOT_LAG).build();
  }

  /**
   * Tests that the calendar must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCalendarSet() {
    CashConvention.builder().withBusinessDayConvention(BDC).withCurrency(CCY).withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM)
    .withSpotLag(SPOT_LAG).build();
  }

  /**
   * Tests that the currency must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurrencySet() {
    CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM)
    .withSpotLag(SPOT_LAG).build();
  }

  /**
   * Tests that the end of month convention must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEomSet() {
    CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY).withDayCount(DAY_COUNT)
    .withSpotLag(SPOT_LAG).build();
  }

  /**
   * Tests that the day count must be set before building.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDayCountSet() {
    CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY).withEndOfMonthConvention(EOM)
    .withSpotLag(SPOT_LAG).build();
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CONVENTION.toCurveInstrument(null, Tenor.ON, Tenor.ONE_MONTH, 1, 0.02);
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
    assertEquals(CONVENTION.getBusinessDayConvention(), BDC);
    assertEquals(CONVENTION.getCalendar(), CALENDAR);
    assertEquals(CONVENTION.getCurrency(), CCY);
    assertEquals(CONVENTION.getDayCount(), DAY_COUNT);
    assertTrue(CONVENTION.isEndOfMonthConvention());
    assertEquals(CONVENTION.getSpotLag(), SPOT_LAG);
    CashConvention other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    final String expected =
        "CashConvention [currency=USD, calendar=Saturday / Sunday, spotLag=2, dayCount=Actual/360, businessDayConvention=Modified Following, endOfMonth=true]";
    assertEquals(CONVENTION.toString(), expected);
    other = CashConvention.builder().withBusinessDayConvention(BusinessDayConventions.FOLLOWING).withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();
    assertNotEquals(CONVENTION, other);
    other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(WeekendWorkingDayCalendar.FRIDAY_SATURDAY).withCurrency(CCY)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();
    assertNotEquals(CONVENTION, other);
    other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(Currency.EUR)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();
    assertNotEquals(CONVENTION, other);
    other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH).withSpotLag(SPOT_LAG).build();
    assertNotEquals(CONVENTION, other);
    other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DayCounts.ACT_365).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG).build();
    assertNotEquals(CONVENTION, other);
    other = CashConvention.builder().withBusinessDayConvention(BDC).withCalendar(CALENDAR).withCurrency(CCY)
        .withDayCount(DAY_COUNT).withEndOfMonthConvention(EOM).withSpotLag(SPOT_LAG + 1).build();
    assertNotEquals(CONVENTION, other);
  }

  /**
   * Tests that the cash definition is the same as that produced using the generator.
   */
  @Test
  public void testGeneratorEquivalence() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 7, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.ONE_MONTH;
    final double rate = 0.01;
    final GeneratorDeposit generator = new GeneratorDeposit("", CCY, new CalendarAdapter(CALENDAR), SPOT_LAG, DAY_COUNT, BDC, true);
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(startTenor.getPeriod(), endTenor.getPeriod());
    assertEquals(CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate), generator.generateInstrument(date, rate, 1, attribute));
  }

  /**
   * Tests the cash definition.
   */
  @Test
  public void testCashDefinition() {
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 1, 27);
    Tenor startTenor = Tenor.ON;
    Tenor endTenor = Tenor.ONE_MONTH;
    final double rate = 0.01;
    CashDefinition cash = CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate);
    assertEquals(cash.getStartDate(), DateUtils.getUTCDate(2017, 1, 31));
    assertEquals(cash.getEndDate(), DateUtils.getUTCDate(2017, 2, 28));
    assertEquals(cash.getAccrualFactor(), 28 / 360., 1e-15);
    assertEquals(cash.getRate(), rate, 1e-15);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getNotional(), 1, 1e-15);
    startTenor = Tenor.of(Period.ZERO);
    endTenor = Tenor.SN;
    cash = CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate);
    assertEquals(cash.getStartDate(), DateUtils.getUTCDate(2017, 1, 27));
    assertEquals(cash.getEndDate(), DateUtils.getUTCDate(2017, 1, 30));
    assertEquals(cash.getAccrualFactor(), 3 / 360., 1e-15);
    assertEquals(cash.getRate(), rate, 1e-15);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getNotional(), 1, 1e-15);
  }
}
