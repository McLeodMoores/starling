/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link RoleMaster}.
 */
public class RemoteRoleMaster
extends AbstractRemoteMaster
implements RoleMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteRoleMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteRoleMaster(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(final String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    final URI uri = DataRoleMasterUris.uriNameExists(getBaseUri(), roleName);
    final ClientResponse response = accessRemote(uri).get(ClientResponse.class);
    if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
      return false;
    }
    if (response.getStatus() == Status.OK.getStatusCode()) {
      return true;
    }
    throw new IllegalStateException("Unexpected response from server: " + response.getStatus());
  }

  @Override
  public ManageableRole getByName(final String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    final URI uri = DataRoleMasterUris.uriRoleByName(getBaseUri(), roleName);
    return accessRemote(uri).get(ManageableRole.class);
  }

  @Override
  public ManageableRole getById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final URI uri = DataRoleMasterUris.uriRoleById(getBaseUri(), objectId);
    return accessRemote(uri).get(ManageableRole.class);
  }

  @Override
  public UniqueId add(final ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    final URI uri = DataRoleMasterUris.uriAdd(getBaseUri());
    return accessRemote(uri).post(UniqueId.class, role);
  }

  @Override
  public UniqueId update(final ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    ArgumentChecker.notNull(role.getUniqueId(), "role.uniqueId");
    final URI uri = DataRoleMasterUris.uriRoleById(getBaseUri(), role.getUniqueId());
    return accessRemote(uri).put(UniqueId.class, role);
  }

  @Override
  public UniqueId save(final ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    if (role.getUniqueId() != null) {
      return update(role);
    }
    return add(role);
  }

  @Override
  public void removeByName(final String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    final URI uri = DataRoleMasterUris.uriRoleByName(getBaseUri(), roleName);
    accessRemote(uri).delete();
  }

  @Override
  public void removeById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final URI uri = DataRoleMasterUris.uriRoleById(getBaseUri(), objectId);
    accessRemote(uri).delete();
  }

  @Override
  public RoleSearchResult search(final RoleSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final URI uri = DataRoleMasterUris.uriSearch(getBaseUri());
    return accessRemote(uri).post(RoleSearchResult.class, request);
  }

  @Override
  public RoleEventHistoryResult eventHistory(final RoleEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    final URI uri = DataRoleMasterUris.uriEventHistory(getBaseUri(), request);
    return accessRemote(uri).get(RoleEventHistoryResult.class);
  }

  @Override
  public UserAccount resolveAccount(final UserAccount account) {
    ArgumentChecker.notNull(account, "account");
    final URI uri = DataRoleMasterUris.uriResolveRole(getBaseUri(), account);
    return accessRemote(uri).post(UserAccount.class, account);
  }

}
