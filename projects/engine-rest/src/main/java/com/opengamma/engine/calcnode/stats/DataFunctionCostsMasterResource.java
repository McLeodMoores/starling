/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for function costs.
 * <p>
 * This resource receives and processes RESTful calls to the function costs master.
 */
@Path("/fncMaster")
public class DataFunctionCostsMasterResource extends AbstractDataResource {

  /**
   * The function costs master.
   */
  private final FunctionCostsMaster _functionCostsMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param functionCostsMaster  the underlying master, not null
   */
  public DataFunctionCostsMasterResource(final FunctionCostsMaster functionCostsMaster) {
    ArgumentChecker.notNull(functionCostsMaster, "functionCostsMaster");
    _functionCostsMaster = functionCostsMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the master.
   * 
   * @return the master, not null
   */
  public FunctionCostsMaster getFunctionCostsMaster() {
    return _functionCostsMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("functioncosts")
  public Response status() {
    // simple GET to quickly return as a ping
    return responseOk();
  }

  @GET
  @Path("functioncosts")
  public Response search(@QueryParam("configurationName") String configurationName, @QueryParam("functionId") String functionId, @QueryParam("versionAsOf") String versionAsOfStr) {
    Instant versionAsOf = (versionAsOfStr != null ? Instant.parse(versionAsOfStr) : null);
    FunctionCostsDocument result = getFunctionCostsMaster().load(configurationName, functionId, versionAsOf);
    return responseOkObject(result);
  }

  @POST
  @Path("functioncosts")
  public Response store(@Context UriInfo uriInfo, FunctionCostsDocument request) {
    FunctionCostsDocument result = getFunctionCostsMaster().store(request);
    return responseOkObject(result);
  }

}
