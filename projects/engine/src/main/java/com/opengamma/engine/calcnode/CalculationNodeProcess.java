/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.xml.sax.InputSource;

/**
 * Starts a calculation node and joins a job dispatcher.
 */
public final class CalculationNodeProcess {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalculationNodeProcess.class);

  private static final int CONFIGURATION_RETRY = 3;
  private static final int CONFIGURATION_POLL_PERIOD = 5;

  private static HttpClient s_httpClient;

  /**
   * A job item execution watchdog that can terminate the host process if one (or all) calculation threads hang.
   */
  public static class JobItemExecutionWatchdog extends MaximumJobItemExecutionWatchdog {

    public JobItemExecutionWatchdog() {
      setTimeoutAction(new Action() {
        @Override
        public void jobItemExecutionLimitExceeded(final CalculationJobItem jobItem, final Thread thread) {
          LOGGER.error("Starting graceful shutdown after thread {} hung on {}", thread, jobItem);
          startGracefulShutdown();
          if (!areThreadsAlive()) {
            LOGGER.error("Halting remote calc process", thread, jobItem);
            System.exit(0);
          }
        }
      });
    }

  }

  private CalculationNodeProcess() {
  }

  private static void sleep(final int period) {
    try {
      Thread.sleep(1000 * period);
    } catch (final InterruptedException e) {
    }
  }

  private static String getConfigurationXml(final String url) {
    if (s_httpClient == null) {
      s_httpClient = new DefaultHttpClient();
    }
    LOGGER.debug("Fetching {}", url);
    final HttpResponse resp;
    try {
      resp = s_httpClient.execute(new HttpGet(url));
    } catch (final Exception e) {
      LOGGER.warn("Error fetching {} - {}", url, e.getMessage());
      return null;
    }
    LOGGER.debug("HTTP result {}", resp.getStatusLine());
    if (resp.getStatusLine().getStatusCode() != 200) {
      LOGGER.warn("No configuration available (HTTP {})", resp.getStatusLine().getStatusCode());
      return null;
    }
    try {
      final Reader in = new InputStreamReader(resp.getEntity().getContent());
      final StringBuilder sb = new StringBuilder();
      final char[] buf = new char[1024];
      int i = in.read(buf);
      while (i > 0) {
        sb.append(buf, 0, i);
        i = in.read(buf);
      }
      LOGGER.debug("Configuration document received - {} characters", sb.length());
      return sb.toString();
    } catch (final IOException e) {
      LOGGER.warn("Error retrieving response from {} - {}", url, e.getMessage());
      return null;
    }
  }

  private static boolean startContext(final String configuration) {
    try {
      final GenericApplicationContext context = new GenericApplicationContext();
      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(context);
      beanReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
      LOGGER.debug("Loading configuration");
      beanReader.loadBeanDefinitions(new InputSource(new StringReader(configuration)));
      LOGGER.debug("Instantiating beans");
      context.refresh();
      LOGGER.debug("Starting node");
      context.start();
      return true;
    } catch (final RuntimeException e) {
      LOGGER.warn("Spring initialisation error", e);
      return false;
    }
  }

  private static String getBaseUrl(final String url) {
    final int slash = url.lastIndexOf('/');
    return url.substring(0, slash + 1);
  }

  private static void setConnectionDefaults(final String url) {
    try {
      final URI uri = new URI(url);
      if (uri.getHost() != null) {
        System.setProperty("opengamma.engine.calcnode.host", uri.getHost());
      }
      if (uri.getPort() != -1) {
        System.setProperty("opengamma.engine.calcnode.port", Integer.toString(uri.getPort()));
      }
    } catch (final URISyntaxException e) {
      LOGGER.warn("Couldn't set connection defaults", e);
    }
  }

  private static void startGracefulShutdown() {
    LOGGER.error("TODO: [PLAT-2351] start graceful shutdown");
    // TODO: [PLAT-2351] stop accepting jobs and allow current ones to run to completion
  }

  /**
   * Starts a calculation node, retrieving configuration from the given URL
   *
   * @param url The URL to use
   */
  public static void main(final String url) {
    LOGGER.info("Using configuration URL {}", url);
    String configuration = getConfigurationXml(url);
    if (configuration == null) {
      for (int i = 0; i < CONFIGURATION_RETRY; i++) {
        LOGGER.warn("Failed to retrieve configuration - retrying");
        sleep(1);
        configuration = getConfigurationXml(url);
        if (configuration != null) {
          break;
        }
      }
      if (configuration == null) {
        LOGGER.error("No response from {}", url);
        System.exit(1);
      }
    }
    // Create and start the spring config
    System.setProperty("opengamma.engine.calcnode.baseurl", getBaseUrl(url));
    setConnectionDefaults(url);
    if (startContext(configuration)) {
      LOGGER.info("Calculation node started");
    } else {
      LOGGER.error("Couldn't start calculation node");
      System.exit(1);
    }
    // Terminate if the configuration changes - the O/S will restart us
    int retry = 0;
    do {
      sleep(CONFIGURATION_POLL_PERIOD);
      final String newConfiguration = getConfigurationXml(url);
      if (newConfiguration != null) {
        if (!configuration.equals(newConfiguration)) {
          LOGGER.info("Configuration at {} has changed", url);
          System.exit(0);
        }
        retry = 0;
      } else {
        switch (++retry) {
          case 1:
            LOGGER.debug("No response from configuration at {}", url);
            break;
          case 2:
            LOGGER.info("No response from configuration at {}", url);
            break;
          case 3:
            LOGGER.warn("No response from configuration at {}", url);
            break;
          case 4:
            LOGGER.error("No response from configuration at {}", url);
            startGracefulShutdown();
            // TODO: wait for the graceful shutdown to complete (i.e. node goes idle)
            System.exit(0);
            break;
        }
      }
      LOGGER.info("Free memory = {}Mb, total memory = {}Mb", Runtime.getRuntime().freeMemory() / (1024d * 1024d), Runtime.getRuntime().totalMemory() / (1024d * 1024d));
    } while (true);
  }

  /**
   * Starts a calculation node
   *
   * @param args the arguments, should contain one parameter - the configuration URL to use
   */
  public static void main(final String[] args) { // CSIGNORE
    if (args.length != 1) {
      LOGGER.error("Configuration URL not specified");
      System.exit(1);
    }
    main(args[0]);
  }

}
