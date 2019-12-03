/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.analytics.push.UpdateListener;

/**
 * View implementation that delegates operations to another view, catches any exceptions and notifies the client.
 */
/* package */ class CatchingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final ErrorManager _errorManager;
  private final UpdateListener _listener;

  /* package */ CatchingAnalyticsView(final AnalyticsView delegate, final ErrorManager errorManager, final UpdateListener listener) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(errorManager, "errorManager");
    ArgumentChecker.notNull(listener, "listener");
    _errorManager = errorManager;
    _listener = listener;
    _delegate = delegate;
  }

  @Override
  public List<String> updateStructure(final CompiledViewDefinition compiledViewDefinition, final Portfolio resolvedPortfolio) {
    try {
      return _delegate.updateStructure(compiledViewDefinition, resolvedPortfolio);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String viewCompilationFailed(final Throwable ex) {
    try {
      return _delegate.viewCompilationFailed(ex);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> updateResults(final ViewResultModel results, final ViewCycle viewCycle) {
    try {
      return _delegate.updateResults(results, viewCycle);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int viewportId) {
    try {
      return _delegate.getGridStructure(gridType, viewportId);
    } catch (final Exception e) {
      throw e;
    }
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType) {
    try {
      return _delegate.getInitialGridStructure(gridType);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public boolean createViewport(final int requestId,
                                final GridType gridType,
                                final int viewportId,
                                final String callbackId,
                                final String structureCallbackId,
                                final ViewportDefinition viewportDefinition) {
    try {
      return _delegate.createViewport(requestId,
                                      gridType,
                                      viewportId,
                                      callbackId,
                                      structureCallbackId,
                                      viewportDefinition);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String updateViewport(final GridType gridType, final int viewportId, final ViewportDefinition viewportDefinition) {
    try {
      return _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteViewport(final GridType gridType, final int viewportId) {
    try {
      _delegate.deleteViewport(gridType, viewportId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getData(final GridType gridType, final int viewportId) {
    try {
      return _delegate.getData(gridType, viewportId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getGridStructure(final GridType gridType, final int graphId, final int viewportId) {
    try {
      return _delegate.getGridStructure(gridType, graphId, viewportId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public GridStructure getInitialGridStructure(final GridType gridType, final int graphId) {
    try {
      return _delegate.getInitialGridStructure(gridType, graphId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void openDependencyGraph(final int requestId, final GridType gridType, final int graphId, final String callbackId, final int row, final int col) {
    try {
      _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void openDependencyGraph(final int requestId,
                                  final GridType gridType,
                                  final int graphId,
                                  final String callbackId,
                                  final String calcConfigName,
                                  final ValueRequirement valueRequirement) {
    try {
      _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, calcConfigName, valueRequirement);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void closeDependencyGraph(final GridType gridType, final int graphId) {
    try {
      _delegate.closeDependencyGraph(gridType, graphId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public boolean createViewport(final int requestId,
                                final GridType gridType,
                                final int graphId,
                                final int viewportId,
                                final String callbackId,
                                final String structureCallbackId,
                                final ViewportDefinition viewportDefinition) {
    try {
      return _delegate.createViewport(requestId,
                                      gridType,
                                      graphId,
                                      viewportId,
                                      callbackId,
                                      structureCallbackId,
                                      viewportDefinition);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public String updateViewport(final GridType gridType, final int graphId, final int viewportId, final ViewportDefinition viewportDefinition) {
    try {
      return _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteViewport(final GridType gridType, final int graphId, final int viewportId) {
    try {
      _delegate.deleteViewport(gridType, graphId, viewportId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getData(final GridType gridType, final int graphId, final int viewportId) {
    try {
      return _delegate.getData(gridType, graphId, viewportId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> entityChanged(final MasterChangeNotification<?> notification) {
    try {
      return _delegate.entityChanged(notification);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<String> portfolioChanged() {
    try {
      return _delegate.portfolioChanged();
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public ViewportResults getAllGridData(final GridType gridType, final TypeFormatter.Format format) {
    try {
      return _delegate.getAllGridData(gridType, format);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public UniqueId getViewDefinitionId() {
    try {
      return _delegate.getViewDefinitionId();
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public List<ErrorInfo> getErrors() {
    try {
      return _delegate.getErrors();
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }

  @Override
  public void deleteError(final long errorId) {
    try {
      _delegate.deleteError(errorId);
    } catch (final Exception e) {
      final String id = _errorManager.add(e);
      _listener.itemUpdated(id);
      throw e;
    }
  }
}
