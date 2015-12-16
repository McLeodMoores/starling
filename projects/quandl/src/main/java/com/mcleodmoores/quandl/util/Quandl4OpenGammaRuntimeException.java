package com.mcleodmoores.quandl.util;

/**
 * A runtime exception wrapper.
 */
public class Quandl4OpenGammaRuntimeException extends RuntimeException {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor when another exception is being included.
   * @param message a message describing the exception, not null
   * @param cause the cause of the exception if there is one, not null
   */
  public Quandl4OpenGammaRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor when exception is not caused by an underlying exception.
   * @param message a message describing the exception, not null
   */
  public Quandl4OpenGammaRuntimeException(final String message) {
    super(message);
  }
}
