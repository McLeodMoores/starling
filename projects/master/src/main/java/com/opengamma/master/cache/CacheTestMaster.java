/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;

class CacheTestMaster implements AbstractChangeProvidingMaster<CacheTestDocument> {

  @Override
  public CacheTestDocument get(final UniqueId uniqueId) {
    return null;
  }

  @Override
  public CacheTestDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<UniqueId, CacheTestDocument> get(final Collection<UniqueId> uniqueIds) {
    return null;
  }

  @Override
  public CacheTestDocument add(final CacheTestDocument document) {
    return null;
  }

  @Override
  public CacheTestDocument update(final CacheTestDocument document) {
    return null;
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
  }

  @Override
  public CacheTestDocument correct(final CacheTestDocument document) {
    return null;
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<CacheTestDocument> replacementDocuments) {
    return null;
  }

  @Override
  public UniqueId replaceVersion(final CacheTestDocument replacementDocument) {
    return null;
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final CacheTestDocument documentToAdd) {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return null;
  }

}
