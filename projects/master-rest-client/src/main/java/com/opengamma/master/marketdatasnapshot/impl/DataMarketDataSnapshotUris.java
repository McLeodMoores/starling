/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for market data snapshots.
 */
public class DataMarketDataSnapshotUris extends AbstractDataDocumentUris {
  @Override
  protected String getResourceName() {
    return "snapshots";
  }
}
