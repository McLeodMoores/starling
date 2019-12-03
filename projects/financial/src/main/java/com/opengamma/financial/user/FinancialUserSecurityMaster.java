/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.ChangeProvidingDecorator;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Wraps a security master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserSecurityMaster extends AbstractFinancialUserMaster<SecurityDocument> implements SecurityMaster {

  /**
   * The underlying master.
   */
  private final SecurityMaster _underlying;
  private final AbstractChangeProvidingMaster<SecurityDocument> _changeProvidingMaster;

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserSecurityMaster(final FinancialClient client, final SecurityMaster underlying) {
    super(client, FinancialUserDataType.SECURITY);
    _underlying = underlying;
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
  }

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    return _changeProvidingMaster.add(document);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final SecurityDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  @Override
  public Map<UniqueId, SecurityDocument> get(final Collection<UniqueId> uniqueIds) {
    return _changeProvidingMaster.get(uniqueIds);
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    _changeProvidingMaster.remove(oid);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    _changeProvidingMaster.removeVersion(uniqueId);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final SecurityDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<SecurityDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public SecurityDocument update(final SecurityDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    return _underlying.metaData(request);
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    return _underlying.search(request);
  }
}
