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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidayHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayHistoryResultTest extends AbstractFudgeBuilderTestCase {
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);
  private static final ManageableHoliday HOL_1 = new ManageableHoliday();
  private static final ManageableHoliday HOL_2 = new ManageableHoliday();
  private static final ManageableHoliday HOL_3 = new ManageableHoliday();
  private static final HolidayDocument DOC_1 = new HolidayDocument();
  private static final HolidayDocument DOC_2 = new HolidayDocument();
  private static final HolidayDocument DOC_3 = new HolidayDocument();
  static {
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v1"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v2"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val"), "v3"));
    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(200000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_1.setHoliday(HOL_1);
    DOC_2.setHoliday(HOL_2);
    DOC_3.setHoliday(HOL_3);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new HolidayHistoryResult((Collection<HolidayDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new HolidayHistoryResult().setDocuments(null);
  }

  /**
   * Tests the getters when there are results.
   */
  @Test
  public void testGetters() {
    final List<HolidayDocument> documents = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidayHistoryResult result = new HolidayHistoryResult(documents);
    result.setPaging(PAGING);
    assertEquals(result.getDocuments(), documents);
    assertEquals(result.getFirstDocument(), DOC_1);
    assertEquals(result.getFirstHoliday(), HOL_1);
    assertEquals(result.getPaging(), PAGING);
    assertEquals(result.getHolidays(), Arrays.asList(HOL_1, HOL_2, HOL_3));
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstDocumentNoDocuments() {
    assertNull(new HolidayHistoryResult().getFirstDocument());
  }

  /**
   * Tests that null is returned.
   */
  @Test
  public void testGetFirstHolidayNoDocuments() {
    assertNull(new HolidayHistoryResult().getFirstHoliday());
  }

  /**
   * Tests that there must be at least one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleHolidayNoDocuments() {
    new HolidayHistoryResult().getSingleHoliday();
  }

  /**
   * Tests that there must be only one document.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleHolidayMultipleDocuments() {
    new HolidayHistoryResult(Arrays.asList(DOC_1, DOC_2)).getSingleHoliday();
  }

  /**
   * Tests getting a single holiday.
   */
  @Test
  public void testGetSingleHoliday() {
    assertEquals(new HolidayHistoryResult(Collections.singletonList(DOC_2)).getSingleHoliday(), HOL_2);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidayHistoryResult result = new HolidayHistoryResult(docs);
    result.setPaging(PAGING);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "HolidayHistoryResult{paging=Paging[first=0, size=20, totalItems=3], "
        + "documents=[HolidayDocument{versionFromInstant=1970-01-02T03:46:40Z, versionToInstant=null, "
        + "correctionFromInstant=null, correctionToInstant=null, holiday=ManageableHolidayWithWeekend{uniqueId=null, "
        + "type=null, regionExternalId=null, exchangeExternalId=null, customExternalId=null, currency=null, holidayDates=[], "
        + "weekendType=null}, uniqueId=oid~val~v1, name=null, providerId=null}, "
        + "HolidayDocument{versionFromInstant=1970-01-03T07:33:20Z, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, holiday=ManageableHolidayWithWeekend{uniqueId=null, type=null, regionExternalId=null, "
        + "exchangeExternalId=null, customExternalId=null, currency=null, holidayDates=[], weekendType=null}, "
        + "uniqueId=oid~val~v2, name=null, providerId=null}, HolidayDocument{versionFromInstant=1970-01-04T11:20:00Z, "
        + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
        + "holiday=ManageableHolidayWithWeekend{uniqueId=null, type=null, regionExternalId=null, exchangeExternalId=null, "
        + "customExternalId=null, currency=null, holidayDates=[], weekendType=null}, uniqueId=oid~val~v3, name=null, providerId=null}]}");
    final HolidayHistoryResult other = new HolidayHistoryResult(docs);
    other.setPaging(PAGING);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(Collections.singletonList(DOC_1));
    assertNotEquals(result, other);
    other.setDocuments(docs);
    other.setPaging(Paging.of(PagingRequest.NONE, 3));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    final HolidayHistoryResult result = new HolidayHistoryResult(docs);
    result.setPaging(PAGING);
    assertEquals(result.propertyNames().size(), 2);
    assertEquals(result.metaBean().documents().get(result), docs);
    assertEquals(result.metaBean().paging().get(result), PAGING);
    assertEquals(result.property("documents").get(), docs);
    assertEquals(result.property("paging").get(), PAGING);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HolidayHistoryResult result = new HolidayHistoryResult();
    //assertEncodeDecodeCycle(HolidayHistoryResult.class, result);
    final List<HolidayDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    result.setDocuments(docs);
    assertEncodeDecodeCycle(HolidayHistoryResult.class, result);
    //    result.setPaging(PAGING);
    //    assertEncodeDecodeCycle(HolidayHistoryResult.class, result);
  }
}