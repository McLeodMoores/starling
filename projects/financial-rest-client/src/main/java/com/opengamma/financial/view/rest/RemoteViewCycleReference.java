/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.rest.FudgeRestClient;

/**
 *
 */
public class RemoteViewCycleReference extends RemoteEngineResourceReference<ViewCycle> {

  public RemoteViewCycleReference(final URI baseUri, final ScheduledExecutorService scheduler) {
    super(baseUri, scheduler);
  }

  public RemoteViewCycleReference(final URI baseUri, final ScheduledExecutorService scheduler, final FudgeRestClient client) {
    super(baseUri, scheduler, client);
  }

  @Override
  protected ViewCycle getRemoteResource(final URI baseUri) {
    return new RemoteViewCycle(baseUri, getClient());
  }

}
