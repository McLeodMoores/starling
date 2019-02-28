/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.AnalyticsViewManager;
import com.opengamma.web.analytics.ErrorInfo;
import com.opengamma.web.analytics.GridCell;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.MarketDataSpecificationJsonReader;
import com.opengamma.web.analytics.ValueRequirementTargetForCell;
import com.opengamma.web.analytics.ViewRequest;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.analytics.json.ValueRequirementFormParam;
import com.opengamma.web.analytics.push.ClientConnection;
import com.opengamma.web.analytics.push.ConnectionManager;

/**
 * REST resource for the analytics grid. This resource class specifies the endpoints of every object in the
 * hierarchy of grids, dependency graphs and viewports in the analytics viewer.
 */
@Path("views")
public class WebUiResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebUiResource.class);
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

  /** For generating IDs for the views. */
  private static final AtomicLong NEXT_VIEW_ID = new AtomicLong(0);
  /** For generating IDs for the viewports and dependency graphs. */
  private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

  /** For creating and retrieving views. */
  private final AnalyticsViewManager _viewManager;
  /** For looking up a client's connection. */
  private final ConnectionManager _connectionManager;

  public WebUiResource(final AnalyticsViewManager viewManager, final ConnectionManager connectionManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(connectionManager, "connectionManager");
    _viewManager = viewManager;
    _connectionManager = connectionManager;
  }

  @POST
  public Response createView(@Context final UriInfo uriInfo,
      @Context final HttpServletRequest httpRequest,
      @FormParam("requestId") final String requestId,
      @FormParam("viewDefinitionId") final String viewDefinitionId,
      @FormParam("aggregators") final List<String> aggregators,
      @FormParam("marketDataProviders") final String marketDataProviders,
      @FormParam("valuationTime") final String valuationTime,
      @FormParam("portfolioVersionTime") final String portfolioVersionTime,
      @FormParam("portfolioCorrectionTime") final String portfolioCorrectionTime,
      @FormParam("clientId") final String clientId,
      @FormParam("blotter") final Boolean blotter) {
    ArgumentChecker.notEmpty(requestId, "requestId");
    ArgumentChecker.notEmpty(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(aggregators, "aggregators");
    ArgumentChecker.notEmpty(marketDataProviders, "marketDataProviders");
    ArgumentChecker.notEmpty(clientId, "clientId");
    final boolean blotterColumns = blotter == null ? false : blotter;
    final List<MarketDataSpecification> marketDataSpecs =
        MarketDataSpecificationJsonReader.buildSpecifications(marketDataProviders);
    final VersionCorrection versionCorrection = VersionCorrection.of(parseInstant(portfolioVersionTime),
        parseInstant(portfolioCorrectionTime));
    final ViewRequest viewRequest = _viewManager.createViewRequest(UniqueId.parse(viewDefinitionId), aggregators, marketDataSpecs,
        parseInstant(valuationTime), versionCorrection, blotterColumns);
    final String viewId = Long.toString(NEXT_VIEW_ID.getAndIncrement());
    final URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("portfolio")
        .build();
    final URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("primitives")
        .build();

    final String userName = AuthUtils.isPermissive() ? null : AuthUtils.getUserName();
    final ClientConnection connection = _connectionManager.getConnectionByClientId(userName, clientId);
    final URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    final ImmutableMap<String, Object> callbackMap =
        ImmutableMap.<String, Object>of("id", requestId, "message", uri.getPath());
    final URI errorUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("errors")
        .build();
    // Get session id or create one
    final String sessionId = "session-id:" + httpRequest.getSession().getId();
    // Track user principal using session id rather than ip address
    final UserPrincipal ogUserPrincipal = userName != null ? new UserPrincipal(userName, sessionId) : UserPrincipal.getTestUser();
    _viewManager.createView(viewRequest, clientId, ogUserPrincipal, connection, viewId, callbackMap,
        portfolioGridUri.getPath(), primitivesGridUri.getPath(), errorUri.getPath());
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}")
  @DELETE
  public void deleteView(@PathParam("viewId") final String viewId) {
    _viewManager.deleteView(viewId);
  }

  @Path("{viewId}/pauseOrResume")
  @PUT
  public Response pauseOrResumeView(@PathParam("viewId") final String viewId,
      @FormParam("state") final String state) {
    final ViewClient viewClient = _viewManager.getViewCient(viewId);
    String trimmedState = StringUtils.stripToNull(state);
    Response response = Response.status(Response.Status.BAD_REQUEST).build();
    if (trimmedState != null) {
      final ViewClientState currentState = viewClient.getState();
      trimmedState = trimmedState.toUpperCase();
      switch (trimmedState) {
        case "PAUSE":
        case "P":
          if (currentState != ViewClientState.TERMINATED) {
            viewClient.pause();
            response = Response.ok().build();
          }
          break;
        case "RESUME":
        case "R":
          if (currentState != ViewClientState.TERMINATED) {
            viewClient.resume();
            response = Response.ok().build();
          }
          break;
        default:
          LOGGER.warn("client {} requesting for invalid view client state change to {}", viewId, state);
          response = Response.status(Response.Status.BAD_REQUEST).build();
          break;
      }
    }
    return response;
  }

  @Path("{viewId}/{gridType}")
  @GET
  public GridStructure getGridStructure(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType) {
    return _viewManager.getView(viewId).getInitialGridStructure(gridType(gridType));
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}/valuereq/{row}/{col}")
  @GET
  public ValueRequirementTargetForCell getValueRequirementForTargetForCell(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("row") final int row,
      @PathParam("col") final int col,
      @PathParam("viewportId") final int viewportId) {

    final GridStructure gridStructure =  _viewManager.getView(viewId).getGridStructure(gridType(gridType), viewportId);

    final Pair<String, ValueRequirement> pair = gridStructure.getValueRequirementForCell(row, col);
    return new ValueRequirementTargetForCell(pair.getFirst(), pair.getSecond());

  }

  @Path("{viewId}/{gridType}/viewports")
  @POST
  public Response createViewport(@Context final UriInfo uriInfo,
      @PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @FormParam("requestId") final int requestId,
      @FormParam("version") final int version,
      @FormParam("rows") final List<Integer> rows,
      @FormParam("columns") final List<Integer> columns,
      @FormParam("cells") final List<GridCell> cells,
      @FormParam("format") final TypeFormatter.Format format,
      @FormParam("enableLogging") final Boolean enableLogging) {
    final ViewportDefinition viewportDefinition = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    final int viewportId = NEXT_ID.getAndIncrement();
    final String viewportIdStr = Integer.toString(viewportId);
    final UriBuilder viewportUriBuilder = uriInfo.getAbsolutePathBuilder().path(viewportIdStr);
    final String callbackId = viewportUriBuilder.build().getPath();
    final String structureCallbackId = viewportUriBuilder.path("structure").build().getPath();
    _viewManager.getView(viewId).createViewport(requestId,
        gridType(gridType),
        viewportId,
        callbackId,
        structureCallbackId,
        viewportDefinition);
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @PUT
  public void updateViewport(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("viewportId") final int viewportId,
      @FormParam("version") final int version,
      @FormParam("rows") final List<Integer> rows,
      @FormParam("columns") final List<Integer> columns,
      @FormParam("cells") final List<GridCell> cells,
      @FormParam("format") final TypeFormatter.Format format,
      @FormParam("enableLogging") final Boolean enableLogging) {
    final ViewportDefinition viewportDef = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    _viewManager.getView(viewId).updateViewport(gridType(gridType), viewportId, viewportDef);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}/structure")
  @GET
  public GridStructure getViewportGridStructure(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("viewportId") final int viewportId) {
    return _viewManager.getView(viewId).getGridStructure(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @GET
  public ViewportResults getViewportData(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("viewportId") final int viewportId) {
    return _viewManager.getView(viewId).getData(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @DELETE
  public void deleteViewport(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("viewportId") final int viewportId) {
    _viewManager.getView(viewId).deleteViewport(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/depgraphs")
  @POST
  public Response openDependencyGraph(@Context final UriInfo uriInfo,
      @PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @FormParam("requestId") final int requestId,
      @FormParam("row") final Integer row,
      @FormParam("col") final Integer col,
      @FormParam("colset") final String calcConfigName,
      @FormParam("req") final ValueRequirementFormParam valueRequirementParam) {
    final int graphId = NEXT_ID.getAndIncrement();
    final String graphIdStr = Integer.toString(graphId);
    final URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    final String callbackId = graphUri.getPath();
    if (row != null && col != null) {
      _viewManager.getView(viewId).openDependencyGraph(requestId, gridType(gridType), graphId, callbackId, row, col);
    } else if (calcConfigName != null && valueRequirementParam != null) {
      final ValueRequirement valueRequirement = valueRequirementParam.getValueRequirement();
      _viewManager.getView(viewId).openDependencyGraph(requestId,
          gridType(gridType),
          graphId,
          callbackId,
          calcConfigName,
          valueRequirement);
    }
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}")
  @GET
  public GridStructure getDependencyGraphGridStructure(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId) {
    return _viewManager.getView(viewId).getInitialGridStructure(gridType(gridType), depgraphId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}")
  @DELETE
  public void deleteDependencyGraph(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId) {
    _viewManager.getView(viewId).closeDependencyGraph(gridType(gridType), depgraphId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports")
  @POST
  public Response createDependencyGraphViewport(@Context final UriInfo uriInfo,
      @PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId,
      @FormParam("requestId") final int requestId,
      @FormParam("version") final int version,
      @FormParam("rows") final List<Integer> rows,
      @FormParam("columns") final List<Integer> columns,
      @FormParam("cells") final List<GridCell> cells,
      @FormParam("format") final TypeFormatter.Format format,
      @FormParam("enableLogging") final Boolean enableLogging) {
    final ViewportDefinition viewportDefinition = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    final int viewportId = NEXT_ID.getAndIncrement();
    final String viewportIdStr = Integer.toString(viewportId);
    final UriBuilder viewportUriBuilder = uriInfo.getAbsolutePathBuilder().path(viewportIdStr);
    final String callbackId = viewportUriBuilder.build().getPath();
    final String structureCallbackId = viewportUriBuilder.path("structure").build().getPath();
    _viewManager.getView(viewId).createViewport(requestId,
        gridType(gridType),
        depgraphId,
        viewportId,
        callbackId,
        structureCallbackId,
        viewportDefinition);
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @PUT
  public void updateDependencyGraphViewport(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId,
      @PathParam("viewportId") final int viewportId,
      @FormParam("version") final int version,
      @FormParam("rows") final List<Integer> rows,
      @FormParam("columns") final List<Integer> columns,
      @FormParam("cells") final List<GridCell> cells,
      @FormParam("format") final TypeFormatter.Format format,
      @FormParam("enableLogging") final Boolean enableLogging) {
    final ViewportDefinition viewportDef = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    _viewManager.getView(viewId).updateViewport(gridType(gridType), depgraphId, viewportId, viewportDef);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}/structure")
  @GET
  public GridStructure getDependencyGraphViewportGridStructure(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId,
      @PathParam("viewportId") final int viewportId) {
    final GridStructure g = _viewManager.getView(viewId).getGridStructure(gridType(gridType), depgraphId, viewportId);
    return g;
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @GET
  public ViewportResults getDependencyGraphViewportData(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId,
      @PathParam("viewportId") final int viewportId) {
    return _viewManager.getView(viewId).getData(gridType(gridType), depgraphId, viewportId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @DELETE
  public void deleteDependencyGraphViewport(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridType,
      @PathParam("depgraphId") final int depgraphId,
      @PathParam("viewportId") final int viewportId) {
    _viewManager.getView(viewId).deleteViewport(gridType(gridType), depgraphId, viewportId);
  }

  @Path("{viewId}/errors")
  @GET
  public List<ErrorInfo> getErrors(@PathParam("viewId") final String viewId) {
    return _viewManager.getView(viewId).getErrors();
  }


  @Path("{viewId}/errors/{errorId}")
  @DELETE
  public void deleteError(@PathParam("viewId") final String viewId, @PathParam("errorId") final long errorId) {
    _viewManager.getView(viewId).deleteError(errorId);
  }

  /**
   * Produces view port results as CSV
   *
   * @param response the injected servlet response, not null.
   * @param viewId ID of the view
   * @param gridTypeStr the grid type, 'portfolio' or 'primitives'
   * @return The view port result as csv
   */
  @GET
  @Path("{viewId}/{gridType}/data")
  @Produces(RestUtils.TEXT_CSV)
  public ViewportResults getViewportResultAsCsv(@PathParam("viewId") final String viewId,
      @PathParam("gridType") final String gridTypeStr,
      @Context final HttpServletResponse response) {
    final AnalyticsView view = _viewManager.getView(viewId);
    final AnalyticsView.GridType gridType = gridType(gridTypeStr);
    final ViewportResults result = view.getAllGridData(gridType, TypeFormatter.Format.CELL);
    Instant valuationTime;
    if (result.getValuationTime() != null) {
      valuationTime = result.getValuationTime();
    } else {
      valuationTime = OpenGammaClock.getInstance().instant();
    }
    final LocalDateTime time = LocalDateTime.ofInstant(valuationTime, OpenGammaClock.getZone());

    final String filename = String.format("%s-%s-%s.csv",
        view.getViewDefinitionId(),
        gridType.name().toLowerCase(),
        time.format(CSV_TIME_FORMAT));
    response.addHeader("content-disposition", "attachment; filename=\"" + filename + "\"");
    return view.getAllGridData(gridType, TypeFormatter.Format.CELL);
  }

  /**
   * @param instantString An ISO-8601 string representing an instant or null
   * @return The parsed string or null if the input is null
   */
  private static Instant parseInstant(final String instantString) {
    if (instantString == null) {
      return null;
    }
    try {
      return Instant.parse(instantString);
    } catch (final DateTimeParseException e) {
      // parse YYYY-MM-DDTHH:mmZ - Instant.parse requires seconds as well
      final int length = instantString.length();
      if (length == 17 && instantString.charAt(length - 1) == 'Z') {
        return Instant.parse(instantString.substring(0, length - 1) + ":00Z");
      }
      throw e;
    }
  }

  private static AnalyticsView.GridType gridType(final String gridType) {
    return AnalyticsView.GridType.valueOf(gridType.toUpperCase());
  }
}
