/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;

/**
 * Simple implementation of a loader that is unsupported.
 */
public class UnsupportedSecurityLoader extends AbstractSecurityLoader {

  /**
   * Creates an instance.
   */
  public UnsupportedSecurityLoader() {
    super();
  }

  // override to avoid short-circuit in superclass that returns an empty result
  @Override
  public SecurityLoaderResult loadSecurities(final SecurityLoaderRequest request) {
    throw new UnsupportedOperationException("Security loading is not supported");
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityLoaderResult doBulkLoad(final SecurityLoaderRequest request) {
    throw new UnsupportedOperationException("Security loading is not supported");
  }

}
