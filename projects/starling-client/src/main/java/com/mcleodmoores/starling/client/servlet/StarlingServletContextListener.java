package com.mcleodmoores.starling.client.servlet;

import com.opengamma.util.fudgemsg.ServletContextHolder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Servlet context listener to allow annotation scanning on classpath.
 */
public class StarlingServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContextHolder.setContext(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}