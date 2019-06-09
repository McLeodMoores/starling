/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.annotations.VisibleForTesting;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.provider.DependencyGraphTraceProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Expose a simple dependency graph building service over the network for debugging/diagnostic purposes. This is intended to be simple to access using hand
 * written URLs - there is not currently a corresponding programatic interface to the service this provides.
 * <p>
 * For example to find out why a graph building configuration can't satisfy a requirement, a URL such as "/value/Present Value/SECURITY/SecDb~1234" will return
 * the failure trace (or the graph if successful).
 */
public final class DependencyGraphTraceProviderResource extends AbstractDataResource {

  private final DependencyGraphTraceProvider _provider;

  private final FudgeContext _fudgeContext;

  private final DependencyGraphTraceBuilderProperties _properties;

  public DependencyGraphTraceProviderResource(final DependencyGraphTraceProvider provider, final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
    _provider = provider;
    _properties = new DependencyGraphTraceBuilderProperties();
  }

  private DependencyGraphTraceProviderResource(final DependencyGraphTraceProvider provider, final FudgeContext fudgeContext,
      final DependencyGraphTraceBuilderProperties properties) {
    _fudgeContext = fudgeContext;
    _provider = provider;
    _properties = properties;
  }

  @VisibleForTesting
  FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @VisibleForTesting
  DependencyGraphTraceProvider getProvider() {
    return _provider;
  }

  @VisibleForTesting
  DependencyGraphTraceBuilderProperties getProperties() {
    return _properties;
  }

  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @Path("valuationTime/{valuationTime}")
  public DependencyGraphTraceProviderResource setValuationTime(@PathParam("valuationTime") final String valuationTime) {
    final Instant parsedValuationTime = Instant.parse(valuationTime);
    final DependencyGraphTraceBuilderProperties properties = _properties.valuationTime(parsedValuationTime);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("resolutionTime/{resolutionTime}")
  public DependencyGraphTraceProviderResource setResolutionTime(@PathParam("resolutionTime") final String resolutionTime) {
    final VersionCorrection parsedResolutionTime = VersionCorrection.parse(resolutionTime);
    final DependencyGraphTraceBuilderProperties properties = _properties.resolutionTime(parsedResolutionTime);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("calculationConfigurationName/{calculationConfigurationName}")
  public DependencyGraphTraceProviderResource setCalculationConfigurationName(
      @PathParam("calculationConfigurationName") final String calculationConfigurationName) {
    final DependencyGraphTraceBuilderProperties properties = _properties.calculationConfigurationName(calculationConfigurationName);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("defaultProperties/{defaultProperties}")
  public DependencyGraphTraceProviderResource setDefaultProperties(@PathParam("defaultProperties") final String defaultProperties) {
    final ValueProperties valueProperties = ValueProperties.parse(defaultProperties);
    final DependencyGraphTraceBuilderProperties properties = _properties.defaultProperties(valueProperties);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("value/{valueName}/{targetType}/{targetId}")
  public DependencyGraphTraceProviderResource setValueRequirementByUniqueId(@PathParam("valueName") final String valueName,
      @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    final UniqueId uniqueId = UniqueId.parse(targetId);
    final ValueRequirement valueRequirement = toValueRequirement(valueName,
        new ComputationTargetSpecification(ComputationTargetType.parse(targetType), uniqueId));
    final DependencyGraphTraceBuilderProperties properties = _properties.addRequirement(valueRequirement);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("requirement/{valueName}/{targetType}/{targetId}")
  public DependencyGraphTraceProviderResource setValueRequirementByExternalId(@PathParam("valueName") final String valueName,
      @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    final ExternalId externalId = ExternalId.parse(targetId);
    final ValueRequirement valueRequirement = toValueRequirement(valueName,
        new ComputationTargetRequirement(ComputationTargetType.parse(targetType), externalId));
    final DependencyGraphTraceBuilderProperties properties = _properties.addRequirement(valueRequirement);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataSnapshot/{snapshotId}")
  public DependencyGraphTraceProviderResource setMarketDataSnapshot(@PathParam("snapshotId") final String snapshotId) {
    final UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    final DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataLiveDefault")
  public DependencyGraphTraceProviderResource setMarketDataLiveDefault() {
    final MarketDataSpecification marketData = MarketData.live();
    final DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataLive/{dataSource}")
  public DependencyGraphTraceProviderResource setMarketDataLive(@PathParam("dataSource") final String dataSource) {
    final MarketDataSpecification marketData = MarketData.live(dataSource);
    final DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataHistorical/{localDate}/{timeSeriesResolverKey}")
  public DependencyGraphTraceProviderResource setMarketDataHistorical(@PathParam("localDate") final String localDateStr,
      @PathParam("timeSeriesResolverKey") final String timeSeriesResolverKey) {
    final LocalDate localDate = LocalDate.parse(localDateStr);
    final MarketDataSpecification marketData = MarketData.historical(localDate, timeSeriesResolverKey);
    final DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @GET
  @Path("build")
  public FudgeMsgEnvelope build() {
    final DependencyGraphBuildTrace result = _provider.getTrace(_properties);
    final MutableFudgeMsg fudgeMsg = new FudgeSerializer(_fudgeContext).objectToFudgeMsg(result);
    return new FudgeMsgEnvelope(fudgeMsg);
  }

  private ValueRequirement toValueRequirement(final String valueName, final ComputationTargetReference target) {
    final String name;
    final ValueProperties constraints;
    final int i = valueName.indexOf('{');
    if (i > 0 && valueName.charAt(valueName.length() - 1) == '}') {
      name = valueName.substring(0, i);
      constraints = ValueProperties.parse(valueName.substring(i));
    } else {
      name = valueName;
      constraints = ValueProperties.none();
    }
    return new ValueRequirement(name, target, constraints);
  }

}
