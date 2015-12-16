/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Simple mock region source for use in tests. The source is backed by a {@link ConcurrentHashMap} that maps an
 * {@link ExternalId} to {@link Region}. This source does not maintain any hierarchy of regions and will return
 * the first region found for a bundle. Version-corrections are ignored.
 */
public class MockRegionSource implements RegionSource {
  /** The backing map */
  private final ConcurrentMap<ExternalId, Region> _regions = new ConcurrentHashMap<>(new HashMap<ExternalId, Region>());

  /**
   * Adds a region to the source.
   * @param id  the id of the region, not null
   * @param region  the region, not null
   * @return  the previous region associated with this id, or null if there was no entry
   */
  public Region addRegion(final ExternalId id, final Region region) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(region, "region");
    return _regions.putIfAbsent(id, region);
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return get(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Region>> result = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      result.put(bundle, get(bundle, versionCorrection));
    }
    return result;
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle) {
    final Collection<Region> regions = new HashSet<>();
    for (final ExternalId id : bundle) {
      final Region region = _regions.get(id);
      if (region != null) {
        regions.add(region);
      }
    }
    return regions;
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle) {
    for (final ExternalId id : bundle) {
      final Region region = _regions.get(id);
      if (region != null) {
        return region;
      }
    }
    return null;
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return getSingle(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Region> result = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      final Region region = getSingle(bundle);
      if (region != null) {
        result.put(bundle, region);
      }
    }
    return result;
  }

  @Override
  public Region get(final UniqueId uniqueId) {
    return getSingle(uniqueId.toExternalId().toBundle());
  }

  @Override
  public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getSingle(ExternalIdBundle.of(objectId.getScheme(), objectId.getValue()));
  }

  @Override
  public Map<UniqueId, Region> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Region> result = new HashMap<>();
    for (final UniqueId uniqueId : uniqueIds) {
      final Region region = get(uniqueId);
      if (region != null) {
        result.put(uniqueId, region);
      }
    }
    return result;
  }

  @Override
  public Map<ObjectId, Region> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Region> result = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Region region = get(objectId, versionCorrection);
      if (region != null) {
        result.put(objectId, region);
      }
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public Region getHighestLevelRegion(final ExternalId externalId) {
    return getSingle(externalId.toBundle());
  }

  @Override
  public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
    return getSingle(bundle);
  }

}
