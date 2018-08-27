/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

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
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an exchange.
 */
@Path("/exchanges/{exchangeId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebExchangeVersionsResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebExchangeVersionsResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(data().getExchange().getUniqueId());
    final ExchangeHistoryResult result = data().getExchangeMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getExchanges());
    return getFreemarker().build(HTML_DIR + "exchangeversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(data().getExchange().getUniqueId());
    request.setPagingRequest(pr);
    final ExchangeHistoryResult result = data().getExchangeMaster().history(request);

    final FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getExchanges());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    final String json = getFreemarker().build(JSON_DIR + "exchangeversions.ftl", out);
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
    final ExchangeDocument doc = data().getExchange();
    out.put("exchangeDoc", doc);
    out.put("exchange", doc.getExchange());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebExchangeVersionResource findVersion(@PathParam("versionId") final String idStr) {
    data().setUriVersionId(idStr);
    final ExchangeDocument doc = data().getExchange();
    final UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      final ExchangeDocument versioned = data().getExchangeMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebExchangeVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    final String exchangeId = data.getBestExchangeUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeVersionsResource.class).build(exchangeId);
  }

}
