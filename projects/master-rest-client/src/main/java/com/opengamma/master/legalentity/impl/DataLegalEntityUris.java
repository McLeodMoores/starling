/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for legal entities
 */
public class DataLegalEntityUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "legalEntities";
  }
}
