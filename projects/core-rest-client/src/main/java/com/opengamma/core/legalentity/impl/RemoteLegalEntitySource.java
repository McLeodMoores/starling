/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

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
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link LegalEntitySource}.
 */
public class RemoteLegalEntitySource
extends AbstractRemoteSource<LegalEntity>
implements LegalEntitySource {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteLegalEntitySource(final URI baseUri) {
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
  public RemoteLegalEntitySource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataLegalEntitySourceUris.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(LegalEntity.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataLegalEntitySourceUris.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(LegalEntity.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<LegalEntity> get(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    final URI uri = DataLegalEntitySourceUris.uriSearchList(getBaseUri(), bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  @Override
  public Collection<LegalEntity> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataLegalEntitySourceUris.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<UniqueId, LegalEntity> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    final URI uri = DataLegalEntitySourceUris.uriBulk(getBaseUri(), uniqueIds);
    final List<LegalEntity> list = accessRemote(uri).get(FudgeListWrapper.class).getList();
    final Map<UniqueId, LegalEntity> result = Maps.newHashMap();
    for (final LegalEntity legalEntity : list) {
      result.put(legalEntity.getUniqueId(), legalEntity);
    }
    return result;
  }

  @Override
  public Map<ExternalIdBundle, Collection<LegalEntity>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getAll(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntity getSingle(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    final LegalEntity legalEntity = doGetSingle(externalId.toBundle(), null, null);
    if (legalEntity == null) {
      throw new DataNotFoundException("No legal entity: " + externalId);
    }
    return legalEntity;
  }

  @Override
  public LegalEntity getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    final LegalEntity legalEntity = doGetSingle(bundle, null, null);
    if (legalEntity == null) {
      throw new DataNotFoundException("No legal entity: " + bundle);
    }
    return legalEntity;
  }

  @Override
  public LegalEntity getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final LegalEntity legalEntity = doGetSingle(bundle, versionCorrection, null);
    if (legalEntity == null) {
      throw new DataNotFoundException("No legal entity: " + bundle + " " + versionCorrection);
    }
    return legalEntity;
  }

  /**
   * Gets a single legal entity or return null.
   *
   * @param bundle
   *          the identifiers
   * @param versionCorrection
   *          the version
   * @param type
   *          the type
   * @return the legal entity or null
   * @param <T>
   *          the type of the legal entity
   */
  @SuppressWarnings("unchecked")
  protected <T extends LegalEntity> T doGetSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection, final Class<T> type) {
    try {
      final URI uri = DataLegalEntitySourceUris.uriSearchSingle(getBaseUri(), bundle, versionCorrection, type);
      final LegalEntity legalEntity = accessRemote(uri).get(LegalEntity.class);
      if (type != null) {
        return type.cast(legalEntity);
      }
      return (T) legalEntity;
    } catch (final DataNotFoundException ex) {
      return null;
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Map<ExternalIdBundle, LegalEntity> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundles, "bundles");
    // TODO: Implement this properly as a REST call
    return AbstractSourceWithExternalBundle.getSingle(this, bundles, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
