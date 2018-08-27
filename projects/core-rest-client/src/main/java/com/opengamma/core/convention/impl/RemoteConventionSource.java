/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

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
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link ConventionSource}.
 */
public class RemoteConventionSource
    extends AbstractRemoteSource<Convention>
    implements ConventionSource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteConventionSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   * @param changeManager the change manager, not null
   */
  public RemoteConventionSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Convention get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataConventionSourceUris.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Convention.class);
  }

  @Override
  public <T extends Convention> T get(final UniqueId uniqueId, final Class<T> type) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(type, "type");
    final Convention convention = get(uniqueId);
    return type.cast(convention);
  }

  //-------------------------------------------------------------------------
  @Override
  public Convention get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataConventionSourceUris.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Convention.class);
  }

  @Override
  public <T extends Convention> T get(final ObjectId objectId, final VersionCorrection versionCorrection, final Class<T> type) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    final Convention convention = get(objectId, versionCorrection);
    return type.cast(convention);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Convention> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    final URI uri = DataConventionSourceUris.uriSearchList(getBaseUri(), bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Convention> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataConventionSourceUris.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Map<UniqueId, Convention> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    final URI uri = DataConventionSourceUris.uriBulk(getBaseUri(), uniqueIds);
    final List<Convention> list = accessRemote(uri).get(FudgeListWrapper.class).getList();
    final Map<UniqueId, Convention> result = Maps.newHashMap();
    for (final Convention convention : list) {
      result.put(convention.getUniqueId(), convention);
    }
    return result;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public Convention getSingle(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return doGetSingle(externalId.toBundle(), null, null);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalId externalId, final Class<T> type) {
    ArgumentChecker.notNull(externalId, "externalId");
    return doGetSingle(externalId.toBundle(), null, type);
  }

  @Override
  public Convention getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    return doGetSingle(bundle, null, null);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle bundle, final Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    return doGetSingle(bundle, null, type);
  }

  @Override
  public Convention getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return doGetSingle(bundle, versionCorrection, null);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection, final Class<T> type) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(type, "type");
    return doGetSingle(bundle, versionCorrection, type);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Convention> T doGetSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection, final Class<T> type) {
    try {
      final URI uri = DataConventionSourceUris.uriSearchSingle(getBaseUri(), bundle, versionCorrection, type);
      final Convention convention = accessRemote(uri).get(Convention.class);
      if (type != null) {
        return type.cast(convention);
      } else {
        return (T) convention;
      }
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
