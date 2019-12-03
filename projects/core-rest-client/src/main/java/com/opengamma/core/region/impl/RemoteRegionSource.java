/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link RegionSource}.
 */
public class RemoteRegionSource extends AbstractRemoteSource<Region> implements RegionSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance using a basic change manager.
   *
   * @param baseUri
   *          the base target URI for all RESTful web services, not null
   */
  public RemoteRegionSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri
   *          the base target URI for all RESTful web services, not null
   * @param changeManager
   *          the change manager, not null
   */
  public RemoteRegionSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Region get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataRegionSourceUris.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Region.class);
  }

  @Override
  public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataRegionSourceUris.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Region.class);
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataRegionSourceUris.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Region getHighestLevelRegion(final ExternalId externalId) {
    try {
      return getHighestLevelRegion(ExternalIdBundle.of(externalId));
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    try {
      final URI uri = DataRegionSourceUris.uriSearchHighest(getBaseUri(), bundle);
      return accessRemote(uri).get(Region.class);
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Collection<Region> get(final ExternalIdBundle bundle) {
    return AbstractSourceWithExternalBundle.get(this, bundle);
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundle);
  }

  @Override
  public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
