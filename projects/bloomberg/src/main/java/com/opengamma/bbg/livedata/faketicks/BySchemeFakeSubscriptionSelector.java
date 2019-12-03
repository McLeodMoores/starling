/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Decides based of scheme which subscriptions to fake. Fakes iff any of the identifiers in the bundle has one of these schemes
 */
public class BySchemeFakeSubscriptionSelector implements FakeSubscriptionSelector {

  private final Set<ExternalScheme> _toFake;

  public BySchemeFakeSubscriptionSelector(final String... toFakes) {
    this(getSchemes(toFakes));
  }

  public BySchemeFakeSubscriptionSelector(final ExternalScheme... toFakes) {
    this(Sets.newHashSet(toFakes));
  }

  public BySchemeFakeSubscriptionSelector(final Set<ExternalScheme> toFake) {
    _toFake = toFake;
  }

  @Override
  public ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
      final FakeSubscriptionBloombergLiveDataServer server, final Collection<LiveDataSpecification> specs) {
    if (specs.isEmpty()) {
      return ObjectsPair.of((Collection<LiveDataSpecification>) new ArrayList<LiveDataSpecification>(),
          (Collection<LiveDataSpecification>) new ArrayList<LiveDataSpecification>());
    }

    final Set<LiveDataSpecification> fakes = new HashSet<>();
    final Set<LiveDataSpecification> underlyings = new HashSet<>();

    for (final LiveDataSpecification liveDataSpecification : specs) {
      if (shouldFake(liveDataSpecification)) {
        fakes.add(liveDataSpecification);
      } else {
        underlyings.add(liveDataSpecification);
      }
    }

    return ObjectsPair.of((Collection<LiveDataSpecification>) underlyings, (Collection<LiveDataSpecification>) fakes);
  }

  private boolean shouldFake(final LiveDataSpecification liveDataSpecification) {
    for (final ExternalScheme scheme : _toFake) {
      if (liveDataSpecification.getIdentifier(scheme) != null) {
        return true;
      }
    }
    return false;
  }

  private static Set<ExternalScheme> getSchemes(final String[] schemes) {
    final HashSet<ExternalScheme> ret = new HashSet<>();
    for (final String scheme : schemes) {
      ret.add(ExternalScheme.of(scheme));
    }
    return ret;
  }
}
