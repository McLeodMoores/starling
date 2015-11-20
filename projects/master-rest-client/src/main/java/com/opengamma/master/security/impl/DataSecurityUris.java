/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
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
