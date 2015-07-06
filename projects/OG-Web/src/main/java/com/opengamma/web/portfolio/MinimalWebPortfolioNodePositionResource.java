/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * RESTful resource for all positions in a node.
 */
@Path("/portfolios/{portfolioId}/nodes/{nodeId}/positions/{positionId}")
public class MinimalWebPortfolioNodePositionResource extends AbstractMinimalWebPortfolioResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public MinimalWebPortfolioNodePositionResource(final AbstractMinimalWebPortfolioResource parent) {
    super(parent);
  }

  /**
   * Deletes a position from the latest version of the portfolio node. Throws {@link DataNotFoundException} if the position is not
   * found in the node.
   * @return  a redirection response to the portfolio node
   */
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final ObjectId positionId = ObjectId.parse(data().getUriPositionId());
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      final ManageablePortfolioNode node = data().getNode();
      if (!node.getPositionIds().remove(positionId)) {
        throw new DataNotFoundException("Position id not found: " + positionId);
      }
      data().getPortfolioMaster().update(doc);
    }
    return Response.seeOther(MinimalWebPortfolioNodeResource.uri(data())).build();
  }

  /**
   * Deletes a position from the latest version of the portfolio node. Throws {@link DataNotFoundException} if the position is not
   * found in the node.
   * @return  an OK response
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final ObjectId positionId = ObjectId.parse(data().getUriPositionId());
    final PortfolioDocument doc = data().getPortfolio();
    if (doc.isLatest()) {
      final ManageablePortfolioNode node = data().getNode();
      if (!node.getPositionIds().remove(positionId)) {
        throw new DataNotFoundException("Position id not found: " + positionId);
      }
      data().getPortfolioMaster().update(doc);
    }
    return Response.ok().build();
  }

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
   * @param overridePositionId  the override node id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebPortfoliosData data, final ObjectIdentifiable overridePositionId) {
    final String portfolioId = data.getBestPortfolioUriId(null);
    final String nodeId = data.getBestNodeUriId(null);
    final String positionId = overridePositionId.getObjectId().toString();
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPortfolioNodePositionResource.class).build(portfolioId, nodeId, positionId);
  }

}
