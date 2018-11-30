/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidayHistoryRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayHistoryRequestTest extends AbstractFudgeBuilderTestCase {
  private static final PagingRequest PAGING = PagingRequest.FIRST_PAGE;
  private static final List<LocalDate> DATES = Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2018,  12,  31));
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(10000L);
  private static final Instant VERSION_TO = Instant.ofEpochSecond(20000L);
  private static final Instant CORRECTED_FROM = Instant.ofEpochSecond(15000L);
  private static final Instant CORRECTED_TO = Instant.ofEpochSecond(25000L);
  private static final UniqueId UID = UniqueId.of("unique", "id");
  private static final HolidayDocument DOC = new HolidayDocument();
  static {
    final ManageableHoliday holiday = new ManageableHoliday(new SimpleHolidayWithWeekend(DATES, WeekendType.THURSDAY_FRIDAY));
    holiday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    holiday.setType(HolidayType.BANK);
    holiday.setUniqueId(UID);
    DOC.setUniqueId(UID);
    DOC.setVersionFromInstant(Instant.ofEpochSecond(100000L));
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentConstructor1() {
    new HolidayHistoryRequest(null);
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentConstructor() {
    new HolidayHistoryRequest(null, VERSION_FROM, CORRECTED_TO);
  }

  /**
   * Tests that the object id can be set to null.
   */
  @Test
  public void testSetNullObjectId() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest();
    request.setObjectId(null);
    assertNull(request.getObjectId());
  }

  /**
   * Tests that the version instant can be null.
   */
  @Test
  public void testNullVersionInstant() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest(DOC, null, CORRECTED_TO);
    assertNull(request.getVersionsFromInstant());
    assertNull(request.getVersionsToInstant());
  }

  /**
   * Tests that the correction instant can be null.
   */
  @Test
  public void testNullCorrectionInstant() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest(DOC, VERSION_TO, null);
    assertNull(request.getCorrectionsFromInstant());
    assertNull(request.getCorrectionsToInstant());
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    assertEquals(request.getCorrectionsFromInstant(), CORRECTED_TO);
    assertEquals(request.getCorrectionsToInstant(), CORRECTED_TO);
    assertEquals(request.getObjectId(), DOC.getObjectId());
    assertEquals(request.getPagingRequest(), PagingRequest.ALL);
    assertEquals(request.getVersionsFromInstant(), VERSION_TO);
    assertEquals(request.getVersionsToInstant(), VERSION_TO);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    request.setPagingRequest(PAGING);
    request.setVersionsFromInstant(VERSION_FROM);
    assertEquals(request.getCorrectionsFromInstant(), CORRECTED_FROM);
    assertEquals(request.getCorrectionsToInstant(), CORRECTED_TO);
    assertEquals(request.getObjectId(), DOC.getObjectId());
    assertEquals(request.getPagingRequest(), PAGING);
    assertEquals(request.getVersionsFromInstant(), VERSION_FROM);
    assertEquals(request.getVersionsToInstant(), VERSION_TO);
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "HolidayHistoryRequest{pagingRequest=PagingRequest[first=0, size=20], objectId=unique~id, "
        + "versionsFromInstant=1970-01-01T02:46:40Z, versionsToInstant=1970-01-01T05:33:20Z, correctionsFromInstant=1970-01-01T04:10:00Z, "
        + "correctionsToInstant=1970-01-01T06:56:40Z}");
    final HolidayHistoryRequest other = new HolidayHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    other.setCorrectionsFromInstant(CORRECTED_FROM);
    other.setPagingRequest(PAGING);
    other.setVersionsFromInstant(VERSION_FROM);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setCorrectionsFromInstant(null);
    assertNotEquals(request, other);
    other.setCorrectionsFromInstant(CORRECTED_FROM);
    other.setCorrectionsToInstant(null);
    assertNotEquals(request, other);
    other.setCorrectionsToInstant(CORRECTED_TO);
    other.setObjectId(ObjectId.of("oid", "new"));
    assertNotEquals(request, other);
    other.setObjectId(DOC.getObjectId());
    other.setVersionsFromInstant(null);
    assertNotEquals(request, other);
    other.setVersionsFromInstant(VERSION_FROM);
    other.setVersionsToInstant(null);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest(DOC, VERSION_TO, CORRECTED_TO);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    request.setPagingRequest(PAGING);
    request.setVersionsFromInstant(VERSION_FROM);
    assertEquals(request.propertyNames().size(), 6);
    assertEquals(request.metaBean().correctionsFromInstant().get(request), CORRECTED_FROM);
    assertEquals(request.metaBean().correctionsToInstant().get(request), CORRECTED_TO);
    assertEquals(request.metaBean().pagingRequest().get(request), PAGING);
    assertEquals(request.metaBean().objectId().get(request), DOC.getObjectId());
    assertEquals(request.metaBean().versionsFromInstant().get(request), VERSION_FROM);
    assertEquals(request.metaBean().versionsToInstant().get(request), VERSION_TO);
    assertEquals(request.property("correctionsFromInstant").get(), CORRECTED_FROM);
    assertEquals(request.property("correctionsToInstant").get(), CORRECTED_TO);
    assertEquals(request.property("pagingRequest").get(), PAGING);
    assertEquals(request.property("objectId").get(), DOC.getObjectId());
    assertEquals(request.property("versionsFromInstant").get(), VERSION_FROM);
    assertEquals(request.property("versionsToInstant").get(), VERSION_TO);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest();
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setCorrectionsFromInstant(CORRECTED_FROM);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setCorrectionsToInstant(CORRECTED_TO);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setObjectId(ObjectId.of("oid", "new"));
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setPagingRequest(PAGING);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setVersionsFromInstant(VERSION_FROM);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
    request.setVersionsToInstant(VERSION_TO);
    assertEncodeDecodeCycle(HolidayHistoryRequest.class, request);
  }
}
