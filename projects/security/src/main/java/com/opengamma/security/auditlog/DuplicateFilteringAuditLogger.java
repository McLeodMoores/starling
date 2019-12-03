/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Objects;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * A decorator <code>AuditLogger</code> that only sends the message
 * onwards to its delegate if the message is not a duplicate (within
 * a given time period).
 * <p>
 * This implementation is thread-safe.
 */
public class DuplicateFilteringAuditLogger extends AbstractAuditLogger {

  private final AbstractAuditLogger _delegate;
  private final Cache _cache;

  /**
   * Creates an instance.
   *
   * @param delegate  the delegate logger, not null
   * @param maxElementsInMemory  the maximum number of elements to keep in memory
   * @param secondsToKeepInMemory  the number of seconds to keep in memory
   */
  public DuplicateFilteringAuditLogger(final AbstractAuditLogger delegate,
      final int maxElementsInMemory, final int secondsToKeepInMemory) {
    _delegate = ArgumentChecker.notNull(delegate, "Delegate logger");
    _cache = new Cache("audit_log_entry_cache", maxElementsInMemory, false,
        false, secondsToKeepInMemory, secondsToKeepInMemory);
    _cache.setCacheManager(EHCacheUtils.createCacheManager());
    _cache.initialise();
  }

  @Override
  public synchronized void log(final String user, final String originatingSystem,
      final String object, final String operation, final String description, final boolean success) {

    final CacheKey key = new CacheKey(user, originatingSystem, object, operation, description, success);

    if (_cache.get(key) == null) {
      final Element element = new Element(key, new Object());
      _cache.put(element);

      _delegate.log(user, object, operation, description, success);
    }
  }

  /**
   * Creates a cache key.
   */
  private static class CacheKey {
    private final String _user;
    private final String _originatingSystem;
    private final String _object;
    private final String _operation;
    private final String _description;
    private final boolean _success;

    CacheKey(final String user, final String originatingSystem,
        final String object, final String operation, final String description, final boolean success) {
      _user = user;
      _originatingSystem = originatingSystem;
      _object = object;
      _operation = operation;
      _description = description;
      _success = success;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + (_description == null ? 0 : _description.hashCode());
      result = prime * result + (_object == null ? 0 : _object.hashCode());
      result = prime * result
          + (_operation == null ? 0 : _operation.hashCode());
      result = prime * result
          + (_originatingSystem == null ? 0 : _originatingSystem.hashCode());
      result = prime * result + (_success ? 1231 : 1237);
      result = prime * result + (_user == null ? 0 : _user.hashCode());
      return result;
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
      final CacheKey other = (CacheKey) obj;
      if (!Objects.equals(_description, other._description)) {
        return false;
      }
      if (!Objects.equals(_object, other._object)) {
        return false;
      }
      if (!Objects.equals(_operation, other._operation)) {
        return false;
      }
      if (!Objects.equals(_originatingSystem, other._originatingSystem)) {
        return false;
      }
      if (_success != other._success) {
        return false;
      }
      if (!Objects.equals(_user, other._user)) {
        return false;
      }
      return true;
    }
  }
}
