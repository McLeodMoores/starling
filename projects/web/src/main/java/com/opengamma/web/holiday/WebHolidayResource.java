/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import org.threeten.bp.LocalDate;
import org.threeten.bp.Year;

import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  /**
   * Gets the holiday as XML.
   * @return  the holiday
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    final HolidayDocument doc = data().getHoliday();
    out.put("holidayXml", StringEscapeUtils.escapeJava(createBeanXML(doc.getHoliday())));
    return getFreemarker().build(HTML_DIR + "holiday.ftl", out);
  }

  /**
   * Gets the holiday as JSON.
   * @param request  the request, not null
   * @return  the holiday
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    ArgumentChecker.notNull(request, "request");
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
  /**
   * Stores a new or updated holiday in the master.
   * @param name  the holiday name
   * @param xml  the bean XML
   * @return  the response
   */
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

  /**
   * Stores a new or updated holiday in the master.
   * @param name  the holiday name
   * @param json  the bean JSON
   * @param xml  the bean XML
   * @return  the response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(@FormParam("name") final String name, @FormParam("holidayJSON") final String json,
      @FormParam("holidayXML") final String xml) {
    if (!data().getHoliday().isLatest()) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedJson = StringUtils.trimToNull(json);
    final String trimmedXml = StringUtils.trimToNull(xml);
    // JSON allows a null holiday to just change the name
    if (trimmedName == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    ManageableHoliday holidayValue = null;
    if (trimmedJson != null) {
      holidayValue = (ManageableHoliday) parseJSON(trimmedJson);
    } else if (trimmedXml != null) {
      holidayValue = parseXML(trimmedXml, ManageableHoliday.class);
    }
    updateHoliday(trimmedName, holidayValue);
    return Response.ok().build();
  }

  /**
   * Deletes a holiday from the master.
   * @return  the response
   */
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

  /**
   * Deletes a holiday from the master.
   * @return  the response
   */
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
    out.put("holidayDatesByYear", getHolidayDatesByYear(doc));
    final String weekendType;
    // not all holidays have the weekend explicitly set
    if (doc.getHoliday() instanceof WeekendTypeProvider) {
      weekendType = ((WeekendTypeProvider) doc.getHoliday()).getWeekendType().name();
    } else {
      weekendType = WeekendType.SATURDAY_SUNDAY.name();
    }
    out.put("weekendType", weekendType);
    out.put("holidayType", doc.getHoliday().getType());
    return out;
  }

  private static List<Pair<Year, List<LocalDate>>> getHolidayDatesByYear(final HolidayDocument doc) {
    final List<LocalDate> holidayDates = doc.getHoliday().getHolidayDates();
    final List<Pair<Year, List<LocalDate>>> datesByYear = new ArrayList<>();
    if (holidayDates.isEmpty()) {
      datesByYear.add(Pairs.of(Year.of(LocalDate.now().getYear()), holidayDates));
    }
    if (holidayDates.size() > 0) {
      int year = holidayDates.get(0).getYear();
      int start = 0;
      int pos = 0;
      for ( ; pos < holidayDates.size(); pos++) {
        if (holidayDates.get(pos).getYear() == year) {
          continue;
        }
        datesByYear.add(Pairs.of(Year.of(year), holidayDates.subList(start, pos)));
        year = holidayDates.get(pos).getYear();
        start = pos;
      }
      datesByYear.add(Pairs.of(Year.of(year), holidayDates.subList(start, pos)));
    }
    return datesByYear;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a version resource for holidays.
   * @return  the versions resource
   */
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

  /**
   * Updates a holiday
   * @param name  the holiday name
   * @param snapshot  the updated holiday
   * @return  the URI
   */
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
