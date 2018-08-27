/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link LiveDataClient} that tracks subscriptions and can resubscribe.
 * This allows subscriptions to be refreshed in the event of the underlying data source going up and down. The
 * subscription operations are handed off to a delegate {@link LiveDataClient}.
 */
public class ResubscribingLiveDataClient implements LiveDataClient {

  private final LiveDataClient _delegate;
  private final Set<Subscription> _subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<Subscription, Boolean>());

  public ResubscribingLiveDataClient(final LiveDataClient delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public void subscribe(final UserPrincipal user, final LiveDataSpecification spec, final LiveDataListener listener) {
    _subscriptions.add(new Subscription(user, spec, listener));
    _delegate.subscribe(user, spec, listener);
  }

  @Override
  public void subscribe(final UserPrincipal user, final Collection<LiveDataSpecification> specs, final LiveDataListener listener) {
    for (final LiveDataSpecification spec : specs) {
      _subscriptions.add(new Subscription(user, spec, listener));
    }
    _delegate.subscribe(user, specs, listener);
  }

  @Override
  public void unsubscribe(final UserPrincipal user, final LiveDataSpecification spec, final LiveDataListener listener) {
    _subscriptions.remove(new Subscription(user, spec, listener));
    _delegate.unsubscribe(user, spec, listener);
  }

  @Override
  public void unsubscribe(final UserPrincipal user, final Collection<LiveDataSpecification> specs, final LiveDataListener listener) {
    for (final LiveDataSpecification spec : specs) {
      _subscriptions.remove(new Subscription(user, spec, listener));
    }
    _delegate.unsubscribe(user, specs, listener);
  }

  @Override
  public LiveDataSubscriptionResponse snapshot(final UserPrincipal user, final LiveDataSpecification spec, final long timeout) {
    return _delegate.snapshot(user, spec, timeout);
  }

  @Override
  public Collection<LiveDataSubscriptionResponse> snapshot(final UserPrincipal user,
                                                           final Collection<LiveDataSpecification> specs,
                                                           final long timeout) {
    return _delegate.snapshot(user, specs, timeout);
  }

  @Override
  public String getDefaultNormalizationRuleSetId() {
    return _delegate.getDefaultNormalizationRuleSetId();
  }

  @Override
  public void close() {
    _delegate.close();
  }

  @Override
  public boolean isEntitled(final UserPrincipal user, final LiveDataSpecification spec) {
    return _delegate.isEntitled(user, spec);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(final UserPrincipal user, final Collection<LiveDataSpecification> specs) {
    return _delegate.isEntitled(user, specs);
  }

  public void resubscribe() {
    for (final Subscription subscription : _subscriptions) {
      _delegate.subscribe(subscription._user, subscription._spec, subscription._listener);
    }
  }

  private static final class Subscription {

    private final UserPrincipal _user;
    private final LiveDataSpecification _spec;
    private final LiveDataListener _listener;

    private Subscription(final UserPrincipal user, final LiveDataSpecification spec, final LiveDataListener listener) {
      _user = user;
      _spec = spec;
      _listener = listener;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_user, _spec, _listener);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Subscription other = (Subscription) obj;
      return
          Objects.equals(this._user, other._user) &&
          Objects.equals(this._spec, other._spec) &&
          Objects.equals(this._listener, other._listener);
    }
  }
}
