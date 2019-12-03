/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.AbstractLiveDataClient;
import com.opengamma.livedata.client.SubscriptionHandle;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;

/**
 * A {@code LiveDataClient} that works completely in memory
 * and actually does not contact any server.
 */
public class TestLiveDataClient extends AbstractLiveDataClient {

  private final List<LiveDataSpecification> _cancelRequests = new ArrayList<>();
  private final List<Collection<SubscriptionHandle>> _subscriptionRequests = new ArrayList<>();
  private final AtomicLong _sequenceGenerator = new AtomicLong(0);

  @Override
  protected void cancelPublication(final LiveDataSpecification fullyQualifiedSpecification) {
    _cancelRequests.add(fullyQualifiedSpecification);
  }

  @Override
  protected void handleSubscriptionRequest(final Collection<SubscriptionHandle> subHandles) {
    _subscriptionRequests.add(subHandles);
  }

  public List<LiveDataSpecification> getCancelRequests() {
    return _cancelRequests;
  }

  public List<Collection<SubscriptionHandle>> getSubscriptionRequests() {
    return _subscriptionRequests;
  }

  public void marketDataReceived(final LiveDataSpecification fullyQualifiedSpecification, final FudgeMsg fields) {
    final LiveDataValueUpdateBean bean = new LiveDataValueUpdateBean(_sequenceGenerator.incrementAndGet(), fullyQualifiedSpecification, fields);
    getValueDistributor().notifyListeners(bean);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(final UserPrincipal user,
      final Collection<LiveDataSpecification> requestedSpecifications) {
    final Map<LiveDataSpecification, Boolean> returnValue = new HashMap<>();
    for (final LiveDataSpecification spec : requestedSpecifications) {
      returnValue.put(spec, isEntitled(user, spec));
    }
    return returnValue;
  }

  @Override
  public boolean isEntitled(final UserPrincipal user, final LiveDataSpecification requestedSpecification) {
    return true;
  }

  @Override
  public void subscriptionRequestSatisfied(final SubscriptionHandle subHandle, final LiveDataSubscriptionResponse response) {
    super.subscriptionRequestSatisfied(subHandle, response);
  }

}
