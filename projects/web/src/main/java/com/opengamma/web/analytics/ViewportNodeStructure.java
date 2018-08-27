/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ final class ViewportNodeStructure {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewportNodeStructure.class);

  private final AnalyticsNode _rootNode;
  private final Map<Integer, List<String>> _rowToPath = Maps.newHashMap();

  /* package */ ViewportNodeStructure(final AnalyticsNode root, final TargetLookup targetLookup) {
    final Set<List<String>>  expandedNodes = new HashSet<>();
    _rootNode = createNode(root, targetLookup, expandedNodes);
  }

  /* package */ ViewportNodeStructure(final AnalyticsNode root, final TargetLookup targetLookup, final Set<List<String>> expandedNodes) {
    _rootNode = createNode(root, targetLookup, expandedNodes);
  }

  /* package */ AnalyticsNode createNode(final AnalyticsNode root, final TargetLookup targetLookup, final Set<List<String>> expandedNodes) {
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    // root can be null if a view only contains primitives and doesn't have a portfolio
    if (root == null) {
      return null;
    } else {
      return createNode(root, Collections.<String>emptyList(), targetLookup, expandedNodes);
    }
  }

  private AnalyticsNode createNode(final AnalyticsNode gridStructureNode,
                                   final List<String> parentPath,
                                   final TargetLookup targetLookup,
                                   final Set<List<String>> expandedNodes) {
    final List<String> path = Lists.newArrayList(parentPath);
    path.add(targetLookup.getRow(gridStructureNode.getStartRow()).getName());
    final boolean expanded = expandedNodes.contains(path);
    if (expanded) {
      LOGGER.debug("Building expanded node {}", path);
    }
    final List<AnalyticsNode> viewportStructureChildNodes = Lists.newArrayList();
    for (final AnalyticsNode gridStructureChildNode : gridStructureNode.getChildren()) {
      final AnalyticsNode viewportStructureChildNode = createNode(gridStructureChildNode, path, targetLookup, expandedNodes);
      viewportStructureChildNodes.add(viewportStructureChildNode);
    }
    _rowToPath.put(gridStructureNode.getStartRow(), Collections.unmodifiableList(path));

    return new AnalyticsNode(gridStructureNode.getStartRow(),
                             gridStructureNode.getEndRow(),
                             viewportStructureChildNodes,
                             !expanded);
  }

  /* package */ List<String> getPathForRow(final int rowIndex) {
    return _rowToPath.get(rowIndex);
  }

  /* package */ AnalyticsNode getRootNode() {
    return _rootNode;
  }
}
