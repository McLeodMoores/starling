/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sub-class of the standard {@link OpenGammaComponentServer} that works with the
 * Advanced Installer service wrappers when installed on Windows.
 */
public class OpenGammaComponentService extends OpenGammaComponentServer {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenGammaComponentService.class);
  /** Logger. */
  private static final Logger STARTUP_LOGGER = LoggerFactory.getLogger(ComponentManager.class);
  /**
   * Single instance.
   */
  private static final OpenGammaComponentService INSTANCE = new OpenGammaComponentService();

  /**
   * Latch used when stopping.
   */
  private final CountDownLatch _stopNotify = new CountDownLatch(1);
  /**
   * Latch used when stopping.
   */
  private final CountDownLatch _stopConfirm = new CountDownLatch(1);
  /**
   * The component repository.
   */
  private final AtomicReference<ComponentRepository> _repository = new AtomicReference<ComponentRepository>();

  //-------------------------------------------------------------------------
  /**
   * Starts the service, blocking until the stop signal is received.
   * 
   * @param args the command line arguments, the last element is the service name
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Starting service {}", args[args.length - 1]);
    final String[] runArgs = new String[args.length - 1];
    System.arraycopy(args, 0, runArgs, 0, runArgs.length);
    try {
      if (!INSTANCE.run(runArgs)) {
        LOGGER.error("One or more errors occurred starting the service");
        System.exit(1);
        //} else {
        //System.exit(0);
      }
    } catch (Throwable e) {
      LOGGER.error("Couldn't start service", e);
      System.exit(1);
    }
  }

  /**
   * Stops the service.
   */
  public static void stop() {
    LOGGER.info("Stopping service");
    INSTANCE.serverStopping();
    LOGGER.info("Service stopped");
    // This is bad. Not everything currently responds nicely to the "stop" and non-daemon threads
    // keep the process alive. Remove this hack when there are no more non-daemon threads that can
    // outlive their components and prevent process termination when running as a service.
    int aliveCount = 0, nonDaemon = 0;
    for (Map.Entry<Thread, StackTraceElement[]> active : Thread.getAllStackTraces().entrySet()) {
      final Thread t = active.getKey();
      if (t.isAlive()) {
        if (!t.isDaemon()) {
          LOGGER.debug("Thread {} still active", t);
          for (StackTraceElement stack : active.getValue()) {
            LOGGER.debug("Stack: {}", stack);
          }
          nonDaemon++;
        }
        aliveCount++;
      }
    }
    if (nonDaemon > 0) {
      LOGGER.error("{} non-daemon thread(s) (of {}) still active at shutdown, calling system.exit", nonDaemon, aliveCount);
      System.exit(1);
    } else {
      LOGGER.info("No non-daemon threads (of {}) active at shutdown", aliveCount);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean run(final String[] args) {
    if (!super.run(args)) {
      return false;
    }
    LOGGER.info("Service started -- waiting for stop signal");
    try {
      _stopNotify.await();
      LOGGER.info("Service stopped");
      _stopConfirm.countDown();
      return true;
    } catch (InterruptedException e) {
      LOGGER.warn("Service interrupted");
      return false;
    }
  }

  @Override
  protected ComponentLogger createLogger(int verbosity) {
    return new ComponentLogger.Slf4JLogger(STARTUP_LOGGER);
  }

  @Override
  protected void serverStarting(final ComponentManager manager) {
    LOGGER.debug("Server starting - got component repository");
    final ComponentRepository previous = _repository.getAndSet(manager.getRepository());
    assert (previous == null);
  }

  protected void serverStopping() {
    final ComponentRepository repository = _repository.getAndSet(null);
    if (repository != null) {
      LOGGER.info("Stopping components");
      try {
        repository.stop();
      } catch (Throwable e) {
        LOGGER.error("Couldn't stop components", e);
      }
      LOGGER.debug("Releasing main thread");
      _stopNotify.countDown();
      LOGGER.info("Waiting for confirmation signal");
      try {
        _stopConfirm.await();
      } catch (InterruptedException e) {
        LOGGER.warn("Service interrupted");
        System.exit(1);
      }
    } else {
      LOGGER.warn("Stop signal received before service startup completed");
      System.exit(1);
    }
  }

}
