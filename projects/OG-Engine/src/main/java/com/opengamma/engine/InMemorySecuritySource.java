/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of securities.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class InMemorySecuritySource extends AbstractSecuritySource {

  /**
   * The securities keyed by identifier.
   */
  private final Map<ObjectId, Security> _securities = Maps.newHashMap();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdSupplier _uidSupplier;

  /**
   * Creates the security master.
   */
  public InMemorySecuritySource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @Override
  public Security get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Security security = _securities.get(uniqueId.getObjectId());
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + uniqueId);
    }
    return security;
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Security security = _securities.get(objectId);
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + objectId);
    }
    return security;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final List<Security> result = new ArrayList<Security>();
    for (final Security sec : _securities.values()) {
      if (sec.getExternalIdBundle().containsAny(bundle)) {
        result.add(sec);
      }
    }
    return result;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // Mock source doesn't support versioning
    return get(bundle);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (final ExternalId secId : bundle.getExternalIds()) {
      for (final Security sec : _securities.values()) {
        if (sec.getExternalIdBundle().contains(secId)) {
          return sec;
        }
      }
    }
    // TODO should this throw DataNotFoundException?
    return null;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // Mock source doesn't support versioning
    return getSingle(bundle);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security to the source.
   *
   * @param security  the security to add, not null
   */
  public void addSecurity(final Security security) {
    ArgumentChecker.notNull(security, "security");
    IdUtils.setInto(security, _uidSupplier.get());
    _securities.put(security.getUniqueId().getObjectId(), security);
  }

}
