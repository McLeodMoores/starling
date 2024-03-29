/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS exception mapper to convert {@code Throwable} to a RESTful 500.
 */
@Provider
public class ThrowableExceptionMapper
    extends AbstractSpecificExceptionMapper<Throwable> {

  /**
   * Creates the mapper.
   */
  public ThrowableExceptionMapper() {
    super(Status.INTERNAL_SERVER_ERROR);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(final Throwable exception) {
    final Map<String, String> data = getMessage();
    buildOutputMessage(exception, data);
    return createHtmlErrorPage("error-servererror.html", data);
  }

  @Override
  protected void logHtmlException(final Throwable exception, final String htmlPage) {
    LOGGER.error("RESTful website exception caught", exception);
  }

  @Override
  protected void logRestfulError(final Throwable exception) {
    LOGGER.error("RESTful web-service exception caught and tunnelled to client:", exception);
  }

  /**
   * Gets the error message.
   *
   * @return  the message
   */
  Map<String, String> getMessage() {
    return new HashMap<>();
  }
}
