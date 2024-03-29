/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.Permission;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * A decorator for a security master that applies permissions.
 * <p>
 * Two kinds of permissions are applied by this class.
 * <p>
 * The first kind of permission is master-based. These are provided as static
 * constants on this class and cover the basic view, add, update and remove
 * operations.
 * <p>
 * The second kind of permission is entity-based. The
 * {@link com.opengamma.master.security.ManageableSecurity} class implements
 * {@link com.opengamma.util.auth.Permissionable}. This provides each security
 * with a set of permissions that a user needs to be able to view the data. This
 * master enforces those permissions.
 * <p>
 * For the {@code search} and {@code history} methods, each restricted document
 * is removed from the result. Since this happens after paging, it is possible
 * to see pages of data that are smaller than the requested page size.
 * <p>
 * For the bulk {@code get} method, each restricted document is removed from the
 * result.
 * <p>
 * For the {@code get} methods, a restricted document causes an exception to be
 * thrown.
 */
public class PermissionedSecurityMaster implements SecurityMaster {

  /**
   * The permission object for viewing data.
   */
  public static final Permission PERMISSION_VIEW = AuthUtils.getPermissionResolver().resolvePermission("SecurityMaster:view");
  /**
   * The permission object for adding data.
   */
  public static final Permission PERMISSION_ADD = AuthUtils.getPermissionResolver().resolvePermission("SecurityMaster:edit:add");
  /**
   * The permission object for updating data.
   */
  public static final Permission PERMISSION_UPDATE = AuthUtils.getPermissionResolver().resolvePermission("SecurityMaster:edit:update");
  /**
   * The permission object for removing data.
   */
  public static final Permission PERMISSION_REMOVE = AuthUtils.getPermissionResolver().resolvePermission("SecurityMaster:edit:remove");
  /**
   * The permission object for correcting data.
   */
  public static final Permission PERMISSION_CORRECT = AuthUtils.getPermissionResolver().resolvePermission("SecurityMaster:edit:correct");

  /**
   * The underlying security master.
   */
  private final SecurityMaster _underlying;

  //-------------------------------------------------------------------------
  /**
   * Wraps an underlying master if appropriate.
   * <p>
   * No wrapping occurs if permissions are not in use.
   *
   * @param underlying  the underlying master, not null
   * @return the master, not null
   */
  public static SecurityMaster wrap(final SecurityMaster underlying) {
    if (AuthUtils.isPermissive()) {
      return underlying;
    }
    return new PermissionedSecurityMaster(underlying);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   *
   * @param underlying  the underlying security master, not null
   */
  public PermissionedSecurityMaster(final SecurityMaster underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security master.
   *
   * @return the underlying master, not null
   */
  protected SecurityMaster getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    final SecurityDocument doc = getUnderlying().get(uniqueId);
    AuthUtils.checkPermissions(doc.getValue());
    return doc;
  }

  @Override
  public SecurityDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    final SecurityDocument doc = getUnderlying().get(objectId, versionCorrection);
    AuthUtils.checkPermissions(doc.getValue());
    return doc;
  }

  @Override
  public Map<UniqueId, SecurityDocument> get(final Collection<UniqueId> uniqueIds) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    final Map<UniqueId, SecurityDocument> result = new HashMap<>(getUnderlying().get(uniqueIds));
    for (final Iterator<SecurityDocument> it = result.values().iterator(); it.hasNext();) {
      final SecurityDocument doc = it.next();
      if (!AuthUtils.isPermitted(doc.getValue())) {
        it.remove();
      }
    }
    return result;
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    final SecuritySearchResult result = getUnderlying().search(request);
    int removed = 0;
    for (final Iterator<SecurityDocument> it = result.getDocuments().iterator(); it.hasNext();) {
      final SecurityDocument doc = it.next();
      if (!AuthUtils.isPermitted(doc.getValue())) {
        it.remove();
        removed++;
      }
    }
    result.setUnauthorizedCount(removed);
    return result;
  }

  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    final SecurityHistoryResult result = getUnderlying().history(request);
    int removed = 0;
    for (final Iterator<SecurityDocument> it = result.getDocuments().iterator(); it.hasNext();) {
      final SecurityDocument doc = it.next();
      if (!AuthUtils.isPermitted(doc.getValue())) {
        it.remove();
        removed++;
      }
    }
    result.setUnauthorizedCount(removed);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    AuthUtils.getSubject().checkPermission(PERMISSION_VIEW);
    return getUnderlying().metaData(request);
  }

  @Override
  public SecurityDocument add(final SecurityDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_ADD);
    return getUnderlying().add(document);
  }

  @Override
  public SecurityDocument update(final SecurityDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_UPDATE);
    return getUnderlying().update(document);
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    AuthUtils.getSubject().checkPermission(PERMISSION_REMOVE);
    getUnderlying().remove(oid);
  }

  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<SecurityDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<SecurityDocument> replacementDocuments) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final SecurityDocument replacementDocument) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    getUnderlying().removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final SecurityDocument documentToAdd) {
    AuthUtils.getSubject().checkPermission(PERMISSION_CORRECT);
    return getUnderlying().addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getUnderlying());
  }

}
