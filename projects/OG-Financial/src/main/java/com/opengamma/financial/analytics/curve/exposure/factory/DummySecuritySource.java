/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A dummy implementation of a {@link SecuritySource} that does not implement any methods. Calling any of the methods
 * will result in an {@link UnsupportedOperationException}.
 */
/* package */ final class DummySecuritySource implements SecuritySource {
  /** An instance */
  private static final SecuritySource INSTANCE = new DummySecuritySource();
  /** The error message */
  private static final String EXCEPTION_MESSAGE = "DummySecuritySource methods should not be called";

  /**
   * Returns an instance.
   * @return The instance
   */
  public static SecuritySource getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private DummySecuritySource() {
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
  }

}
