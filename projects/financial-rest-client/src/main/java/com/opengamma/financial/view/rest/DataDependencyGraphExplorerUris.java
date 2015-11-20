/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful URIs for {@link DependencyGraphExplorer}.
 */
public class DataDependencyGraphExplorerUris {

  // CSOFF: just constants
  public static final String PATH_WHOLE_GRAPH = "wholeGraph";
  public static final String PATH_SUBGRAPH_PRODUCING = "subgraphProducing";
  // CSON: just constants

  public static URI uriSubgraph(URI baseUri, ValueSpecification output) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(PATH_SUBGRAPH_PRODUCING);
    if (output != null) {
      bld.queryParam("msg", RestUtils.encodeBase64(output));
    }
    return bld.build();
  }

  public static URI uriWholeGraph(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(DataDependencyGraphExplorerUris.PATH_WHOLE_GRAPH);
    URI uriWholeGraph = bld.build();
    return uriWholeGraph;
  }

}
