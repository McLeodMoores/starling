/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * An extension to {@code UniformInterfaceException} to represent a 204.
 * <p>
 * This allows catch clauses to be simpler.
 */
@Provider
public class UniformInterfaceException204NoContent extends UniformInterfaceException {

  /** Serialization version. */
  private static final long serialVersionUID = -8266318713789190845L;

  /**
   * Creates an exception.
   *
   * @param response  the response
   * @param bufferResponseEntity  true to buffer the response entity
   */
  public UniformInterfaceException204NoContent(final ClientResponse response, final boolean bufferResponseEntity) {
    super(response, bufferResponseEntity);
  }

  /**
   * Creates an exception that buffers the response entity.
   *
   * @param response  the response
   */
  public UniformInterfaceException204NoContent(final ClientResponse response) {
    super(response);
  }

  /**
   * Creates an exception.
   *
   * @param message  the message
   * @param response  the response
   * @param bufferResponseEntity  true to buffer the response entity
   */
  public UniformInterfaceException204NoContent(final String message, final ClientResponse response, final boolean bufferResponseEntity) {
    super(message, response, bufferResponseEntity);
  }

  /**
   * Creates an exception that buffers the response entity.
   *
   * @param message  the message
   * @param response  the response
   */
  public UniformInterfaceException204NoContent(final String message, final ClientResponse response) {
    super(message, response);
  }

}
