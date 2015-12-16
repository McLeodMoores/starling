/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Iterables;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Simple mock convention source for use in tests. The source is backed by a {@link ConcurrentHashMap} that maps
 * {@link ExternalId} to {@link Convention} and ignores {@link VersionCorrection}s. The source matches on the first
 * convention found and does not combine results from id bundles.
 */
public class MockConventionSource implements ConventionSource {
  /** The backing map */
  private final ConcurrentMap<ExternalId, Convention> _conventions = new ConcurrentHashMap<>(new HashMap<ExternalId, Convention>());

  /**
   * Constructor.
   */
  public MockConventionSource() {
  }

  /**
   * Adds a convention to the source. If there is already a convention for this identifier, it is replaced and
   * the method returns the convention that was previous stored in the source.
   * @param id  the id, not null
   * @param convention  the convention, not null
   * @return  the previous convention for this id, or null if there was no convention for this id
   */
  public Convention addConvention(final ExternalId id, final Convention convention) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(convention, "convention");
    return _conventions.putIfAbsent(id, convention);
  }

  @Override
  public Collection<Convention> get(final ExternalIdBundle externalIds, final VersionCorrection versionCorrection) {
    return get(externalIds);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(final Collection<ExternalIdBundle> externalIds, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Convention>> result = new HashMap<>();
    for (final ExternalIdBundle idBundle : externalIds) {
      result.put(idBundle, get(idBundle, versionCorrection));
    }
    return result;
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(final Collection<ExternalIdBundle> externalIds, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Convention> result = new HashMap<>();
    for (final ExternalIdBundle idBundle : externalIds) {
      result.put(idBundle, Iterables.getOnlyElement(get(idBundle, versionCorrection)));
    }
    return result;
  }

  @Override
  public Convention get(final UniqueId uniqueId) {
    return getSingle(uniqueId.toExternalId());
  }

  @Override
  public Map<UniqueId, Convention> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Convention> result = new HashMap<>();
    for (final UniqueId id : uniqueIds) {
      result.put(id, get(id));
    }
    return result;
  }

  @Override
  public Convention get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getSingle(ExternalId.of(objectId.getScheme(), objectId.getValue()));
  }

  @Override
  public Map<ObjectId, Convention> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Convention> conventions = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Convention convention = get(objectId, versionCorrection);
      if (convention != null) {
        conventions.put(objectId, convention);
      }
    }
    return conventions;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public Collection<Convention> get(final ExternalIdBundle externalIds) {
    for (final ExternalId externalId : externalIds) {
      final Convention convention = _conventions.get(externalId);
      if (convention != null) {
        return Collections.singleton(convention);
      }
    }
    return null;
  }

  @Override
  public <T extends Convention> T get(final UniqueId uniqueId, final Class<T> expectedType) {
    return expectedType.cast(get(uniqueId));
  }

  @Override
  public <T extends Convention> T get(final ObjectId objectId, final VersionCorrection versionCorrection, final Class<T> expectedType) {
    return expectedType.cast(getSingle(ExternalId.of(objectId.getScheme(), objectId.getValue())));
  }

  @Override
  public Convention getSingle(final ExternalId externalId) {
    return _conventions.get(externalId);
  }

  @Override
  public Convention getSingle(final ExternalIdBundle externalIds) {
    for (final ExternalId externalId : externalIds) {
      final Convention convention = getSingle(externalId);
      if (convention != null) {
        return convention;
      }
    }
    return null;
  }

  @Override
  public Convention getSingle(final ExternalIdBundle externalIds, final VersionCorrection versionCorrection) {
    return getSingle(externalIds);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalId externalId, final Class<T> expectedType) {
    return expectedType.cast(getSingle(externalId));
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle externalIds, final Class<T> expectedType) {
    return expectedType.cast(getSingle(externalIds));
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle externalIds, final VersionCorrection versionCorrection, final Class<T> expectedType) {
    return expectedType.cast(getSingle(externalIds));
  }

}
