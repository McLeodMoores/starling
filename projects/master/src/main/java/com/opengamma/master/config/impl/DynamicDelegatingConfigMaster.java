/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A Config master that uses the scheme of the unique identifier to determine which underlying master should handle the request.
 * <p>
 * The underlying masters, or delegates, can be registered or deregistered at run time. By default there is an {@link InMemoryConfigMaster} that will be used if
 * specific scheme/delegate combinations have not been registered.
 * <p>
 * Change events are aggregated from the different masters and presented through a single change manager.
 * <p>
 * The {@link #register(String, ConfigMaster)}, {@link #deregister(String)} and {@link #add(String, ConfigDocument)} methods are public API outside of the
 * normal Master interface. Therefore to properly use this class the caller must have a concrete instance of this class and use these methods to properly
 * initialize the delegates as well as clean up resources when a delegate is no longer needed. But the engine itself will be able to interact with the component
 * via standard Master interface.
 */
public class DynamicDelegatingConfigMaster implements ConfigMaster {

  /** The change manager. Aggregates among all the delegates */
  private final AggregatingChangeManager _changeManager;

  /**
   * The default delegate. Should never have data in it. If user ask for data with an unregistered scheme, this empty master will be used
   */
  private final InMemoryConfigMaster _defaultEmptyDelegate;

  /** Delegator for maintaining map from scheme to master */
  private final UniqueIdSchemeDelegator<ConfigMaster> _delegator;

  /**
   * Default constructor that uses an in-memory master
   * ({@link InMemoryConfigMaster}) as the default delegate and a basic change
   * manager ({@link com.opengamma.core.change.BasicChangeManager}).
   */
  public DynamicDelegatingConfigMaster() {
    _changeManager = new AggregatingChangeManager();
    _defaultEmptyDelegate = new InMemoryConfigMaster();
    _delegator = new UniqueIdSchemeDelegator<ConfigMaster>(_defaultEmptyDelegate);
    _changeManager.addChangeManager(_defaultEmptyDelegate.changeManager());
  }

  /**
   * Registers a scheme and delegate pair.
   * <p>
   * The caller is responsible for creating a delegate and registering it before making calls to the DynamicDelegatingConfigMaster
   *
   * @param scheme
   *          the external scheme associated with this delegate master, not null
   * @param delegate
   *          the master to be used for this scheme, not null
   */
  public void register(final String scheme, final ConfigMaster delegate) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(delegate, "delegate");
    _changeManager.addChangeManager(delegate.changeManager());
    _delegator.registerDelegate(scheme, delegate);
  }

  /**
   * Deregisters a scheme and delegate pair.
   * <p>
   * The caller is responsible for deregistering a delegate when it is no longer needed. For example, if delegates are made up of InMemoryMasters and data is no
   * longer needed, call deregister will free up memory
   *
   * @param scheme
   *          the external scheme associated with the delegate master to be removed, not null
   */
  public void deregister(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _changeManager.removeChangeManager(chooseDelegate(scheme).changeManager());
    _delegator.removeDelegate(scheme);
  }

  /**
   * Adds a config document to the appropriate delegate master, using the provided String as the scheme to search for a delegate.
   *
   * @param scheme
   *          the unique identifier scheme that will choose the delegate, not null
   * @param document
   *          the document to add to the master, not null
   * @return the document with unique id set
   */
  public ConfigDocument add(final String scheme, final ConfigDocument document) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(scheme).add(document);
  }

  private ConfigMaster chooseDelegate(final String scheme) {
    return _delegator.chooseDelegate(scheme);
  }

  @Override
  public ConfigDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public ConfigDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, ConfigDocument> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    final Map<UniqueId, ConfigDocument> resultMap = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      final ConfigDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public ConfigDocument add(final ConfigDocument document) {
    throw new UnsupportedOperationException("Cannot add document without explicitly specifying the scheme");
  }

  @Override
  public ConfigDocument update(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getObjectId(), "document.objectId");
    return chooseDelegate(document.getObjectId().getScheme()).update(document);
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    ArgumentChecker.notNull(oid, "objectIdentifiable");
    chooseDelegate(oid.getObjectId().getScheme()).remove(oid);
  }

  @Override
  public ConfigDocument correct(final ConfigDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<ConfigDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final ConfigDocument replacementDocument) {
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
  public UniqueId addVersion(final ObjectIdentifiable objectId, final ConfigDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public <R> ConfigSearchResult<R> search(final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    final Iterable<ConfigSearchResult<R>> delegateResults = transform(_delegator.getAllDelegates(), new Function<ConfigMaster, ConfigSearchResult<R>>() {
      @Override
      public ConfigSearchResult<R> apply(final ConfigMaster input) {
        return input.search(request);
      }
    });
    final ConfigSearchResult<R> result = new ConfigSearchResult<>();
    for (final ConfigSearchResult<R> delegateResult : delegateResults) {
      result.getDocuments().addAll(delegateResult.getDocuments());
    }
    return result;
  }

  @Override
  public <R> ConfigHistoryResult<R> history(final ConfigHistoryRequest<R> request) {
    ArgumentChecker.notNull(request, "request");
    final ObjectId objectId = request.getObjectId();
    ArgumentChecker.notNull(objectId, "objectId");
    return chooseDelegate(objectId.getScheme()).history(request);
  }

  @Override
  public ConfigMetaDataResult metaData(final ConfigMetaDataRequest request) {
    throw new UnsupportedOperationException("metaData() not supported on DynamicDelegatingConfigMaster");
  }
}
