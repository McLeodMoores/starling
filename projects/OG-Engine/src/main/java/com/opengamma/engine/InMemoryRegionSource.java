/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple implementation of a region source intended for use in tests. The regions are stored by ids and are matched
 * only by the highest-level region.
 * <p>
 * This source does not support versioning or change management. It is not thread-safe and so is not suitable for use in production.
 */
public class InMemoryRegionSource implements RegionSource {
  /** A map from identifier to region */
  private final Map<ObjectId, Region> _regions = new HashMap<>();
  /** Supplies unique identifiers */
  private final UniqueIdSupplier _uidSupplier;

  /**
   * Creates an empty region source.
   */
  public InMemoryRegionSource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return get(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Map<ExternalIdBundle, Collection<Region>> regions = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      final Collection<Region> region = get(bundle, versionCorrection);
      if (region != null) {
        regions.put(bundle, region);
      }
    }
    return regions;
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final Collection<Region> regions = new HashSet<>();
    for (final ExternalId id : bundle) {
      final Region region = _regions.get(ObjectId.parse(id.toString()));
      if (region != null) {
        regions.add(region);
      }
    }
    return regions;
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (final ExternalId id : bundle) {
      final Region region = _regions.get(ObjectId.parse(id.toString()));
      if (region != null) {
        return region;
      }
    }
    throw new DataNotFoundException("Could not get any regions for " + bundle);
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return getSingle(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Map<ExternalIdBundle, Region> regions = new HashMap<>();
    for (final ExternalIdBundle bundle : bundles) {
      for (final ExternalId id : bundle) {
        final Region region = _regions.get(ObjectId.parse(id.toString()));
        if (region != null) {
          regions.put(bundle, region);
        }
      }
    }
    return regions;
  }

  @Override
  public Region get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Region region = _regions.get(uniqueId.getObjectId());
    if (region == null) {
      throw new DataNotFoundException("Could not get region: " + uniqueId);
    }
    return region;
  }

  @Override
  public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Region region = _regions.get(objectId);
    if (region == null) {
      throw new DataNotFoundException("Could not get region: " + objectId);
    }
    return region;
  }

  @Override
  public Map<UniqueId, Region> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    final Map<UniqueId, Region> regions = new HashMap<>();
    for (final UniqueId uniqueId : uniqueIds) {
      final Region region = _regions.get(uniqueId.getObjectId());
      if (region != null) {
        regions.put(uniqueId, region);
      }
    }
    return regions;
  }

  @Override
  public Map<ObjectId, Region> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectIds, "objectIds");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Map<ObjectId, Region> regions = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Region region = _regions.get(objectId);
      if (region != null) {
        regions.put(objectId, region);
      }
    }
    return regions;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public Region getHighestLevelRegion(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return getSingle(externalId.toBundle());
  }

  @Override
  public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return getSingle(bundle);
  }

  /**
   * Adds a region to the source.
   * @param region  the region, not null
   */
  public void addRegion(final Region region) {
    ArgumentChecker.notNull(region, "region");
    IdUtils.setInto(region, _uidSupplier.get());
    _regions.put(region.getUniqueId().getObjectId(), region);
    for (final ExternalId externalId : region.getExternalIdBundle()) {
      _regions.put(ObjectId.parse(externalId.toString()), region);
    }
  }
}
