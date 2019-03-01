/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link ConfigMaster}.
 */
public class RemoteConfigMaster
extends AbstractRemoteDocumentMaster<ConfigDocument>
implements ConfigMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteConfigMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteConfigMaster(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigMetaDataResult metaData(final ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");

    final URI uri = DataConfigMasterUris.uriMetaData(getBaseUri(), request);
    return accessRemote(uri).get(ConfigMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");

    final URI uri = DataConfigMasterUris.uriSearch(getBaseUri());
    return accessRemote(uri).post(ConfigSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      final URI uri = DataConfigUris.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(ConfigDocument.class);
    }
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    final URI uri = DataConfigUris.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(ConfigDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument add(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");

    final URI uri = DataConfigMasterUris.uriAdd(getBaseUri());
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument update(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = DataConfigUris.uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    final URI uri = DataConfigUris.uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final URI uri = DataConfigUris.uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(ConfigHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument correct(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.config");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = DataConfigUris.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(ConfigDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }

    final URI uri = DataConfigUris.uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }
    final URI uri = DataConfigUris.uriAll(getBaseUri(), objectId, null, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final ConfigDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "replacementDocument");
      ArgumentChecker.notNull(replacementDocument.getConfig(), "replacementDocument.config");
    }
    final URI uri = DataConfigUris.uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
