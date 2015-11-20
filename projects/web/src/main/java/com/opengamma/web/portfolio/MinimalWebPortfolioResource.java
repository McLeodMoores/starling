/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * RESTful resource for a portfolio.
 */
@Path("/portfolios/{portfolioId}")
public class MinimalWebPortfolioResource extends AbstractMinimalWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public MinimalWebPortfolioResource(final AbstractMinimalWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createPortfolioData();
    return getFreemarker().build(HTML_DIR + "portfolio.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON() {
    final FlexiBean out = createPortfolioData();
    return Response.ok(getFreemarker().build(JSON_DIR + "portfolio.ftl", out)).build();
  }

  private FlexiBean createPortfolioData() {
    final PortfolioDocument doc = data().getPortfolio();
    final PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.setPositionObjectIds(doc.getPortfolio().getRootNode().getPositionIds());
    final PositionSearchResult positionsResult = data().getPositionMaster().search(positionSearch);
    resolveSecurities(positionsResult.getPositions());

    final FlexiBean out = createRootData();
    out.put("positionsResult", positionsResult);
    out.put("positions", positionsResult.getPositions());
    return out;
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(@FormParam("name") String name, @FormParam("hidden") final Boolean isHidden) {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    final DocumentVisibility visibility = BooleanUtils.isTrue(isHidden) ? DocumentVisibility.HIDDEN : DocumentVisibility.VISIBLE;
    if (name == null) {
      final FlexiBean out = createRootData();
      out.put("err_nameMissing", true);
      final String html = getFreemarker().build(HTML_DIR + "portfolio-update.ftl", out);
      return Response.ok(html).build();
    }
    final URI uri = updatePortfolio(name, visibility, doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(@FormParam("name") String name, @FormParam("hidden") final Boolean isHidden) {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    name = StringUtils.trimToNull(name);
    final DocumentVisibility visibility = BooleanUtils.isTrue(isHidden) ? DocumentVisibility.HIDDEN : DocumentVisibility.VISIBLE;
    updatePortfolio(name, visibility, doc);
    return Response.ok().build();
  }

  private URI updatePortfolio(final String name, final DocumentVisibility visibility, PortfolioDocument doc) {
    doc.getPortfolio().setName(name);
    doc.setVisibility(visibility);
    doc = data().getPortfolioMaster().update(doc);
    data().setPortfolio(doc);
    final URI uri = MinimalWebPortfolioResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getPortfolioMaster().remove(doc.getUniqueId());
    final URI uri = MinimalWebPortfolioResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      data().getPortfolioMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final PortfolioDocument doc = data().getPortfolio();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("childNodes", doc.getPortfolio().getRootNode().getChildNodes());
    out.put("deleted", !doc.isLatest());
    out.put("rootNode", doc.getPortfolio().getRootNode());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("nodes")
  public MinimalWebPortfolioNodesResource findNodes() {
    return new MinimalWebPortfolioNodesResource(this);
  }

  @Path("versions")
  public MinimalWebPortfolioVersionsResource findVersions() {
    return new MinimalWebPortfolioVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overridePortfolioId  the override portfolio id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueId overridePortfolioId) {
    final String portfolioId = data.getBestPortfolioUriId(overridePortfolioId);
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPortfolioResource.class).build(portfolioId);
  }

}
