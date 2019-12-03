/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Selects a subscription as Fake iff any of the child subscription selectors select it as a fake.
 */
public class UnionFakeSubscriptionSelector implements FakeSubscriptionSelector {

  private final FakeSubscriptionSelector[] _fakeSubscriptionSelectors;

  public UnionFakeSubscriptionSelector(final FakeSubscriptionSelector... fakeSubscriptionSelectors) {
    _fakeSubscriptionSelectors = fakeSubscriptionSelectors;
  }

  @Override
  public ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
      final FakeSubscriptionBloombergLiveDataServer server, final Collection<LiveDataSpecification> uniqueIds) {
    Collection<LiveDataSpecification> real = uniqueIds;
    final Set<LiveDataSpecification> fakes = new HashSet<>();
    for (final FakeSubscriptionSelector subSelector : _fakeSubscriptionSelectors) {
      final ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> split = subSelector.splitShouldFake(server, real);
      real = split.first;
      fakes.addAll(split.second);
      if (real.isEmpty()) {
        break;
      }
    }
    return ObjectsPair.of(real, (Collection<LiveDataSpecification>) fakes);
  }
}
