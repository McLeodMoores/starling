/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link MarketDataSnapshotter}.
 */
public class DataMarketDataSnapshotterResource extends AbstractDataResource {

  //CSOFF: just constants
  public static final String PATH_CREATE_SNAPSHOT = "create";
  public static final String PATH_YIELD_CURVE_SPECS = "yieldCurveSpecs";
  //CSON: just constants

  private final ViewProcessor _viewProcessor;
  private final MarketDataSnapshotter _snapshotter;

  public DataMarketDataSnapshotterResource(final ViewProcessor viewProcessor, final MarketDataSnapshotter snapshotter) {
    _viewProcessor = viewProcessor;
    _snapshotter = snapshotter;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path(PATH_CREATE_SNAPSHOT + "/{viewClientId}" + "/{viewCycleId}")
  public Response createSnapshot(@PathParam("viewClientId") final String viewClientIdString, @PathParam("viewCycleId") final String viewCycleIdString) {
    final UniqueId viewClientId = UniqueId.parse(viewClientIdString);
    final UniqueId viewCycleId = UniqueId.parse(viewCycleIdString);
    final ViewClient client = _viewProcessor.getViewClient(viewClientId);
    final EngineResourceReference<? extends ViewCycle> cycleReference = client.createCycleReference(viewCycleId);
    if (cycleReference == null) {
      throw new IllegalArgumentException("Cycle is not available");
    }
    try {
      final StructuredMarketDataSnapshot result = _snapshotter.createSnapshot(client, cycleReference.get());
      return responseOkObject(result);
    } finally {
      cycleReference.release();
    }
  }

  @GET
  @Path(PATH_YIELD_CURVE_SPECS + "/{viewClientId}" + "/{viewCycleId}")
  public Response getYieldCurveSpecs(@PathParam("viewClientId") final String viewClientIdString, @PathParam("viewCycleId") final String viewCycleIdString) {
    final UniqueId viewClientId = UniqueId.parse(viewClientIdString);
    final UniqueId viewCycleId = UniqueId.parse(viewCycleIdString);
    final ViewClient client = _viewProcessor.getViewClient(viewClientId);
    final EngineResourceReference<? extends ViewCycle> cycleReference = client.createCycleReference(viewCycleId);

    if (cycleReference == null) {
      throw new IllegalArgumentException("Cycle is not available");
    }
    try {
      final Map<YieldCurveKey, Map<String, ValueRequirement>> result = _snapshotter.getYieldCurveSpecifications(client, cycleReference.get());
      return responseOkObject(result);
    } finally {
      cycleReference.release();
    }
  }

  @GET
  public Response get() {
    return responseOk("Snapshotter");
  }

}
