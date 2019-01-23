/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Config master which tracks accesses using {@link UniqueId}s.
 */
public class DataTrackingConfigMaster extends AbstractDataTrackingMaster<ConfigDocument, ConfigMaster> implements ConfigMaster {

  /**
   * Sets up the delegate config master.
   *
   * @param delegate
   *          the delegate, not null
   */
  public DataTrackingConfigMaster(final ConfigMaster delegate) {
    super(delegate);
  }

  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    final ConfigSearchResult<R> searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    final ConfigHistoryResult<R> historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ConfigMetaDataResult metaData(final ConfigMetaDataRequest request) {
    return delegate().metaData(request);
  }


}
