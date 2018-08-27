/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ArgumentChecker;

/**
 * JMX management of a LiveData server.
 */
@ManagedResource(
    description = "LiveData server attributes and operations that can be managed via JMX"
    )
public class LiveDataServerMBean {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LiveDataServerMBean.class);

  /**
   * The underlying live data server.
   */
  private final StandardLiveDataServer _server;

  /**
   * Creates an instance.
   *
   * @param server  the underlying live data server, not null
   */
  public LiveDataServerMBean(final StandardLiveDataServer server) {
    ArgumentChecker.notNull(server, "server");
    _server = server;
  }

  protected StandardLiveDataServer getServer() {
    return _server;
  }

  @ManagedAttribute(description = "The unique id domain of the underlying server.")
  public String getUniqueIdDomain() {
    try {
      final ExternalScheme uniqueIdDomain = getServer().getUniqueIdDomain();
      return uniqueIdDomain == null ? "<null>" : uniqueIdDomain.toString();
    } catch (final RuntimeException e) {
      LOGGER.error("getUniqueIdDomain() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "The connection status of the underlying server.")
  public String getConnectionStatus() {
    try {
      return getServer().getConnectionStatus().toString();
    } catch (final RuntimeException e) {
      LOGGER.error("getConnectionStatus() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "The type of the underlying server.")
  public String getServerType() {
    try {
      return getServer().getClass().getName();
    } catch (final RuntimeException e) {
      LOGGER.error("getServerType() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "How many different tickers the server subscribes to.")
  public int getNumActiveSubscriptions() {
    try {
      return getServer().getNumActiveSubscriptions();
    } catch (final RuntimeException e) {
      LOGGER.error("getNumActiveSubscriptions() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Security IDs the server subscribes to."
      + " The form of the IDs is dependent on the source system"
      + " - Reuters RICs, Bloomberg unique IDs, etc.")
  public Set<String> getActiveSubscriptionIds() {
    try {
      return getServer().getActiveSubscriptionIds();
    } catch (final RuntimeException e) {
      LOGGER.error("getActiveSubscriptionIds() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "JMS topics the server publishes to.")
  public Set<String> getActiveDistributionSpecs() {
    try {
      return getServer().getActiveDistributionSpecs();
    } catch (final RuntimeException e) {
      LOGGER.error("getActiveDistributionSpecs() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "The number of market data updates the server has processed in its lifetime.")
  public long getNumMarketDataUpdatesReceived() {
    try {
      return getServer().getNumMarketDataUpdatesReceived();
    } catch (final RuntimeException e) {
      LOGGER.error("getNumLiveDataUpdatesSent() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "# of market data updates/sec, calculated over the last 60 seconds")
  public double getNumLiveDataUpdatesSentPerSecondOverLastMinute() {
    try {
      return getServer().getNumLiveDataUpdatesSentPerSecondOverLastMinute();
    } catch (final RuntimeException e) {
      LOGGER.error("getNumLiveDataUpdatesSentPerSecondOverLastMinute() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Subscribes to market data. The subscription will be non-persistent."
      + " If the server already subscribes to the given market data, this method is a "
      + " no-op. Returns the name of the JMS topic market data will be published on.")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String subscribe(final String securityUniqueId) {
    try {
      final LiveDataSubscriptionResponse response = getServer().subscribe(securityUniqueId);
      if (response.getSubscriptionResult() != LiveDataSubscriptionResult.SUCCESS) {
        throw new RuntimeException("Unsuccessful subscription: " + response.getUserMessage());
      }
      return response.getTickDistributionSpecification();
    } catch (final RuntimeException e) {
      LOGGER.error("subscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Subscribes to market data. The subscription will be persistent."
      + " If the server already subscribes to the given market data, this method will make the "
      + " subscription persistent. Returns the name of the JMS topic market data will be published on.")
  @ManagedOperationParameters({ @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String subscribePersistently(final String securityUniqueId) {
    try {
      final LiveDataSpecification spec = getServer().getLiveDataSpecification(securityUniqueId);
      final LiveDataSubscriptionResponse response = getServer().subscribe(spec, true);
      if (response.getSubscriptionResult() != LiveDataSubscriptionResult.SUCCESS) {
        throw new RuntimeException("Unsuccessful subscription: " + response.getUserMessage());
      }
      return response.getTickDistributionSpecification();
    } catch (final RuntimeException e) {
      LOGGER.error("subscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Converts all subscriptions to persistent.")
  public void subscribeAllPersistently() {
    setPersistenceForAll(true);
  }

  @ManagedOperation(description = "Converts all subscriptions to non-persistent.")
  public void unpersistAllSubscribtions() {
    setPersistenceForAll(false);
  }

  private void setPersistenceForAll(final boolean persistent) {
    try {
      final Set<String> activeSubscriptionIds = getServer().getActiveSubscriptionIds();
      for (final String string : activeSubscriptionIds) {
        final MarketDataDistributor marketDataDistributor = getServer().getMarketDataDistributor(string);
        marketDataDistributor.setPersistent(persistent);
      }
    } catch (final RuntimeException e) {
      LOGGER.error("Setting all susbcriptions to persistent=" + persistent + " failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Unsubscribes from market data. "
      + "Works even if the subscription is persistent. "
      + "Returns true if a market data subscription was actually removed,"
      + " false otherwise.")
  @ManagedOperationParameters({
       @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public boolean unsubscribe(final String securityUniqueId) {
    try {
      return getServer().unsubscribe(securityUniqueId);
    } catch (final RuntimeException e) {
      LOGGER.error("unsubscribe(" + securityUniqueId + ") failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Gets the current snapshot of a security. Will not cause an underlying snapshot.")
  @ManagedOperationParameters({
       @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String getSnapshot(final String securityUniqueId) {
    final MarketDataDistributor marketDataDistributor = getServer().getMarketDataDistributor(securityUniqueId);
    return marketDataDistributor != null && marketDataDistributor.getSnapshot() != null ? marketDataDistributor.getSnapshot().toString() : "Unknown";
  }

  @ManagedOperation(description = "Gets the current snapshot of all active securities. Will not cause any underlying snapshots.")
  public String[] getAllSnapshots() {
    try {
      final Set<String> activeSubscriptionIds = getServer().getActiveSubscriptionIds();
      final Iterable<String> results = Iterables.transform(activeSubscriptionIds, new Function<String, String>() {
        @Override
        public String apply(final String from) {
          try {
            return getSnapshot(from);
          } catch (final RuntimeException e) {
            return e.getMessage();
          }
        }
      });
      return Iterators.toArray(results.iterator(), String.class);
    } catch (final RuntimeException e) {
      LOGGER.error("getAllSnapshots() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Gets the current field history of a security. Will not cause an underlying snapshot.")
  @ManagedOperationParameters({ @ManagedOperationParameter(name = "securityUniqueId", description = "Security unique ID. Server type dependent.)") })
  public String getFieldHistory(final String securityUniqueId) {
    try {
      final Subscription subscription = getServer().getSubscription(securityUniqueId);
      if (subscription == null) {
        return null;
      }
      return subscription.getLiveDataHistory().getLastKnownValues().toString();
    } catch (final RuntimeException e) {
      LOGGER.error("getFieldHistory() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedOperation(description = "Gets the current field history of all active securities. Will not cause any underlying snapshots.")
  public String[] getAllFieldHistories() {
    try {
      final Set<String> activeSubscriptionIds = getServer().getActiveSubscriptionIds();
      final Iterable<String> results = Iterables.transform(activeSubscriptionIds, new Function<String, String>() {

        @Override
        public String apply(final String from) {
          try {
            return getFieldHistory(from);
          } catch (final RuntimeException e) {
            return e.getMessage();
          }
        }
      });
      return Iterators.toArray(results.iterator(), String.class);
    } catch (final RuntimeException e) {
      LOGGER.error("getAllFieldHistories() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

}
