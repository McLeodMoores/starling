/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for regions.
 */
public class DataRegionUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "regions";
  }
}
