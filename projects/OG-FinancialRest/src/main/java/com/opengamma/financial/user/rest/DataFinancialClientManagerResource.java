/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.financial.user.FinancialClient;
import com.opengamma.financial.user.FinancialClientManager;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the clients of a single user.
 * <p>
 * This resource receives and processes RESTful calls.
 */
public class DataFinancialClientManagerResource extends AbstractDataResource {

  /**
   * The client manager.
   */
  private final FinancialClientManager _clientManager;

  /**
   * Creates an instance.
   * 
   * @param clientManager  the client manager, not null
   */
  public DataFinancialClientManagerResource(FinancialClientManager clientManager) {
    _clientManager = clientManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the manager.
   * 
   * @return the manager, not null
   */
  public FinancialClientManager getClientManager() {
    return _clientManager;
  }

  //-------------------------------------------------------------------------
  @Path("{clientName}")
  public DataFinancialClientResource findClient(@PathParam("clientName") String clientName) {
    ArgumentChecker.notNull(clientName, "clientName");
    
    FinancialClient client = getClientManager().getOrCreateClient(clientName);
    return new DataFinancialClientResource(client);
  }

}
