/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for portfolios.
 */
public class DataPortfolioUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "portfolios";
  }
}
