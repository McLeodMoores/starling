/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import org.threeten.bp.LocalDate;

/**
 * @author elaine
 *
 */
public interface WorkingDayCalculator {

  boolean isHoliday(Holiday holiday, LocalDate date, boolean includeWeekend);

  boolean isWeekend(LocalDate date);

  LocalDate nextWeekDay(LocalDate date);

  LocalDate previousWeekDay(LocalDate date);

  LocalDate nextWorkingDay(LocalDate date, boolean includeWeekend);

  LocalDate previousWorkingDay(LocalDate date, boolean includeWeekend);

}
