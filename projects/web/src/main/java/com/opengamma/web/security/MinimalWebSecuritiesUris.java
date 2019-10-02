/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * URIs for web-based securities.
 */
public class MinimalWebSecuritiesUris {

  /**
   * The data.
   */
  private final WebSecuritiesData _data;

  /**
   * Creates an instance.
   * 
   * @param data
   *          the web data, not null
   */
  public MinimalWebSecuritiesUris(final WebSecuritiesData data) {
    _data = data;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public URI base() {
    return securities();
  }

  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public URI securities() {
    return MinimalWebSecuritiesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * 
   * @param identifier
   *          the identifier to search for, may be null
   * @return the URI
   */
  public URI securities(final ExternalId identifier) {
    return MinimalWebSecuritiesResource.uri(_data, ExternalIdBundle.of(identifier));
  }

  /**
   * Gets the URI.
   * 
   * @param identifiers
   *          the identifiers to search for, may be null
   * @return the URI
   */
  public URI securities(final ExternalIdBundle identifiers) {
    return MinimalWebSecuritiesResource.uri(_data, identifiers);
  }

  /**
   * Gets the URI, returning a security serach or single security.
   * 
   * @param link
   *          the link to search for, may be null
   * @return the URI
   */
  public URI securities(final SecurityLink link) {
    if (link.getObjectId() != null) {
      return MinimalWebSecurityResource.uri(_data, link.getObjectId().atLatestVersion());
    }
    return MinimalWebSecuritiesResource.uri(_data, link.getExternalId());
  }

  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public URI security() {
    return MinimalWebSecurityResource.uri(_data);
  }

  /**
   * Gets the URI.
   * 
   * @param security
   *          the security, not null
   * @return the URI
   */
  public URI security(final Security security) {
    return MinimalWebSecurityResource.uri(_data, security.getUniqueId());
  }

  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public URI securityVersions() {
    return MinimalWebSecurityVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * 
   * @return the URI
   */
  public URI securityVersion() {
    return MinimalWebSecurityVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * 
   * @param security
   *          the security, not null
   * @return the URI
   */
  public URI securityVersion(final Security security) {
    return MinimalWebSecurityVersionResource.uri(_data, security.getUniqueId());
  }

}
