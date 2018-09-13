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
 * A JAX-RS exception mapper to convert {@code UnsupportedOperationException} to a RESTful 503.
 */
@Provider
public class UnsupportedOperationExceptionMapper
    extends AbstractSpecificExceptionMapper<UnsupportedOperationException> {

  /**
   * Creates the mapper.
   */
  public UnsupportedOperationExceptionMapper() {
    super(Status.SERVICE_UNAVAILABLE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(final UnsupportedOperationException exception) {
    final Map<String, String> data = getMessage();
    buildOutputMessage(exception, data);
    return createHtmlErrorPage("error-unavailable.html", data);
  }

  @Override
  protected void logHtmlException(final UnsupportedOperationException exception, final String htmlPage) {
    LOGGER.error("RESTful website exception caught", exception);
  }

  @Override
  protected void logRestfulError(final UnsupportedOperationException exception) {
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
