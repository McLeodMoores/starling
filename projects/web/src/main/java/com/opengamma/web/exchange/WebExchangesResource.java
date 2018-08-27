/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ExchangeSearchSortOrder;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all exchanges.
 * <p>
 * The exchanges resource represents the whole of a exchange master.
 */
@Path("/exchanges")
public class WebExchangesResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param exchangeMaster  the exchange master, not null
   */
  public WebExchangesResource(final ExchangeMaster exchangeMaster) {
    super(exchangeMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("exchangeId") final List<String> exchangeIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ExchangeSearchSortOrder so = buildSortOrder(sort, ExchangeSearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, exchangeIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "exchanges.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("exchangeId") final List<String> exchangeIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ExchangeSearchSortOrder so = buildSortOrder(sort, ExchangeSearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, exchangeIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "exchanges.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final ExchangeSearchSortOrder so, final String name,
      final List<String> exchangeIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      final ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    for (final String exchangeIdStr : exchangeIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(exchangeIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final ExchangeSearchResult searchResult = data().getExchangeMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
    name = StringUtils.trimToNull(name);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    regionScheme = StringUtils.trimToNull(regionScheme);
    regionValue = StringUtils.trimToNull(regionValue);
    if (name == null || idScheme == null || idValue == null) {
      final FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (idScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      }
      if (regionScheme == null) {
        out.put("err_regionschemeMissing", true);
      }
      if (regionValue == null) {
        out.put("err_regionvalueMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "exchanges-add.ftl", out);
      return Response.ok(html).build();
    }
    final URI uri = createExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
    name = StringUtils.trimToNull(name);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    regionScheme = StringUtils.trimToNull(regionScheme);
    regionValue = StringUtils.trimToNull(regionValue);
    if (name == null || idScheme == null || idValue == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    final URI uri = createExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.created(uri).build();
  }

  private URI createExchange(final String name, final String idScheme, final String idValue, final String regionScheme, final String regionValue) {
    final ExternalId id = ExternalId.of(idScheme, idValue);
    final ExternalId region = ExternalId.of(regionScheme, regionValue);
    final ManageableExchange exchange = new ManageableExchange(ExternalIdBundle.of(id), name, ExternalIdBundle.of(region), null);
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    final ExchangeDocument added = data().getExchangeMaster().add(doc);
    final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return uri;
  }

  //-------------------------------------------------------------------------
  @Path("{exchangeId}")
  public WebExchangeResource findExchange(@PathParam("exchangeId") final String idStr) {
    data().setUriExchangeId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final ExchangeDocument doc = data().getExchangeMaster().get(oid);
      data().setExchange(doc);
    } catch (final DataNotFoundException ex) {
      final ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final ExchangeHistoryResult historyResult = data().getExchangeMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setExchange(historyResult.getFirstDocument());
    }
    return new WebExchangeResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for exchanges.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for exchanges.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data, final ExternalIdBundle identifiers) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebExchangesResource.class);
    if (identifiers != null) {
      final Iterator<ExternalId> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final ExternalId id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
