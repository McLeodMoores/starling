/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * Wraps a {@link DistributionSpecificationResolver} from Strong Bloomberg space into Fake bloomberg space.
 */
public class FakeDistributionSpecificationResolver implements DistributionSpecificationResolver {

  private final DistributionSpecificationResolver _underlying;

  /**
   * @param underlying
   *          the bloomberg spec resolver
   */
  public FakeDistributionSpecificationResolver(final DistributionSpecificationResolver underlying) {
    super();
    _underlying = underlying;
  }

  @Override
  public DistributionSpecification resolve(final LiveDataSpecification liveDataSpecificationFromClient) {
    final DistributionSpecification underResolved = _underlying.resolve(liveDataSpecificationFromClient);
    return wrap(underResolved);
  }

  @Override
  public Map<LiveDataSpecification, DistributionSpecification> resolve(
      final Collection<LiveDataSpecification> liveDataSpecifications) {
    final Map<LiveDataSpecification, DistributionSpecification> underResolved = _underlying
        .resolve(liveDataSpecifications);
    final HashMap<LiveDataSpecification, DistributionSpecification> ret = new HashMap<>();
    for (final Entry<LiveDataSpecification, DistributionSpecification> entry : underResolved.entrySet()) {
      ret.put(entry.getKey(), wrap(entry.getValue()));
    }
    return ret;
  }

  private DistributionSpecification wrap(final DistributionSpecification underResolved) {
    if (underResolved == null) {
      return null;
    }
    final ExternalId identifier = underResolved.getMarketDataId();
    ExternalScheme wrappedScheme;
    if (identifier.getScheme().equals(ExternalSchemes.BLOOMBERG_BUID)) {
      wrappedScheme = ExternalSchemes.BLOOMBERG_BUID_WEAK;
    } else if (identifier.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER)) {
      wrappedScheme = ExternalSchemes.BLOOMBERG_TICKER_WEAK;
    } else {
      throw new IllegalArgumentException("Unsupported scheme: " + identifier.getScheme());
    }
    final ExternalId wrapped = ExternalId.of(wrappedScheme, identifier.getValue());
    return new DistributionSpecification(wrapped, underResolved.getNormalizationRuleSet(), underResolved.getJmsTopic().concat(".Fake"));
  }
}
