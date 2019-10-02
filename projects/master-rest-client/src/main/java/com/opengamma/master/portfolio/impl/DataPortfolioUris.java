/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
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
