/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.AbstractSourceWithExternalBundle;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link SecuritySource}.
 */
public class RemoteSecuritySource extends AbstractRemoteSource<Security> implements SecuritySource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteSecuritySource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteSecuritySource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataSecuritySourceUris.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Security.class);
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataSecuritySourceUris.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Security.class);
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataSecuritySourceUris.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    final URI uri = DataSecuritySourceUris.uriBulk(getBaseUri(), uniqueIds);
    final List<Security> list = accessRemote(uri).get(FudgeListWrapper.class).getList();
    final Map<UniqueId, Security> result = Maps.newHashMap();
    for (final Security security : list) {
      result.put(security.getUniqueId(), security);
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    final URI uri = DataSecuritySourceUris.uriSearchList(getBaseUri(), bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    try {
      final URI uri = DataSecuritySourceUris.uriSearchSingle(getBaseUri(), bundle, null);
      return accessRemote(uri).get(Security.class);
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    try {
      final URI uri = DataSecuritySourceUris.uriSearchSingle(getBaseUri(), bundle, versionCorrection);
      return accessRemote(uri).get(Security.class);
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

}
