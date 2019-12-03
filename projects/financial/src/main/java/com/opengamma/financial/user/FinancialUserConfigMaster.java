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
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Wraps a view definition repository to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserConfigMaster extends AbstractFinancialUserMaster<ConfigDocument> implements ConfigMaster {

  /**
   * The underlying master.
   */
  private final ConfigMaster _underlyingConfigMaster;

  private final AbstractChangeProvidingMaster<ConfigDocument> _changeProvidingMaster;

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserConfigMaster(final FinancialClient client, final ConfigMaster underlying) {
    super(client, FinancialUserDataType.VIEW_DEFINITION);
    _underlyingConfigMaster = underlying;
    _changeProvidingMaster = ChangeProvidingDecorator.wrap(underlying);
  }

  @Override
  public ConfigDocument add(final ConfigDocument document) {
    return _changeProvidingMaster.add(document);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final ConfigDocument documentToAdd) {
    return _changeProvidingMaster.addVersion(objectId, documentToAdd);
  }

  @Override
  public ConfigDocument correct(final ConfigDocument document) {
    return _changeProvidingMaster.correct(document);
  }

  @Override
  public ConfigDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return _changeProvidingMaster.get(objectId, versionCorrection);
  }

  @Override
  public ConfigDocument get(final UniqueId uniqueId) {
    return _changeProvidingMaster.get(uniqueId);
  }

  @Override
  public Map<UniqueId, ConfigDocument> get(final Collection<UniqueId> uniqueIds) {
    return _changeProvidingMaster.get(uniqueIds);
  }

  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    _changeProvidingMaster.remove(objectIdentifiable);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    _changeProvidingMaster.removeVersion(uniqueId);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final ConfigDocument replacementDocument) {
    return _changeProvidingMaster.replaceVersion(replacementDocument);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<ConfigDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    return _changeProvidingMaster.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public ConfigDocument update(final ConfigDocument document) {
    return _changeProvidingMaster.update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeProvidingMaster.changeManager();
  }

  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    return _underlyingConfigMaster.history(request);
  }

  @Override
  public ConfigMetaDataResult metaData(final ConfigMetaDataRequest request) {
    return _underlyingConfigMaster.metaData(request);
  }

  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    return _underlyingConfigMaster.search(request);
  }
}
