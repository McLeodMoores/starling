/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of a legal entity.
 */
@Path("/legalentities/{legalEntityId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebLegalEntityVersionsResource extends AbstractWebLegalEntityResource {

  /**
   * Creates the resource.
   *
   * @param parent
   *          the parent resource, not null
   */
  public WebLegalEntityVersionsResource(final AbstractWebLegalEntityResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  @GET
  public String getHTML() {
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(data().getLegalEntity().getUniqueId());
    final LegalEntityHistoryResult result = data().getLegalEntityMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getLegalEntities());
    return getFreemarker().build(HTML_DIR + "legalentityversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(data().getLegalEntity().getUniqueId());
    request.setPagingRequest(pr);
    final LegalEntityHistoryResult result = data().getLegalEntityMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getLegalEntities());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "legalentityversions.ftl", out);
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
    final LegalEntityDocument doc = data().getLegalEntity();
    out.put("legalEntityDoc", doc);
    out.put("legalEntity", doc.getLegalEntity());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  // -------------------------------------------------------------------------
  @Path("{versionId}")
  public WebLegalEntityVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final LegalEntityDocument doc = data().getLegalEntity();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (!doc.getUniqueId().equals(combined)) {
      final LegalEntityDocument versioned = data().getLegalEntityMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebLegalEntityVersionResource(this);
  }

  // -------------------------------------------------------------------------

  /**
   * Builds a URI for this resource.
   *
   * @param data
   *          the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebLegalEntityData data) {
    final String legalEntityId = data.getBestLegalEntityUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebLegalEntityVersionsResource.class).build(legalEntityId);
  }

}
