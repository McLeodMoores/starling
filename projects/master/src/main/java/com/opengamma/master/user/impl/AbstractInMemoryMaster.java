/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.DataVersionException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base providing a simple, in-memory master.
 * <p>
 * This master does not support versioning.
 * @param <T> the type of the object to be stored, updated, queried, etc.
 */
abstract class AbstractInMemoryMaster<T extends UniqueIdentifiable & MutableUniqueIdentifiable> {
  // see UserMaster and RoleMaster for method descriptions

  /**
   * The map of object identifier by name.
   */
  private final ConcurrentMap<String, ObjectId> _names = new ConcurrentHashMap<>();
  /**
   * The main map of objects.
   */
  private final ConcurrentMap<ObjectId, T> _objects = new ConcurrentHashMap<>();
  /**
   * The object description for errors.
   */
  private final String _objectDescription;
  /**
   * The removed marker object.
   */
  private final T _removed;
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param objectDescription  the object description for error messages, not null
   * @param removed  the placeholder object for removing, not null
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  AbstractInMemoryMaster(final String objectDescription, final T removed, final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectDescription, "objectDescription");
    ArgumentChecker.notNull(removed, "removed");
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectDescription = objectDescription;
    _removed = removed;
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the stored values, not to be altered.
   *
   * @return the values, not null
   */
  Collection<T> getStoredValues() {
    return _objects.values();
  }

  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  ChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Extracts the name from the object.
   *
   * @param object  the object, not null
   * @return the name, not null
   */
  abstract String extractName(T object);

  //-------------------------------------------------------------------------
  String caseInsensitive(final String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  @SuppressWarnings("unchecked")
  T clone(final T original) {
    return (T) JodaBeanUtils.clone((Bean) original);
  }

  boolean nameExists(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _names.containsKey(caseInsensitive(name));
  }

  T getByName(final String name) {
    ArgumentChecker.notNull(name, "name");
    final T stored = getByName0(name);
    return clone(stored);
  }

  T getByName0(final String name) {
    final ObjectId objectId = _names.get(caseInsensitive(name));
    if (objectId == null) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    return getById0(objectId);
  }

  T getById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final T stored = getById0(objectId);
    return clone(stored);
  }

  T getById0(final ObjectId objectId) {
    final T stored = _objects.get(objectId);
    if (stored == null || stored == _removed) {
      throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
    }
    return stored;
  }

  //-------------------------------------------------------------------------
  UniqueId add(final T object) {
    ArgumentChecker.notNull(object, "object");
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("1");
    final T o = clone(object);
    o.setUniqueId(uniqueId);
    final String nameKey = caseInsensitive(extractName(o));
    synchronized (this) {
      if (_names.containsKey(nameKey)) {
        throw new DataDuplicationException(_objectDescription + " already exists: " + extractName(o));
      }
      _objects.put(objectId, o);
      _names.put(nameKey, objectId);
    }
    final Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.ADDED, objectId, now, null, now);
    return uniqueId;
  }

  UniqueId update(final T object) {
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.notNull(object.getUniqueId(), "object.uniqueId");
    ArgumentChecker.notNull(object.getUniqueId().getVersion(), "object.uniqueId.version");
    final ObjectId objectId = object.getUniqueId().getObjectId();
    final String oldVersion = object.getUniqueId().getVersion();
    final UniqueId newUniqueId = objectId.atVersion(Integer.toString(Integer.parseInt(oldVersion) + 1));
    final T o = clone(object);
    o.setUniqueId(newUniqueId);
    final String nameKey = caseInsensitive(extractName(o));
    synchronized (this) {
      final T stored = getById0(objectId);
      if (!stored.getUniqueId().getVersion().equals(oldVersion)) {
        throw new DataVersionException("Invalid version, " + _objectDescription + " has already been updated: " + objectId);
      }
      if (nameKey.equals(caseInsensitive(extractName(stored)))) {
        _objects.put(objectId, o);  // replace
      } else {
        if (_names.containsKey(nameKey)) {
          throw new DataDuplicationException(_objectDescription + " cannot be renamed, new name already exists: " + extractName(o));
        }
        _objects.put(objectId, o);  // replace
        _names.put(nameKey, objectId);
        // leave old name to forward to same object
      }
    }
    final Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.CHANGED, objectId, now, null, now);
    return newUniqueId;
  }

  UniqueId save(final T object) {
    ArgumentChecker.notNull(object, "object");
    if (object.getUniqueId() != null) {
      return update(object);
    }
    return add(object);
  }

  //-------------------------------------------------------------------------
  void removeByName(final String name) {
    ArgumentChecker.notNull(name, "name");
    // no need to synchronize as names is append-only
    final ObjectId objectId = _names.get(caseInsensitive(name));
    if (objectId == null) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    removeById(objectId);
  }

  void removeById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    synchronized (this) {
      final T stored = _objects.get(objectId);
      if (stored == null) {
        throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
      } else if (stored == _removed) {
        return;  // return quietly to be idempotent
      }
      _objects.put(objectId, _removed);  // replace
    }
    final Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.REMOVED, objectId, now, null, now);
  }

  //-------------------------------------------------------------------------
  List<HistoryEvent> eventHistory(final ObjectId objectId, final String name) {
    if (objectId != null && !_objects.containsKey(objectId)) {
      throw new DataNotFoundException(_objectDescription + " identifier not found: " + objectId);
    } else if (name != null && !_names.containsKey(caseInsensitive(name))) {
      throw new DataNotFoundException(_objectDescription + " name not found: " + name);
    }
    return ImmutableList.of();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[size=%d]", getClass().getSimpleName(), _objects.size());
  }

}
