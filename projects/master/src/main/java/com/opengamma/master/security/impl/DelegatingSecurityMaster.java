/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A master of Securities that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different masters and presented through a single change manager.
 */
public class DelegatingSecurityMaster extends UniqueIdSchemeDelegator<SecurityMaster> implements SecurityMaster {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   */
  public DelegatingSecurityMaster(final SecurityMaster defaultMaster) {
    super(defaultMaster);
    _changeManager = defaultMaster.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   *
   * @param defaultMaster the master to use when no scheme matches, not null
   * @param schemePrefixToMasterMap  the map of masters by scheme to switch on, not null
   */
  public DelegatingSecurityMaster(final SecurityMaster defaultMaster, final Map<String, SecurityMaster> schemePrefixToMasterMap) {
    super(defaultMaster, schemePrefixToMasterMap);
    final AggregatingChangeManager changeManager = new AggregatingChangeManager();

    changeManager.addChangeManager(defaultMaster.changeManager());
    for (final SecurityMaster master : schemePrefixToMasterMap.values()) {
      changeManager.addChangeManager(master.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    return chooseDelegate(request.getObjectId().getScheme()).history(request);
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final String uniqueIdScheme = request.getUniqueIdScheme();
    if (uniqueIdScheme == null) {
      return getDefaultDelegate().search(request);
    }
    return chooseDelegate(uniqueIdScheme).search(request);
  }

  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    final UniqueId uniqueId = document.getUniqueId();
    if (uniqueId == null) {
      return getDefaultDelegate().add(document);
    }
    return chooseDelegate(uniqueId.getScheme()).add(document);
  }

  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).update(document);
  }

  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    chooseDelegate(objectIdentifiable.getObjectId().getScheme()).remove(objectIdentifiable);
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final SecurityDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final SecurityDocument replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    ArgumentChecker.notNull(replacementDocument.getObjectId(), "replacementDocument.getObjectId");
    return chooseDelegate(replacementDocument.getObjectId().getScheme()).replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    chooseDelegate(uniqueId.getScheme()).removeVersion(uniqueId);
  }

  @Override
  public Map<UniqueId, SecurityDocument> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    final Map<UniqueId, SecurityDocument> resultMap = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      final SecurityDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    final String uniqueIdScheme = request.getUniqueIdScheme();
    if (uniqueIdScheme == null) {
      return getDefaultDelegate().metaData(request);
    }
    return chooseDelegate(uniqueIdScheme).metaData(request);
  }

}
