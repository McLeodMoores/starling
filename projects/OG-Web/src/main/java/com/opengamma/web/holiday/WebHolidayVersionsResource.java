/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an holiday.
 */
@Path("/holidays/{holidayId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebHolidayVersionsResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHolidayVersionsResource(final AbstractWebHolidayResource parent) {
    super(parent);
  }

  @GET
  public String getHTML() {
    final HolidayHistoryRequest request = new HolidayHistoryRequest(data().getHoliday().getUniqueId());
    final HolidayHistoryResult result = data().getHolidayMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getHolidays());
    return getFreemarker().build(HTML_DIR + "holidayversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final HolidayHistoryRequest request = new HolidayHistoryRequest(data().getHoliday().getUniqueId());
    request.setPagingRequest(pr);
    final HolidayHistoryResult result = data().getHolidayMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getHolidays());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "holidayversions.ftl", out);
    return Response.ok(json).build();
  }

  //-------------------------------------------------------------------------
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
    out.put("holidayDescription", getHolidayTypesProvider().getDescription(doc.getHoliday().getType().name()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebHolidayVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final HolidayDocument doc = data().getHoliday();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined)) {
      data().setVersioned(doc);
    } else {
      final HolidayDocument versioned = data().getHolidayMaster().get(combined);
      data().setVersioned(versioned);
    }
    return new WebHolidayVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data) {
    final String holidayId = data.getBestHolidayUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebHolidayVersionsResource.class).build(holidayId);
  }

}
