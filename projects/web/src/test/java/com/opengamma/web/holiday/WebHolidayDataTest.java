/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebHolidayData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebHolidayDataTest extends AbstractBeanTestCase {
  private static final String HOLIDAY_URI = "hol";
  private static final String VERSION_URI = "version=1";
  private static final ManageableHoliday HOLIDAY = new ManageableHoliday();
  private static final HolidayDocument DOCUMENT = new HolidayDocument();
  private static final HolidayDocument VERSIONED = new HolidayDocument();
  static {
    HOLIDAY.setType(HolidayType.CUSTOM);
    DOCUMENT.setHoliday(HOLIDAY);
    VERSIONED.setHoliday(HOLIDAY);
    VERSIONED.setVersionFromInstant(Instant.now());
  }
  private static final WebHolidayData DATA = new WebHolidayData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("hol", "0"));
    DATA.setHoliday(DOCUMENT);
    DATA.setUriHolidayId(HOLIDAY_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebHolidayData.class, Arrays.asList("uriHolidayId", "uriVersionId", "holiday", "versioned"),
        Arrays.asList(HOLIDAY_URI, VERSION_URI, DOCUMENT, VERSIONED), Arrays.asList(VERSION_URI, HOLIDAY_URI, VERSIONED, DOCUMENT));
  }

  /**
   * Tests getting the best holiday if the override id is not null.
   */
  public void testBestHolidayOverrideId() {
    final UniqueId uid = UniqueId.of("snp", "1");
    assertEquals(DATA.getBestHolidayUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best holiday if there is no holiday document.
   */
  public void testBestHolidayNoHolidayDocument() {
    final WebHolidayData data = DATA.clone();
    data.setHoliday(null);
    assertEquals(data.getBestHolidayUriId(null), HOLIDAY_URI);
  }

  /**
   * Tests getting the best holiday from the document.
   */
  public void testBestHolidayFromDocument() {
    assertEquals(DATA.getBestHolidayUriId(null), DOCUMENT.getUniqueId().toString());
  }

}
