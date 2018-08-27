/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = false)
public class NonVersionedRedisHolidaySourceTest extends AbstractRedisTestCase {

  @Test
  public void addGetByUniqueIdEmpty() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final Holiday result = source.get(UniqueId.of("TEST", "No Such Thing"));
    assertNull(result);
  }

  @Test
  public void addGetByUniqueIdCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    source.addHoliday(usd);

    final Holiday result = source.get(usd.getUniqueId());
    assertNotNull(result);
    assertEquals(Currency.USD, result.getCurrency());
    assertEquals(usd.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.CURRENCY, result.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());
  }

  @Test
  public void isHolidayCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    source.addHoliday(usd);

    assertTrue(source.isHoliday(LocalDate.now(), Currency.USD));
    assertFalse(source.isHoliday(LocalDate.now(), Currency.CAD));
    assertFalse(source.isHoliday(LocalDate.now().plusDays(1), Currency.USD));
    final LocalDate saturday = LocalDate.of(2012, 2, 11);
    assertTrue(source.isHoliday(saturday, Currency.USD));
  }

  @Test
  public void addGetByUniqueIdRegion() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usBank = generateHoliday(20);
    usBank.setType(HolidayType.BANK);
    usBank.setRegionExternalId(ExternalId.of("RegionScheme", "Chicago"));
    usBank.setUniqueId(UniqueId.of("TEST", "USD Test Bank Holiday"));
    source.addHoliday(usBank);

    final Holiday result = source.get(usBank.getUniqueId());
    assertNotNull(result);
    assertEquals(usBank.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.BANK, result.getType());
    assertNull(usBank.getCurrency());
    assertEquals(usBank.getRegionExternalId(), result.getRegionExternalId());
    assertNull(result.getExchangeExternalId());
  }

  @Test
  public void isHolidayByTypeExternalId() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final ExternalId exchangeId = ExternalId.of("ExchangeScheme", "Eurex");
    final SimpleHoliday holiday1 = generateHoliday(20);
    holiday1.setType(HolidayType.TRADING);

    holiday1.setExchangeExternalId(exchangeId);
    holiday1.setUniqueId(UniqueId.of("EUREX", "1"));
    source.addHoliday(holiday1);

    final SimpleHoliday holiday2 = generateHoliday(20);
    holiday2.setType(HolidayType.SETTLEMENT);
    holiday2.setExchangeExternalId(exchangeId);
    holiday2.setUniqueId(UniqueId.of("EUREX", "2"));
    source.addHoliday(holiday2);

    assertTrue(source.isHoliday(LocalDate.now(), HolidayType.TRADING, exchangeId));
    compareVsWeekends(LocalDate.now().minusDays(10), source, HolidayType.TRADING, exchangeId);
    compareVsWeekends(LocalDate.now().plusYears(1), source, HolidayType.TRADING, exchangeId);

    assertTrue(source.isHoliday(LocalDate.now(), HolidayType.SETTLEMENT, exchangeId));
    compareVsWeekends(LocalDate.now().minusDays(10), source, HolidayType.SETTLEMENT, exchangeId);
    compareVsWeekends(LocalDate.now().plusYears(1), source, HolidayType.SETTLEMENT, exchangeId);

    final LocalDate saturday = LocalDate.of(2012, 2, 11);
    assertTrue(source.isHoliday(saturday, HolidayType.SETTLEMENT, exchangeId));
    assertTrue(source.isHoliday(saturday, HolidayType.TRADING, exchangeId));
  }

  protected void compareVsWeekends(final LocalDate date, final NonVersionedRedisHolidaySource source, final HolidayType holidayType, final ExternalId id) {
    final boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    assertEquals(isWeekend, source.isHoliday(date, holidayType, id));
  }

  protected SimpleHoliday generateHoliday(final int nHolidays) {
    final SimpleHoliday holiday = new SimpleHoliday();

    LocalDate date = LocalDate.now();
    for (int i = 0; i < nHolidays; i++) {
      holiday.addHolidayDate(date);
      date = date.plusDays(3);
    }

    return holiday;
  }

}
