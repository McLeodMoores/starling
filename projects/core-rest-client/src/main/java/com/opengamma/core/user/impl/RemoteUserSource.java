/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.net.URI;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link UserSource}.
 */
public class RemoteUserSource extends AbstractRemoteClient implements UserSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteUserSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  /**
   * Creates an instance.
   *
   * @param baseUri
   *          the base target URI for all RESTful web services, not null
   * @param changeManager
   *          the change manager to use, not null
   */
  public RemoteUserSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount getAccount(final String userName) {
    ArgumentChecker.notNull(userName, "userName");

    final URI uri = DataUserSourceUris.uriUserByName(getBaseUri(), userName);
    return accessRemote(uri).get(UserAccount.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
