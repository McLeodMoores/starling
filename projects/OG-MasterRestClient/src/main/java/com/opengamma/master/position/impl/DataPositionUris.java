/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for positions.
 */
public class DataPositionUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "positions";
  }
}
