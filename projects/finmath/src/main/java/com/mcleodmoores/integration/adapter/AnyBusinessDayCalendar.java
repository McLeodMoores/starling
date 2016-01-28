/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarAny;

/**
 * An adapter for {@link net.finmath.time.businessdaycalendar.BusinessdayCalendarAny} that allows use of the
 * named instance factory.
 */
@FinmathBusinessDayType(name = "None")
public final class AnyBusinessDayCalendar extends FinmathBusinessDay {

  /**
   * Creates an instance of this adapter with name "None" and implementation {@link BusinessdayCalendarAny}.
   */
  public AnyBusinessDayCalendar() {
    super("None", new BusinessdayCalendarAny());
  }

  /**
   * Creates an instance that wraps (@link BusinessdayCalendarAny}.
   * @param name  the name of the convention, not null
   */
  public AnyBusinessDayCalendar(final String name) {
    super(name, new BusinessdayCalendarAny());
  }
}
