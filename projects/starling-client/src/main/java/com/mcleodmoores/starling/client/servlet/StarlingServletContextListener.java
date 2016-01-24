/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.opengamma.util.fudgemsg.ServletContextHolder;

/**
 * Servlet context listener to allow annotation scanning on classpath.
 */
public class StarlingServletContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    ServletContextHolder.setContext(sce.getServletContext());
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {

  }
}
