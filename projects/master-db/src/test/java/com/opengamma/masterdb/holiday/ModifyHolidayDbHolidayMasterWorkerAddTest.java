/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyHolidayDbHolidayMasterWorkerAddTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyHolidayDbHolidayMasterWorkerAddTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addHoliday_nullDocument() {
    _holMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noHoliday() {
    final HolidayDocument doc = new HolidayDocument();
    _holMaster.add(doc);
  }

  @Test
  public void test_add_add_currency() {
    final Instant now = Instant.now(_holMaster.getClock());

    final ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    final HolidayDocument doc = new HolidayDocument(holiday);
    final String name = doc.getName();
    final HolidayDocument test = _holMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHol", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uniqueId, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.CURRENCY, testHoliday.getType());
    assertEquals("USD", testHoliday.getCurrency().getCode());
    assertEquals(null, testHoliday.getRegionExternalId());
    assertEquals(null, testHoliday.getExchangeExternalId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_bank() {
    final Instant now = Instant.now(_holMaster.getClock());

    final ManageableHoliday holiday = new ManageableHoliday(HolidayType.BANK, ExternalId.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    final HolidayDocument doc = new HolidayDocument(holiday);
    final String name = doc.getName();
    final HolidayDocument test = _holMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHol", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uniqueId, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.BANK, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(ExternalId.of("A", "B"), testHoliday.getRegionExternalId());
    assertEquals(null, testHoliday.getExchangeExternalId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_settlement() {
    final Instant now = Instant.now(_holMaster.getClock());

    final ManageableHoliday holiday = new ManageableHoliday(HolidayType.SETTLEMENT, ExternalId.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    final HolidayDocument doc = new HolidayDocument(holiday);
    final String name = doc.getName();
    final HolidayDocument test = _holMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHol", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uniqueId, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.SETTLEMENT, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionExternalId());
    assertEquals(ExternalId.of("A", "B"), testHoliday.getExchangeExternalId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_trading() {
    final Instant now = Instant.now(_holMaster.getClock());

    final ManageableHoliday holiday = new ManageableHoliday(HolidayType.TRADING, ExternalId.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    final HolidayDocument doc = new HolidayDocument(holiday);
    final String name = doc.getName();
    final HolidayDocument test = _holMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHol", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uniqueId, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.TRADING, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionExternalId());
    assertEquals(ExternalId.of("A", "B"), testHoliday.getExchangeExternalId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_addThenGet() {
    final ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    final HolidayDocument doc = new HolidayDocument(holiday);
    final HolidayDocument added = _holMaster.add(doc);

    final HolidayDocument test = _holMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingTypeProperty() {
    final ManageableHoliday holiday = new ManageableHoliday();
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test
  public void test_add_addBankWithMinimalProperties() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.BANK);
    holiday.setRegionExternalId(ExternalId.of("A", "B"));
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addBankWithMissingRegionProperty() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.BANK);
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test
  public void test_add_addCurrencyWithMinimalProperties() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.CURRENCY);
    holiday.setCurrency(Currency.USD);
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addCurrencyWithMissingCurrencyProperty() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.CURRENCY);
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test
  public void test_add_addSettlementWithMinimalProperties() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.SETTLEMENT);
    holiday.setExchangeExternalId(ExternalId.of("A", "B"));
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addSettlementWithMissingExchangeProperty() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.SETTLEMENT);
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test
  public void test_add_addTradingWithMinimalProperties() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.TRADING);
    holiday.setExchangeExternalId(ExternalId.of("A", "B"));
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addTradingWithMissingExchangeProperty() {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.TRADING);
    final HolidayDocument doc = new HolidayDocument();
    doc.setName("Test");
    doc.setHoliday(holiday);
    _holMaster.add(doc);
  }

}
