/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *  
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.threeten.bp.Clock;

import com.opengamma.master.portfolio.impl.DataPortfolioMasterResource;
import com.opengamma.masterdb.TimeOverrideRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for portfolios.
 * <p>
 * The portfolios resource receives and processes RESTful calls to a database portfolio master.
 */
@Path("portfolioMaster")
public class DataDbPortfolioMasterResource extends DataPortfolioMasterResource {

  /**
   * The portfolio master.
   */
  private final DbPortfolioMaster _dbPortfolioMaster;
  
  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param dbPortfolioMaster  the underlying database portfolio master, not null
   */
  public DataDbPortfolioMasterResource(final DbPortfolioMaster dbPortfolioMaster) {
    super(dbPortfolioMaster);
    _dbPortfolioMaster = dbPortfolioMaster;
  }
  
  //-------------------------------------------------------------------------
  public DbPortfolioMaster getDbPortfolioMaster() {
    return _dbPortfolioMaster;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path("timeOverride")
  public Response setTimeOverride(final TimeOverrideRequest doc) {
    ArgumentChecker.notNull(doc, "doc");
    if (doc.getTimeOverride() == null) {
      getDbPortfolioMaster().resetClock();
    } else {
      getDbPortfolioMaster().setClock(Clock.fixed(doc.getTimeOverride(), getDbPortfolioMaster().getClock().getZone()));
    }
    return responseOk();
  }

}
