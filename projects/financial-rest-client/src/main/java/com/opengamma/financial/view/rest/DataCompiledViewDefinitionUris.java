/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;

/**
 * RESTful URIs for {@link CompiledViewDefinitionWithGraphsImpl}.
 */
public class DataCompiledViewDefinitionUris {

  //CSOFF: just constants
  public static final String PATH_VIEW_DEFINITION = "viewDefinition";
  public static final String PATH_PORTFOLIO = "portfolio";
  public static final String PATH_VALID_FROM = "validFrom";
  public static final String PATH_VALID_TO = "validTo";
  public static final String PATH_MARKET_DATA_REQUIREMENTS = "marketDataRequirements";
  public static final String PATH_COMPUTATION_TARGETS = "computationTargets";
  public static final String PATH_COMPILED_CALCULATION_CONFIGURATIONS = "compiledCalculationConfigurations";
  public static final String PATH_COMPILED_CALCULATION_CONFIGURATIONS_MAP = "compiledCalculationConfigurationsMap";
  public static final String PATH_GRAPHS = "graphs";
  //CSON: just constants
 
  public static URI uriCompiledCalculationConfiguration(URI baseUri, String calcConfigName) {
    return UriBuilder.fromUri(baseUri).segment(calcConfigName).build();
  }

}
