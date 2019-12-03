/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.MasterUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * A simple, in-memory implementation of {@code ConfigMaster}.
 * <p>
 * This master does not support versioning of configuration documents.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile. As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryConfigMaster implements ConfigMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemCfg";

  /**
   * A cache of configurations by identifier.
   */
  private final ConcurrentMap<ObjectId, ConfigDocument> _store = new ConcurrentHashMap<>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   */
  public InMemoryConfigMaster() {
    this(new ObjectIdSupplier(InMemoryConfigMaster.DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager the change manager, not null
   */
  public InMemoryConfigMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(InMemoryConfigMaster.DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   */
  public InMemoryConfigMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier the supplier of object identifiers, not null
   * @param changeManager the change manager, not null
   */
  public InMemoryConfigMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> ConfigSearchResult<T> search(final ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    final ConfigSearchRequest<T> req = request.clone();
    req.setVersionCorrection(VersionCorrection.LATEST);
    final List<ConfigDocument> list = new ArrayList<>();
    for (final ConfigDocument doc : _store.values()) {
      if (req.matches(doc)) {
        list.add(doc);
      }
    }
    Collections.sort(list, req.getSortOrder());

    final ConfigSearchResult<T> result = new ConfigSearchResult<>();
    result.setPaging(Paging.of(req.getPagingRequest(), list));
    result.getDocuments().addAll(req.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument add(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getConfig(), "document.object");
    ArgumentChecker.notNull(document.getConfig().getName(), "document.object.name");
    ArgumentChecker.notNull(document.getConfig().getValue(), "document.object.value");

    final ConfigItem<?> item = document.getConfig();
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();
    item.setUniqueId(uniqueId);
    IdUtils.setInto(item.getValue(), uniqueId);
    final ConfigDocument doc = new ConfigDocument(item);
    doc.setConfig(document.getConfig());
    doc.setUniqueId(uniqueId);
    doc.setVersionFromInstant(now);
    _store.put(objectId, doc);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, doc.getVersionFromInstant(), doc.getVersionToInstant(), now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument update(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getConfig(), "document.object");
    ArgumentChecker.notNull(document.getConfig().getValue(), "document.object.value");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final ConfigDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Config not found: " + uniqueId);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    document.setUniqueId(uniqueId);
    IdUtils.setInto(document.getConfig().getValue(), uniqueId);
    if (!_store.replace(uniqueId.getObjectId(), storedDocument, document)) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    // we do this before because we get called back to see what the 'old' version was to determine the type of config being removed.
    final Instant now = Instant.now();
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), Instant.MIN.plusNanos(1),
        now, now); // start needs to be min + 1 because change provider does -1 nano.
    if (_store.remove(objectIdentifiable.getObjectId()) == null) {
      throw new DataNotFoundException("Config not found: " + objectIdentifiable);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument correct(final ConfigDocument document) {
    return update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "document.uniqueId");
    final ConfigDocument document = _store.get(uniqueId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Config not found: " + uniqueId.getObjectId());
    }
    return document;
  }

  @Override
  public ConfigDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ConfigDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Config not found: " + objectId);
    }
    return document;
  }

  @Override
  public Map<UniqueId, ConfigDocument> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, ConfigDocument> resultMap = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      resultMap.put(uniqueId, _store.get(uniqueId.getObjectId()));
    }
    return resultMap;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigMetaDataResult metaData(final ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ConfigMetaDataResult result = new ConfigMetaDataResult();
    if (request.isConfigTypes()) {
      final Set<Class<?>> types = Sets.newHashSet();
      for (final ConfigDocument doc : _store.values()) {
        types.add(doc.getConfig().getType());
      }
      result.getConfigTypes().addAll(types);
    }
    return result;
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    return replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<ConfigDocument> replacementDocuments) {
    return replaceAllVersions(uniqueId.getObjectId(), replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    ArgumentChecker.notNull(objectId, "objectId");
    for (final ConfigDocument replacementDocument : replacementDocuments) {
      validateDocument(replacementDocument);
    }
    final Instant now = Instant.now();
    ArgumentChecker.isTrue(MasterUtils.checkUniqueVersionsFrom(replacementDocuments), "No two versioned documents may have the same \"version from\" instant");

    final ConfigDocument storedDocument = _store.get(objectId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Document not found: " + objectId.getObjectId());
    }

    if (replacementDocuments.isEmpty()) {
      _store.remove(objectId.getObjectId());
      _changeManager.entityChanged(ChangeType.REMOVED, objectId.getObjectId(), null, null, now);
      return Collections.emptyList();
    }
    final Instant storedVersionFrom = storedDocument.getVersionFromInstant();
    final Instant storedVersionTo = storedDocument.getVersionToInstant();

    final List<ConfigDocument> orderedReplacementDocuments = MasterUtils.adjustVersionInstants(now, storedVersionFrom, storedVersionTo, replacementDocuments);

    final ConfigDocument lastReplacementDocument = orderedReplacementDocuments.get(orderedReplacementDocuments.size() - 1);

    if (!_store.replace(objectId.getObjectId(), storedDocument, lastReplacementDocument)) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return ImmutableList.of(lastReplacementDocument.getUniqueId());
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final ConfigDocument documentToAdd) {
    final List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    }
    return result.get(0);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<ConfigDocument>emptyList());
  }

  @Override
  public UniqueId replaceVersion(final ConfigDocument replacementDocument) {
    final List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    }
    return result.get(0);
  }

  private static void validateDocument(final ConfigDocument document) {
    ArgumentChecker.notNull(document.getConfig(), "document.object");
    ArgumentChecker.notNull(document.getConfig().getValue(), "document.object.value");
    ArgumentChecker.notNull(document.getName(), "document.name");
  }

  //-------------------------------------------------------------------------
  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");

    final ConfigDocument doc = get(request.getObjectId(), VersionCorrection.LATEST);
    final ConfigHistoryResult<R> result = new ConfigHistoryResult<>();
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.ofAll(result.getDocuments()));
    return result;
  }

}
