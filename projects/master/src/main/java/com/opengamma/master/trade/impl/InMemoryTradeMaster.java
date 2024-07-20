/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeHistoryResult;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a Trade master.
 */
public class InMemoryTradeMaster extends SimpleAbstractInMemoryMaster<TradeDocument> implements TradeMaster {

  /**
   * The default scheme used for each {@link ObjectId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemPos";

  /**
   * A cache of trades by identifier.
   */
  private final ConcurrentMap<ObjectId, ManageableTrade> _storeTrades = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   */
  public InMemoryTradeMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryTradeMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryTradeMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryTradeMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(final TradeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getTrade(), "document.Trade");
  }

  @Override
  public TradeDocument get(final UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final TradeDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Trade not found: " + objectId);
    }
    return cloneTradeDocument(document);
  }

  private TradeDocument cloneTradeDocument(final TradeDocument document) {
    if (isCloneResults()) {
      final TradeDocument clone = JodaBeanUtils.clone(document);
      final ManageableTrade clonedTrade = new ManageableTrade(document.getTrade());
      if (!document.getTrade().getSecurityLink().getExternalId().isEmpty()) {
        clonedTrade.setSecurityLink(new ManageableSecurityLink(document.getTrade().getSecurityLink().getExternalId()));
      } else if (document.getTrade().getSecurityLink().getObjectId() != null) {
        clonedTrade.setSecurityLink(new ManageableSecurityLink(document.getTrade().getSecurityLink().getObjectId()));
      }
      clone.setTrade(clonedTrade);
      return clone;
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeDocument add(final TradeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getTrade(), "document.Trade");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();

    final TradeDocument clonedDoc = cloneTradeDocument(document);
    setDocumentID(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);
    storeTrades(clonedDoc.getTrade().getTrades(), document.getTrade().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private static void setDocumentID(final TradeDocument document, final TradeDocument clonedDoc, final UniqueId uniqueId) {
    document.getTrade().setUniqueId(uniqueId);
    clonedDoc.getTrade().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }

  private void storeTrades(final List<ManageableTrade> clonedTrades, final List<ManageableTrade> trades, final UniqueId parentTradeId) {
    for (int i = 0; i < clonedTrades.size(); i++) {
      final ObjectId objectId = _objectIdSupplier.get();
      final UniqueId uniqueId = objectId.atVersion("");
      final ManageableTrade origTrade = trades.get(i);
      final ManageableTrade clonedTrade = clonedTrades.get(i);
      final ManageableSecurityLink origLink = origTrade.getSecurityLink();
      if (origLink.getTarget() != null) { // unlink otherwise we're storing a copy in memory here.  This causes issues.
        clonedTrade.setSecurityLink(new ManageableSecurityLink(origLink.getTarget().getExternalIdBundle()));
      }
      clonedTrade.setUniqueId(uniqueId);
      origTrade.setUniqueId(uniqueId);
      clonedTrade.setParentTradeId(parentTradeId);
      origTrade.setParentTradeId(parentTradeId);
      _storeTrades.put(objectId, clonedTrade);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeDocument update(final TradeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getTrade(), "document.Trade");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final TradeDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }

    final TradeDocument clonedDoc = cloneTradeDocument(document);
    removeTrades(storedDocument.getTrade().getTrades());

    setVersionTimes(document, clonedDoc, now, null, now, null);

    if (!_store.replace(uniqueId.getObjectId(), storedDocument, clonedDoc)) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeTrades(clonedDoc.getTrade().getTrades(), document.getTrade().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), storedDocument.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private static void setVersionTimes(final TradeDocument document, final TradeDocument clonedDoc,
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

  private void removeTrades(final List<ManageableTrade> trades) {
    for (final ManageableTrade trade : trades) {
      if (_storeTrades.remove(trade.getUniqueId().getObjectId()) == null) {
        throw new DataNotFoundException("Trade not found: " + trade.getUniqueId());
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    final TradeDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Trade not found: " + objectIdentifiable);
    }
    removeTrades(storedDocument.getTrade().getTrades());
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeDocument correct(final TradeDocument document) {
    return update(document);
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by InMemoryTradeMaster");
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeSearchResult search(final TradeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<TradeDocument> list = new ArrayList<>();
    for (final TradeDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(cloneTradeDocument(doc));
      }
    }
    Collections.sort(list, new Comparator<TradeDocument>() {
      @Override
      public int compare(final TradeDocument obj1, final TradeDocument obj2) {
        return obj1.getObjectId().compareTo(obj2.getObjectId());
      }
    });
    final TradeSearchResult result = new TradeSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    final ManageableTrade trade = _storeTrades.get(tradeId.getObjectId());
    if (trade == null) {
      throw new DataNotFoundException("Trade not found: " + tradeId.getObjectId());
    }
    return JodaBeanUtils.clone(trade);
  }

}
