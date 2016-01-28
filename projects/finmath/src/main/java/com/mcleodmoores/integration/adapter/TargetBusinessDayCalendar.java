/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays} that allows use of the
 * named instance factory.
 */
@FinmathBusinessDayType(name = "TARGET")
public final class TargetBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter called "TARGET" and implementation
   * {@link BusinessdayCalendarExcludingTARGETHolidays}.
   */
  public TargetBusinessDayCalendar() {
    super("TARGET", new BusinessdayCalendarExcludingTARGETHolidays());
  }

  /**
   * Creates an instance of this adapter that wraps {@link BusinessdayCalendarExcludingTARGETHolidays}.
   * @param name  the name of the convention, not null
   */
  public TargetBusinessDayCalendar(final String name) {
    super(name, new BusinessdayCalendarExcludingTARGETHolidays());
  }
}
