/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument.Meta;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidayDocument}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayDocumentTest extends AbstractFudgeBuilderTestCase {
  private static final List<LocalDate> DATES = Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018,  12,  31));
  private static final Instant CORRECTION_FROM = Instant.ofEpochSecond(10000L);
  private static final Instant CORRECTION_TO = CORRECTION_FROM.plusSeconds(100000L);
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(5000L);
  private static final Instant VERSION_TO = VERSION_FROM.plusSeconds(1000000L);
  private static final ExternalId PROVIDER_ID = ExternalId.of("holiday", "provider");
  private static final UniqueId UID = UniqueId.of("unique", "id");

  /**
   * Tests that the holiday cannot be null when constructing the document.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHoliday() {
    new HolidayDocument(null);
  }

  /**
   * Tests the behaviour when the type is not set.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNoType() {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    new HolidayDocument(holiday);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    final HolidayDocument document = new HolidayDocument(holiday);
    document.setCorrectionFromInstant(CORRECTION_FROM);
    document.setCorrectionToInstant(CORRECTION_TO);
    document.setProviderId(PROVIDER_ID);
    document.setVersionFromInstant(VERSION_FROM);
    document.setVersionToInstant(VERSION_TO);
    assertEquals(document, document);
    assertNotEquals(null, document);
    assertEquals(document.toString(), "HolidayDocument{versionFromInstant=1970-01-01T01:23:20Z, versionToInstant=1970-01-12T15:10:00Z, "
        + "correctionFromInstant=1970-01-01T02:46:40Z, correctionToInstant=1970-01-02T06:33:20Z, holiday=ManageableHoliday{uniqueId=unique~id, "
        + "type=BANK, regionExternalId=ISO_COUNTRY_ALPHA2~US, exchangeExternalId=null, customExternalId=null, currency=null, "
        + "holidayDates=[2018-01-01, 2018-12-31]}, uniqueId=unique~id, name=US, providerId=holiday~provider}");
    assertEquals(document.getCorrectionFromInstant(), CORRECTION_FROM);
    assertEquals(document.getCorrectionToInstant(), CORRECTION_TO);
    assertEquals(document.getHoliday(), holiday);
    assertEquals(document.getName(), "US");
    assertEquals(document.getObjectId(), UID.getObjectId());
    assertEquals(document.getProviderId(), PROVIDER_ID);
    assertEquals(document.getUniqueId(), UID);
    assertEquals(document.getValue(), holiday);
    assertEquals(document.getVersionFromInstant(), VERSION_FROM);
    assertEquals(document.getVersionToInstant(), VERSION_TO);
    final HolidayDocument other = new HolidayDocument(holiday);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    other.setCorrectionToInstant(CORRECTION_TO);
    other.setProviderId(PROVIDER_ID);
    other.setVersionFromInstant(VERSION_FROM);
    other.setVersionToInstant(VERSION_TO);
    assertEquals(document, other);
    assertEquals(document.hashCode(), other.hashCode());
    other.setHoliday(null);
    assertNotEquals(document, other);
    other.setHoliday(holiday);
    other.setCorrectionFromInstant(CORRECTION_TO);
    assertNotEquals(document, other);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    other.setCorrectionToInstant(CORRECTION_FROM);
    assertNotEquals(document, other);
    other.setCorrectionToInstant(CORRECTION_TO);
    other.setName("NAME");
    assertNotEquals(document, other);
    other.setName("US");
    other.setProviderId(ExternalId.of("other", "id"));
    assertNotEquals(document, other);
    other.setProviderId(PROVIDER_ID);
    other.setUniqueId(UniqueId.of("other", "uid"));
    assertNotEquals(document, other);
    other.setUniqueId(UID);
    other.setVersionFromInstant(VERSION_TO);
    assertNotEquals(document, other);
    other.setVersionFromInstant(VERSION_FROM);
    other.setVersionToInstant(VERSION_FROM);
    assertNotEquals(document, other);
  }

  /**
   * Tests name construction.
   */
  @Test
  public void testName() {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    HolidayDocument document = new HolidayDocument(holiday);
    assertEquals(document.getName(), "US");

    holiday.setType(HolidayType.CURRENCY);
    holiday.setCurrency(Currency.USD);
    document.setHoliday(holiday);
    // currently the name isn't changed - need to fix
    assertEquals(document.getName(), "US");
    document = new HolidayDocument(holiday);
    assertEquals(document.getName(), "USD");

    holiday.setType(HolidayType.SETTLEMENT);
    holiday.setExchangeExternalId(ExternalId.of("exchange", "lse"));
    document.setHoliday(holiday);
    assertEquals(document.getName(), "USD");
    document = new HolidayDocument(holiday);
    assertEquals(document.getName(), "lse");

    holiday.setType(HolidayType.TRADING);
    holiday.setExchangeExternalId(ExternalId.of("exchange", "nyse"));
    document.setHoliday(holiday);
    assertEquals(document.getName(), "lse");
    document = new HolidayDocument(holiday);
    assertEquals(document.getName(), "nyse");

    holiday.setType(HolidayType.CUSTOM);
    holiday.setCustomExternalId(ExternalId.of("custom", "id"));
    document.setHoliday(holiday);
    assertEquals(document.getName(), "nyse");
    document = new HolidayDocument(holiday);
    assertEquals(document.getName(), "id");
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    final HolidayDocument document = new HolidayDocument(holiday);
    document.setCorrectionFromInstant(CORRECTION_FROM);
    document.setCorrectionToInstant(CORRECTION_TO);
    document.setProviderId(PROVIDER_ID);
    document.setVersionFromInstant(VERSION_FROM);
    document.setVersionToInstant(VERSION_TO);
    final Meta metaBean = document.metaBean();
    assertEquals(metaBean.correctionFromInstant().get(document), CORRECTION_FROM);
    assertEquals(metaBean.correctionToInstant().get(document), CORRECTION_TO);
    assertEquals(metaBean.holiday().get(document), holiday);
    assertEquals(metaBean.providerId().get(document), PROVIDER_ID);
    assertEquals(metaBean.uniqueId().get(document), UID);
    assertEquals(metaBean.versionFromInstant().get(document), VERSION_FROM);
    assertEquals(metaBean.versionToInstant().get(document), VERSION_TO);
    assertEquals(document.property("correctionFromInstant").get(), CORRECTION_FROM);
    assertEquals(document.property("correctionToInstant").get(), CORRECTION_TO);
    assertEquals(document.property("holiday").get(), holiday);
    assertEquals(document.property("providerId").get(), PROVIDER_ID);
    assertEquals(document.property("uniqueId").get(), UID);
    assertEquals(document.property("versionFromInstant").get(), VERSION_FROM);
    assertEquals(document.property("versionToInstant").get(), VERSION_TO);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    final HolidayDocument document = new HolidayDocument(holiday);
    document.setCorrectionFromInstant(CORRECTION_FROM);
    document.setCorrectionToInstant(CORRECTION_TO);
    document.setProviderId(PROVIDER_ID);
    document.setVersionFromInstant(VERSION_FROM);
    document.setVersionToInstant(VERSION_TO);
    assertEncodeDecodeCycle(HolidayDocument.class, document);
  }
}
