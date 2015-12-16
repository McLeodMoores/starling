/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Simple mock security source for use in tests. The source is backed by a {@link ConcurrentHashMap} that maps
 * {@link ExternalId} to {@link Security} and ignores {@link VersionCorrection}s. The source matches on the first
 * security found and does not combine results from id bundles.
 */
public class MockSecuritySource implements SecuritySource {
  /** The backing map */
  private final ConcurrentMap<ExternalId, Security> _securities = new ConcurrentHashMap<>(new HashMap<ExternalId, Security>());

  /**
   * Constructor.
   */
  public MockSecuritySource() {
  }

  /**
   * Adds a security to the source. If there is already a security for this identifier, it is replaced and
   * the method returns the security that was previously stored in the source.
   * @param id  the id, not null
   * @param security  the security
   * @return  the previous security for this id, or null if there was no security for this id
   */
  public Security addSecurity(final ExternalId id, final Security security) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(security, "security");
    return _securities.putIfAbsent(id, security);
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle externalIds, final VersionCorrection versionCorrection) {
    return get(externalIds);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> externalIds, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Security>> result = new HashMap<>();
    for (final ExternalIdBundle idBundle : externalIds) {
      result.put(idBundle, get(idBundle, versionCorrection));
    }
    return result;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle externalIds) {
    for (final ExternalId externalId : externalIds) {
      final Security security = _securities.get(externalId);
      if (security != null) {
        return Collections.singleton(security);
      }
    }
    return null;
  }

  @Override
  public Security getSingle(final ExternalIdBundle externalIds) {
    for (final ExternalId externalId : externalIds) {
      final Security security = _securities.get(externalId);
      if (security != null) {
        return security;
      }
    }
    return null;
  }

  @Override
  public Security getSingle(final ExternalIdBundle externalIds, final VersionCorrection versionCorrection) {
    return getSingle(externalIds);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> externalIds, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    for (final ExternalIdBundle bundle : externalIds) {
      final Security security = getSingle(bundle);
      if (security != null) {
        securities.put(bundle, security);
      }
    }
    return securities;
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    return getSingle(uniqueId.toExternalId().toBundle());
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getSingle(ExternalId.of(objectId.getScheme(), objectId.getValue()).toBundle());
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> securities = new HashMap<>();
    for (final UniqueId id : uniqueIds) {
      securities.put(id, get(id));
    }
    return securities;
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Security> securities = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Security security = get(objectId, versionCorrection);
      if (security != null) {
        securities.put(objectId, security);
      }
    }
    return securities;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
