/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for {@link DependencyGraphExplorer}.
 */
public class DataDependencyGraphExplorerResource extends AbstractDataResource {

  // CSOFF: just constants
  public static final String PATH_WHOLE_GRAPH = "wholeGraph";
  public static final String PATH_SUBGRAPH_PRODUCING = "subgraphProducing";
  // CSON: just constants

  private final DependencyGraphExplorer _explorer;

  public DataDependencyGraphExplorerResource(DependencyGraphExplorer explorer) {
    ArgumentChecker.notNull(explorer, "explorer");
    _explorer = explorer;
  }

  @Path(PATH_WHOLE_GRAPH)
  @GET
  public Response getWholeGraph() {
    return responseOkObject(_explorer.getWholeGraph());
  }

  @Path(PATH_SUBGRAPH_PRODUCING)
  @GET
  public Response getSubgraphProducing(@QueryParam("msg") String msgBase64) {
    ValueSpecification output = RestUtils.decodeBase64(ValueSpecification.class, msgBase64);
    return responseOkObject(_explorer.getSubgraphProducing(output));
  }

}
