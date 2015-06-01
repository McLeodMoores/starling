/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class TestConventionSource implements ConventionSource {
  private final Map<ExternalId, Convention> _conventions;

  public TestConventionSource(final Map<ExternalId, Convention> conventions) {
    _conventions = conventions;
  }

  @Override
  public Convention getSingle(final ExternalId identifier) {
    final Convention convention = _conventions.get(identifier);
    if (convention == null) {
      throw new DataNotFoundException("No convention found: " + identifier);
    }
    return convention;
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalId identifier, final Class<T> clazz) {
    final Convention convention = _conventions.get(identifier);
    if (convention == null) {
      throw new DataNotFoundException("No convention found: " + identifier);
    }
    return clazz.cast(convention);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle identifiers, final Class<T> clazz) {
    return null;
  }

  @Override
  public Collection<Convention> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Convention getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    for (final ExternalId id : bundle) {
      final Convention convention = _conventions.get(id);
      if (convention != null) {
        return convention;
      }
    }
    throw new DataNotFoundException("No convention found: " + bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Convention get(final UniqueId uniqueId) {
    return null;
  }

  @Override
  public Convention get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<UniqueId, Convention> get(final Collection<UniqueId> uniqueIds) {
    return null;
  }

  @Override
  public Map<ObjectId, Convention> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return null;
  }

  @Override
  public <T extends Convention> T get(final UniqueId uniqueId, final Class<T> type) {
    return null;
  }

  @Override
  public <T extends Convention> T get(final ObjectId objectId, final VersionCorrection versionCorrection, final Class<T> type) {
    return null;
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection, final Class<T> type) {
    return null;
  }

  @Override
  public Collection<Convention> get(final ExternalIdBundle bundle) {
    return null;
  }

  @Override
  public Convention getSingle(final ExternalIdBundle bundle) {
    return null;
  }

}
