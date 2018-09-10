/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.HolidayWithWeekendAdapter;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidayWithWeekendAdapter}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayWithWeekendAdapterTest extends AbstractFudgeBuilderTestCase {
  private static final SimpleHoliday HOLIDAY = new SimpleHoliday(Arrays.asList(LocalDate.of(2018, 9, 9), LocalDate.of(2018, 9, 10)));
  private static final HolidayWithWeekendAdapter SAT_SUN = new HolidayWithWeekendAdapter(HOLIDAY);
  private static final HolidayWithWeekendAdapter FRI_SAT = new HolidayWithWeekendAdapter(HOLIDAY, WeekendType.FRIDAY_SATURDAY);
  static {
    HOLIDAY.setCurrency(Currency.AUD);
    HOLIDAY.setType(HolidayType.CURRENCY);
  }

  /**
   * Tests that the holiday cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHoliday1() {
    new HolidayWithWeekendAdapter(null);
  }

  /**
   * Tests that the holiday cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHoliday2() {
    new HolidayWithWeekendAdapter().setHoliday(null);
  }

  /**
   * Tests that the holiday cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHoliday3() {
    new HolidayWithWeekendAdapter(null, WeekendType.SATURDAY_SUNDAY);
  }

  /**
   * Tests that the weekend type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendType1() {
    new HolidayWithWeekendAdapter(HOLIDAY, null);
  }

  /**
   * Tests that the weekend type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeekendType2() {
    new HolidayWithWeekendAdapter().setWeekendType(null);
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetCurrencyNullHoliday() {
    new HolidayWithWeekendAdapter().getCurrency();
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetTypeNullHoliday() {
    new HolidayWithWeekendAdapter().getType();
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetRegionNullHoliday() {
    new HolidayWithWeekendAdapter().getRegionExternalId();
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetExchangeNullHoliday() {
    new HolidayWithWeekendAdapter().getExchangeExternalId();
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetCustomExternalIdNullHoliday() {
    new HolidayWithWeekendAdapter().getCustomExternalId();
  }

  /**
   * Tests that the underlying must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetHolidayDatesNullHoliday() {
    new HolidayWithWeekendAdapter().getHolidayDates();
  }

  /**
   * Tests that the unique id has not been set, as the holiday has not been stored in a master.
   */
  @Test
  public void testUniqueIdIsNull() {
    assertNull(FRI_SAT.getUniqueId());
  }

  /**
   * Tests delegation to the underlying.
   */
  @Test
  public void testDelegation() {
    assertEquals(FRI_SAT.getCurrency(), HOLIDAY.getCurrency());
    assertEquals(FRI_SAT.getCustomExternalId(), HOLIDAY.getCustomExternalId());
    assertEquals(FRI_SAT.getExchangeExternalId(), HOLIDAY.getExchangeExternalId());
    assertEquals(FRI_SAT.getHolidayDates(), HOLIDAY.getHolidayDates());
    assertEquals(FRI_SAT.getRegionExternalId(), HOLIDAY.getRegionExternalId());
    assertEquals(FRI_SAT.getType(), HOLIDAY.getType());
  }

  /**
   * Tests the weekend type.
   */
  @Test
  public void testWeekendType() {
    assertEquals(FRI_SAT.getWeekendType(), WeekendType.FRIDAY_SATURDAY);
    assertEquals(SAT_SUN.getWeekendType(), WeekendType.SATURDAY_SUNDAY);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(HolidayWithWeekendAdapter.class, FRI_SAT);
    assertEncodeDecodeCycle(HolidayWithWeekendAdapter.class, SAT_SUN);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(FRI_SAT.metaBean());
    assertNotNull(FRI_SAT.metaBean().holiday());
    assertNotNull(FRI_SAT.metaBean().weekendType());
    assertNotNull(FRI_SAT.metaBean().uniqueId());
    assertEquals(FRI_SAT.metaBean().holiday().get(FRI_SAT), HOLIDAY);
    assertEquals(FRI_SAT.metaBean().weekendType().get(FRI_SAT), WeekendType.FRIDAY_SATURDAY);
    assertNull(FRI_SAT.metaBean().uniqueId().get(FRI_SAT));
    assertEquals(FRI_SAT.property("holiday").get(), HOLIDAY);
    assertEquals(FRI_SAT.property("weekendType").get(), WeekendType.FRIDAY_SATURDAY);
    assertNull(FRI_SAT.property("uniqueId").get());
  }

  /**
   * Tests the Object methods.
   */
  @Test
  public void testObject() {
    assertEquals(FRI_SAT, FRI_SAT);
    assertNotEquals(null, FRI_SAT);
    assertNotEquals(HOLIDAY, FRI_SAT);
    final HolidayWithWeekendAdapter other = new HolidayWithWeekendAdapter(HOLIDAY, WeekendType.FRIDAY_SATURDAY);
    assertEquals(FRI_SAT, other);
    assertEquals(FRI_SAT.hashCode(), other.hashCode());
    assertNotEquals(FRI_SAT, new HolidayWithWeekendAdapter(new SimpleHoliday(), WeekendType.FRIDAY_SATURDAY));
    assertNotEquals(FRI_SAT, SAT_SUN);
    assertEquals(FRI_SAT.toString(), "HolidayWithWeekendAdapter{uniqueId=null, "
        + "holiday=SimpleHoliday{uniqueId=null, type=CURRENCY, regionExternalId=null, "
        + "exchangeExternalId=null, customExternalId=null, currency=AUD, "
        + "holidayDates=[2018-09-09, 2018-09-10]}, weekendType=FRIDAY_SATURDAY}");
  }
}
