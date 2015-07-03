/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends} that allows use of the
 * named instance factory.
 */
public class WeekendBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with name Weekend and implementation
   * {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends}.
   */
  public WeekendBusinessDayCalendar() {
    super("Weekend", new BusinessdayCalendarExcludingWeekends());
  }

}
