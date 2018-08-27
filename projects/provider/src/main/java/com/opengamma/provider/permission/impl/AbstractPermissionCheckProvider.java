/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;

/**
 * Abstract class to make implementation of PermissionCheckProvider easier.
 *
 * Subclass should provide implementation for isPermitted(PermissionCheckProviderRequest)
 */
public abstract class AbstractPermissionCheckProvider implements PermissionCheckProvider {

  @Override
  public boolean isPermitted(final ExternalIdBundle userIdBundle, final String ipAddress, final String requestedPermission) {
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermission);
    final PermissionCheckProviderResult holderResult = isPermitted(request);
    return holderResult.isPermitted(requestedPermission);
  }

  @Override
  public Map<String, Boolean> isPermitted(final ExternalIdBundle userIdBundle, final String ipAddress, final Set<String> requestedPermissions) {
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermissions);
    final PermissionCheckProviderResult holderResult = isPermitted(request);
    return holderResult.getCheckedPermissions();
  }

}
