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
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides access to a remote {@link UserMaster}.
 */
public class RemoteUserMaster
extends AbstractRemoteMaster
implements UserMaster {

  /**
   * The role master.
   */
  private final RemoteRoleMaster _roleMaster;

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteUserMaster(final URI baseUri) {
    super(baseUri);
    _roleMaster = new RemoteRoleMaster(baseUri.resolve("users"));
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteUserMaster(final URI baseUri, final ChangeManager changeManager) {
    this(baseUri, changeManager, changeManager);
  }

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param userChangeManager  the change manager, not null
   * @param roleChangeManager  the change manager, not null
   */
  public RemoteUserMaster(final URI baseUri, final ChangeManager userChangeManager, final ChangeManager roleChangeManager) {
    super(baseUri, userChangeManager);
    _roleMaster = new RemoteRoleMaster(baseUri.resolve("users"), roleChangeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final URI uri = DataUserMasterUris.uriNameExists(getBaseUri(), userName);
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
  public ManageableUser getByName(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final URI uri = DataUserMasterUris.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(ManageableUser.class);
  }

  @Override
  public ManageableUser getById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final URI uri = DataUserMasterUris.uriUserById(getBaseUri(), objectId);
    return accessRemote(uri).get(ManageableUser.class);
  }

  @Override
  public UniqueId add(final ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    final URI uri = DataUserMasterUris.uriAdd(getBaseUri());
    return accessRemote(uri).post(UniqueId.class, user);
  }

  @Override
  public UniqueId update(final ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(user.getUniqueId(), "user.uniqueId");
    final URI uri = DataUserMasterUris.uriUserById(getBaseUri(), user.getUniqueId());
    return accessRemote(uri).put(UniqueId.class, user);
  }

  @Override
  public UniqueId save(final ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    if (user.getUniqueId() != null) {
      return update(user);
    }
    return add(user);
  }

  @Override
  public void removeByName(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final URI uri = DataUserMasterUris.uriUserByName(getBaseUri(), userName);
    accessRemote(uri).delete();
  }

  @Override
  public void removeById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final URI uri = DataUserMasterUris.uriUserById(getBaseUri(), objectId);
    accessRemote(uri).delete();
  }

  @Override
  public UserSearchResult search(final UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final URI uri = DataUserMasterUris.uriSearch(getBaseUri());
    return accessRemote(uri).post(UserSearchResult.class, request);
  }

  @Override
  public UserEventHistoryResult eventHistory(final UserEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    final URI uri = DataUserMasterUris.uriEventHistory(getBaseUri(), request);
    return accessRemote(uri).get(UserEventHistoryResult.class);
  }

  @Override
  public UserAccount getAccount(final String userName) {
    ArgumentChecker.notNull(userName, "userName");
    final URI uri = DataUserMasterUris.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(UserAccount.class);
  }

  @Override
  public RoleMaster roleMaster() {
    return _roleMaster;
  }

}
