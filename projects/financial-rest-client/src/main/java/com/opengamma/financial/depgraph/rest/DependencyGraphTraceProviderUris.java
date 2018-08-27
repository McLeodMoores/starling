/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * URIs for a simple dependency graph building service over the network for debugging/diagnostic purposes. This is intended to be simple to access using hand written URLs - there is not currently a
 * corresponding programatic interface to the service this provides.
 * <p>
 * For example to find out why a graph building configuration can't satisfy a requirement, a URL such as "/value/Present Value/SECURITY/SecDb~1234" will return the failure trace (or the graph if
 * successful).
 */
public final class DependencyGraphTraceProviderUris {

  /**
   * Builds URI for remote access to getTraceWithCalculationConfigurationName.
   * @param baseUri the base uri
   * @param calculationConfigurationName the calculation configuration name
   * @return the URI
   */
  public static URI uriCalculationConfigurationName(final URI baseUri, final String calculationConfigurationName) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("calculationConfigurationName/{calculationConfigurationName}");
    return bld.build(calculationConfigurationName);
  }

  /**
   * Builds URI for remote access to getTraceWithValuationTime.
   * @param baseUri the base uri
   * @param valuationInstant the valuation time
   * @return the URI
   */
  public static URI uriValuationTime(final URI baseUri, final Instant valuationInstant) {
    final String valuationInstantStr = valuationInstant.toString();
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("valuationTime/{valuationTime}");
    return bld.build(valuationInstantStr);
  }

  /**
   * Builds URI for remote access to getTraceWithResolutionTime.
   * @param baseUri the base uri
   * @param resolutionTime the resolution time
   * @return the URI
   */
  public static URI uriResolutionTime(final URI baseUri, final VersionCorrection resolutionTime) {
    final String resolutionTimeStr = resolutionTime.toString();
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("resolutionTime/{resolutionTime}");
    return bld.build(resolutionTimeStr);
  }

  /**
   * Builds URI for remote access to getTraceWithDefaultProperties.
   * @param baseUri the base uri
   * @param defaultProperties the default properties
   * @return the URI
   */
  public static URI uriDefaultProperties(final URI baseUri, final ValueProperties defaultProperties) {
    final String defaultPropertiesStr = defaultProperties.toString();
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("defaultProperties/{defaultProperties}");
    return bld.build(defaultPropertiesStr);
  }

  /**
   * Builds URI for remote access to getTraceWithMarketData.
   * @param baseUri the base uri
   * @param marketData the market data
   * @return the URI
   */
  public static URI uriMarketData(URI baseUri, final List<MarketDataSpecification> marketData) {
    for (final MarketDataSpecification mdSpec : marketData) {
      if (mdSpec instanceof UserMarketDataSpecification) {
        final String snapshotId = ((UserMarketDataSpecification) mdSpec).getUserSnapshotId().toString();
        final UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataSnapshot/{snapshotId}");
        baseUri = bld.build(snapshotId);
      } else if (mdSpec instanceof LiveMarketDataSpecification) {
        final String dataSource = ((LiveMarketDataSpecification) mdSpec).getDataSource();
        if (dataSource == null) {
          final UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataLiveDefault");
          baseUri = bld.build();
        } else {
          final UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataLive/{dataSource}");
          baseUri = bld.build(dataSource);
        }
      } else if (mdSpec instanceof FixedHistoricalMarketDataSpecification) {
        final String snapshotDate = ((FixedHistoricalMarketDataSpecification) mdSpec).getSnapshotDate().toString();
        final String timeSeriesResolverKey = ((FixedHistoricalMarketDataSpecification) mdSpec).getTimeSeriesResolverKey();
        final UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataHistorical/{localDate}/{timeSeriesResolverKey}");
        baseUri = bld.build(snapshotDate, timeSeriesResolverKey);
      }

    }
    return baseUri;
  }

  /**
   * Builds URI for remote access to getTraceWithValueRequirementByUniqueId
   * @param baseUri the base uri
   * @param valueName the value name
   * @param targetType the target type
   * @param uniqueId the unique id
   * @return the URI
   */
  public static URI uriValueRequirementByUniqueId(final URI baseUri, final String valueName, final String targetType, final UniqueId uniqueId) {
    final String uniqueIdStr = uniqueId.toString();
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("value/{valueName}/{targetType}/{targetId}");
    return bld.build(valueName, targetType, uniqueIdStr);
  }

  /**
   * Builds URI for remote access to getTraceWithValueRequirementByExternalId
   * @param baseUri the base uri
   * @param valueName the value name
   * @param targetType the target type
   * @param externalId the external id
   * @return the URI
   */
  public static URI uriValueRequirementByExternalId(final URI baseUri, final String valueName, final String targetType, final ExternalId externalId) {
    final String externalIdStr = externalId.toString();
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("requirement/{valueName}/{targetType}/{targetId}");
    return bld.build(valueName, targetType, externalIdStr);
  }

  /**
   * The build call. This must be called after all of the other parameters have been added.
   * @param baseUri the uri with all params added
   * @return the uri complete with build call
   */
  public static URI uriBuild(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("build");
    return bld.build();
  }

}
