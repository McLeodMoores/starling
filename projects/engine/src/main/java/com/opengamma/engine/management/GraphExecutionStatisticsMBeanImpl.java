/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.CacheException;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a {@link com.opengamma.engine.exec.stats.GraphExecutionStatistics}.
 */
public class GraphExecutionStatisticsMBeanImpl implements GraphExecutionStatisticsMBean {

  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;

  private final UniqueId _viewProcessId;

  private final UniqueId _viewDefinitionId;

  private final String _calcConfigName;

  private final ObjectName _objectName;

  /**
   * Create a management GraphExecutionStatistics.
   *
   * @param viewProcess  the view process
   * @param statisticsProvider  the statistics provider
   * @param viewProcessorName  the view processor name
   * @param calcConfigName  the calculation configuration name
   */
  public GraphExecutionStatisticsMBeanImpl(final ViewProcess viewProcess, final TotallingGraphStatisticsGathererProvider statisticsProvider,
      final String viewProcessorName, final String calcConfigName) {
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    ArgumentChecker.notNull(viewProcessorName, "viewProcessorName");
    ArgumentChecker.notNull(viewProcess, "View Process");
    ArgumentChecker.notNull(calcConfigName, "calcConfig Name");
    _viewProcessId = viewProcess.getUniqueId();
    _viewDefinitionId = viewProcess.getDefinitionId();
    _objectName = createObjectName(viewProcessorName, _viewProcessId, calcConfigName);
    _calcConfigName = calcConfigName;
    _statisticsProvider = statisticsProvider;
  }

  /**
   * Creates an object name using the scheme
   * "com.opengamma:type=GraphExecutionStatistics,ViewProcessor=<viewProcessorName>,View=<viewName>,name=<calcConfigName>".
   */
  static ObjectName createObjectName(final String viewProcessorName, final UniqueId viewProcessId, final String calcConfigName) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=GraphExecutionStatistics,ViewProcessor=ViewProcessor " + viewProcessorName
          + ",ViewProcess=ViewProcess " + viewProcessId.getValue() + ",name=" + calcConfigName);
    } catch (final MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }

  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessId;
  }

  @Override
  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  @Override
  public String getCalcConfigName() {
    return _calcConfigName;
  }

  @Override
  public Long getProcessedGraphs() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedGraphs() : 0;
  }

  private com.opengamma.engine.exec.stats.GraphExecutionStatistics getGraphExecutionStatistics() {
    final Statistics statisticsGatherer = _statisticsProvider.getStatisticsGatherer(_viewProcessId);
    final List<com.opengamma.engine.exec.stats.GraphExecutionStatistics> executionStatistics = statisticsGatherer.getExecutionStatistics();
    for (final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics : executionStatistics) {
      if (graphExecutionStatistics.getCalcConfigName().equals(_calcConfigName) && graphExecutionStatistics.getViewProcessId().equals(_viewProcessId)) {
        return graphExecutionStatistics;
      }
    }
    return null;
  }

  @Override
  public Long getExecutedGraphs() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedGraphs() : null;
  }

  @Override
  public Long getExecutedNodes() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedNodes() : null;
  }

  @Override
  public Long getExecutionTime() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutionTime() : null;
  }

  @Override
  public Long getActualTime() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getActualTime() : 0;
  }

  @Override
  public Long getProcessedJobs() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobs() : null;
  }

  @Override
  public Long getProcessedJobSize() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobSize() : null;
  }

  @Override
  public Long getProcessedJobCycleCost() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobCycleCost() : null;
  }

  @Override
  public Long getProcessedJobDataCost() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobDataCost() : null;
  }

  @Override
  public String getLastProcessedTime() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastProcessedTime().toString() : null;
  }

  @Override
  public String getLastExecutedTime() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastExecutedTime().toString() : null;
  }

  @Override
  public void reset() {
    final com.opengamma.engine.exec.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    if (graphExecutionStatistics != null) {
      graphExecutionStatistics.reset();
    }

  }

  /**
   * Gets the objectName field.
   *
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }

}
