/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.impl.InMemoryCachingReferenceDataProvider;
import com.opengamma.integration.tool.IntegrationToolContext;

/**
 * Executes a DB dump, only including the records which have been accessed.
 */
public class GoldenCopyDumpCreator extends AbstractGoldenCopyDumpCreator {
  private final InMemoryCachingReferenceDataProvider _referenceDataProvider;

  public GoldenCopyDumpCreator(final RegressionIO regressionIO, final IntegrationToolContext tc) {
    super(regressionIO, tc);
    _referenceDataProvider = (InMemoryCachingReferenceDataProvider) tc.getBloombergReferenceDataProvider();
  }

  @Override
  protected void recordDataAccessed() throws IOException {
    final ImmutableMap<String, ReferenceData> data;
    if (_referenceDataProvider != null) {
      data = _referenceDataProvider.getDataAccessed();
    } else {
      data = ImmutableMap.of();
    }
    getRegressionIO().write(null, RegressionReferenceData.create(data), RegressionUtils.REF_DATA_ACCESSES_IDENTIFIER);
  }

}
