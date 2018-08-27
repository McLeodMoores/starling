/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;
import static com.opengamma.engine.view.permission.PortfolioPermission.DENY;

import java.util.Map;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.livedata.UserPrincipal;

/**
 * Permissions checker for portfolio node based on user access rules.
 */
public class UserPermissionNodeChecker implements NodeChecker {

  /**
   * The map of restricted node names and who they are restricted to.
   */
  private final Map<String, String> _restrictedNodes;

  /**
   * The current system user.
   */
  private final UserPrincipal _user;

  public UserPermissionNodeChecker(final Map<String, String> restrictedNodes, final UserPrincipal user) {
    _restrictedNodes = restrictedNodes;
    _user = user;
  }

  @Override
  public PortfolioPermission check(final PortfolioNode node) {
    return portfolioIsRestricted(node) && userIsDeniedAccess(node) ?
        DENY : ALLOW;
  }

  private boolean userIsDeniedAccess(final PortfolioNode node) {
    return !_user.getUserName().equals(_restrictedNodes.get(node.getName()));
  }

  private boolean portfolioIsRestricted(final PortfolioNode node) {
    return _restrictedNodes.containsKey(node.getName());
  }
}
