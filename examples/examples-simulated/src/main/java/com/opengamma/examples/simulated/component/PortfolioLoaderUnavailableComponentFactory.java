/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.component;

import java.util.LinkedHashMap;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.examples.simulated.web.PortfolioLoaderUnavailableResource;

/**
 * Component factory for a placeholder REST resource for the portfolio upload.
 */
public class PortfolioLoaderUnavailableComponentFactory extends AbstractComponentFactory {

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    repo.getRestComponents().publishResource(new PortfolioLoaderUnavailableResource());
  }
}
