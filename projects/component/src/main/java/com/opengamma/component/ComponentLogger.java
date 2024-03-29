/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import org.slf4j.Logger;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple logger for component startup.
 * <p>
 * The component system is a bootstrapping system and so has its own logger.
 */
public interface ComponentLogger {

  /**
   * Logs a verbose info message.
   *
   * @param message  the string message, not null
   */
  void logDebug(String message);

  /**
   * Logs a normal info message.
   *
   * @param message  the string message, not null
   */
  void logInfo(String message);

  /**
   * Logs a warning message.
   *
   * @param message  the string message, not null
   */
  void logWarn(String message);

  /**
   * Logs an error.
   *
   * @param message  the string message, not null
   */
  void logError(String message);

  /**
   * Logs an error.
   *
   * @param throwable  the exception, not null
   */
  void logError(Throwable throwable);

  //-------------------------------------------------------------------------
  /**
   * Logger that outputs no logging.
   */
  final class Sink implements ComponentLogger {
    /**
     * Singleton instance of a sink logger.
     */
    public static final ComponentLogger INSTANCE = new Sink();

    private Sink() {
    }

    @Override
    public void logDebug(final String message) {
      // do nothing
    }

    @Override
    public void logInfo(final String message) {
      // do nothing
    }

    @Override
    public void logWarn(final String message) {
      // do nothing
    }

    @Override
    public void logError(final String message) {
      // do nothing
    }

    @Override
    public void logError(final Throwable throwable) {
      // do nothing
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Logger that throws an exception for errors.
   */
  final class Throws implements ComponentLogger {
    /**
     * Singleton instance of a sink logger.
     */
    public static final ComponentLogger INSTANCE = new Throws();

    private Throws() {
    }

    @Override
    public void logDebug(final String message) {
      // do nothing
    }

    @Override
    public void logInfo(final String message) {
      // do nothing
    }

    @Override
    public void logWarn(final String message) {
      // do nothing
    }

    @Override
    public void logError(final String message) {
      throw new OpenGammaRuntimeException(message);
    }

    @Override
    public void logError(final Throwable throwable) {
      throw new OpenGammaRuntimeException(throwable.getMessage(), throwable);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Logger that outputs to the console.
   */
  final class Console implements ComponentLogger {
    /**
     * Singleton instance of a verbose logger.
     */
    public static final ComponentLogger VERBOSE = new Console(3);

    /**
     * The verbosity.
     */
    private final int _verbosity;

    /**
     * Creates an instance.
     * <p>
     * Level 0 is errors only.<br>
     * Level 1 is errors and warnings only.<br>
     * Level 2 is normal info and above.<br>
     * Level 3 is verbose info and above.<br>
     *
     * @param verbosity  the verbosity level, 0 to 3
     */
    public Console(final int verbosity) {
      _verbosity = verbosity;
    }

    @Override
    public void logDebug(final String message) {
      if (_verbosity >= 3) {
        System.out.println(message);
      }
    }

    @Override
    public void logInfo(final String message) {
      if (_verbosity >= 2) {
        System.out.println(message);
      }
    }

    @Override
    public void logWarn(final String message) {
      if (_verbosity >= 1) {
        System.out.println(message);
      }
    }

    @Override
    public void logError(final String message) {
      System.err.println(message);
    }

    @Override
    public void logError(final Throwable throwable) {
      throwable.printStackTrace(System.err);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Logger that outputs to a real logger.
   */
  class Slf4JLogger implements ComponentLogger {
    private final Logger _logger;

    /**
     * Creates an instance.
     *
     * @param logger  the logger, not null
     */
    public Slf4JLogger(final Logger logger) {
      ArgumentChecker.notNull(logger, "logger");
      _logger = logger;
    }

    @Override
    public void logDebug(final String message) {
      _logger.debug(message);
    }

    @Override
    public void logInfo(final String message) {
      _logger.info(message);
    }

    @Override
    public void logWarn(final String message) {
      _logger.warn(message);
    }

    @Override
    public void logError(final String message) {
      _logger.error(message);
    }

    @Override
    public void logError(final Throwable throwable) {
      _logger.error(throwable.getMessage(), throwable);
    }
  }

}
