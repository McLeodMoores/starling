/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for configuration.
 * <p>
 * This resource receives and processes RESTful calls to the configuration source.
 */
@Path("configSource")
public class DataConfigSourceResource extends AbstractDataResource {

  /**
   * The config source.
   */
  private final ConfigSource _configSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param configSource  the underlying config source, not null
   */
  public DataConfigSourceResource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the configuration source.
   *
   * @return the configuration source, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  @SuppressWarnings("unchecked")
  private static FudgeMsg configItemCollectionResult(final Collection<?> items) {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final ConfigItem<?> item : (Collection<ConfigItem<?>>) items) {
      serializer.addToMessageWithClassHeaders(msg, null, null, item, ConfigItem.class);
    }
    return msg;
  }

  private static FudgeMsg configItemResult(final ConfigItem<?> item) {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    return FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(item), item.getClass(), ConfigItem.class);
  }

  private static FudgeMsg configValueResult(final Class<?> clazz, final Object value) {
    if (value == null) {
      return null;
    }
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    return FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(value), value.getClass(), clazz);
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
   * Searches for a configuration by name, type and / or version.
   *
   * @param typeStr
   *          the type, not null
   * @param versionCorrectionStr
   *          the version/correction, can be null. LATEST is used in that case.
   * @param name
   *          the name, can be null. All configurations of the desired type are
   *          returned in that case.
   * @return the configurations as a Fudge message
   */
  @GET
  @Path("configs")
  public Response search(
      @QueryParam("type") final String typeStr,
      @QueryParam("versionCorrection") final String versionCorrectionStr,
      @QueryParam("name") final String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = versionCorrectionStr != null ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    if (name == null) {
      return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
    }
    return responseOkObject(configItemCollectionResult(getConfigSource().get(type, name, versionCorrection)));
  }

  /**
   * Gets the configuration associated with a unique id.
   *
   * @param uidStr
   *          the unique id, not null
   * @return the configuration
   */
  @GET
  @Path("configs/{uid}")
  public Response get(
      @PathParam("uid") final String uidStr) {
    final UniqueId uid = UniqueId.parse(uidStr);
    final ConfigItem<?> result = getConfigSource().get(uid);
    return responseOkObject(configItemResult(result));
  }

  /**
   * Gets the configuration associated with an object id and version.
   *
   * @param idStr
   *          the object id, not null
   * @param versionCorrectionStr
   *          the version, not null
   * @return the configuration
   */
  @GET
  @Path("configs/{oid}/{versionCorrection}")
  public Response getByOidVersionCorrection(
      @PathParam("oid") final String idStr,
      @PathParam("versionCorrection") final String versionCorrectionStr) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    final ConfigItem<?> result = getConfigSource().get(objectId, versionCorrection);
    return responseOkObject(configItemResult(result));
  }

  /**
   * Searches for a configuration by name, type and / or version.
   *
   * @param typeStr
   *          the type, not null
   * @param versionCorrectionStr
   *          the version/correction, can be null. LATEST is used in that case.
   * @param name
   *          the name, not null
   * @return the configuration as a Fudge message
   */
  @GET
  @Path("configSearches/single")
  public Response searchSingle(
      @QueryParam("type") final String typeStr,
      @QueryParam("versionCorrection") final String versionCorrectionStr,
      @QueryParam("name") final String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = versionCorrectionStr != null ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configValueResult(type, getConfigSource().getSingle(type, name, versionCorrection)));
  }

  /**
   * Searches for configurations by type and version.
   * 
   * @param typeStr
   *          the type, not null
   * @param versionCorrectionStr
   *          the version/correction, can be null. LATEST is used in that case.
   * @return the configurations as a Fudge message
   */
  @GET
  @Path("configSearches")
  public Response search(
      @QueryParam("type") final String typeStr,
      @QueryParam("versionCorrection") final String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = versionCorrectionStr != null ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
  }

  // TODO: put is not a RESTful URI!
  @PUT
  @Path("put")
  public Response put(
      @QueryParam("type") final String typeStr,
      @QueryParam("versionCorrection") final String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = versionCorrectionStr != null ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
  }

}
