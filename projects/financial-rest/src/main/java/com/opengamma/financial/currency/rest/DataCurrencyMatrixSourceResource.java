/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for currency matrices.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("/currencyMatrixSource")
public class DataCurrencyMatrixSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final CurrencyMatrixSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param source the underlying source, not null
   */
  public DataCurrencyMatrixSourceResource(final CurrencyMatrixSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   *
   * @return the source, not null
   */
  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("currencyMatricesByName/{versionCorrection}/{name}")
  public Response getMatrixByName(@PathParam("name") final String name, @PathParam("versionCorrection") final String versionCorrectionStr) {
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    final CurrencyMatrix result = getCurrencyMatrixSource().getCurrencyMatrix(name, versionCorrection);
    return responseOkObject(result);
  }

  @GET
  @Path("currencyMatrices/{uniqueId}")
  public Response getMatrixByUniqueId(@PathParam("uniqueId") final String uniqueIdStr) {
    final UniqueId uniqueId = UniqueId.parse(uniqueIdStr);
    final CurrencyMatrix result = getCurrencyMatrixSource().get(uniqueId);
    return responseOkObject(result);
  }

  @GET
  @Path("currencyMatrices/{objectId}/{versionCorrection}")
  public Response getMatrixByObjectIdentifier(@PathParam("objectId") final String objectIdStr,
      @PathParam("versionCorrection") final String versionCorrectionStr) {
    final ObjectId objectId = ObjectId.parse(objectIdStr);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    final CurrencyMatrix result = getCurrencyMatrixSource().get(objectId, versionCorrection);
    return responseOkObject(result);
  }

}
