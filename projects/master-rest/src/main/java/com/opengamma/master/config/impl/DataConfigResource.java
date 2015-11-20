/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a config.
 */
public class DataConfigResource extends AbstractDataResource {

  /**
   * The configs resource.
   */
  private final DataConfigMasterResource _configsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataConfigResource() {
    _configsResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param configsResource  the parent resource, not null
   * @param configId  the config unique identifier, not null
   */
  public DataConfigResource(final DataConfigMasterResource configsResource, final ObjectId configId) {
    ArgumentChecker.notNull(configsResource, "configsResource");
    ArgumentChecker.notNull(configId, "config");
    _configsResource = configsResource;
    _urlResourceId = configId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the configs resource.
   *
   * @return the configs resource, not null
   */
  public DataConfigMasterResource getConfigsResource() {
    return _configsResource;
  }

  /**
   * Gets the config identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlConfigId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the config master.
   *
   * @return the config master, not null
   */
  public ConfigMaster getConfigMaster() {
    return getConfigsResource().getConfigMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    ConfigDocument result = getConfigMaster().get(getUrlConfigId(), vc);
    return responseOkObject(result);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, ConfigDocument request) {
    if (getUrlConfigId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ConfigDocument result = getConfigMaster().update(request);
    URI uri = DataConfigUris.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(uri, result);
  }

  @DELETE
  public void remove() {
    getConfigMaster().remove(getUrlConfigId().atLatestVersion());
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    ConfigHistoryRequest<?> request = RestUtils.decodeQueryParams(uriInfo, ConfigHistoryRequest.class);
    if (getUrlConfigId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ConfigHistoryResult<?> result = getConfigMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlConfigId().atVersion(versionId);
    ConfigDocument result = getConfigMaster().get(uniqueId);
    return responseOkObject(result);
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, ConfigDocument document) {
    UniqueId uniqueId = getUrlConfigId().atVersion(versionId);
    if (!uniqueId.equals(document.getUniqueId())) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    ConfigDocument result = getConfigMaster().correct(document);
    URI uri = DataConfigUris.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(uri, result);
  }

  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<ConfigDocument> replacementDocuments) {
    UniqueId uniqueId = getUrlConfigId().atVersion(versionId);

    List<UniqueId> result = getConfigMaster().replaceVersion(uniqueId, replacementDocuments);
    return responseOkObject(result);
  }

  @PUT
  public <T> Response replaceAllVersions(List<ConfigDocument> replacementDocuments) {
    ObjectId objectId = getUrlConfigId();
    List<UniqueId> result = getConfigMaster().replaceAllVersions(objectId, replacementDocuments);
    return responseOkObject(result);
  }

  @DELETE
  @Path("versions/{versionId}")
  public void removeVersion(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlConfigId().atVersion(versionId);
    getConfigMaster().removeVersion(uniqueId);
  }

}
