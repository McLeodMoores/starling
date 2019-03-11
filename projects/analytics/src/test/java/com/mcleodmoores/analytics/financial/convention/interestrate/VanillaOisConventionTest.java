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
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.EmptyWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link VanillaOisConvention}.
 */
public class VanillaOisConventionTest {
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Tenor PAYMENT_TENOR = Tenor.ONE_YEAR;
  private static final StubType STUB_TYPE = StubType.SHORT_END;
  private static final int PAYMENT_LAG = 1;
  private static final int SPOT_LAG = 2;
  private static final OvernightIndex INDEX = new OvernightIndex("INDEX", Currency.USD, DayCounts.ACT_360, 1);
  private static final EndOfMonthConvention EOM = EndOfMonthConvention.IGNORE_END_OF_MONTH;
  private static final VanillaOisConvention CONVENTION = VanillaOisConvention.builder()
      .withBusinessDayConvention(BDC)
      .withCalendar(CALENDAR)
      .withEndOfMonth(EOM)
      .withPaymentLag(PAYMENT_LAG)
      .withPaymentTenor(PAYMENT_TENOR)
      .withSpotLag(SPOT_LAG)
      .withStubType(STUB_TYPE)
      .withUnderlyingIndex(INDEX)
      .build();

  /**
   * Tests that the business day convention cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    VanillaOisConvention.builder().withBusinessDayConvention(null);
  }

  /**
   * Tests that the business day convention must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBusinessDayConventionIsSet() {
    VanillaOisConvention.builder()
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the calendar cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    VanillaOisConvention.builder().withCalendar(null);
  }

  /**
   * Tests that the calendar must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCalendarIsSet() {
    VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the end of month convention cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndOfMonthConvention() {
    VanillaOisConvention.builder().withEndOfMonth(null);
  }

  /**
   * Tests that the end of month convention must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testEndOfMonthConventionIsSet() {
    VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the payment lag must be positive.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentLeg() {
    VanillaOisConvention.builder().withPaymentLag(-1);
  }

  /**
   * Tests that the payment tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTenor() {
    VanillaOisConvention.builder().withPaymentTenor(null);
  }

  /**
   * Tests that the payment tenor must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPaymentTenorIsSet() {
    VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
  }
  /**
   * Tests that the spot lag must be positive.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSpotLag() {
    VanillaOisConvention.builder().withSpotLag(-1);
  }

  /**
   * Tests that the stub type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStubType() {
    VanillaOisConvention.builder().withStubType(null);
  }

  /**
   * Tests that the stub type must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testStubTypeIsSet() {
    VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withUnderlyingIndex(INDEX)
    .build();
  }

  /**
   * Tests that the index cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    VanillaOisConvention.builder().withUnderlyingIndex(null);
  }

  /**
   * Tests that the index must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testIndexIsSet() {
    VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    VanillaOisConvention other = VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(STUB_TYPE)
    .withUnderlyingIndex(INDEX)
    .build();
    assertEquals(CONVENTION, other);
    assertEquals(CONVENTION.hashCode(), other.hashCode());
    assertEquals(CONVENTION.toString(), "VanillaOisConvention [index=OvernightIndex[INDEX, currency=USD, day count=Actual/360, publication lag=1], "
        + "paymentTenor=Tenor[P1Y], endOfMonth=false, calendar=Saturday / Sunday: [SATURDAY, SUNDAY], businessDayConvention=BusinessDayConvention [Following], "
        + "isShortStub=true, isLegGeneratedFromEnd=false, paymentLag=1, spotLag=2]");
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BusinessDayConventions.PRECEDING)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(WeekendWorkingDayCalendar.FRIDAY_SATURDAY)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG + 1)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(Tenor.SIX_MONTHS)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG + 1)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(StubType.LONG_END)
        .withUnderlyingIndex(INDEX)
        .build();
    assertNotEquals(CONVENTION, other);
    other = VanillaOisConvention.builder()
        .withBusinessDayConvention(BDC)
        .withCalendar(CALENDAR)
        .withEndOfMonth(EOM)
        .withPaymentLag(PAYMENT_LAG)
        .withPaymentTenor(PAYMENT_TENOR)
        .withSpotLag(SPOT_LAG)
        .withStubType(STUB_TYPE)
        .withUnderlyingIndex(new OvernightIndex("", Currency.USD, DayCounts.ACT_36525, 0))
        .build();
    assertNotEquals(CONVENTION, other);
  }

  /**
   * Tests that the definition is the same as that produced using the generator.
   */
  @Test
  public void testGeneratorEquivalence() {
    final VanillaOisConvention convention = VanillaOisConvention.builder()
    .withBusinessDayConvention(BDC)
    .withCalendar(CALENDAR)
    .withEndOfMonth(EOM)
    .withPaymentLag(PAYMENT_LAG)
    .withPaymentTenor(PAYMENT_TENOR)
    .withSpotLag(SPOT_LAG)
    .withStubType(StubType.SHORT_START) // generator hard-codes to short start, from end
    .withUnderlyingIndex(INDEX)
    .build();
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 8, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.FIVE_YEARS;
    final double rate = 0.01;
    final GeneratorSwapFixedON generator =
        new GeneratorSwapFixedON("", IndexConverter.toIndexOn(INDEX), PAYMENT_TENOR.getPeriod(), DayCounts.ACT_365,
            BDC, EOM == EndOfMonthConvention.ADJUST_FOR_END_OF_MONTH, SPOT_LAG, PAYMENT_LAG, CalendarAdapter.of(CALENDAR));
    final GeneratorAttributeIR attribute = new GeneratorAttributeIR(startTenor.getPeriod(), endTenor.getPeriod());
    assertEquals(convention.toCurveInstrument(date, startTenor, endTenor, 1, rate), generator.generateInstrument(date, rate, 1, attribute));
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
    final SwapFixedONDefinition swap = CONVENTION.toCurveInstrument(date, startTenor, endTenor, 1, rate);
    assertTrue(swap.getFirstLeg() instanceof AnnuityCouponFixedDefinition);
    assertTrue(swap.getSecondLeg() instanceof AnnuityCouponONDefinition);
  }

