/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import com.opengamma.core.change.ChangeProvider;

/**
 * Adds change management capability to a {@link HolidaySource}.
 */
public interface ChangeHolidaySource extends HolidaySource, ChangeProvider {

}
