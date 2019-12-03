/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

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
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an region.
 */
@Path("/regions/{regionId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebRegionVersionsResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * 
   * @param parent
   *          the parent resource, not null
   */
  public WebRegionVersionsResource(final AbstractWebRegionResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  @GET
  public String getHTML() {
    final RegionHistoryRequest request = new RegionHistoryRequest(data().getRegion().getUniqueId());
    final RegionHistoryResult result = data().getRegionMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getRegions());
    return getFreemarker().build(HTML_DIR + "regionversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final RegionHistoryRequest request = new RegionHistoryRequest(data().getRegion().getUniqueId());
    request.setPagingRequest(pr);
    final RegionHistoryResult result = data().getRegionMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getRegions());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "regionversions.ftl", out);
    return Response.ok(json).build();
  }

  // -------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final RegionDocument doc = data().getRegion();
    out.put("regionDoc", doc);
    out.put("region", doc.getRegion());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  // -------------------------------------------------------------------------
  @Path("{versionId}")
  public WebRegionVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final RegionDocument doc = data().getRegion();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (!doc.getUniqueId().equals(combined)) {
      final RegionDocument versioned = data().getRegionMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebRegionVersionResource(this);
  }

  // -------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * 
   * @param data
   *          the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data) {
    final String regionId = data.getBestRegionUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebRegionVersionsResource.class).build(regionId);
  }

}
