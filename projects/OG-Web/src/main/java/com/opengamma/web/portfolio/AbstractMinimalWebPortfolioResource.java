/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.util.concurrent.ExecutorService;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.web.position.MinimalWebPositionsUris;
import com.opengamma.web.position.WebPositionsData;
import com.opengamma.web.security.MinimalWebSecuritiesUris;
import com.opengamma.web.security.WebSecuritiesData;

/**
 * Abstract base class for RESTful portfolio resources used when a minimal configuration is required.
 */
public abstract class AbstractMinimalWebPortfolioResource extends AbstractWebPortfolioResource {

  /**
   * Creates the resource.
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param executor  the executor service, not null
   */
  protected AbstractMinimalWebPortfolioResource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final SecuritySource securitySource,
      final ExecutorService executor) {
    super(portfolioMaster, positionMaster, securitySource, executor);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractMinimalWebPortfolioResource(final AbstractMinimalWebPortfolioResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return  the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    out.put("uris", new MinimalWebPortfoliosUris(data()));
    final WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new MinimalWebSecuritiesUris(secData));
    final WebPositionsData posData = new WebPositionsData(data().getUriInfo());
    out.put("positionUris", new MinimalWebPositionsUris(posData));
    return out;
  }
}
