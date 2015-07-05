/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarAny;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarAny} that allows use of the
 * named instance factory.
 */
public class AnyBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with name None and implementation {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarAny}.
   */
  public AnyBusinessDayCalendar() {
    super("None", new BusinessdayCalendarAny());
  }

}
