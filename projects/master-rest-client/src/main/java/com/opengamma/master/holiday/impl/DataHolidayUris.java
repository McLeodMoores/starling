/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for holidays.
 */
public class DataHolidayUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "holidays";
  }
}
