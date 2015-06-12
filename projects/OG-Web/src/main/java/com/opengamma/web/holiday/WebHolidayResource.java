/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;

/**
 * RESTful resource for a holiday.
 */
@Path("/holidays/{holidayId}")
public class WebHolidayResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHolidayResource(final AbstractWebHolidayResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    final HolidayDocument doc = data().getHoliday();
    out.put("holidayXml", StringEscapeUtils.escapeJava(createBeanXML(doc.getHoliday())));
    return getFreemarker().build(HTML_DIR + "holiday.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getHoliday().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final HolidayDocument doc = data().getHoliday();
    out.put("holidayXML", StringEscapeUtils.escapeJava(createBeanXML(doc.getHoliday())));
    final String json = getFreemarker().build(JSON_DIR + "holiday.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(@FormParam("name") final String name, @FormParam("holidayxml") final String xml) {
    if (data().getHoliday().isLatest()) {
      final String trimmedName = StringUtils.trimToNull(name);
      final String trimmedXml = StringUtils.trimToNull(xml);
      if (trimmedName == null || trimmedXml == null) {
        final FlexiBean out = createRootData();
        if (trimmedName == null) {
          out.put("err_nameMissing", true);
        }
        if (trimmedXml == null) {
          out.put("err_xmlMissing", true);
        }
        final String html = getFreemarker().build(HTML_DIR + "holiday-update.ftl", out);
        return Response.ok(html).build();
      }
      try {
        final ManageableHoliday holiday = parseXML(xml, data().getHoliday().getHoliday().getClass());
        final URI uri = updateHoliday(name, holiday);
        return Response.seeOther(uri).build();
      } catch (final Exception ex) {
        final FlexiBean out = createRootData();
        out.put("holidayXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
        out.put("err_holidayXmlMsg", StringUtils.defaultString(ex.getMessage()));
        final String html = getFreemarker().build(HTML_DIR + "holiday-update.ftl", out);
        return Response.ok(html).build();
      }
    }
    return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
  }

  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final HolidayDocument doc = data().getHoliday();
    if (doc.isLatest()) {
      data().getHolidayMaster().remove(doc.getUniqueId());
      final URI uri = WebHolidayResource.uri(data());
      return Response.seeOther(uri).build();
    }
    return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final HolidayDocument doc = data().getHoliday();
    if (doc.isLatest()) {
      data().getHolidayMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final HolidayDocument doc = data().getHoliday();
    out.put("holidayDoc", doc);
    out.put("holiday", doc.getHoliday());
    out.put("holidayDescriptionMap", getHolidayTypesProvider().getDescription(doc.getHoliday().getType().name()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebHolidayVersionsResource findVersions() {
    return new WebHolidayVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideHolidayId  the override holiday id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data, final UniqueId overrideHolidayId) {
    final String holidayId = data.getBestHolidayUriId(overrideHolidayId);
    return data.getUriInfo().getBaseUriBuilder().path(WebHolidayResource.class).build(holidayId);
  }

  private URI updateHoliday(final String name, final ManageableHoliday snapshot) {
    final HolidayDocument oldDoc = data().getHoliday();
    HolidayDocument doc = new HolidayDocument(snapshot);
    doc.setName(name);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getHolidayMaster().update(doc);
    data().setHoliday(doc);
    return WebHolidayResource.uri(data());
  }
}
