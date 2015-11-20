/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.net.URI;

import com.opengamma.master.position.ManageablePosition;

/**
 * URIs for web-based positions.
 */
public class MinimalWebPositionsUris {

  /**
   * The data.
   */
  private final WebPositionsData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public MinimalWebPositionsUris(final WebPositionsData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return positions();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positions() {
    return MinimalWebPositionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI position() {
    return MinimalWebPositionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param position  the position, not null
   * @return the URI
   */
  public URI position(final ManageablePosition position) {
    return MinimalWebPositionResource.uri(_data, position.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positionVersions() {
    return MinimalWebPositionVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI positionVersion() {
    return MinimalWebPositionVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param position  the position, not null
   * @return the URI
   */
  public URI positionVersion(final ManageablePosition position) {
    return MinimalWebPositionVersionResource.uri(_data, position.getUniqueId());
  }

}
