/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import org.joda.beans.JodaBeanUtils;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.*;
import com.opengamma.master.MasterUtils;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.portfolio.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a portfolio master.
 */
public class InMemoryPortfolioMaster extends SimpleAbstractInMemoryMaster<ManageablePortfolio, PortfolioDocument> implements PortfolioMaster {

  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemPrt";

  /**
   * A cache of portfolio nodes by identifier.
   */
  private final ConcurrentMap<ObjectId, ManageablePortfolioNode> _storeNodes = new ConcurrentHashMap<ObjectId, ManageablePortfolioNode>();

  
  /**
   * Creates an instance.
   */
  public InMemoryPortfolioMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }
  
  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryPortfolioMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }
  
  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryPortfolioMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }
  
  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryPortfolioMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final PortfolioDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Portfolio not found: " + objectId);
    }
    return clonePortfolioDocument(document);
  }
  
  private PortfolioDocument clonePortfolioDocument(PortfolioDocument document) {
    PortfolioDocument clone = JodaBeanUtils.clone(document);
    ManageablePortfolio portfolioClone = JodaBeanUtils.clone(document.getObject());
    portfolioClone.setRootNode(clonePortfolioNode(portfolioClone.getRootNode()));
    clone.setObject(portfolioClone);
    return clone;
  }
  
  private ManageablePortfolioNode clonePortfolioNode(ManageablePortfolioNode node) {
    ManageablePortfolioNode clone = JodaBeanUtils.clone(node);
    List<ManageablePortfolioNode> childNodes = new ArrayList<ManageablePortfolioNode>(node.getChildNodes().size());
    for (ManageablePortfolioNode child : node.getChildNodes()) {
      childNodes.add(clonePortfolioNode(child));
    }
    clone.setChildNodes(childNodes);
    return clone;
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.portfolio");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();
    
    final PortfolioDocument clonedDoc = clonePortfolioDocument(document);
    setDocumentId(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);
    storeNodes(clonedDoc.getObject().getRootNode(), document.getObject().getRootNode(), uniqueId, null);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }
  
  private void setDocumentId(final PortfolioDocument document, final PortfolioDocument clonedDoc, final UniqueId uniqueId) {
    document.getObject().setUniqueId(uniqueId);
    clonedDoc.getObject().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }
  
  private void storeNodes(final ManageablePortfolioNode clonedNode, final ManageablePortfolioNode origNode, final UniqueId portfolioId, final UniqueId parentNodeId) {
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    clonedNode.setUniqueId(uniqueId);
    origNode.setUniqueId(uniqueId);
    clonedNode.setParentNodeId(parentNodeId);
    origNode.setParentNodeId(parentNodeId);
    clonedNode.setPortfolioId(portfolioId);
    origNode.setPortfolioId(portfolioId);
    _storeNodes.put(objectId, clonedNode);
    for (int i = 0; i < clonedNode.getChildNodes().size(); i++) {
      storeNodes(clonedNode.getChildNodes().get(i), origNode.getChildNodes().get(i), portfolioId, uniqueId);
    }
  }
  
  private void setVersionTimes(PortfolioDocument document, final PortfolioDocument clonedDoc, 
      final Instant versionFromInstant, final Instant versionToInstant, final Instant correctionFromInstant, final Instant correctionToInstant) {
    
    clonedDoc.setVersionFromInstant(versionFromInstant);
    document.setVersionFromInstant(versionFromInstant);
    
    clonedDoc.setVersionToInstant(versionToInstant);
    document.setVersionToInstant(versionToInstant);
    
    clonedDoc.setCorrectionFromInstant(correctionFromInstant);
    document.setCorrectionFromInstant(correctionFromInstant);
    
    clonedDoc.setCorrectionToInstant(correctionToInstant);
    document.setCorrectionToInstant(correctionToInstant);
  }

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getObject(), "document.portfolio");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final PortfolioDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found: " + uniqueId);
    }
    
    final PortfolioDocument clonedDoc = clonePortfolioDocument(document);
    removeNodes(storedDocument.getObject().getRootNode());
    
    setVersionTimes(document, clonedDoc, now, null, now, null);
    
    if (_store.replace(uniqueId.getObjectId(), storedDocument, clonedDoc) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeNodes(clonedDoc.getObject().getRootNode(), document.getObject().getRootNode(), uniqueId, null);
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private void removeNodes(ManageablePortfolioNode node) {
    if (_storeNodes.remove(node.getUniqueId().getObjectId()) == null) {
      throw new DataNotFoundException("Node not found: " + node.getUniqueId());
    }
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      removeNodes(childNode);
    }
  }
  
  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    PortfolioDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found " + objectIdentifiable);
    }
    removeNodes(storedDocument.getObject().getRootNode());
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    return update(document);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<PortfolioDocument> list = new ArrayList<PortfolioDocument>();
    for (PortfolioDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(clonePortfolioDocument(doc));
      }
    }
    final PortfolioSearchResult result = new PortfolioSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by " + getClass().getSimpleName());
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    ManageablePortfolioNode node = _storeNodes.get(nodeId.getObjectId());
    if (node == null) {
      throw new DataNotFoundException("Node not found: " + nodeId);
    }
    return clonePortfolioNode(node);
  }

  @Override
  protected void validateDocument(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.portfolio");
  }
}
