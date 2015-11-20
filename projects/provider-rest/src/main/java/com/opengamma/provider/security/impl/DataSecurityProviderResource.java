/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the security provider.
 * <p>
 * This resource receives and processes RESTful calls to the security provider.
 */
@Path("securityProvider")
public class DataSecurityProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final SecurityProvider _securityProvider;

  /**
   * Creates the resource, exposing the underlying provider over REST.
   * 
   * @param securityProvider  the underlying provider, not null
   */
  public DataSecurityProviderResource(final SecurityProvider securityProvider) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    _securityProvider = securityProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security provider.
   * 
   * @return the security provider, not null
   */
  public SecurityProvider getSecurityProvider() {
    return _securityProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("securityGet")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @POST  // should be a get, but query is too large
  @Path("securityGet")
  public Response getSecurity(SecurityProviderRequest request) {
    SecurityProviderResult result = getSecurityProvider().getSecurities(request);
    return responseOkObject(result);
  }

}
