/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter.Format;

/**
 * {@link AnalyticsView} that decorates another view and logs the time taken to execute every method call. Intended
 * to help track down a performance regression.
 */
/* package */ class TimingAnalyticsView implements AnalyticsView {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimingAnalyticsView.class);

  private final AnalyticsView _delegate;

  /* package */ TimingAnalyticsView(final AnalyticsView delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public List<String> updateStructure(final CompiledViewDefinition compiledViewDefinition, final Portfolio portfolio) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.updateStructure");
    final List<String> retVal = _delegate.updateStructure(compiledViewDefinition, portfolio);
    LOGGER.trace("updateStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String viewCompilationFailed(final Throwable t) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.viewCompilationFailed");
    final String retVal = _delegate.viewCompilationFailed(t);
    LOGGER.trace("viewCompilationFailed completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> updateResults(final ViewResultModel results,
                                    final ViewCycle viewCycle) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.updateResults");
    final List<String> retVal = _delegate.updateResults(results, viewCycle);
    LOGGER.trace("updateResults completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getGridStructure");
    final GridStructure retVal = _delegate.getGridStructure(gridType, viewportId);
    LOGGER.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getGridStructure");
    final GridStructure retVal = _delegate.getInitialGridStructure(gridType);
    LOGGER.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(final int requestId,
                                final GridType gridType,
                                final int viewportId,
                                final String callbackId,
                                final String structureCallbackId,
                                final ViewportDefinition viewportDefinition) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.createViewport");
    final boolean retVal = _delegate.createViewport(requestId, gridType, viewportId, callbackId, structureCallbackId, viewportDefinition);
    LOGGER.trace("createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(final GridType gridType,
                               final int viewportId,
                               final ViewportDefinition viewportDefinition) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.updateViewport");
    final String retVal = _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    LOGGER.trace("updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(final GridType gridType, final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, viewportId);
    LOGGER.trace("deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(final GridType gridType, final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getData");
    final ViewportResults retVal = _delegate.getData(gridType, viewportId);
    LOGGER.trace("getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void openDependencyGraph(final int requestId,
                                  final GridType gridType,
                                  final int graphId, final String callbackId, final int row, final int col) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.openDependencyGraph");
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    LOGGER.trace("openDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void openDependencyGraph(final int requestId,
                                  final GridType gridType,
                                  final int graphId,
                                  final String callbackId,
                                  final String calcConfigName,
                                  final ValueRequirement valueRequirement) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.openDependencyGraph");
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, calcConfigName, valueRequirement);
    LOGGER.trace("openDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void closeDependencyGraph(final GridType gridType, final int graphId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.closeDependencyGraph");
    _delegate.closeDependencyGraph(gridType, graphId);
    LOGGER.trace("closeDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int graphId, final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getGridStructure");
    final GridStructure retVal = _delegate.getGridStructure(gridType, graphId, viewportId);
    LOGGER.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType, final int graphId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getGridStructure");
    final GridStructure retVal = _delegate.getInitialGridStructure(gridType, graphId);
    LOGGER.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(final int requestId,
                                final GridType gridType,
                                final int graphId,
                                final int viewportId,
                                final String callbackId,
                                final String structureCallbackId,
                                final ViewportDefinition viewportDefinition) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.createViewport");
    final boolean retVal = _delegate.createViewport(requestId,
                                              gridType,
                                              graphId,
                                              viewportId,
                                              callbackId,
                                              structureCallbackId,
                                              viewportDefinition);
    LOGGER.trace("createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(final GridType gridType,
                               final int graphId,
                               final int viewportId, final ViewportDefinition viewportDefinition) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.updateViewport");
    final String retVal = _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    LOGGER.trace("updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(final GridType gridType,
                             final int graphId,
                             final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, graphId, viewportId);
    LOGGER.trace("deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(final GridType gridType,
                                 final int graphId,
                                 final int viewportId) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getData");
    final ViewportResults retVal = _delegate.getData(gridType, graphId, viewportId);
    LOGGER.trace("getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> entityChanged(final MasterChangeNotification<?> notification) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.entityChanged");
    final List<String> retVal = _delegate.entityChanged(notification);
    LOGGER.trace("entityChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> portfolioChanged() {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.entityChanged");
    final List<String> retVal = _delegate.portfolioChanged();
    LOGGER.trace("portfolioChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public ViewportResults getAllGridData(final GridType gridType, final Format format) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getAllGridData");
    final ViewportResults retVal = _delegate.getAllGridData(gridType, format);
    LOGGER.trace("getAllGridData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public UniqueId getViewDefinitionId() {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getViewDefinitionId");
    final UniqueId retVal = _delegate.getViewDefinitionId();
    LOGGER.trace("getViewDefinitionId completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<ErrorInfo> getErrors() {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.getError");
    final List<ErrorInfo> retVal = _delegate.getErrors();
    LOGGER.trace("getError completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteError(final long id) {
    final long startTime = System.currentTimeMillis();
    LOGGER.trace("Executing AnalyticsView.deleteError");
    _delegate.deleteError(id);
    LOGGER.trace("deleteError completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }
}
