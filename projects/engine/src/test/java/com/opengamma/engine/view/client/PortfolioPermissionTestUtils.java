/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static com.opengamma.engine.view.permission.PortfolioPermission.ALLOW;
import static com.opengamma.engine.view.permission.PortfolioPermission.DENY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.view.permission.PortfolioPermission;
import com.opengamma.id.UniqueId;

/* package */ class PortfolioPermissionTestUtils {

  /* package */ static NodeChecker createMappedNodeChecker(final Map<Integer, PortfolioPermission> permissions) {

    final Map<String, PortfolioPermission> converted = new HashMap<>();
    for (final Map.Entry<Integer, PortfolioPermission> entry : permissions.entrySet()) {
      converted.put("node-" + entry.getKey(), entry.getValue());
    }

    return new NodeChecker() {
      @Override
      public PortfolioPermission check(final PortfolioNode node) {
        return converted.get(node.getName());
      }
    };
  }

  /* package */ static SimplePortfolioNode nodeTree(final int id, final PortfolioNode... children) {

    final String nodeId = "node-" + id;
    final SimplePortfolioNode node = new SimplePortfolioNode(UniqueId.of("TEST", nodeId), nodeId);
    node.addChildNodes(Arrays.asList(children));
    return node;
  }

  /* package */ static NodeChecker createDenyNodeChecker() {
    return createConstantNodeChecker(DENY);
  }

  /* package */ static NodeChecker createAllowNodeChecker() {
    return createConstantNodeChecker(ALLOW);
  }

  private static NodeChecker createConstantNodeChecker(final PortfolioPermission permission) {
    return new NodeChecker() {
      @Override
      public PortfolioPermission check(final PortfolioNode node) {
        return permission;
      }
    };
  }
}
