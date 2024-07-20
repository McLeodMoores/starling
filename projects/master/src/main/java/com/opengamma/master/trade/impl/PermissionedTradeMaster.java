/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.Permission;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeHistoryRequest;
import com.opengamma.master.trade.TradeHistoryResult;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * A decorator for a Trade master that applies permissions.
 * <p>
 * The class applies master-based permissions.
 * These are provided as static constants on this class and cover
 * the basic view, add, update and remove operations.
 */
public class PermissionedTradeMaster implements TradeMaster {

  /**
   * The permission object for viewing data.
   */
  public static final Permission PERMISSION_VIEW = AuthUtils.getPermissionResolver().resolvePermission("TradeMaster:view");
  /**
   * The permission object for adding data.
   */
  public static final Permission PERMISSION_ADD = AuthUtils.getPermissionResolver().resolvePermission("TradeMaster:edit:add");
  /**
   * The permission object for updating data.
   */
  public static final Permission PERMISSION_UPDATE = AuthUtils.getPermissionResolver().resolvePermission("TradeMaster:edit:update");
  /**
   * The permission object for removing data.
   */
  public static final Permission PERMISSION_REMOVE = AuthUtils.getPermissionResolver().resolvePermission("TradeMaster:edit:remove");
  /**
   * The permission object for correcting data.
   */
  public static final Permission PERMISSION_CORRECT = AuthUtils.getPermissionResolver().resolvePermission("TradeMaster:edit:correct");

  /**
   * The underlying Trade master.
   */
  private final TradeMaster _underlying;

  //-------------------------------------------------------------------------
  /**
   * Wraps an underlying master if appropriate.
   * <p>
   * No wrapping occurs if permissions are not in use.
   *
   * @param underlying  the underlying master, not null
   * @return the master, not null
   */
  public static TradeMaster wrap(final TradeMaster underlying) {
    if (AuthUtils.isPermissive()) {
      return underlying;
    }
    return new PermissionedTradeMaster(underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   *
   * @param underlying  the underlying Trade master, not null
   */
  public PermissionedTradeMaster(final TradeMaster underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying Trade master.
   *
   * @return the underlying master, not null
   */
  protected TradeMaster getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public TradeDocument get(final UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueId);
  }

  @Override
  public TradeDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().getTrade(tradeId);
  }

  @Override
  public Map<UniqueId, TradeDocument> get(final Collection<UniqueId> uniqueIds) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public TradeSearchResult search(final TradeSearchRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().search(request);
  }

  @Override
  public TradeHistoryResult history(final TradeHistoryRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public TradeDocument add(final TradeDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_ADD);
    return getUnderlying().add(document);
  }

  @Override
  public TradeDocument update(final TradeDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_UPDATE);
    return getUnderlying().update(document);
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    AuthUtils.getSubject().checkPermission(PERMISSION_REMOVE);
    getUnderlying().remove(oid);
  }

  @Override
  public TradeDocument correct(final TradeDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<TradeDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<TradeDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<TradeDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final TradeDocument replacementDocument) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    getUnderlying().removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final TradeDocument documentToAdd) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getUnderlying());
  }

}
