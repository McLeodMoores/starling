/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * Abstract base class for RESTful resources.
 *
 * @param <D>  the type of the document
 */
public abstract class AbstractDocumentDataResource<D extends AbstractDocument> extends AbstractDataResource {

  protected abstract AbstractMaster<D> getMaster();

  protected abstract String getResourceName();

  protected abstract ObjectId getUrlId();

  //===================== ROUTING HELPERS ==============================================================================

  // @GET
  protected Response get(/*@QueryParam("versionAsOf")*/ final String versionAsOf, /*@QueryParam("correctedTo")*/ final String correctedTo) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final D result = getMaster().get(getUrlId(), vc);
    return responseOkObject(result);
  }

  // @POST
  protected Response update(/*@Context*/ final UriInfo uriInfo, final D request) {
    if (getUrlId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectIdentifiable does not match URI");
    }
    final D result = getMaster().update(request);
    final URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(uri, result);
  }

  // @DELETE
  protected void remove() {
    getMaster().remove(getUrlId().atLatestVersion());
  }


  // @GET
  // @Path("versions/{versionId}")
  protected Response getVersioned(/*@PathParam("versionId")*/ final String versionId) {
    final UniqueId uniqueId = getUrlId().atVersion(versionId);
    final D result = getMaster().get(uniqueId);
    return responseOkObject(result);
  }


  // @PUT
  // @Path("versions/{versionId}")
  protected Response replaceVersion(/*@PathParam("versionId")*/ final String versionId, final List<D> replacementDocuments) {
    final UniqueId uniqueId = getUrlId().atVersion(versionId);

    final List<UniqueId> result = getMaster().replaceVersion(uniqueId, replacementDocuments);
    return responseOkObject(result);
  }

  // @PUT
  protected Response replaceVersions(final List<D> replacementDocuments) {
    final ObjectId objectId = getUrlId();
    final List<UniqueId> result = getMaster().replaceVersions(objectId, replacementDocuments);
    return responseOkObject(result);
  }

  // @PUT
  // @Path("all")
  protected Response replaceAllVersions(final List<D> replacementDocuments) {
    final ObjectId objectId = getUrlId();
    final List<UniqueId> result = getMaster().replaceAllVersions(objectId, replacementDocuments);
    return responseOkObject(result);
  }

  //====================================================================================================================

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectIdentifiable  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public URI uri(final URI baseUri, final ObjectIdentifiable objectIdentifiable, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectIdentifiable.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  // TODO replace URI with something better
  public URI uriAll(final URI baseUri, final ObjectIdentifiable objectId, final VersionCorrection vc) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/all");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }


  /**
   * Builds a URI for the versions of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public URI uriVersions(final URI baseUri, final ObjectIdentifiable objectId, final Object request) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public URI uriVersion(final URI baseUri, final UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    return UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
