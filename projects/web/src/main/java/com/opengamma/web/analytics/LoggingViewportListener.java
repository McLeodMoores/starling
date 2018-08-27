/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Listens for changes to viewports and updates the logging configuration in the {@link ViewClient}.
 */
/* package */ class LoggingViewportListener implements ViewportListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingViewportListener.class);

  private final ViewClient _viewClient;
  private final Map<Pair<String, ValueSpecification>, Integer> _refCounts = Maps.newHashMap();

  /* package */ LoggingViewportListener(final ViewClient viewClient) {
    ArgumentChecker.notNull(viewClient, "viewClient");
    _viewClient = viewClient;
  }

  @Override
  public void viewportCreated(final ViewportDefinition viewportDef, final GridStructure gridStructure) {
    if (viewportDef.enableLogging()) {
      enableLogging(viewportDef, gridStructure);
    }
  }

  private void enableLogging(final ViewportDefinition viewportDef, final GridStructure gridStructure) {
    final Set<Pair<String, ValueSpecification>> newVals = Sets.newHashSet();
    for (final GridCell cell : viewportDef) {
      final Pair<String, ValueSpecification> valueSpec = gridStructure.getValueSpecificationForCell(cell.getRow(),
                                                                                              cell.getColumn());
      if (valueSpec == null) {
        continue;
      }
      final Integer refCount = _refCounts.get(valueSpec);
      Integer newRefCount;
      if (refCount == null) {
        newRefCount = 1;
        newVals.add(valueSpec);
      } else {
        newRefCount = refCount + 1;
      }
      _refCounts.put(valueSpec, newRefCount);
    }
    LOGGER.debug("Setting log mode to FULL for {}", newVals);
    _viewClient.setMinimumLogMode(ExecutionLogMode.FULL, newVals);
  }

  @Override
  public void viewportUpdated(final ViewportDefinition currentDef, final ViewportDefinition newDef, final GridStructure gridStructure) {
    if (currentDef.enableLogging() && newDef.enableLogging()) {
      // logging enabled for both versions of the viewport
      final Set<GridCell> currentCells = Sets.newHashSet(currentDef.iterator());
      final Set<GridCell> newCells = Sets.newHashSet(newDef.iterator());
      final Set<GridCell> cellsRemoved = Sets.difference(currentCells, newCells);
      final Set<GridCell> cellsAdded = Sets.difference(newCells, currentCells);
      enableLogging(targetsFor(cellsAdded.iterator(), gridStructure));
      disableLogging(targetsFor(cellsRemoved.iterator(), gridStructure));
    } else if (!currentDef.enableLogging() && newDef.enableLogging()) {
      // no logging for current viewport, increase log level for all cells
      enableLogging(targetsFor(newDef.iterator(), gridStructure));
    } else if (currentDef.enableLogging() && !newDef.enableLogging()) {
      // reduce logging level for all cells in current viewport
      disableLogging(targetsFor(currentDef.iterator(), gridStructure));
    }
  }

  private Set<Pair<String, ValueSpecification>> targetsFor(final Iterator<GridCell> cellIterator, final GridStructure gridStructure) {
    final Set<Pair<String, ValueSpecification>> targets = Sets.newHashSet();
    while (cellIterator.hasNext()) {
      final GridCell cell = cellIterator.next();
      targets.add(gridStructure.getValueSpecificationForCell(cell.getRow(), cell.getColumn()));
    }
    return targets;
  }

  @Override
  public void viewportDeleted(final ViewportDefinition viewportDef, final GridStructure gridStructure) {
    if (viewportDef.enableLogging()) {
      disableLogging(targetsFor(viewportDef.iterator(), gridStructure));
    }
  }

  /**
   * Increments the ref count for the value specs and returns the set of specs whose ref count was previously zero.
   * @param targets The referenced specs
   */
  private void enableLogging(final Set<Pair<String, ValueSpecification>> targets) {
    final Set<Pair<String, ValueSpecification>> newlyReferenced = Sets.newHashSet();
    for (final Pair<String, ValueSpecification> target : targets) {
      final Integer refCount = _refCounts.get(target);
      Integer newRefCount;
      if (refCount == null) {
        newRefCount = 1;
        newlyReferenced.add(target);
      } else {
        newRefCount = refCount + 1;
      }
      _refCounts.put(target, newRefCount);
    }
    _viewClient.setMinimumLogMode(ExecutionLogMode.FULL, newlyReferenced);
  }

  /**
   * Decrements the ref count for the value specs and returns the set of specs whose ref count is now zero.
   * @param targets The referenced specs
   */
  private void disableLogging(final Set<Pair<String, ValueSpecification>> targets) {
    final Set<Pair<String, ValueSpecification>> dereferenced = Sets.newHashSet();
    for (final Pair<String, ValueSpecification> target : targets) {
      final Integer refCount = _refCounts.get(target);
      Integer newRefCount;
      if (refCount != null) {
        if (refCount.equals(1)) {
          _refCounts.remove(target);
          dereferenced.add(target);
        } else {
          newRefCount = refCount - 1;
          _refCounts.put(target, newRefCount);
        }
      }
    }
    LOGGER.debug("Setting log mode to INDICATORS for {}", dereferenced);
    _viewClient.setMinimumLogMode(ExecutionLogMode.INDICATORS, dereferenced);
  }
}
