/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
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
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidaySearchSortOrder}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidaySearchSortOrderTest {
  private static final List<LocalDate> DATES = Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018,  12,  31));
  private static final Instant CORRECTION_FROM = Instant.ofEpochSecond(10000L);
  private static final Instant CORRECTION_TO = CORRECTION_FROM.plusSeconds(100000L);
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(5000L);
  private static final Instant VERSION_TO = VERSION_FROM.plusSeconds(1000000L);
  private static final ExternalId PROVIDER_ID = ExternalId.of("holiday", "provider");
  private static final UniqueId UID = UniqueId.of("unique", "id");
  private static final HolidayDocument DOCUMENT;
  static {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    DOCUMENT = new HolidayDocument(holiday);
    DOCUMENT.setCorrectionFromInstant(CORRECTION_FROM);
    DOCUMENT.setCorrectionToInstant(CORRECTION_TO);
    DOCUMENT.setProviderId(PROVIDER_ID);
    DOCUMENT.setVersionFromInstant(VERSION_FROM);
    DOCUMENT.setVersionToInstant(VERSION_TO);
  }

  /**
   * Tests sort by id ascending.
   */
  @Test
  public void testOidAscending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setUniqueId(UniqueId.of(UID.getScheme(), UID.getValue() + "1"));
    doc2.setUniqueId(UniqueId.of(UID.getScheme(), UID.getValue() + "2"));
    final List<HolidayDocument> docs = Arrays.asList(DOCUMENT, doc2, doc1);
    Collections.sort(docs, HolidaySearchSortOrder.OBJECT_ID_ASC);
    assertEquals(docs, Arrays.asList(DOCUMENT, doc1, doc2));
  }

  /**
   * Tests sort by id descending.
   */
  @Test
  public void testOidDescending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setUniqueId(UniqueId.of(UID.getScheme(), UID.getValue() + "1"));
    doc2.setUniqueId(UniqueId.of(UID.getScheme(), UID.getValue() + "2"));
    final List<HolidayDocument> docs = Arrays.asList(DOCUMENT, doc2, doc1);
    Collections.sort(docs, HolidaySearchSortOrder.OBJECT_ID_DESC);
    assertEquals(docs, Arrays.asList(doc2, doc1, DOCUMENT));
  }

  /**
   * Tests sort by version ascending.
   */
  @Test
  public void testVersionAscending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setVersionFromInstant(VERSION_FROM.plusMillis(10000));
    doc2.setVersionFromInstant(VERSION_FROM.plusMillis(20000));
    final List<HolidayDocument> docs = Arrays.asList(doc2, doc1, DOCUMENT);
    Collections.sort(docs, HolidaySearchSortOrder.VERSION_FROM_INSTANT_ASC);
    assertEquals(docs, Arrays.asList(DOCUMENT, doc1, doc2));
  }

  /**
   * Tests sort by version descending.
   */
  @Test
  public void testVersionFromDescending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setVersionFromInstant(VERSION_FROM.plusMillis(10000));
    doc2.setVersionFromInstant(VERSION_FROM.plusMillis(20000));
    final List<HolidayDocument> docs = Arrays.asList(doc2, doc1, DOCUMENT);
    Collections.sort(docs, HolidaySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    assertEquals(docs, Arrays.asList(doc2, doc1, DOCUMENT));
  }

  /**
   * Tests sort by id ascending.
   */
  @Test
  public void testNameAscending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setName(DOCUMENT.getName() + "1");
    doc2.setName(DOCUMENT.getName() + "2");
    final List<HolidayDocument> docs = Arrays.asList(doc2, DOCUMENT, doc1);
    Collections.sort(docs, HolidaySearchSortOrder.NAME_ASC);
    assertEquals(docs, Arrays.asList(DOCUMENT, doc1, doc2));
  }

  /**
   * Tests sort by name descending.
   */
  @Test
  public void testNameDescending() {
    final HolidayDocument doc1 = DOCUMENT.clone();
    final HolidayDocument doc2 = DOCUMENT.clone();
    doc1.setName(DOCUMENT.getName() + "1");
    doc2.setName(DOCUMENT.getName() + "2");
    final List<HolidayDocument> docs = Arrays.asList(doc2, DOCUMENT, doc1);
    Collections.sort(docs, HolidaySearchSortOrder.NAME_DESC);
    assertEquals(docs, Arrays.asList(doc2, doc1, DOCUMENT));
  }
}
