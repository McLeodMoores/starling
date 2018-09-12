/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Turns any master into change providing master.
 */
public class ChangeProvidingDecorator {

  public static <D extends AbstractDocument> AbstractChangeProvidingMaster<D> wrap(final AbstractMaster<D> underlying) {
    return wrap(underlying, Clock.systemUTC());
  }

  public static <D extends AbstractDocument> AbstractChangeProvidingMaster<D> wrap(final AbstractMaster<D> underlying, final Clock clock) {
    return new AbstractChangeProvidingMaster<D>() {
      private final BasicChangeManager _changeManager = new BasicChangeManager();

      @Override
      public ChangeManager changeManager() {
        return _changeManager;
      }

      @Override
      public D add(final D document) {
        final D doc = underlying.add(document);
        _changeManager.entityChanged(ChangeType.CHANGED, doc.getObjectId(), doc.getVersionFromInstant(), doc.getVersionToInstant(), Instant.now(clock));
        return doc;
      }

      @Override
      public UniqueId addVersion(final ObjectIdentifiable objectId, final D documentToAdd) {
        final UniqueId uid = underlying.addVersion(objectId, documentToAdd);
        _changeManager.entityChanged(ChangeType.ADDED, uid.getObjectId(), null, null, Instant.now(clock));
        return uid;
      }

      @Override
      public D correct(final D document) {
        final D doc = underlying.correct(document);
        _changeManager.entityChanged(ChangeType.CHANGED, doc.getObjectId(), doc.getVersionFromInstant(), doc.getVersionToInstant(), Instant.now(clock));
        return doc;
      }

      @Override
      public D get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
        return underlying.get(objectId, versionCorrection);
      }

      @Override
      public D get(final UniqueId uniqueId) {
        return underlying.get(uniqueId);
      }

      @Override
      public Map<UniqueId, D> get(final Collection<UniqueId> uniqueIds) {
        return underlying.get(uniqueIds);
      }

      @Override
      public void remove(final ObjectIdentifiable objectIdentifiable) {
        underlying.remove(objectIdentifiable);
        _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now(clock));
      }

      @Override
      public void removeVersion(final UniqueId uniqueId) {
        underlying.removeVersion(uniqueId);
        _changeManager.entityChanged(ChangeType.REMOVED, uniqueId.getObjectId(), null, null, Instant.now(clock));
      }

      @Override
      public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
        final List<UniqueId> removed = underlying.replaceAllVersions(objectId, replacementDocuments);
        _changeManager.entityChanged(ChangeType.REMOVED, objectId.getObjectId(), null, null, Instant.now(clock));
        return removed;
      }

      @Override
      public UniqueId replaceVersion(final D replacementDocument) {
        final UniqueId uid = underlying.replaceVersion(replacementDocument);
        _changeManager.entityChanged(ChangeType.CHANGED, uid.getObjectId(), null, null, Instant.now(clock));
        return uid;
      }

      @Override
      public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<D> replacementDocuments) {
        final List<UniqueId> replaced = underlying.replaceVersion(uniqueId, replacementDocuments);
        _changeManager.entityChanged(ChangeType.CHANGED, uniqueId.getObjectId(), null, null, Instant.now(clock));
        return replaced;
      }

      @Override
      public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
        final List<UniqueId> replaced = underlying.replaceVersions(objectId, replacementDocuments);
        _changeManager.entityChanged(ChangeType.CHANGED, objectId.getObjectId(), null, null, Instant.now(clock));
        return replaced;
      }

      @Override
      public D update(final D document) {
        final D doc = underlying.update(document);
        _changeManager.entityChanged(ChangeType.CHANGED, doc.getObjectId(), doc.getVersionFromInstant(), doc.getVersionToInstant(), Instant.now(clock));
        return doc;
      }
    };

  }
}
