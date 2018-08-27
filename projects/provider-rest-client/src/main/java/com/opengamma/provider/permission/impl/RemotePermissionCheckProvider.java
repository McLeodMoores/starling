/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote permission check provider.
 * <p>
 * This is a client that connects to a permission check provider at a remote URI.
 */
public class RemotePermissionCheckProvider extends AbstractRemoteClient implements PermissionCheckProvider {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePermissionCheckProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isPermitted(final ExternalIdBundle userIdBundle, final String ipAddress, final String requestedPermission) {
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermission);
    final PermissionCheckProviderResult holderResult = isPermitted(request);
    return holderResult.isPermitted(requestedPermission);
  }

  @Override
  public Map<String, Boolean> isPermitted(final ExternalIdBundle userIdBundle, final String ipAddress, final Set<String> requestedPermissions) {
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermissions);
    final PermissionCheckProviderResult permissionResult = isPermitted(request);
    return permissionResult.getCheckedPermissions();
  }

  @Override
  public PermissionCheckProviderResult isPermitted(final PermissionCheckProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    final URI uri = DataPermissionCheckProviderUris.uriGet(getBaseUri());
    return accessRemote(uri).post(PermissionCheckProviderResult.class, request);
  }

}
