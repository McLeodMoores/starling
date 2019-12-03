/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class PortfolioGridViewport extends MainGridViewport {

  /** The node structure. */
  private ViewportNodeStructure _nodeStructure;
  /** The current expanded paths. */
  private final Set<List<String>> _currentExpandedPaths;
  /** Row and column structure of the grid. */
  private MainGridStructure _gridStructure;
  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioGridViewport.class);

  /**
   * @param gridStructure
   *          Row and column structure of the grid
   * @param callbackId
   *          ID that's passed to listeners when the grid structure changes initially
   * @param structureCallbackId
   *          ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition
   *          The viewport definition
   * @param cycle
   *          The view cycle from the previous calculation cycle
   * @param cache
   *          The current results
   */
  PortfolioGridViewport(final MainGridStructure gridStructure,
      final String callbackId,
      final String structureCallbackId,
      final ViewportDefinition viewportDefinition,
      final ViewCycle cycle,
      final ResultsCache cache) {
    super(callbackId, structureCallbackId, viewportDefinition);
    _gridStructure = gridStructure;
    final Set<List<String>> expandedPaths = getExpandedPaths(gridStructure.getRootNode(),
        Collections.<String> emptyList(),
        gridStructure.getTargetLookup());
    _nodeStructure = new ViewportNodeStructure(getGridStructure().getRootNode(),
        getGridStructure().getTargetLookup(),
        expandedPaths);
    _currentExpandedPaths = expandedPaths;
    update(viewportDefinition, cycle, cache);
  }

  private static Set<List<String>> getExpandedPaths(final AnalyticsNode node, final List<String> parentPath, final TargetLookup targetLookup) {
    final Set<List<String>> paths = Sets.newHashSet();
    final List<String> path = Lists.newArrayList(parentPath);
    path.add(targetLookup.getRow(node.getStartRow()).getName());
    if (!node.isCollapsed()) {
      paths.add(path);
    }
    for (final AnalyticsNode childNode : node.getChildren()) {
      paths.addAll(getExpandedPaths(childNode, path, targetLookup));
    }
    return paths;
  }

  @Override
  public MainGridStructure getGridStructure() {
    return _gridStructure;
  }

  /**
   * Updates the structure of the tree nodes in the viewport. called when the first set of results arrives after a view def recompilation
   *
   * @param gridStructure
   *          The latest structure of the grid
   */
  public void updateResultsAndStructure(final PortfolioGridStructure gridStructure) {
    final ViewportNodeStructure node = new ViewportNodeStructure(gridStructure.getRootNode(),
        gridStructure.getTargetLookup(),
        _currentExpandedPaths);
    setViewportDefinition(ViewportDefinition.createEmpty(0));
    _gridStructure = gridStructure.withNode(node.getRootNode());
    _nodeStructure = new ViewportNodeStructure(getGridStructure().getRootNode(), getGridStructure().getTargetLookup());
  }

  /**
   * Updates the viewport definition (e.g. in response to the user scrolling the grid and changing the visible area).
   *
   * @param viewportDefinition
   *          The new viewport definition
   * @param viewCycle
   *          The view cycle from the previous calculation cycle
   * @param cache
   *          The current results
   */
  @Override
  public void update(final ViewportDefinition viewportDefinition, final ViewCycle viewCycle, final ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(getGridStructure())) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: "
          + viewportDefinition + ", grid: " + getGridStructure());
    }
    if (getDefinition() != null) {
      final Pair<Integer, Boolean> changedNode = getDefinition().getChangedNode(viewportDefinition);
      // if this is null then the user scrolled the viewport and didn't expand or collapse a node
      if (changedNode != null) {
        final Integer rowIndex = changedNode.getFirst();
        // was it expanded or collapsed
        final Boolean expanded = changedNode.getSecond();
        final List<String> path = _nodeStructure.getPathForRow(rowIndex);
        LOGGER.debug("Node at row {} {}", rowIndex.toString(), expanded ? "expanded" : "collapsed");
        // System.out.println("Row: " + rowIndex.toString() + " Expanded: " + expanded.toString() + " Path: " + path);
        if (expanded) {
          _currentExpandedPaths.add(path);
          LOGGER.debug("Expanding {}", path);
        } else {
          _currentExpandedPaths.remove(path);
          LOGGER.debug("Collapsing {}", path);
        }
        LOGGER.debug("Current expanded set of nodes {}", _currentExpandedPaths);
      }
    }
    setViewportDefinition(viewportDefinition);
    updateResults(cache);
  }

}
