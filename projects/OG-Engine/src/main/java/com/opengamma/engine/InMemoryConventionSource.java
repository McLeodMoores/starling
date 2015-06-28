/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple implementation of a convention source intended for use in tests. It does not support versioning
 * or change management.
 * <p>
 * It is not thread-safe and so is not suitable for use in production.
 */
public class InMemoryConventionSource extends AbstractSourceWithExternalBundle<Convention> implements ConventionSource {
  /** A map from identifier to convention */
  private final Map<ObjectId, Convention> _conventions = new HashMap<>();
  /** Supplies unique identifiers */
  private final UniqueIdSupplier _uidSupplier;

  /**
   * Creates an empty convention source.
   */
  public InMemoryConventionSource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
  }

  @Override
  public Collection<Convention> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return get(bundle);
  }

  @Override
  public Convention get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Convention convention = _conventions.get(uniqueId.getObjectId());
    if (convention == null) {
      throw new DataNotFoundException("Convention not found: " + uniqueId);
    }
    return convention;
  }

  @Override
  public Convention get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Convention convention = _conventions.get(objectId);
    if (convention == null) {
      throw new DataNotFoundException("Convention not found: " + objectId);
    }
    return convention;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public <T extends Convention> T get(final UniqueId uniqueId, final Class<T> type) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(type, "type");
    final Convention convention = get(uniqueId);
    return type.cast(convention);
  }

  @Override
  public <T extends Convention> T get(final ObjectId objectId, final VersionCorrection versionCorrection, final Class<T> type) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    final Convention convention = get(objectId, versionCorrection);
    return type.cast(convention);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection, final Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    for (final ExternalId externalId : bundle) {
      final Convention convention = getSingle(externalId);
      if (convention != null) {
        return type.cast(convention);
      }
    }
    throw new DataNotFoundException("Convention not found for any id in bundle: " + bundle);
  }

  @Override
  public Convention getSingle(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    final ObjectId objectId = ObjectId.parse(externalId.toString());
    final Convention convention = _conventions.get(objectId);
    if (convention == null) {
      throw new DataNotFoundException("Convention not found: " + objectId);
    }
    return convention;
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalId externalId, final Class<T> type) {
    ArgumentChecker.notNull(externalId, "externalId");
    ArgumentChecker.notNull(type, "type");
    final ObjectId objectId = ObjectId.parse(externalId.toString());
    final Convention convention = _conventions.get(objectId);
    if (convention == null) {
      throw new DataNotFoundException("Convention not found: " + objectId);
    }
    return type.cast(convention);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle bundle, final Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(type, "type");
    for (final ExternalId externalId : bundle) {
      final Convention convention = getSingle(externalId);
      if (convention != null) {
        return type.cast(convention);
      }
    }
    throw new DataNotFoundException("Convention not found for any id in bundle: " + bundle);
  }

  /**
   * Adds a convention to the source.
   * @param convention  the convention, not null
   */
  public void addConvention(final Convention convention) {
    ArgumentChecker.notNull(convention, "convention");
    IdUtils.setInto(convention, _uidSupplier.get());
    _conventions.put(convention.getUniqueId().getObjectId(), convention);
    for (final ExternalId externalId : convention.getExternalIdBundle()) {
      _conventions.put(ObjectId.parse(externalId.toString()), convention);
    }
  }
}
