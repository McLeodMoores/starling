/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays} that allows use of the
 * named instance factory.
 */
public class TargetBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with name None and implementation
   * {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays}.
   */
  public TargetBusinessDayCalendar() {
    super("TARGET", new BusinessdayCalendarExcludingTARGETHolidays());
  }

}
