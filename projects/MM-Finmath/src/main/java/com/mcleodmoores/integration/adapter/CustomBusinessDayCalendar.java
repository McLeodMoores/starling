/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * An adapter for {@link Calendar} that allows use of the named instance factory by wrapping a
 * {@link BusinessDayCalendarAdapter}.
 */
public class CustomBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with the same name as the calendar and implementation {@link BusinessDayCalendarAdapter}.
   * @param calendar The calendar, not null
   */
  public CustomBusinessDayCalendar(final Calendar calendar) {
    super(calendar.getName(), new BusinessDayCalendarAdapter(calendar));
  }

}
