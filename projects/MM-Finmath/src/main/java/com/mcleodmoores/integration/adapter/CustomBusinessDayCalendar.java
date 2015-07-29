/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import com.opengamma.analytics.date.WorkingDayCalendar;

/**
 * An adapter for {@link WorkingDayCalendar} that allows use of the named instance factory by wrapping a
 * {@link BusinessDayCalendarAdapter}.
 */
public class CustomBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with the same name as the calendar and implementation {@link BusinessDayCalendarAdapter}.
   * @param calendar The calendar, not null
   */
  public CustomBusinessDayCalendar(final WorkingDayCalendar calendar) {
    super(calendar.getName(), new BusinessDayCalendarAdapter(calendar));
  }

}
