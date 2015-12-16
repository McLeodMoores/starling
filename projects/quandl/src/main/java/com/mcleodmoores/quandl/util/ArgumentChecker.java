package com.mcleodmoores.quandl.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Utility class for checking arguments.
 */
public final class ArgumentChecker {
  /** The logger */
  private static Logger s_logger = LoggerFactory.getLogger(ArgumentChecker.class);

  /**
   * Restricted constructor.
   */
  private ArgumentChecker() {
  }

  /**
   * Throws an exception if the argument is not null.
   * @param <T>  type of object
   * @param argument  the object to check
   * @param name  the name of the parameter
   * @return  the original argument if it is not null
   */
  public static <T> T notNull(final T argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was null");
    }
    return argument;
  }

  /**
   * Throws an exception if the array argument is not null or empty.
   * @param <E>  type of array
   * @param argument  the object to check
   * @param name  the name of the parameter
   * @return  the original argument if it is not null or empty
   */
  public static <E> E[] notNullOrEmpty(final E[] argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was null");
    } else if (argument.length == 0) {
      s_logger.error("Argument {} was empty array", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was empty array");
    }
    return argument;
  }

  /**
   * Throws an exception if the collection argument is not null or empty.
   * @param <E>  type of array
   * @param argument  the object to check
   * @param name  the name of the parameter
   * @return  the original argument if it is not null or empty
   */
  public static <E> Collection<E> notNullOrEmpty(final Collection<E> argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was null");
    } else if (argument.size() == 0) {
      s_logger.error("Argument {} was empty collection", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was empty collection");
    }
    return argument;
  }

  /**
   * Throws an exception if the string argument is not null or empty.
   * @param argument  the String to check
   * @param name  the name of the parameter
   * @return  the original argument if it is not null
   */
  public static String notNullOrEmpty(final String argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was null");
    } else if (argument.length() == 0) {
      s_logger.error("Argument {} was empty string", name);
      throw new Quandl4OpenGammaRuntimeException("Value " + name + " was empty string");
    }
    return argument;
  }

  /**
   * Throws an exception if the boolean is false. Used to check inputs.
   * @param validIfTrue  true if the test is valid
   * @param message  an error message
   */
  public static void isTrue(final boolean validIfTrue, final String message) {
    if (!validIfTrue) {
      throw new Quandl4OpenGammaRuntimeException(message);
    }
  }

  /**
   * Throws an exception if the boolean is false. Used to check inputs.
   * @param validIfTrue  true if the test is valid.
   * @param message  a message with {} place-holders
   * @param arg  the message arguments
   */
  public static void isTrue(final boolean validIfTrue, final String message, final Object... arg) {
    if (!validIfTrue) {
      throw new Quandl4OpenGammaRuntimeException(MessageFormatter.arrayFormat(message, arg).getMessage());
    }
  }
}
