/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingWeekends} that allows use of the
 * named instance factory.
 */
@FinmathBusinessDayType(name = "Weekend", aliases = {"Saturday / Sunday" })
public final class WeekendBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter called "Weekend" and implementation
   * {@link BusinessdayCalendarExcludingWeekends}.
   */
  public WeekendBusinessDayCalendar() {
    super("Weekend", new BusinessdayCalendarExcludingWeekends());
  }

  /**
   * Creates an instance of this adapter that implements {@link BusinessdayCalendarExcludingWeekends}.
   * @param name  the name of the convention, not null
   */
  public WeekendBusinessDayCalendar(final String name) {
    super(name, new BusinessdayCalendarExcludingWeekends());
  }
}
