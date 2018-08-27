/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import net.sf.ehcache.CacheManager;

/**
 * A {@link StandardLiveDataServer} which delegates all the work to a set of {@link StandardLiveDataServer}
 */
public abstract class CombiningLiveDataServer extends StandardLiveDataServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(CombiningLiveDataServer.class);

  private static final ExecutorService SUBSCRIPTION_EXECUTOR = NamedThreadPoolFactory.newCachedThreadPool("CombiningLiveDataServer", true);

  private final Set<StandardLiveDataServer> _underlyings;

  public CombiningLiveDataServer(final CacheManager cacheManager, final StandardLiveDataServer... otherUnderlyings) {
    this(Arrays.asList(otherUnderlyings), cacheManager);
  }

  public CombiningLiveDataServer(final Collection<? extends StandardLiveDataServer> otherUnderlyings, final CacheManager cacheManager) {
    super(cacheManager);
    _underlyings = Sets.newHashSet();
    _underlyings.addAll(otherUnderlyings);
  }

  @Override
  public int expireSubscriptions() {
    int expired = 0;
    for (final StandardLiveDataServer server : _underlyings) {
      expired += server.expireSubscriptions();
    }
    return expired;
  }

  @Override
  protected void startExpirationManager() {
    // No-op; the underlyings will have their own
  }

  @Override
  protected void stopExpirationManager() {
    // No-op; the underlyings will have their own
  }

  @Override
  public Collection<LiveDataSubscriptionResponse> subscribe(final Collection<LiveDataSpecification> liveDataSpecificationsFromClient, final boolean persistent) {
    return subscribeByServer(
        liveDataSpecificationsFromClient,
        new SubscribeAction() {

          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(final StandardLiveDataServer server, final Collection<LiveDataSpecification> specifications) {
            return server.subscribe(specifications, persistent);
          }

          @Override
          public String getName() {
            return "Subscribe";
          }
        });
  }

  @Override
  public LiveDataSubscriptionResponseMsg subscriptionRequestMadeImpl(final LiveDataSubscriptionRequest subscriptionRequest) {
    //Need to override here as well in order to catch the resolution/entitlement checking

    final Collection<LiveDataSubscriptionResponse> responses = subscribeByServer(
        subscriptionRequest.getSpecifications(),
        new SubscribeAction() {
          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(final StandardLiveDataServer server, final Collection<LiveDataSpecification> specifications) {
            final LiveDataSubscriptionRequest liveDataSubscriptionRequest = buildSubRequest(subscriptionRequest, specifications);
            //NOTE: we call up to subscriptionRequestMade to get the exception catching
            final LiveDataSubscriptionResponseMsg response = server.subscriptionRequestMade(liveDataSubscriptionRequest);

            //Check that we know how to combine these responses
            if (response.getRequestingUser() != subscriptionRequest.getUser()) {
              throw new OpenGammaRuntimeException("Unexpected user in response " + response.getRequestingUser());
            }
            return response.getResponses();
          }

          @Override
          public String getName() {
            return "SubscriptionRequestMade";
          }
        });
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUser(), responses);
  }

  private LiveDataSubscriptionRequest buildSubRequest(final LiveDataSubscriptionRequest subscriptionRequest, final Collection<LiveDataSpecification> specifications) {
    final LiveDataSubscriptionRequest liveDataSubscriptionRequest = new LiveDataSubscriptionRequest(subscriptionRequest.getUser(), subscriptionRequest.getType(), specifications);
    return liveDataSubscriptionRequest;
  }

  private interface SubscribeAction {
    Collection<LiveDataSubscriptionResponse> subscribe(StandardLiveDataServer server, Collection<LiveDataSpecification> specifications);

    String getName();
  }

  private Collection<LiveDataSubscriptionResponse> subscribeByServer(final Collection<LiveDataSpecification> specifications, final SubscribeAction action)
  {
    return forEachServer(specifications, new Function<Pair<StandardLiveDataServer, Collection<LiveDataSpecification>>, Collection<LiveDataSubscriptionResponse>>() {
      @Override
      public Collection<LiveDataSubscriptionResponse> apply(final Pair<StandardLiveDataServer, Collection<LiveDataSpecification>> input) {
        final StandardLiveDataServer specs = input.getFirst();
        final Collection<LiveDataSpecification> server = input.getSecond();
        LOGGER.debug("Sending subscription ({}) for {} to underlying server {}", new Object[] {action.getName(), specs, server });
        return action.subscribe(specs, server);
      }
    });
  }

  private <T> Collection<T> forEachServer(final Collection<LiveDataSpecification> specifications, final Function<Pair<StandardLiveDataServer, Collection<LiveDataSpecification>>, Collection<T>> operation)
  {
    final Map<StandardLiveDataServer, Collection<LiveDataSpecification>> mapped = groupByServer(specifications);

    final Collection<Future<Collection<T>>> futures = new ArrayList<>(mapped.size());
    for (final Entry<StandardLiveDataServer, Collection<LiveDataSpecification>> entry : mapped.entrySet()) {
      if (entry.getValue().isEmpty()) {
        continue;
      }
      final Future<Collection<T>> future = SUBSCRIPTION_EXECUTOR.submit(new Callable<Collection<T>>() {

        @Override
        public Collection<T> call() throws Exception {
          return operation.apply(Pairs.of(entry.getKey(), entry.getValue()));
        }
      });

      futures.add(future);
    }
    final List<T> responses = new ArrayList<>(specifications.size());
    for (final Future<Collection<T>> future : futures) {
      try {
        responses.addAll(future.get());
      } catch (final InterruptedException ex) {
        //Should be rare, since the subscription methods should bundle everything into the response
        LOGGER.error("Unexpected exception when delegating subscription", ex);
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      } catch (final ExecutionException ex) {
        //Should be rare, since the subscription methods should bundle everything into the response
        LOGGER.error("Unexpected exception when delegating subscription", ex);
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      }
    }
    return responses;
  }

  protected abstract Map<StandardLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      Collection<LiveDataSpecification> specs);

  private StandardLiveDataServer getServer(final LiveDataSpecification spec) {
    final Map<StandardLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(Sets.newHashSet(spec));
    for (final Entry<StandardLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      if (entry.getValue().size() > 0) {
        return entry.getKey();
      }
    }
    throw new OpenGammaRuntimeException("Couldn't find server for " + spec);
  }

  @Override
  public void addSubscriptionListener(final SubscriptionListener subscriptionListener) {
    for (final StandardLiveDataServer server : _underlyings) {
      server.addSubscriptionListener(subscriptionListener);
    }
  }

  @Override
  public Set<Subscription> getSubscriptions() {
    final Set<Subscription> ret = new HashSet<>();
    for (final StandardLiveDataServer server : _underlyings) {
      final Set<Subscription> serversSubscriptions = server.getSubscriptions();
      LOGGER.debug("Server {} has {} subscriptions", server, serversSubscriptions.size());
      ret.addAll(serversSubscriptions);
    }
    return ret;
  }

  @Override
  public Subscription getSubscription(final LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getSubscription(fullyQualifiedSpec);
  }

  @Override
  public MarketDataDistributor getMarketDataDistributor(final LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getMarketDataDistributor(fullyQualifiedSpec);
  }

  @Override
  public Map<LiveDataSpecification, MarketDataDistributor> getMarketDataDistributors(final Collection<LiveDataSpecification> fullyQualifiedSpecs) {
    final Map<StandardLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(fullyQualifiedSpecs);
    final HashMap<LiveDataSpecification, MarketDataDistributor> ret = new HashMap<>();
    for (final Entry<StandardLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      final Map<LiveDataSpecification, MarketDataDistributor> entries = entry.getKey().getMarketDataDistributors(entry.getValue());
      ret.putAll(entries);
    }
    return ret;
  }

  @Override
  public boolean stopDistributor(final MarketDataDistributor distributor) {
    return getServer(distributor.getFullyQualifiedLiveDataSpecification()).stopDistributor(distributor);
  }

  @Override
  protected void doConnect() {
    for (final StandardLiveDataServer server : _underlyings) {
      server.start();
    }
  }

  @Override
  protected void doDisconnect() {
    for (final StandardLiveDataServer server : _underlyings) {
      server.stop();
    }
  }

  //----- Shouldn't happen -----
  @Override
  protected Map<String, Object> doSubscribe(final Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected void doUnsubscribe(final Collection<Object> subscriptionHandles) {
    throw new IllegalArgumentException();
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(final Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    throw new IllegalArgumentException();
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(final Subscription subscription) {
    throw new IllegalArgumentException();
  }
}
