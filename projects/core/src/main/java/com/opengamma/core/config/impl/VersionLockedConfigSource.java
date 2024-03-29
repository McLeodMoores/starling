/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.Collection;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ConfigSource} wrapper which sets a specific version/correction on all requests that would otherwise request "latest".
 * <p>
 * Where possible, code should be written that explicitly passes the necessary version/correction information
 * around - this is an intermediate solution for working with existing code that is not properly version aware.
 *
 * @deprecated Call code that is properly version aware (whenever possible)
 */
@Deprecated
public class VersionLockedConfigSource implements ConfigSource {

  private final ConfigSource _underlying;
  private final VersionCorrection _versionCorrection;

  /**
   * Constructs a config source with a locked version.
   *
   * @param underlying  the underlying source, not null
   * @param versionCorrection  the version/correction, not null
   */
  public VersionLockedConfigSource(final ConfigSource underlying, final VersionCorrection versionCorrection) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
    _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
  }

  /**
   * Gets the underlying config source.
   *
   * @return  the source
   */
  protected ConfigSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the locked version/correction.
   *
   * @return  the version/correction
   */
  protected VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Locks the version/correction. If the input is either the latest version or correction, then the locked
   * version or correction respectively is returned in the result. Otherwise, the original version is used.
   *
   * @param versionCorrection  the new version/correction
   * @return  the version/correction that is used to get data from the source
   */
  protected VersionCorrection lockVersionCorrection(final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      final Instant version = versionCorrection.getVersionAsOf() == null ? getVersionCorrection().getVersionAsOf() : versionCorrection.getVersionAsOf();
      final Instant correction = versionCorrection.getCorrectedTo() == null ? getVersionCorrection().getCorrectedTo() : versionCorrection.getCorrectedTo();
      return VersionCorrection.of(version, correction);
    }
    return versionCorrection;
  }

  @Override
  public Map<UniqueId, ConfigItem<?>> get(final Collection<UniqueId> uniqueIds) {
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public Map<ObjectId, ConfigItem<?>> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, lockVersionCorrection(versionCorrection));
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public ConfigItem<?> get(final UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    return getUnderlying().get(clazz, configName, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
    return getUnderlying().getAll(clazz, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
    return getUnderlying().getConfig(clazz, uniqueId);
  }

  @Override
  public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getUnderlying().getConfig(clazz, objectId, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(clazz, configName, lockVersionCorrection(versionCorrection));
  }

  @Override
  public <R> R getLatestByName(final Class<R> clazz, final String name) {
    return getUnderlying().getSingle(clazz, name, getVersionCorrection());
  }

}
