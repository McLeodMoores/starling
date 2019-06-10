/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

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
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of a security.
 */
@Path("/securities/{securityId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebSecurityVersionsResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * 
   * @param parent
   *          the parent resource, not null
   */
  public WebSecurityVersionsResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  @GET
  public String getHTML() {
    final SecurityHistoryRequest request = new SecurityHistoryRequest(data().getSecurity().getUniqueId());
    final SecurityHistoryResult result = data().getSecurityMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getSecurities());
    return getFreemarker().build(HTML_DIR + "securityversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final SecurityHistoryRequest request = new SecurityHistoryRequest(data().getSecurity().getUniqueId());
    request.setPagingRequest(pr);
    final SecurityHistoryResult result = data().getSecurityMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getSecurities());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "securityversions.ftl", out);
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
    final SecurityDocument doc = data().getSecurity();
    out.put("securityDoc", doc);
    out.put("security", doc.getSecurity());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  // -------------------------------------------------------------------------
  @Path("{versionId}")
  public WebSecurityVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final SecurityDocument doc = data().getSecurity();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (!doc.getUniqueId().equals(combined)) {
      final SecurityDocument versioned = data().getSecurityMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebSecurityVersionResource(this);
  }

  // -------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * 
   * @param data
   *          the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    final String securityId = data.getBestSecurityUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityVersionsResource.class).build(securityId);
  }

}
