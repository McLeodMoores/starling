/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This class produces a {@code DistributionSpecification}
 * using an underlying {@code IdResolver}, {@code NormalizationRuleResolver}, and
 * {@code JmsTopicNameResolver}.
 */
public class DefaultDistributionSpecificationResolver
  extends AbstractResolver<LiveDataSpecification, DistributionSpecification>
  implements DistributionSpecificationResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDistributionSpecificationResolver.class);

  private final IdResolver _idResolver;
  private final NormalizationRuleResolver _normalizationRuleResolver;
  private final JmsTopicNameResolver _jmsTopicNameResolver;

  public DefaultDistributionSpecificationResolver(
      final IdResolver idResolver,
      final NormalizationRuleResolver normalizationRuleResolver,
      final JmsTopicNameResolver jmsTopicNameResolver) {

    ArgumentChecker.notNull(idResolver, "ID Resolver");
    ArgumentChecker.notNull(normalizationRuleResolver, "Normalization rule resolver");
    ArgumentChecker.notNull(jmsTopicNameResolver, "JMS topic name resolver");

    _idResolver = idResolver;
    _normalizationRuleResolver = normalizationRuleResolver;
    _jmsTopicNameResolver = jmsTopicNameResolver;
  }

  @Override
  public Map<LiveDataSpecification, DistributionSpecification> resolve(final Collection<LiveDataSpecification> liveDataSpecifications) {

    ArgumentChecker.notNull(liveDataSpecifications, "Live Data specification");

    final Collection<ExternalIdBundle> identifierBundles = new ArrayList<>();
    final Map<ExternalIdBundle, Collection<LiveDataSpecification>> identifierBundle2LiveDataSpec = new HashMap<>();
    final Map<LiveDataSpecification, ExternalId> liveDataSec2Identifier = new HashMap<>();
    final Map<LiveDataSpecification, NormalizationRuleSet> liveDataSec2NormalizationRule = new HashMap<>();
    final Collection<JmsTopicNameResolveRequest> jmsTopicNameRequests = new ArrayList<>();
    final Map<JmsTopicNameResolveRequest, Collection<LiveDataSpecification>> jmsTopicNameRequest2LiveDataSec = new HashMap<>();
    final Map<LiveDataSpecification, String> liveDataSec2JmsTopicName = new HashMap<>();

    for (final LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      identifierBundles.add(liveDataSpec.getIdentifiers());
      Collection<LiveDataSpecification> liveDataSpecs = identifierBundle2LiveDataSpec.get(liveDataSpec.getIdentifiers());
      if (liveDataSpecs == null) {
        liveDataSpecs = new ArrayList<>();
        identifierBundle2LiveDataSpec.put(liveDataSpec.getIdentifiers(), liveDataSpecs);
      }
      liveDataSpecs.add(liveDataSpec);

      final NormalizationRuleSet normalizationRule = _normalizationRuleResolver.resolve(liveDataSpec.getNormalizationRuleSetId());
      liveDataSec2NormalizationRule.put(liveDataSpec, normalizationRule);
    }

    final Map<ExternalIdBundle, ExternalId> bundle2Identifier = _idResolver.resolve(identifierBundles);
    for (final Map.Entry<ExternalIdBundle, ExternalId> entry : bundle2Identifier.entrySet()) {
      final Collection<LiveDataSpecification> liveDataSpecsForBundle = identifierBundle2LiveDataSpec.get(entry.getKey());
      for (final LiveDataSpecification liveDataSpecForBundle : liveDataSpecsForBundle) {
        liveDataSec2Identifier.put(liveDataSpecForBundle, entry.getValue());
      }
    }

    for (final LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      final ExternalId identifier = liveDataSec2Identifier.get(liveDataSpec);
      final NormalizationRuleSet normalizationRule = liveDataSec2NormalizationRule.get(liveDataSpec);
      if (identifier == null || normalizationRule == null) {
        liveDataSec2JmsTopicName.put(liveDataSpec, null);
      } else {
        final JmsTopicNameResolveRequest jmsTopicNameRequest = new JmsTopicNameResolveRequest(identifier, normalizationRule);
        jmsTopicNameRequests.add(jmsTopicNameRequest);

        Collection<LiveDataSpecification> liveDataSpecs = jmsTopicNameRequest2LiveDataSec.get(jmsTopicNameRequest);
        if (liveDataSpecs == null) {
          liveDataSpecs = new ArrayList<>();
          jmsTopicNameRequest2LiveDataSec.put(jmsTopicNameRequest, liveDataSpecs);
        }
        liveDataSpecs.add(liveDataSpec);
      }
    }

    final Map<JmsTopicNameResolveRequest, String> jmsTopicNames = _jmsTopicNameResolver.resolve(jmsTopicNameRequests);
    for (final Map.Entry<JmsTopicNameResolveRequest, String> entry : jmsTopicNames.entrySet()) {
      final Collection<LiveDataSpecification> liveDataSpecsForRequest = jmsTopicNameRequest2LiveDataSec.get(entry.getKey());
      for (final LiveDataSpecification liveDataSpecForRequest : liveDataSpecsForRequest) {
        liveDataSec2JmsTopicName.put(liveDataSpecForRequest, entry.getValue());
      }
    }

    final Map<LiveDataSpecification, DistributionSpecification> returnValue = new HashMap<>();

    for (final LiveDataSpecification liveDataSpec : liveDataSpecifications) {
      final ExternalId identifier = liveDataSec2Identifier.get(liveDataSpec);
      final NormalizationRuleSet normalizationRule = liveDataSec2NormalizationRule.get(liveDataSpec);
      final String jmsTopicName = liveDataSec2JmsTopicName.get(liveDataSpec);
      if (identifier == null || normalizationRule == null || jmsTopicName == null) {
        LOGGER.info("Unable to resolve liveDataSpec: {} - identifier: {}, normalizationRule: {}, jmsTopicName: {}",
                      liveDataSpec, identifier, normalizationRule, jmsTopicName);
        returnValue.put(liveDataSpec, null);
      } else {
        final DistributionSpecification distributionSpec = new DistributionSpecification(
            identifier,
            normalizationRule,
            jmsTopicName);
        returnValue.put(liveDataSpec, distributionSpec);
      }
    }

    return returnValue;
  }

}
