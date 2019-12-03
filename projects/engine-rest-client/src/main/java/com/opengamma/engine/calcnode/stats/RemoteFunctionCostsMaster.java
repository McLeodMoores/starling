/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides access to a remote {@link FunctionCostsMaster}.
 */
public class RemoteFunctionCostsMaster extends AbstractRemoteClient implements FunctionCostsMaster {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteFunctionCostsMaster(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument load(final String configurationName, final String functionId, final Instant versionAsOf) {
    ArgumentChecker.notNull(configurationName, "configurationName");
    ArgumentChecker.notNull(functionId, "functionId");

    final URI uri = DataFunctionCostsMasterUris.uriLoad(getBaseUri(), configurationName, functionId, versionAsOf);
    try {
      return accessRemote(uri).get(FunctionCostsDocument.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public FunctionCostsDocument store(final FunctionCostsDocument costs) {
    ArgumentChecker.notNull(costs, "costs");

    final URI uri = DataFunctionCostsMasterUris.uriStore(getBaseUri());
    return accessRemote(uri).post(FunctionCostsDocument.class, costs);
  }

}
