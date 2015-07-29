/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples.populator;

import java.util.Collections;

import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class TestHolidaysPopulator {

  public static void populateHolidayMaster(final HolidayMaster holidayMaster) {
    final ManageableHoliday usdHolidays = new ManageableHoliday(new SimpleHolidayWithWeekend(Collections.<LocalDate>emptySet(), WeekendType.SATURDAY_SUNDAY));
    usdHolidays.setCurrency(Currency.USD);
    usdHolidays.setType(HolidayType.CURRENCY);
    holidayMaster.add(new HolidayDocument(usdHolidays));
  }
}
