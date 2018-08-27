/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
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

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions")
public class WebPortfolioNodePositionsResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodePositionsResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @POST
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response postHTML(
      @FormParam("positionurl") String positionUrlStr) {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(new WebPortfolioNodeResource(this).getHTML()).build();
    }

    positionUrlStr = StringUtils.trimToNull(positionUrlStr);
    if (positionUrlStr == null) {
      final FlexiBean out = createRootData();
      out.put("err_positionUrlMissing", true);
      final String html = getFreemarker().build(HTML_DIR + "portfolionodepositions-add.ftl", out);
      return Response.ok(html).build();
    }
    UniqueId positionId = null;
    try {
      new URI(positionUrlStr);  // validates whole URI
      String uniqueIdStr = StringUtils.substringAfterLast(positionUrlStr, "/positions/");
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

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(@FormParam("uid") String uniqueIdStr) {
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(new WebPortfolioNodeResource(this).getHTML()).build();
    }
    uniqueIdStr = StringUtils.trimToNull(uniqueIdStr);
    if (uniqueIdStr == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    UniqueId positionId = null;
    try {
      positionId = UniqueId.parse(uniqueIdStr);
      data().getPositionMaster().get(positionId);  // validate position exists
    } catch (final Exception ex) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    final URI uri = addPosition(doc, positionId);
    return Response.created(uri).build();
  }

  private URI addPosition(PortfolioDocument doc, final UniqueId positionId) {
    final ManageablePortfolioNode node = data().getNode();
    final URI uri = WebPortfolioNodeResource.uri(data());  // lock URI before updating data()
    if (node.getPositionIds().contains(positionId) == false) {
      node.addPosition(positionId);
      doc = data().getPortfolioMaster().update(doc);
      data().setPortfolio(doc);
    }
    return uri;
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public WebPortfolioNodePositionResource findNode(@PathParam("positionId") final String idStr) {
    data().setUriPositionId(idStr);
    return new WebPortfolioNodePositionResource(this);
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
    final ManageablePortfolioNode node = data().getNode();
    out.put("portfolioDoc", doc);
    out.put("portfolio", doc.getPortfolio());
    out.put("parentNode", data().getParentNode());
    out.put("node", node);
    out.put("childNodes", node.getChildNodes());
    out.put("deleted", !doc.isLatest());
    return out;
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
   * @param overrideNodeId  the override node id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final UniqueId overrideNodeId) {
    final String portfolioId = data.getBestPortfolioUriId(null);
    final String nodeId = data.getBestNodeUriId(overrideNodeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodePositionsResource.class).build(portfolioId, nodeId);
  }

}
