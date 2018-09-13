/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.fiftyonred.mock_jedis.MockJedisPool;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Test for {@link NonVersionedRedisHolidaySource}.
 */
@Test(groups = TestGroup.UNIT)
public class NonVersionedRedisHolidaySourceTest extends AbstractRedisTestCase {
  private MockJedisPool _pool;

  @Override
  @BeforeClass
  public void launchJedisPool() {
    final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    _pool = new MockJedisPool(config, "host");
  }

  @Override
  @AfterClass
  public void clearJedisPool() {
    if (_pool == null) {
      return;
    }
    _pool.getResource().close();
    _pool.destroy();
  }

  @Override
  @BeforeMethod
  public void clearRedisDb() {
    final Jedis jedis = _pool.getResource();
    jedis.flushDB();
    _pool.returnResource(jedis);
  }

  @Override
  protected JedisPool getJedisPool() {
    return _pool;
  }

  @Override
  protected String getRedisPrefix() {
    return "prefix";
  }

  /**
   * Tests conversion of ids.
   */
  @Test
  public void testKeyConversion() {
    final UniqueId uid = UniqueId.of("uid", "1");
    final ObjectId oid = ObjectId.of("oid", "10");
    final ExternalId eid = ExternalId.of("eid", "100");
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), "prefix");
    assertEquals(source.toRedisKey(uid), "prefix-UNQ-uid~1");
    assertEquals(source.toRedisKey(oid), "prefix-UNQ-oid~10");
    assertEquals(source.toRedisKey(eid, HolidayType.SETTLEMENT), "prefix-EXT-eid~100-SETTLEMENT");
    source = new NonVersionedRedisHolidaySource(getJedisPool());
    assertEquals(source.toRedisKey(uid), "UNQ-uid~1");
    assertEquals(source.toRedisKey(oid), "UNQ-oid~10");
    assertEquals(source.toRedisKey(eid, HolidayType.SETTLEMENT), "EXT-eid~100-SETTLEMENT");
  }

  /**
   * Tests the result when there is no holiday for a unique id.
   */
  @Test
  public void testGetByUniqueIdEmpty() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    final Holiday result = source.get(UniqueId.of("TEST", "No Such Thing"));
    assertNull(result);
  }

  /**
   * Tests getting a currency holiday using the unique id.
   */
  @Test
  public void testGetByUniqueIdCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    final SimpleHoliday cad = generateHoliday(20);
    cad.setCurrency(Currency.CAD);
    cad.setType(HolidayType.CURRENCY);
    cad.setUniqueId(UniqueId.of("TEST", "CAD Test Holiday"));

    source.addHoliday(usd);
    source.addHoliday(cad);

    final Holiday result = source.get(usd.getUniqueId());
    assertNotNull(result);
    assertEquals(Currency.USD, result.getCurrency());
    assertEquals(usd.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.CURRENCY, result.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());

    final Map<UniqueId, Holiday> holidays = source.get(Arrays.asList(usd.getUniqueId(), cad.getUniqueId()));
    assertEquals(holidays.size(), 2);
    assertEquals(holidays.get(usd.getUniqueId()), usd);
    assertEquals(holidays.get(cad.getUniqueId()), cad);
  }

  /**
   * Tests getting a currency holiday.
   */
  @Test
  public void testGetByCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));

    source.addHoliday(usd);

    final Collection<Holiday> result = source.get(Currency.USD);
    assertNotNull(result);
    final Holiday holiday = result.iterator().next();
    assertEquals(Currency.USD, holiday.getCurrency());
    assertEquals(usd.getUniqueId(), holiday.getUniqueId());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());
  }

  /**
   * Tests getting a currency holiday using the object id.
   */
  @Test
  public void testGetByObjectIdCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    final SimpleHoliday cad = generateHoliday(20);
    cad.setCurrency(Currency.CAD);
    cad.setType(HolidayType.CURRENCY);
    cad.setUniqueId(UniqueId.of("TEST", "CAD Test Holiday"));

    source.addHoliday(usd);
    source.addHoliday(cad);

    final Holiday result = source.get(ObjectId.of("TEST", "USD Test Holiday"), VersionCorrection.LATEST);
    assertNotNull(result);
    assertEquals(Currency.USD, result.getCurrency());
    assertEquals(usd.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.CURRENCY, result.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());

    final Map<ObjectId, Holiday> holidays = source.get(
        Arrays.asList(ObjectId.of("TEST", "USD Test Holiday"), ObjectId.of("TEST", "CAD Test Holiday")), VersionCorrection.LATEST);
    assertEquals(holidays.size(), 2);
    assertEquals(holidays.get(ObjectId.of("TEST", "USD Test Holiday")), usd);
    assertEquals(holidays.get(ObjectId.of("TEST", "CAD Test Holiday")), cad);
  }

  /**
   * Tests getting a currency holiday using the holiday type and external id.
   */
  @Test
  public void testGetByExternalIdHolidayType() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CUSTOM);
    usd.setCustomExternalId(ExternalId.of("TEST", "USD Test Holiday"));
    final SimpleHoliday cad = generateHoliday(20);
    cad.setCurrency(Currency.CAD);
    cad.setType(HolidayType.CUSTOM);
    cad.setCustomExternalId(ExternalId.of("TEST", "CAD Test Holiday"));

    source.addHoliday(usd);
    source.addHoliday(cad);

    final Collection<Holiday> result = source.get(HolidayType.CUSTOM, ExternalId.of("TEST", "USD Test Holiday").toBundle());
    assertNotNull(result);
    assertEquals(result.size(), 1);
    final Holiday holiday = result.iterator().next();
    assertNull(holiday.getCurrency());
    assertEquals(usd.getUniqueId(), holiday.getUniqueId());
    assertEquals(HolidayType.CUSTOM, holiday.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());
  }

  /**
  * Tests whether or not a date is a holiday in a currency.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayCurrency() {
    final NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());

    final SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    source.addHoliday(usd);

    assertTrue(source.isHoliday(LocalDate.of(2018, 9, 8), Currency.USD));
    assertFalse(source.isHoliday(LocalDate.of(2018, 9, 10), Currency.CAD));
    assertFalse(source.isHoliday(LocalDate.of(2018, 9, 11), Currency.USD));
    assertTrue(source.isHoliday(LocalDate.of(2018, 9, 12), Currency.USD));
  }

  /**
   * Tests getting a region holiday using the unique id.
   */
  @Test
  public void testGetByUniqueIdRegion() {
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

  /**
   * Tests whether or not a date is a holiday in a region.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayByTypeExternalId() {
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

    final LocalDate date = LocalDate.of(2018, 9, 6);
    assertTrue(source.isHoliday(date, HolidayType.TRADING, exchangeId));
    compareVsWeekends(date.minusDays(10), source, HolidayType.TRADING, exchangeId);
    compareVsWeekends(date.plusYears(1), source, HolidayType.TRADING, exchangeId);

    assertTrue(source.isHoliday(date, HolidayType.SETTLEMENT, exchangeId));
    compareVsWeekends(date.minusDays(10), source, HolidayType.SETTLEMENT, exchangeId);
    compareVsWeekends(date.plusYears(1), source, HolidayType.SETTLEMENT, exchangeId);

    assertTrue(source.isHoliday(LocalDate.of(2018, 9, 8), HolidayType.SETTLEMENT, exchangeId));
    assertTrue(source.isHoliday(LocalDate.of(2018, 9, 8), HolidayType.TRADING, exchangeId));
  }

  /**
   * Tests that a Saturday or Sunday is a holiday.
   *
   * @param date  the date
   * @param source  the holiday source
   * @param holidayType  the type
   * @param id  the id
   */
  @SuppressWarnings("deprecation")
  protected void compareVsWeekends(final LocalDate date, final NonVersionedRedisHolidaySource source, final HolidayType holidayType, final ExternalId id) {
    final boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    assertEquals(isWeekend, source.isHoliday(date, holidayType, id));
  }

  /**
   * Generates holidays from today.
   *
   * @param nHolidays  the number of holiday dates
   * @return  the holiday
   */
  protected SimpleHoliday generateHoliday(final int nHolidays) {
    final SimpleHoliday holiday = new SimpleHoliday();

    LocalDate date = LocalDate.of(2018, 9, 6);
    for (int i = 0; i < nHolidays; i++) {
      holiday.addHolidayDate(date);
      date = date.plusDays(3);
    }

    return holiday;
  }

}
