/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.LogUtils;

/**
 * Utilities for setting up the infrastructure around tools.
 */
public final class ToolUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ToolUtils.class);
  
  /**
   * Default logback file.
   */
  private static final String TOOL_LOGBACK_XML = "tool-logback.xml";
  
  /**
   * Hidden constructor.
   */
  private ToolUtils() {
  }
  
  //-------------------------------------------------------------------------
  public static boolean initLogback(String logbackResource) {
    s_logger.trace("Configuring logging from {}", logbackResource);
    // Don't reconfigure if already configured from the default property or any existing loggers will break
    // and stop reporting anything.
    return logbackResource.equals(getSystemDefaultLogbackConfiguration()) ? true : LogUtils.configureLogger(logbackResource);
  }
  
  /**
   * Returns the name of the default logback configuration file if none is explicitly specified. This will be {@link #TOOL_LOGBACK_XML} unless the global {@code logback.configurationFile property} has
   * been set.
   * 
   * @return the logback configuration file resource address, not null
   */
  public static String getDefaultLogbackConfiguration() {
    final String globalConfiguration = getSystemDefaultLogbackConfiguration();
    if (globalConfiguration != null) {
      return globalConfiguration;
    } else {
      return TOOL_LOGBACK_XML;
    }
  }
  
  //-------------------------------------------------------------------------
  private static String getSystemDefaultLogbackConfiguration() {
    return System.getProperty("logback.configurationFile");
  }
  
}
