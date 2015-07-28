/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.impl.ViewProcessInternal;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for attributes and operations on a view process.
 * @deprecated use ViewProcessMXBeanImpl
 */
@Deprecated
public class ViewProcessMBeanImpl implements ViewProcessMBean {

  /**
   * The backing view process instance
   */
  private final ViewProcessInternal _viewProcess;

  private final ObjectName _objectName;

  /**
   * Create a management View
   * 
   * @param viewProcess the underlying view process
   * @param viewProcessor the view processor responsible for the view process
   */
  public ViewProcessMBeanImpl(ViewProcessInternal viewProcess, com.opengamma.engine.view.ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _viewProcess = viewProcess;
    _objectName = createObjectName(viewProcessor.getName(), viewProcess.getUniqueId());
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewProcessId>"
   */
  static ObjectName createObjectName(String viewProcessorName, UniqueId viewProcessId) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=ViewProcess,ViewProcessor=ViewProcessor " + viewProcessorName + ",name=ViewProcess " + viewProcessId.getValue());
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }
  
  @Override
  public UniqueId getUniqueId() {
    return _viewProcess.getUniqueId();
  }
  
  @Override
  public String getPortfolioId() {
    return _viewProcess.getLatestViewDefinition().getPortfolioId().toString();
  }

  @Override
  public UniqueId getDefinitionId() {
    return _viewProcess.getDefinitionId();
  }
  
  @Override
  public boolean isPersistent() {
    return _viewProcess.getLatestViewDefinition().isPersistent();
  }

  @Override
  public ViewProcessState getState() {
    return _viewProcess.getState();
  }

  @Override
  public void shutdown() {
    _viewProcess.shutdown();
  }
  
  @Override
  public void suspend() {
    _viewProcess.suspend();
  }

  @Override
  public void resume() {
    _viewProcess.resume();
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
