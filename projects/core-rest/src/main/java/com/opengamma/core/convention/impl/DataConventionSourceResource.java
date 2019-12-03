/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

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

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for conventions.
 * <p>
 * The conventions resource receives and processes RESTful calls to the convention source.
 */
@Path("conventionSource")
public class DataConventionSourceResource extends AbstractDataResource {

  /**
   * The convention source.
   */
  private final ConventionSource _conventionSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param conventionSource  the underlying convention source, not null
   */
  public DataConventionSourceResource(final ConventionSource conventionSource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    _conventionSource = conventionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention source.
   *
   * @return the convention source, not null
   */
  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a HATEAOS response.
   *
   * @param uriInfo
   *          the URI, not null
   * @return the response
   */
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  /**
   * Searches for a convention by version, correction and external identifiers.
   *
   * @param versionAsOf
   *          the version, can be null. If null, the latest is used.
   * @param correctedTo
   *          the correction, can be null. If null, the latest is used.
   * @param externalIdStrs
   *          the external ids, not null
   * @return the conventions as a Fudge message
   */
  @GET
  @Path("conventions")
  public Response search(
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo,
      @QueryParam("id") final List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final Collection<? extends Convention> result = getConventionSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  /**
   * Searches for a convention by identifier, version, version and correction.
   *
   * @param idStr
   *          the object identifier, not null
   * @param version
   *          the version, can be null. If null, the latest is used
   * @param versionAsOf
   *          the version, can be null. If null, the latest is used.
   * @param correctedTo
   *          the correction, can be null. If null, the latest is used.
   * @return the convention as a Fudge message
   */
  @GET
  @Path("conventions/{conventionId}")
  public Response get(
      @PathParam("conventionId") final String idStr,
      @QueryParam("version") final String version,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Convention result = getConventionSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    }
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final Convention result = getConventionSource().get(objectId, vc);
    return responseOkObject(result);
  }

  /**
   * Gets multiple conventions.
   *
   * @param uniqueIdStrs
   *          the convention identifiers, not null
   * @return the conventions as a Fudge message
   */
  @GET
  @Path("conventionSearches/bulk")
  public Response getBulk(
      @QueryParam("id") final List<String> uniqueIdStrs) {
    final List<UniqueId> uids = IdUtils.parseUniqueIds(uniqueIdStrs);
    final Map<UniqueId, Convention> result = getConventionSource().get(uids);
    return responseOkObject(FudgeListWrapper.of(result.values()));
  }

  // deprecated
  //-------------------------------------------------------------------------
  /**
   * Searches for conventions by external identifiers.
   *
   * @param externalIdStrs
   *          the external ids, not null
   * @return the conventions as a Fudge message
   */
  @GET
  @Path("conventionSearches/list")
  public Response searchList(@QueryParam("id") final List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    @SuppressWarnings("deprecation")
    final Collection<? extends Convention> result = getConventionSource().get(bundle);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  /**
   * Searches for conventions by identifiers, version, version and correction.
   *
   * @param externalIdStrs
   *          the object identifier, not null
   * @param versionAsOf
   *          the version, can be null. If null, the latest is used.
   * @param correctedTo
   *          the correction, can be null. If null, the latest is used.
   * @param typeStr
   *          the type of the convention, can be null
   * @return the conventions as a Fudge message
   */
  @GET
  @Path("conventionSearches/single")
  public Response searchSingle(
      @QueryParam("id") final List<String> externalIdStrs,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo,
      @QueryParam("type") final String typeStr) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    if (typeStr != null) {
      final Class<? extends Convention> type = ClassUtils.loadClassRuntime(typeStr, Convention.class);
      final Convention result = getConventionSource().getSingle(bundle, vc, type);
      return responseOkObject(result);
    }
    final Convention result = getConventionSource().getSingle(bundle, vc);
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
    message.add("conventionSource", getConventionSource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