  /**
   * Tests the stub type logic.
   */
  @Test
  public void testStubType() {
    // no lags, adjustments or holidays for these calculations
    final VanillaOisConvention.Builder builder = VanillaOisConvention.builder()
      .withBusinessDayConvention(BusinessDayConventions.NONE)
      .withCalendar(EmptyWorkingDayCalendar.INSTANCE)
      .withEndOfMonth(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withPaymentLag(0)
      .withPaymentTenor(PAYMENT_TENOR)
      .withSpotLag(0)
      .withUnderlyingIndex(new OvernightIndex("INDEX", Currency.USD, DayCounts.ACT_360, 0));
    final ZonedDateTime date = DateUtils.getUTCDate(2017, 8, 31);
    final Tenor startTenor = Tenor.of(Period.ZERO);
    final Tenor endTenor = Tenor.ofMonths(25); // 2Y + 1M
    final double rate = 0.01;
    // no stub - 2 coupons
    AnnuityCouponFixedDefinition fixedLeg = builder.withStubType(StubType.NONE).build().toCurveInstrument(date, startTenor, endTenor, 1, rate).getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), 2);
    assertEquals(fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate(), date.toLocalDate());
    assertEquals(fixedLeg.getNthPayment(0).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2).plusMonths(1));
    // short start - one month at start
    fixedLeg = builder.withStubType(StubType.SHORT_START).build().toCurveInstrument(date, startTenor, endTenor, 1, rate).getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), 3);
    assertEquals(fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate(), date.toLocalDate());
    assertEquals(fixedLeg.getNthPayment(0).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(1).plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(2).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(1).plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(2).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2).plusMonths(1));
    // short end - one month at end
    fixedLeg = builder.withStubType(StubType.SHORT_END).build().toCurveInstrument(date, startTenor, endTenor, 1, rate).getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), 3);
    assertEquals(fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate(), date.toLocalDate());
    assertEquals(fixedLeg.getNthPayment(0).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2));
    assertEquals(fixedLeg.getNthPayment(2).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(2));
    assertEquals(fixedLeg.getNthPayment(2).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2).plusMonths(1));
    // long start - eleven months at start
    fixedLeg = builder.withStubType(StubType.LONG_START).build().toCurveInstrument(date, startTenor, endTenor, 1, rate).getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), 2);
    assertEquals(fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate(), date.toLocalDate());
    assertEquals(fixedLeg.getNthPayment(0).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(1).plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(1).plusMonths(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2).plusMonths(1));
    // long end - eleven months at end
    fixedLeg = builder.withStubType(StubType.LONG_END).build().toCurveInstrument(date, startTenor, endTenor, 1, rate).getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), 2);
    assertEquals(fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate(), date.toLocalDate());
    assertEquals(fixedLeg.getNthPayment(0).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualStartDate().toLocalDate(), date.toLocalDate().plusYears(1));
    assertEquals(fixedLeg.getNthPayment(1).getAccrualEndDate().toLocalDate(), date.toLocalDate().plusYears(2).plusMonths(1));
  }
}
