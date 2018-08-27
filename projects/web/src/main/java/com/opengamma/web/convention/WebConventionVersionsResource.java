/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

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
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an convention.
 */
@Path("/conventions/{conventionId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebConventionVersionsResource extends AbstractWebConventionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConventionVersionsResource(final AbstractWebConventionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    final ConventionHistoryRequest request = new ConventionHistoryRequest(data().getConvention().getUniqueId());
    final ConventionHistoryResult result = data().getConventionMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getConventions());
    return getFreemarker().build(HTML_DIR + "conventionversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ConventionHistoryRequest request = new ConventionHistoryRequest(data().getConvention().getUniqueId());
    request.setPagingRequest(pr);
    final ConventionHistoryResult result = data().getConventionMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getConventions());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "conventionversions.ftl", out);
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
    final ConventionDocument doc = data().getConvention();
    out.put("conventionDoc", doc);
    out.put("convention", doc.getConvention());
    out.put("conventionDescription", getConventionTypesProvider().getDescription(doc.getConvention().getClass()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebConventionVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final ConventionDocument doc = data().getConvention();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      final ConventionDocument versioned = data().getConventionMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebConventionVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConventionData data) {
    final String conventionId = data.getBestConventionUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebConventionVersionsResource.class).build(conventionId);
  }

}
