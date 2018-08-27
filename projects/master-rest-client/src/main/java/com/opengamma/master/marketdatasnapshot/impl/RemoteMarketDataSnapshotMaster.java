/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link MarketDataSnapshotMaster}.
 */
public class RemoteMarketDataSnapshotMaster
    extends AbstractRemoteDocumentMaster<MarketDataSnapshotDocument>
    implements MarketDataSnapshotMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteMarketDataSnapshotMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteMarketDataSnapshotMaster(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(request, "request");

    final URI uri = DataMarketDataSnapshotMasterUris.uriSearch(getBaseUri());
    return accessRemote(uri).post(MarketDataSnapshotSearchResult.class, request);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      final URI uri = new DataMarketDataSnapshotUris().uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(MarketDataSnapshotDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    final URI uri = new DataMarketDataSnapshotUris().uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(MarketDataSnapshotDocument.class);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("deprecation")
  @Override
  public MarketDataSnapshotDocument add(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");

    final URI uri = DataMarketDataSnapshotMasterUris.uriAdd(getBaseUri());
    return accessRemote(uri).post(MarketDataSnapshotDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("deprecation")
  @Override
  public MarketDataSnapshotDocument update(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = new DataMarketDataSnapshotUris().uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).post(MarketDataSnapshotDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    final URI uri = new DataMarketDataSnapshotUris().uri(getBaseUri(), objectIdentifiable, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final URI uri = new DataMarketDataSnapshotUris().uriVersions(getBaseUri(), request.getObjectId(), request);
    return accessRemote(uri).get(MarketDataSnapshotHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument correct(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = new DataMarketDataSnapshotUris().uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(MarketDataSnapshotDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<MarketDataSnapshotDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final MarketDataSnapshotDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSnapshot(), "documentToAdd.snapshot");
    }
    final URI uri = new DataMarketDataSnapshotUris().uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<MarketDataSnapshotDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final MarketDataSnapshotDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSnapshot(), "documentToAdd.snapshot");
    }
    final URI uri = new DataMarketDataSnapshotUris().uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<MarketDataSnapshotDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final MarketDataSnapshotDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getSnapshot(), "documentToAdd.snapshot");
    }
    final URI uri = new DataMarketDataSnapshotUris().uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
