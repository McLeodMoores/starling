/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * The grid viewport.
 */
public class PrimitivesGridViewport extends MainGridViewport {

  /** Row and column structure of the grid. */
  private final MainGridStructure _gridStructure;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes initially
   * @param structureCallbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   * @param cycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  PrimitivesGridViewport(final MainGridStructure gridStructure,
                         final String callbackId,
                         final String structureCallbackId,
                         final ViewportDefinition viewportDefinition,
                         final ViewCycle cycle,
                         final ResultsCache cache) {
    super(callbackId, structureCallbackId, viewportDefinition);
    _gridStructure = gridStructure;
    update(viewportDefinition, cycle, cache);
  }

  @Override
  public MainGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  public void update(final ViewportDefinition viewportDefinition, final ViewCycle viewCycle, final ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    setViewportDefinition(viewportDefinition);
    updateResults(cache);
  }

}
