/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryHolidayMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryHolidayMasterTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final Currency GBP = Currency.GBP;

  private InMemoryHolidayMaster _master;
  private HolidayDocument _addedDoc;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryHolidayMaster();
    final ManageableHoliday inputHoliday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    final HolidayDocument inputDoc = new HolidayDocument(inputHoliday);
    _addedDoc = _master.add(inputDoc);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetNoMatch() {
    _master.get(UniqueId.of("A", "B"));
  }

  /**
   *
   */
  public void testGetMatch() {
    final HolidayDocument result = _master.get(_addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemHol", "1"), result.getUniqueId());
    assertEquals(_addedDoc, result);
  }

}
