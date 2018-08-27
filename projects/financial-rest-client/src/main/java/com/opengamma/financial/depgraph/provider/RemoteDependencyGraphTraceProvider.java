/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import static java.lang.String.format;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceProviderUris;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link DependencyGraphTraceProvider}.
 */
public class RemoteDependencyGraphTraceProvider extends AbstractRemoteClient implements DependencyGraphTraceProvider {

  /**
   * Creates an instance.
   *
   * @param baseUri the target URI for all RESTful web services, not null
   */
  public RemoteDependencyGraphTraceProvider(final URI baseUri) {
    super(baseUri);
  }

  @Override
  public DependencyGraphBuildTrace getTrace(final DependencyGraphTraceBuilderProperties properties) {
    final URI uri = buildUri(properties);

    return accessRemote(uri).get(DependencyGraphBuildTrace.class);
  }

  /**
   * Builds the url to use for the remote access.
   * @param properties the properties to use
   * @return a full URI
   */
  @VisibleForTesting
  URI buildUri(final DependencyGraphTraceBuilderProperties properties) {
    URI uri = getBaseUri();

    //process single value properties:
    final String calcConfigName = properties.getCalculationConfigurationName();
    uri = DependencyGraphTraceProviderUris.uriCalculationConfigurationName(uri, calcConfigName);

    final ValueProperties defaultProperties = properties.getDefaultProperties();
    uri = DependencyGraphTraceProviderUris.uriDefaultProperties(uri, defaultProperties);

    final List<MarketDataSpecification> marketData = properties.getMarketData();
    if (marketData != null) {
      uri = DependencyGraphTraceProviderUris.uriMarketData(uri, marketData);
    }

    final VersionCorrection resolutionTime = properties.getResolutionTime();
    uri = DependencyGraphTraceProviderUris.uriResolutionTime(uri, resolutionTime);

    final Instant valuationTime = properties.getValuationTime();
    if (valuationTime != null) {
      uri = DependencyGraphTraceProviderUris.uriValuationTime(uri, valuationTime);
    }

    //process requirements:
    uri = processRequirements(uri, properties.getRequirements());

    return DependencyGraphTraceProviderUris.uriBuild(uri);
  }

  /**
   * Unpacks the requirements into URI form.
   * @param uri the uri to append to
   * @param requirements the requirements to append
   */
  private URI processRequirements(URI uri, final Collection<ValueRequirement> requirements) {
    for (final ValueRequirement valueRequirement : requirements) {

      final String valueName = valueRequirement.getValueName();

      final ValueProperties constraints = valueRequirement.getConstraints();

      final String contraintStr = constraints.isEmpty() ? "" : constraints.toString();

      final String constrainedValueName = valueName + contraintStr;

      final ComputationTargetReference targetReference = valueRequirement.getTargetReference();
      final String targetType = targetReference.getType().toString();

      if (targetReference instanceof ComputationTargetRequirement) {
        final ComputationTargetRequirement requirement = (ComputationTargetRequirement) targetReference;
        final Set<ExternalId> externalIds = requirement.getIdentifiers().getExternalIds();
        ArgumentChecker.isTrue(externalIds.size() == 1, "One (and only one) external id must be specified currently.");
        final ExternalId externalId = Iterables.get(externalIds, 0);
        uri = DependencyGraphTraceProviderUris.uriValueRequirementByExternalId(uri, constrainedValueName, targetType, externalId);
      } else if (targetReference instanceof ComputationTargetSpecification) {
        final UniqueId uniqueId = ((ComputationTargetSpecification) targetReference).getUniqueId();
        uri = DependencyGraphTraceProviderUris.uriValueRequirementByUniqueId(uri, constrainedValueName, targetType, uniqueId);
      } else {
        throw new IllegalArgumentException(format("Unrecognised ValueRequirement class: %s", ValueRequirement.class.getName()));
      }
    }
    return uri;
  }
}
