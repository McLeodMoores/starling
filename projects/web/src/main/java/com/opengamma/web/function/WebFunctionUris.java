/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import java.net.URI;

/**
 * URIs for web-based functions.
 */
public class WebFunctionUris {

  /**
   * The data.
   */
  private final WebFunctionData _data;

  /**
   * Creates an instance.
   * 
   * @param data
   *          the web data, not null
   */
  public WebFunctionUris(final WebFunctionData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   *
   * @return the URI
   */
  public URI base() {
    return functions();
  }


  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI functions() {
    return WebFunctionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   *
   * @param functionId
   *          the function identifier
   * @return the URI
   */
  public URI parameterziedFunction(final String functionId) {
    return WebFunctionsResource.parameterziedFunctionUri(_data, functionId);
  }

}
