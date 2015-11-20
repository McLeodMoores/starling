/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for market data snapshots
 */
public class DataMarketDataSnapshotUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "snapshots";
  }
}
