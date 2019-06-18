/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

/**
 *
 */
public abstract class Schedule {
  /** Empty array of LocalDate. */
  protected static final LocalDate[] EMPTY_LOCAL_DATE_ARRAY = new LocalDate[0];
  /** Empty array of ZonedDateTime. */
  protected static final ZonedDateTime[] EMPTY_ZONED_DATE_TIME_ARRAY = new ZonedDateTime[0];

  public abstract LocalDate[] getSchedule(LocalDate startDate, LocalDate endDate, boolean fromEnd, boolean generateRecursive);

  public abstract ZonedDateTime[] getSchedule(ZonedDateTime startDate, ZonedDateTime endDate, boolean fromEnd, boolean generateRecursive);

}
