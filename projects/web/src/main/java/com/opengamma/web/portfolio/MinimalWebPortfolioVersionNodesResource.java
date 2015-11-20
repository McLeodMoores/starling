/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * RESTful resource for all nodes in a portfolio version.
 */
@Path("/portfolios/{portfolioId}/versions/{versionId}/nodes")
public class MinimalWebPortfolioVersionNodesResource extends MinimalWebPortfolioNodesResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public MinimalWebPortfolioVersionNodesResource(final AbstractMinimalWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Override
  @Path("{nodeId}")
  public MinimalWebPortfolioNodeResource findNode(@PathParam("nodeId") final String idStr) {
    data().setUriNodeId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    final PortfolioDocument portfolioDoc = data().getVersioned();
    final Stack<ManageablePortfolioNode> nodes = portfolioDoc.getPortfolio().getRootNode().findNodeStackByObjectId(oid);
    if (nodes.isEmpty()) {
      throw new DataNotFoundException("PortfolioNode not found: " + idStr);
    }
    data().setNode(nodes.pop());
    if (nodes.size() > 0) {
      data().setParentNode(nodes.pop());
    }
    return new MinimalWebPortfolioVersionNodeResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data) {
    final String portfolioId = data.getBestPortfolioUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPortfolioNodesResource.class).build(portfolioId);
  }

}
