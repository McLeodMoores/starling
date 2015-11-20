/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;
import java.util.Stack;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all nodes in a portfolio.
 */
@Path("/portfolios/{portfolioId}/nodes")
public class WebPortfolioNodesResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebPortfolioNodesResource(final AbstractWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Path("{nodeId}")
  public WebPortfolioNodeResource findNode(@PathParam("nodeId") String idStr) {
    data().setUriNodeId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    PortfolioDocument portfolioDoc = data().getPortfolio();
    Stack<ManageablePortfolioNode> nodes = portfolioDoc.getPortfolio().getRootNode().findNodeStackByObjectId(oid);
    if (nodes.isEmpty()) {
      throw new DataNotFoundException("PortfolioNode not found: " + idStr);
    }
    data().setNode(nodes.pop());
    if (nodes.size() > 0) {
      data().setParentNode(nodes.pop());
    }
    return new WebPortfolioNodeResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    String portfolioId = data.getBestPortfolioUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebPortfolioNodesResource.class).build(portfolioId);
  }

}
