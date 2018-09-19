/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link ViewPermissionProvider} that always responds positively.
 */
public class PermissiveViewPermissionProvider implements ViewPermissionProvider {

  @Override
  public boolean canAccessCompiledViewDefinition(final UserPrincipal user, final CompiledViewDefinition compiledViewDefinition) {
    return true;
  }

  @Override
  public boolean canAccessComputationResults(final UserPrincipal user, final CompiledViewDefinition compiledViewDefinition,
      final boolean hasMarketDataPermissions) {
    return true;
  }

}
