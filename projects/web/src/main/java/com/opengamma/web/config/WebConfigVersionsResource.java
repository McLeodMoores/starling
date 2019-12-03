/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

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
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an config.
 */
@Path("/configs/{configId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebConfigVersionsResource extends AbstractWebConfigResource {

  /**
   * Creates the resource.
   * 
   * @param parent
   *          the parent resource, not null
   */
  public WebConfigVersionsResource(final AbstractWebConfigResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  @GET
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public String getHTML() {
    final ConfigHistoryRequest request = new ConfigHistoryRequest(data().getConfig().getUniqueId(), Object.class);
    final ConfigHistoryResult<?> result = data().getConfigMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getValues());
    return getFreemarker().build(HTML_DIR + "configversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ConfigHistoryRequest request = new ConfigHistoryRequest(data().getConfig().getUniqueId(), Object.class);
    request.setPagingRequest(pr);
    final ConfigHistoryResult<?> result = data().getConfigMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getValues());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "configversions.ftl", out);
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
    final ConfigDocument doc = data().getConfig();
    out.put("configDoc", doc);
    out.put("config", doc.getConfig().getValue());
    out.put("configDescription", getConfigTypesProvider().getDescription(doc.getConfig().getType()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  // -------------------------------------------------------------------------
  @Path("{versionId}")
  public WebConfigVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final ConfigDocument doc = data().getConfig();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (!doc.getUniqueId().equals(combined)) {
      final ConfigDocument versioned = data().getConfigMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebConfigVersionResource(this);
  }

  // -------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * 
   * @param data
   *          the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData data) {
    final String configId = data.getBestConfigUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigVersionsResource.class).build(configId);
  }

}
