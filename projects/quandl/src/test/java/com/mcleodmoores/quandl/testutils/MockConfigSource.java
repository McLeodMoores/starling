/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.threeten.bp.Instant;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;

/**
 * A mock config source for testing.
 */
public class MockConfigSource extends AbstractSource<ConfigItem<?>> implements ConfigSource {
  /** The scheme for objects stored in this source */
  private static final String SCHEME = "MockCfg";
  /** The counter */
  private final AtomicLong _counter = new AtomicLong(0);
  /** The map of data  */
  private final ConcurrentMap<ObjectId, ConfigDocument> _store = new ConcurrentHashMap<>(new HashMap<ObjectId, ConfigDocument>());
  /** The change manager */
  private final ChangeManager _changeManager = new BasicChangeManager();

  /**
   * Adds a config to the mock source. If the object is not {@link MutableUniqueIdentifiable}, the {@link UniqueId} must
   * be set, or an exception is thrown.
   * @param config  the config, not null
   * @param name  the name of the config, not null
   * @param clazz  the class of the object being stored, not null
   * @return  the config item that was stored
   */
  public ConfigItem<?> put(final Object config, final String name, final Class<?> clazz) {
    ArgumentChecker.notNull(config, "config");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(clazz, "clazz");
    final ConfigItem<Object> item = ConfigItem.of(config, name, clazz);
    ObjectId oid;
    if (config instanceof UniqueIdentifiable) {
      if (((UniqueIdentifiable) config).getUniqueId() == null && config instanceof MutableUniqueIdentifiable) {
        final UniqueId uid = UniqueId.of(SCHEME, Long.toString(_counter.incrementAndGet()));
        oid = uid.getObjectId();
        ((MutableUniqueIdentifiable) config).setUniqueId(uid);
      } else {
        throw new IllegalStateException("Config " + config + " was not MutableUniqueIdentifiable but did not have a UniqueId");
      }
    } else {
      final UniqueId uid = UniqueId.of(SCHEME, Long.toString(_counter.incrementAndGet()));
      item.setUniqueId(uid);
      oid = uid.getObjectId();
    }
    final ConfigDocument document = new ConfigDocument(item);
    final ConfigDocument replacedDocument = _store.putIfAbsent(oid, document);
    final Instant now = Instant.now();
    if (replacedDocument == null) {
      _changeManager.entityChanged(ChangeType.ADDED, oid, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    } else {
      _changeManager.entityChanged(ChangeType.CHANGED, oid, replacedDocument.getVersionFromInstant(), document.getVersionToInstant(), now);
    }
    return item;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    final ConfigDocument document = _store.get(uniqueId.getObjectId());
    if (document != null) {
      return document.getConfig();
    }
    throw new DataNotFoundException(uniqueId.toString());
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    for (final ConfigDocument document : _store.values()) {
      if (document != null && document.getObjectId().equals(objectId)) {
        return document.getConfig();
      }
    }
    throw new DataNotFoundException(objectId.toString());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    final Collection<ConfigItem<R>> result = new ArrayList<>();
    for (final ConfigDocument document : _store.values()) {
      final ConfigItem<R> item = (ConfigItem<R>) document.getConfig();
      if (clazz.isAssignableFrom(item.getType()) && item.getName().equals(configName)) {
        result.add(item);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    final Collection<ConfigItem<R>> result = new ArrayList<>();
    for (final ConfigDocument document : _store.values()) {
      final ConfigItem<R> item = (ConfigItem<R>) document.getConfig();
      if (clazz.isAssignableFrom(item.getType())) {
        result.add(item);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    return (R) get(uniqueId).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    return (R) get(objectId, versionCorrection).getValue();
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String configName) {
    return getSingle(clazz, configName, VersionCorrection.LATEST);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    for (final ConfigDocument document : _store.values()) {
      final ConfigItem<R> item = (ConfigItem<R>) document.getConfig();
      if (clazz.isAssignableFrom(item.getType()) && item.getName().equals(configName)) {
        return item.getValue();
      }
    }
    throw new DataNotFoundException(configName);
  }

}

