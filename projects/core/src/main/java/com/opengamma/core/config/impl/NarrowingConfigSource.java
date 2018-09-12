/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A config source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingConfigSource implements ConfigSource {

  private final ConfigSource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingConfigSource(final ConfigSource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(final Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    return _delegate.get(clazz, configName, versionCorrection);
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("This method should not be used");
  }

  @Override
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    return checkAndCast(clazz, get(uniqueId));
  }

  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    return checkAndCast(clazz, get(objectId, versionCorrection));
  }

  private static <R> R checkAndCast(final Class<R> clazz, final ConfigItem<?> item) {
    return clazz.isAssignableFrom(item.getType()) ? clazz.cast(item.getValue()) : null;
  }

  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    final Collection<ConfigItem<R>> result = get(clazz, configName, versionCorrection);
    return result.isEmpty() ? null : result.iterator().next().getValue();
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getSingle(clazz, name, VersionCorrection.LATEST);
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }
}
