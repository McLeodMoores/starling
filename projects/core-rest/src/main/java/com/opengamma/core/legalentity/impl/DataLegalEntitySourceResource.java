/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for legal entities.
 * <p>
 * The legal entities resource receives and processes RESTful calls to the legal entity source.
 */
@Path("legalentitySource")
public class DataLegalEntitySourceResource extends AbstractDataResource {

  /**
   * The legal entity source.
   */
  private final LegalEntitySource _secSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param legalentitySource the underlying legal entity source, not null
   */
  public DataLegalEntitySourceResource(final LegalEntitySource legalentitySource) {
    ArgumentChecker.notNull(legalentitySource, "legalentitySource");
    _secSource = legalentitySource;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the legal entity source.
   *
   * @return the legal entity source, not null
   */
  public LegalEntitySource getLegalEntitySource() {
    return _secSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("legal entities")
  public Response search(
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo,
      @QueryParam("id") final List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final Collection<? extends LegalEntity> result = getLegalEntitySource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("legal entities/{legalentityId}")
  public Response get(
      @PathParam("legalentityId") final String idStr,
      @QueryParam("version") final String version,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final LegalEntity result = getLegalEntitySource().get(objectId.atVersion(version));
      return responseOkObject(result);
    }
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final LegalEntity result = getLegalEntitySource().get(objectId, vc);
    return responseOkObject(result);
  }

  @GET
  @Path("legalentitySearches/bulk")
  public Response getBulk(
      @QueryParam("id") final List<String> uniqueIdStrs) {
    final List<UniqueId> uids = IdUtils.parseUniqueIds(uniqueIdStrs);
    final Map<UniqueId, LegalEntity> result = getLegalEntitySource().get(uids);
    return responseOkObject(FudgeListWrapper.of(result.values()));
  }


  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("legalentitySearches/list")
  public Response searchList(@QueryParam("id") final List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    @SuppressWarnings("deprecation")
    final
    Collection<? extends LegalEntity> result = getLegalEntitySource().get(bundle);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("legalentitySearches/single")
  public Response searchSingle(
      @QueryParam("id") final List<String> externalIdStrs,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {

    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final LegalEntity result = getLegalEntitySource().getSingle(bundle, vc);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------

  /**
   * For debugging purposes only.
   *
   * @return some debug information about the state of this resource object
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = OpenGammaFudgeContext.getInstance().newMessage();
    message.add("fudgeContext", OpenGammaFudgeContext.getInstance().toString());
    message.add("legalentitySource", getLegalEntitySource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
