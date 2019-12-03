/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A security source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingSecuritySource implements SecuritySource {

  private final SecuritySource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingSecuritySource(final SecuritySource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return _delegate.get(bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles,
                                                            final VersionCorrection versionCorrection) {

    final ImmutableMap.Builder<ExternalIdBundle, Collection<Security>> builder = ImmutableMap.builder();

    // Iterating and requesting singly is horrible but is
    // all the underlying source will do anyway so there
    // is no performance downside to doing it
    for (final ExternalIdBundle bundle : bundles) {
      final Collection<Security> securities = get(bundle, versionCorrection);
      if (!securities.isEmpty()) {
        builder.put(bundle, securities);
      }
    }
    return builder.build();
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    return _delegate.get(bundle);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final Collection<Security> securities = get(bundle, versionCorrection);
    return securities.isEmpty() ? null : securities.iterator().next();
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles,
                                                   final VersionCorrection versionCorrection) {

    final ImmutableMap.Builder<ExternalIdBundle, Security> builder = ImmutableMap.builder();

    // Iterating and requesting singly is horrible but is
    // all the underlying source will do anyway so there
    // is no performance downside to doing it
    for (final ExternalIdBundle bundle : bundles) {
      final Security security = getSingle(bundle, versionCorrection);
      if (security != null) {
        builder.put(bundle, security);
      }
    }
    return builder.build();
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }
}
