/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.groupingBy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;

/**
 * A source of snapshots that uses the scheme of the unique identifier to determine which underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingSnapshotSource extends UniqueIdSchemeDelegator<MarketDataSnapshotSource> implements MarketDataSnapshotSource {

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultSource
   *          the source to use when no scheme matches, not null
   */
  public DelegatingSnapshotSource(final MarketDataSnapshotSource defaultSource) {
    super(defaultSource);
  }

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultSource
   *          the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap
   *          the map of sources by scheme to switch on, not null
   */
  public DelegatingSnapshotSource(final MarketDataSnapshotSource defaultSource, final Map<String, MarketDataSnapshotSource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
  }

  @Override
  public StructuredMarketDataSnapshot get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return chooseDelegate(objectId.getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public StructuredMarketDataSnapshot get(final UniqueId uniqueId) {
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public Map<UniqueId, StructuredMarketDataSnapshot> get(final Collection<UniqueId> uniqueIds) {
    final Map<String, List<UniqueId>> groups = uniqueIds.stream().collect(groupingBy(UniqueId::getScheme));
    final Map<UniqueId, StructuredMarketDataSnapshot> snapshots = newHashMap();
    for (final Map.Entry<String, List<UniqueId>> entries : groups.entrySet()) {
      snapshots.putAll(chooseDelegate(entries.getKey()).get(entries.getValue()));
    }
    return snapshots;
  }

  @Override
  public Map<ObjectId, StructuredMarketDataSnapshot> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<String, List<ObjectId>> groups = objectIds.stream().collect(groupingBy(ObjectId::getScheme));
    final Map<ObjectId, StructuredMarketDataSnapshot> snapshots = newHashMap();
    for (final Map.Entry<String, List<ObjectId>> entries : groups.entrySet()) {
      snapshots.putAll(chooseDelegate(entries.getKey()).get(entries.getValue(), versionCorrection));
    }
    return snapshots;
  }

  @Override
  public void addChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    chooseDelegate(uniqueId.getScheme()).addChangeListener(uniqueId, listener);
  }

  @Override
  public void removeChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    chooseDelegate(uniqueId.getScheme()).removeChangeListener(uniqueId, listener);
  }

  @Override
  public <S extends NamedSnapshot> S getSingle(final Class<S> type, final String snapshotName, final VersionCorrection versionCorrection) {
    // As we have no information about the scheme we can't do anything but use the default
    return getDefaultDelegate().getSingle(type, snapshotName, versionCorrection);
  }
}
