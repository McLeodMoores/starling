/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This class produces a {@code DistributionSpecification}
 * from a fixed map.
 */
public class FixedDistributionSpecificationResolver
extends AbstractResolver<LiveDataSpecification, DistributionSpecification>
implements DistributionSpecificationResolver {

  private final Map<LiveDataSpecification, DistributionSpecification> _liveDataSpec2DistSpec;

  /**
   * @param fixed
   *          a map from live data specifications to distribution
   *          specifications, not null
   */
  public FixedDistributionSpecificationResolver(final Map<LiveDataSpecification, DistributionSpecification> fixed) {
    ArgumentChecker.notNull(fixed, "Fixed distribution specifications");
    _liveDataSpec2DistSpec = new HashMap<>(fixed);
  }

  @Override
  public DistributionSpecification resolve(final LiveDataSpecification liveDataSpecificationFromClient) throws IllegalArgumentException {
    final DistributionSpecification spec = _liveDataSpec2DistSpec.get(liveDataSpecificationFromClient);
    return spec;
  }

}
