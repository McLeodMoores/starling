/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.threeten.bp.Clock;

import com.opengamma.master.security.impl.DataSecurityMasterResource;
import com.opengamma.masterdb.TimeOverrideRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for securities.
 * <p>
 * The securities resource receives and processes RESTful calls to a database security master.
 */
@Path("securityMaster")
public class DataDbSecurityMasterResource extends DataSecurityMasterResource {

  /**
   * The security master.
   */
  private final DbSecurityMaster _dbSecurityMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param dbSecurityMaster  the underlying database security master, not null
   */
  public DataDbSecurityMasterResource(final DbSecurityMaster dbSecurityMaster) {
    super(dbSecurityMaster);
    _dbSecurityMaster = dbSecurityMaster;
  }
  
  //-------------------------------------------------------------------------
  public DbSecurityMaster getDbSecurityMaster() {
    return _dbSecurityMaster;
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Path("timeOverride")
  public Response setTimeOverride(final TimeOverrideRequest doc) {
    ArgumentChecker.notNull(doc, "doc");
    if (doc.getTimeOverride() == null) {
      getDbSecurityMaster().resetClock();
    } else {
      getDbSecurityMaster().setClock(Clock.fixed(doc.getTimeOverride(), getDbSecurityMaster().getClock().getZone()));
    }
    return responseOk();
  }
  
}
