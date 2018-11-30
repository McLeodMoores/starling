/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidaySearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidaySearchResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 12);
  private static final List<LocalDate> DATES = Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018,  12,  31));
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(10000L);
  private static final Instant CORRECTED_FROM = Instant.ofEpochSecond(15000L);
  private static final VersionCorrection VC = VersionCorrection.of(VERSION_FROM, CORRECTED_FROM);
  private static final UniqueId UID = UniqueId.of("unique", "id");
  private static final ManageableHoliday HOL_1, HOL_2, HOL_3;
  private static final HolidayDocument DOC_1 = new HolidayDocument();
  private static final HolidayDocument DOC_2 = new HolidayDocument();
  private static final HolidayDocument DOC_3 = new HolidayDocument();
  static {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    HOL_1 = holiday.clone();
    holiday.setRegionExternalId(null);
    holiday.setType(HolidayType.CURRENCY);
    holiday.setCurrency(Currency.USD);
    HOL_2 = holiday.clone();
    holiday.setCurrency(null);
    holiday.setType(HolidayType.CUSTOM);
    holiday.setCustomExternalId(ExternalId.of("custom", "id"));
    HOL_3 = holiday.clone();
    DOC_1.setHoliday(HOL_1);
    DOC_2.setHoliday(HOL_2);
    DOC_3.setHoliday(HOL_3);
    DOC_1.setCorrectionFromInstant(CORRECTED_FROM);
    DOC_2.setCorrectionFromInstant(CORRECTED_FROM);
    DOC_3.setCorrectionFromInstant(CORRECTED_FROM);
    DOC_1.setVersionFromInstant(VERSION_FROM);
    DOC_2.setVersionFromInstant(VERSION_FROM);
    DOC_3.setVersionFromInstant(VERSION_FROM);
  }

  /**
   * Tests that the version-correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionConstructor() {
    new HolidaySearchResult((VersionCorrection) null);
  }

  /**
   * Tests that the version-correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionSetter() {
    new HolidaySearchResult().setVersionCorrection(null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new HolidaySearchResult((Collection<HolidayDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new HolidaySearchResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<HolidayDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidaySearchResult result = new HolidaySearchResult(documents);
    result.setVersionCorrection(VC);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstHoliday(), HOL_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getHolidays(), Arrays.asList(HOL_1, HOL_2, HOL_3));
    assertEquals(result.getVersionCorrection(), VC);
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new HolidaySearchResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstHolidayNoDocuments() {
    assertNull(new HolidaySearchResult().getFirstHoliday());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleHolidayNoDocuments() {
    new HolidaySearchResult().getSingleHoliday();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleHolidayMultipleDocuments() {
    new HolidaySearchResult(Arrays.asList(DOC_1, DOC_2)).getSingleHoliday();
  }

  /**
   * Tests getting a single holiday.
   */
  @Test
  public void testGetSingleHoliday() {
    assertEquals(new HolidaySearchResult(Collections.singletonList(DOC_2)).getSingleHoliday(), HOL_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidaySearchResult result = new HolidaySearchResult(docs);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "HolidaySearchResult{paging=Paging[first=0, size=20, totalItems=12], "
        + "documents=[HolidayDocument{versionFromInstant=1970-01-01T02:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=1970-01-01T04:10:00Z, correctionToInstant=null, "
        + "holiday=ManageableHoliday{uniqueId=unique~id, type=BANK, regionExternalId=ISO_COUNTRY_ALPHA2~US, "
        + "exchangeExternalId=null, customExternalId=null, currency=null, holidayDates=[2018-01-01, 2018-12-31]}, "
        + "uniqueId=null, name=null, providerId=null}, HolidayDocument{versionFromInstant=1970-01-01T02:46:40Z, "
        + "versionToInstant=null, correctionFromInstant=1970-01-01T04:10:00Z, correctionToInstant=null, "
        + "holiday=ManageableHoliday{uniqueId=unique~id, type=CURRENCY, regionExternalId=null, exchangeExternalId=null, "
        + "customExternalId=null, currency=USD, holidayDates=[2018-01-01, 2018-12-31]}, uniqueId=null, name=null, "
        + "providerId=null}, HolidayDocument{versionFromInstant=1970-01-01T02:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=1970-01-01T04:10:00Z, correctionToInstant=null, holiday=ManageableHoliday{uniqueId=unique~id, "
        + "type=CUSTOM, regionExternalId=null, exchangeExternalId=null, customExternalId=custom~id, currency=null, "
        + "holidayDates=[2018-01-01, 2018-12-31]}, uniqueId=null, name=null, providerId=null}], "
        + "versionCorrection=V1970-01-01T02:46:40Z.C1970-01-01T04:10:00Z}");
    final HolidaySearchResult other = new HolidaySearchResult(docs);
    other.setPaging(PAGING);
    other.setVersionCorrection(VC);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(Collections.singletonList(DOC_1));
    assertNotEquals(result, other);
    other.setDocuments(docs);
    other.setPaging(Paging.of(PagingRequest.NONE, 3));
    assertNotEquals(result, other);
    other.setPaging(PAGING);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidaySearchResult result = new HolidaySearchResult(docs);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEquals(result.propertyNames().size(), 3);
    assertEquals(result.metaBean().documents().get(result), docs);
    assertEquals(result.metaBean().paging().get(result), PAGING);
    assertEquals(result.metaBean().versionCorrection().get(result), VC);
    assertEquals(result.property("documents").get(), docs);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HolidaySearchResult result = new HolidaySearchResult();
    assertEncodeDecodeCycle(HolidaySearchResult.class, result);
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(HolidaySearchResult.class, result);
    result.setPaging(PAGING);
    assertEncodeDecodeCycle(HolidaySearchResult.class, result);
    result.setVersionCorrection(VC);
    assertEncodeDecodeCycle(HolidaySearchResult.class, result);
  }
}