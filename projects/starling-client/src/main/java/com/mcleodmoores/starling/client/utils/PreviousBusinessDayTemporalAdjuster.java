/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.utils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Temporal adjuster to find previous business day (in the loose sense, not using calendars.
 */
public class PreviousBusinessDayTemporalAdjuster implements TemporalAdjuster {
  @Override
  public Temporal adjustInto(Temporal temporal) {
    Temporal yesterday = temporal.minus(Period.ofDays(1));
    DayOfWeek lastWeekday = DayOfWeek.from(yesterday);
    if (lastWeekday.equals(DayOfWeek.SATURDAY) || lastWeekday.equals(DayOfWeek.SUNDAY)) {
      return yesterday.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
    } else {
      return yesterday;
    }
  }
}
