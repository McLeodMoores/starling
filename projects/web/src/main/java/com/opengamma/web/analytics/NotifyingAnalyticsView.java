/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter.Format;
import com.opengamma.web.analytics.push.UpdateListener;

/**
 * {@link AnalyticsView} that decorates another view and notifies a listener when any items are updated in the underlying view. This class allows the underlying
 * view to be protected by a lock without having to make an external call to the listener whilst holding the lock.
 * 
 * @see SimpleAnalyticsView
 */
/* package */ class NotifyingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final UpdateListener _listener;

  /* package */ NotifyingAnalyticsView(final AnalyticsView delegate, final UpdateListener listener) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(listener, "listener");
    _delegate = delegate;
    _listener = listener;
  }

  @Override
  public List<String> updateStructure(final CompiledViewDefinition compiledViewDefinition, final Portfolio portfolio) {
    final List<String> callbackIds = _delegate.updateStructure(compiledViewDefinition, portfolio);
    _listener.itemsUpdated(callbackIds);
    return callbackIds;
  }

  @Override
  public String viewCompilationFailed(final Throwable t) {
    final String callbackId = _delegate.viewCompilationFailed(t);
    _listener.itemUpdated(callbackId);
    return callbackId;
  }

  @Override
  public List<String> updateResults(final ViewResultModel results, final ViewCycle viewCycle) {
    final List<String> callbackIds = _delegate.updateResults(results, viewCycle);
    _listener.itemsUpdated(callbackIds);
    return callbackIds;
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int viewportId) {
    return _delegate.getGridStructure(gridType, viewportId);
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType) {
    return _delegate.getInitialGridStructure(gridType);
  }

  @Override
  public boolean createViewport(final int requestId, final GridType gridType, final int viewportId, final String callbackId, final String structureCallbackId,
      final ViewportDefinition viewportDefinition) {
    final boolean hasData = _delegate.createViewport(requestId, gridType, viewportId, callbackId, structureCallbackId, viewportDefinition);
    final ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object> of("id", requestId, "message", callbackId);
    // TODO is this logic correct? just because the viewport doesn't contain data updated in the previous cycle it
    // doesn't mean it doesn't have any data.
    if (hasData) {
      _listener.itemsUpdated(ImmutableList.of(callbackMap, callbackId));
    } else {
      _listener.itemUpdated(callbackMap);
    }
    return hasData;
  }

  @Override
  public String updateViewport(final GridType gridType, final int viewportId, final ViewportDefinition viewportDefinition) {
    final String callbackId = _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    if (callbackId != null) {
      // TODO is this logic correct? just because the viewport doesn't contain data updated in the previous cycle it
      // doesn't mean it doesn't have any data.
      _listener.itemUpdated(callbackId);
    }
    return callbackId;
  }

  @Override
  public void deleteViewport(final GridType gridType, final int viewportId) {
    _delegate.deleteViewport(gridType, viewportId);
  }

  @Override
  public ViewportResults getData(final GridType gridType, final int viewportId) {
    return _delegate.getData(gridType, viewportId);
  }

  @Override
  public void openDependencyGraph(final int requestId, final GridType gridType, final int graphId, final String callbackId, final int row, final int col) {
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    final ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object> of("id", requestId, "message", callbackId);
    _listener.itemUpdated(callbackMap);
  }

  @Override
  public void openDependencyGraph(final int requestId,
      final GridType gridType,
      final int graphId,
      final String callbackId,
      final String calcConfigName,
      final ValueRequirement valueRequirement) {
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, calcConfigName, valueRequirement);
    final ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object> of("id", requestId, "message", callbackId);
    _listener.itemUpdated(callbackMap);
  }

  @Override
  public void closeDependencyGraph(final GridType gridType, final int graphId) {
    _delegate.closeDependencyGraph(gridType, graphId);
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int graphId, final int viewportId) {
    return _delegate.getGridStructure(gridType, graphId, viewportId);
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType, final int graphId) {
    return _delegate.getInitialGridStructure(gridType, graphId);
  }

  @Override
  public boolean createViewport(final int requestId, final GridType gridType, final int graphId, final int viewportId, final String callbackId,
      final String structureCallbackId, final ViewportDefinition viewportDefinition) {
    final boolean hasData = _delegate.createViewport(requestId, gridType, graphId, viewportId, callbackId, structureCallbackId, viewportDefinition);
    final ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object> of("id", requestId, "message", callbackId);
    if (hasData) {
      _listener.itemsUpdated(ImmutableList.of(callbackMap, callbackId));
    } else {
      _listener.itemUpdated(callbackMap);
    }
    return hasData;
  }

  @Override
  public String updateViewport(final GridType gridType, final int graphId, final int viewportId, final ViewportDefinition viewportDefinition) {
    final String callbackId = _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    if (callbackId != null) {
      _listener.itemUpdated(callbackId);
    }
    return callbackId;
  }

  @Override
  public void deleteViewport(final GridType gridType, final int graphId, final int viewportId) {
    _delegate.deleteViewport(gridType, graphId, viewportId);
  }

  @Override
  public ViewportResults getData(final GridType gridType, final int graphId, final int viewportId) {
    return _delegate.getData(gridType, graphId, viewportId);
  }

  @Override
  public List<String> entityChanged(final MasterChangeNotification<?> notification) {
    final List<String> ids = _delegate.entityChanged(notification);
    _listener.itemsUpdated(ids);
    return ids;
  }

  @Override
  public List<String> portfolioChanged() {
    final List<String> ids = _delegate.portfolioChanged();
    _listener.itemsUpdated(ids);
    return ids;
  }

  @Override
  public ViewportResults getAllGridData(final GridType gridType, final Format format) {
    return _delegate.getAllGridData(gridType, format);
  }

  @Override
  public UniqueId getViewDefinitionId() {
    return _delegate.getViewDefinitionId();
  }

  @Override
  public List<ErrorInfo> getErrors() {
    return _delegate.getErrors();
  }

  @Override
  public void deleteError(final long id) {
    _delegate.deleteError(id);
  }
}
