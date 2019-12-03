/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions")
public class MinimalWebPortfolioNodePositionsResource extends AbstractMinimalWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public MinimalWebPortfolioNodePositionsResource(final AbstractMinimalWebPortfolioResource parent) {
    super(parent);
  }

  /**
   * Adds a position to the latest version of a portfolio.
   * @param positionUrlStr  the position URL
   * @return  a response
   */
  @POST
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response postHTML(@FormParam("positionurl") final String positionUrlStr) {
    final PortfolioDocument doc = data().getPortfolio();
    if (!doc.isLatest()) {
      return Response.status(Status.FORBIDDEN).entity(new MinimalWebPortfolioNodeResource(this).getHTML()).build();
    }
    final String trimmedPositionUrlStr = StringUtils.trimToNull(positionUrlStr);
    if (trimmedPositionUrlStr == null) {
      final FlexiBean out = createRootData();
      out.put("err_positionUrlMissing", true);
      final String html = getFreemarker().build(HTML_DIR + "portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    UniqueId positionId = null;
    try {
      new URI(trimmedPositionUrlStr); // validates whole URI
      String uniqueIdStr = StringUtils.substringAfterLast(trimmedPositionUrlStr, "/positions/");
      uniqueIdStr = StringUtils.substringBefore(uniqueIdStr, "/");
      positionId = UniqueId.parse(uniqueIdStr);
      data().getPositionMaster().get(positionId);  // validate position exists
    } catch (final Exception ex) {
      final FlexiBean out = createRootData();
      out.put("err_positionUrlInvalid", true);
      final String html = getFreemarker().build(HTML_DIR + "portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    final URI uri = addPosition(doc, positionId);
    return Response.seeOther(uri).build();
  }

  /**
   * Adds a position to the latest version of a portfolio.
   * @param uniqueIdStr  the position unique id
   * @return  a response
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("uid") final String uniqueIdStr) {
    final PortfolioDocument doc = data().getPortfolio();
    if (!doc.isLatest()) {
      return Response.status(Status.FORBIDDEN).entity(new MinimalWebPortfolioNodeResource(this).getHTML()).build();
    }
    final String trimmedUniqueIdStr = StringUtils.trimToNull(uniqueIdStr);
    if (trimmedUniqueIdStr == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    UniqueId positionId = null;
    try {
      positionId = UniqueId.parse(trimmedUniqueIdStr);
      data().getPositionMaster().get(positionId);  // validate position exists
    } catch (final Exception ex) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    final URI uri = addPosition(doc, positionId);
    return Response.created(uri).build();
  }

  /**
   * Adds a position to the portfolio.
   * @param doc  the portfolio
   * @param positionId  the position id
   * @return  the URI of the portfolio node
   */
  private URI addPosition(final PortfolioDocument doc, final UniqueId positionId) {
    final ManageablePortfolioNode node = data().getNode();
    final ObjectId objectId = positionId.getObjectId();
    final URI uri = MinimalWebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    if (!node.getPositionIds().contains(objectId)) {
      node.addPosition(positionId);
      data().setPortfolio(data().getPortfolioMaster().update(doc));
    }
    return uri;
  }

  /**
   * Finds the node for a position.
   * @param idStr  the position id
   * @return  a web resource for all positions in a node
   */
  @Path("{positionId}")
  public MinimalWebPortfolioNodePositionResource findNode(@PathParam("positionId") final String idStr) {
    data().setUriPositionId(idStr);
    return new MinimalWebPortfolioNodePositionResource(this);
  }

  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final PortfolioDocument doc = data().getPortfolio();
    final ManageablePortfolioNode node = data().getNode();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("parentNode", data().getParentNode());
    out.put("node", node);
    out.put("childNodes", node.getChildNodes());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return  the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideNodeId  the override node id, null uses information from data
   * @return  the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueId overrideNodeId) {
    final String portfolioId = data.getBestPortfolioUriId(null);
    final String nodeId = data.getBestNodeUriId(overrideNodeId);
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPortfolioNodePositionsResource.class).build(portfolioId, nodeId);
  }

}
