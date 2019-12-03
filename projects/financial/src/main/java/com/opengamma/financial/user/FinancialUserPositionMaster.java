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
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Wraps a position master to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserPositionMaster extends AbstractFinancialUserMaster<PositionDocument> implements PositionMaster {

  /**
   * The underlying master.
   */
  private final PositionMaster _underlying;
  private final AbstractChangeProvidingMaster<PositionDocument> _changeProvidingMaster;

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserPositionMaster(final FinancialClient client, final PositionMaster underlying) {
    super(client, FinancialUserDataType.POSITION);
    _underlying = underlying;
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
  }

  @Override
  public PositionDocument add(final PositionDocument document) {
    return _changeProvidingMaster.add(document);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final PositionDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  @Override
  public PositionDocument correct(final PositionDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public PositionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public PositionDocument get(final UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  @Override
  public Map<UniqueId, PositionDocument> get(final Collection<UniqueId> uniqueIds) {
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
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<PositionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final PositionDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<PositionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<PositionDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public PositionDocument update(final PositionDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    return _underlying.getTrade(tradeId);
  }

  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    return _underlying.history(request);
  }

  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    return _underlying.search(request);
  }
}
