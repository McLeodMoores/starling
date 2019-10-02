/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for conventions.
 */
public class DataConventionUris extends AbstractDataDocumentUris {

  @Override
  protected String getResourceName() {
    return "conventions";
  }

}
