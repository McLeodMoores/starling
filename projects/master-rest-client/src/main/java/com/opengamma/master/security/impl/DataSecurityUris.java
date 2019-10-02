/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for securities.
 */
public class DataSecurityUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "securities";
  }
}
