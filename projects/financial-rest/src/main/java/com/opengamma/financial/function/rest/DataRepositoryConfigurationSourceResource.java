/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;

import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for repository configuration.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("/repoConfigSource")
public class DataRepositoryConfigurationSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final FunctionConfigurationSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param source the underlying source, not null
   */
  public DataRepositoryConfigurationSourceResource(final FunctionConfigurationSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   *
   * @return the source, not null
   */
  public FunctionConfigurationSource getRepositoryConfigurationSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("repoConfigs/all/{version}")
  public Response getAll(@PathParam("version") final String version) {
    final FunctionConfigurationBundle result = getRepositoryConfigurationSource().getFunctionConfiguration(Instant.parse(version));
    return responseOkObject(result);
  }

}
