/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * An implementation of {@link SecuritySource} designed to work with any non-versioned
 * SecuritySource for basic caching.
 * It also does not cache failures to resolve (e.g. a result of null) so that if the underlying
 * is changed underneath this will be picked up.
 * @see NonVersionedRedisSecuritySource
 */
public class NonVersionedEHCachingSecuritySource implements SecuritySource {
  private final SecuritySource _underlying;
  private final Cache _cache;

  public NonVersionedEHCachingSecuritySource(final SecuritySource underlying, final Cache cache) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cache, "cache");
    _underlying = underlying;
    _cache = cache;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache.
   * @return the cache
   */
  protected Cache getCache() {
    return _cache;
  }

  private enum CacheEntryType {
    SINGLE,
    COLLECTION
  }

  private static class SecurityCacheEntry {
    SecurityCacheEntry(final CacheEntryType entryType, final ExternalIdBundle bundle, final UniqueId uniqueId) {
      _entryType = entryType;
      _bundle = bundle;
      _uniqueId = uniqueId;
      _hashCode = generateHashCode();
    }
    private final CacheEntryType _entryType;
    private final ExternalIdBundle _bundle;
    private final UniqueId _uniqueId;
    private final int _hashCode;

    private int generateHashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (_bundle == null ? 0 : _bundle.hashCode());
      result = prime * result + (_entryType == null ? 0 : _entryType.hashCode());
      result = prime * result + (_uniqueId == null ? 0 : _uniqueId.hashCode());
      return result;
    }

    @Override
    public int hashCode() {
      return _hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final SecurityCacheEntry other = (SecurityCacheEntry) obj;
      if (_bundle == null) {
        if (other._bundle != null) {
          return false;
        }
      } else if (!_bundle.equals(other._bundle)) {
        return false;
      }
      if (_uniqueId == null) {
        if (other._uniqueId != null) {
          return false;
        }
      } else if (!_uniqueId.equals(other._uniqueId)) {
        return false;
      }
      if (_entryType != other._entryType) {
        return false;
      }
      return true;
    }

  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return get(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Security>> result = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      result.put(bundle, get(bundle));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    final SecurityCacheEntry cacheEntry = new SecurityCacheEntry(CacheEntryType.COLLECTION, bundle, null);
    Element element = getCache().get(cacheEntry);
    Collection<Security> result = null;
    if (element == null) {
      result = getUnderlying().get(bundle);
      if (result != null) {
        element = new Element(cacheEntry, result);
        getCache().put(element);
      }
    } else {
      result = (Collection<Security>) element.getObjectValue();
    }
    return result;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    final SecurityCacheEntry cacheEntry = new SecurityCacheEntry(CacheEntryType.SINGLE, bundle, null);
    Element element = getCache().get(cacheEntry);
    Security result = null;
    if (element == null) {
      result = getUnderlying().getSingle(bundle);
      if (result != null) {
        element = new Element(cacheEntry, result);
        getCache().put(element);
      }
    } else {
      result = (Security) element.getObjectValue();
    }
    return result;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return getSingle(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Security> result = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      result.put(bundle, getSingle(bundle));
    }
    return result;
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    final SecurityCacheEntry cacheEntry = new SecurityCacheEntry(CacheEntryType.SINGLE, null, uniqueId);
    Element element = getCache().get(cacheEntry);
    Security result = null;
    if (element == null) {
      result = getUnderlying().get(uniqueId);
      if (result != null) {
        element = new Element(cacheEntry, result);
        getCache().put(element);
      }
    } else {
      result = (Security) element.getObjectValue();
    }
    return result;
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return get(UniqueId.of(objectId, null));
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> result = new HashMap<>();
    for (final UniqueId uniqueId : uniqueIds) {
      result.put(uniqueId, get(uniqueId));
    }
    return result;
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Security> result = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      result.put(objectId, get(UniqueId.of(objectId, null)));
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
