/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;
import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.impl.AbstractRemoteDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.GenericType;

/**
 * Provides access to a remote {@link InterpolatedYieldCurveDefinitionMaster}.
 */
public class RemoteInterpolatedYieldCurveDefinitionMaster extends AbstractRemoteDocumentMaster<YieldCurveDefinitionDocument> implements InterpolatedYieldCurveDefinitionMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteInterpolatedYieldCurveDefinitionMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteInterpolatedYieldCurveDefinitionMaster(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    if (uniqueId.isVersioned()) {
      final URI uri = new DataInterpolatedYieldCurveDefinitionUris().uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(YieldCurveDefinitionDocument.class);
    }
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    final URI uri = new DataInterpolatedYieldCurveDefinitionUris().uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(YieldCurveDefinitionDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument add(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");

    final URI uri = DataInterpolatedYieldCurveDefinitionMasterUris.uri(getBaseUri());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument addOrUpdate(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");

    final URI uri = DataInterpolatedYieldCurveDefinitionMasterUris.uri(getBaseUri());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument update(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uri(getBaseUri(), document.getUniqueId(), null);
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");

    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uri(getBaseUri(), objectIdentifiable, null);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinitionDocument correct(final YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.definition");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).post(YieldCurveDefinitionDocument.class, document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uriVersion(getBaseUri(), uniqueId);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uriAll(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<YieldCurveDefinitionDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    for (final YieldCurveDefinitionDocument replacementDocument : replacementDocuments) {
      ArgumentChecker.notNull(replacementDocument, "documentToAdd");
      ArgumentChecker.notNull(replacementDocument.getYieldCurveDefinition(), "document.definition");
    }
    final URI uri = new DataInterpolatedYieldCurveDefinitionMasterUris().uri(getBaseUri(), objectId, null);
    return accessRemote(uri).put(new GenericType<List<UniqueId>>() {
    }, replacementDocuments);
  }
}
