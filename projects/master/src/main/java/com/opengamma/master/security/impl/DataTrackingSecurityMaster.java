/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Security master which tracks accesses using {@link UniqueId}s.
 */
public class DataTrackingSecurityMaster extends AbstractDataTrackingMaster<SecurityDocument, SecurityMaster> implements SecurityMaster {

  /**
   * Sets up the delegate security master.
   *
   * @param delegate
   *          the delegate, not null
   */
  public DataTrackingSecurityMaster(final SecurityMaster delegate) {
    super(delegate);
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    final SecuritySearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    final SecurityHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    return delegate().metaData(request);
  }


}
