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
 * A JAX-RS exception mapper to convert {@code IllegalArgumentException} to a RESTful 400.
 */
@Provider
public class IllegalArgumentExceptionMapper
    extends AbstractSpecificExceptionMapper<IllegalArgumentException> {

  /**
   * Creates the mapper.
   */
  public IllegalArgumentExceptionMapper() {
    super(Status.BAD_REQUEST);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String buildHtmlErrorPage(final IllegalArgumentException exception) {
    final Map<String, String> data = getMessage();
    buildOutputMessage(exception, data);
    return createHtmlErrorPage("error-badrequest.html", data);
  }

  @Override
  protected void logHtmlException(final IllegalArgumentException exception, final String htmlPage) {
    LOGGER.error("RESTful website exception caught", exception);
  }

  @Override
  protected void logRestfulError(final IllegalArgumentException exception) {
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
