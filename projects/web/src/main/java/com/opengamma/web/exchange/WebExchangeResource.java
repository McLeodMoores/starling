/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * RESTful resource for a exchange.
 */
@Path("/exchanges/{exchangeId}")
public class WebExchangeResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebExchangeResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "exchange.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getExchange().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final String json = getFreemarker().build(JSON_DIR + "exchange.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") final String name,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue,
      @FormParam("regionscheme") final String regionScheme,
      @FormParam("regionvalue") final String regionValue) {
    if (data().getExchange().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
    final String trimmedIdValue = StringUtils.trimToNull(idValue);
    if (trimmedName == null || trimmedIdScheme == null || trimmedIdValue == null) {
      final FlexiBean out = createRootData();
      if (trimmedName == null) {
        out.put("err_nameMissing", true);
      }
      if (trimmedIdScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (trimmedIdValue == null) {
        out.put("err_idvalueMissing", true);
      }
      if (regionScheme == null) {
        out.put("err_regionschemeMissing", true);
      }
      if (regionValue == null) {
        out.put("err_regionvalueMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "exchange-update.ftl", out);
      return Response.ok(html).build();
    }
    final URI uri = updateExchange(trimmedName, trimmedIdScheme, trimmedIdValue, regionScheme, regionValue);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") final String name,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue,
      @FormParam("regionscheme") final String regionScheme,
      @FormParam("regionvalue") final String regionValue) {
    if (data().getExchange().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
    final String trimmedIdValue = StringUtils.trimToNull(idValue);
    if (trimmedName == null || trimmedIdScheme == null || trimmedIdValue == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    updateExchange(trimmedName, trimmedIdScheme, trimmedIdValue, regionScheme, regionValue);
    return Response.ok().build();
  }

  private URI updateExchange(final String name, final String idScheme, final String idValue, final String regionScheme, final String regionValue) {
    final ManageableExchange exchange = data().getExchange().getExchange().clone();
    exchange.setName(name);
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of(idScheme, idValue)));
    exchange.setRegionIdBundle(ExternalIdBundle.of(ExternalId.of(regionScheme, regionValue)));
    ExchangeDocument doc = new ExchangeDocument(exchange);
    doc = data().getExchangeMaster().update(doc);
    data().setExchange(doc);
    final URI uri = WebExchangeResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final ExchangeDocument doc = data().getExchange();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getExchangeMaster().remove(doc.getUniqueId());
    final URI uri = WebExchangeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final ExchangeDocument doc = data().getExchange();
    if (doc.isLatest()) {
      data().getExchangeMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final ExchangeDocument doc = data().getExchange();
    out.put("exchangeDoc", doc);
    out.put("exchange", doc.getExchange());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebExchangeVersionsResource findVersions() {
    return new WebExchangeVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideExchangeId  the override exchange id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data, final UniqueId overrideExchangeId) {
    final String exchangeId = data.getBestExchangeUriId(overrideExchangeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeResource.class).build(exchangeId);
  }

}
