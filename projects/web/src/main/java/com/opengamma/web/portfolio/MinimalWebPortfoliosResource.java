/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.portfolio.PortfolioSearchSortOrder;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all portfolios.
 * <p>
 * The portfolios resource represents the whole of a portfolio master.
 */
@Path("/portfolios")
public class MinimalWebPortfoliosResource extends AbstractMinimalWebPortfolioResource {

  /**
   * Creates the resource.
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param executor  the executor service, not null
   */
  public MinimalWebPortfoliosResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final SecuritySource securitySource,
      final ExecutorService executor) {
    super(portfolioMaster, positionMaster, securitySource, executor);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.PORTFOLIO)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("portfolioId") final List<String> portfolioIdStrs,
      @QueryParam("nodeId") final List<String> nodeIdStrs,
      @QueryParam("includeHidden") final Boolean includeHidden) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final PortfolioSearchSortOrder so = buildSortOrder(sort, PortfolioSearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, portfolioIdStrs, nodeIdStrs, includeHidden);
    return getFreemarker().build(HTML_DIR + "portfolios.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.PORTFOLIO)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("portfolioId") final List<String> portfolioIdStrs,
      @QueryParam("nodeId") final List<String> nodeIdStrs,
      @QueryParam("includeHidden") final Boolean includeHidden) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final PortfolioSearchSortOrder so = buildSortOrder(sort, PortfolioSearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, portfolioIdStrs, nodeIdStrs, includeHidden);
    return getFreemarker().build(JSON_DIR + "portfolios.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final PortfolioSearchSortOrder sort, final String name,
      final List<String> portfolioIdStrs, final List<String> nodeIdStrs, final Boolean includeHidden) {
    final FlexiBean out = createRootData();

    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(sort);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setDepth(1);  // see PLAT-1733, also, depth is set to 1 for knowing # of childNodes for UI tree
    searchRequest.setIncludePositions(true);  // initially false because of PLAT-2012, now true for portfolio tree
    if (BooleanUtils.isTrue(includeHidden)) {
      searchRequest.setVisibility(DocumentVisibility.HIDDEN);
    }
    for (final String portfolioIdStr : portfolioIdStrs) {
      searchRequest.addPortfolioObjectId(ObjectId.parse(portfolioIdStr));
    }
    for (final String nodeIdStr : nodeIdStrs) {
      searchRequest.addNodeObjectId(ObjectId.parse(nodeIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final PortfolioSearchResult searchResult = data().getPortfolioMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    if (name == null) {
      final FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      final String html = getFreemarker().build(HTML_DIR + "portfolios-add.ftl", out);
      return Response.ok(html).build();
    }
    final URI uri = createPortfolio(name);
    return Response.seeOther(uri).build();
  }

  private URI createPortfolio(final String name) {
    final ManageablePortfolio portfolio = new ManageablePortfolio(name);
    final PortfolioDocument doc = new PortfolioDocument(portfolio);
    final PortfolioDocument added = data().getPortfolioMaster().add(doc);
    return data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("name") String name) {
    name = StringUtils.trimToNull(name);
    final URI uri = createPortfolio(name);
    return Response.created(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{portfolioId}")
  public MinimalWebPortfolioResource findPortfolio(@Subscribe @PathParam("portfolioId") final String idStr) {
    data().setUriPortfolioId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final PortfolioDocument doc = data().getPortfolioMaster().get(oid);
      data().setPortfolio(doc);
      data().setNode(doc.getPortfolio().getRootNode());
    } catch (final DataNotFoundException ex) {
      final PortfolioHistoryRequest historyRequest = new PortfolioHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final PortfolioHistoryResult historyResult = data().getPortfolioMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setPortfolio(historyResult.getFirstDocument());
      data().setNode(historyResult.getFirstDocument().getPortfolio().getRootNode());
    }
    return new MinimalWebPortfolioResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for portfolios.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPortfoliosResource.class).build();
  }

}
