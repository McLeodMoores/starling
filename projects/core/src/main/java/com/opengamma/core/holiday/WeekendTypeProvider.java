/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

/**
 * Provides information about which days of the week are weekends.
 */
public interface WeekendTypeProvider {

  /**
   * Gets the weekend type.
   * @return  the weekend type
   */
  WeekendType getWeekendType();
}
