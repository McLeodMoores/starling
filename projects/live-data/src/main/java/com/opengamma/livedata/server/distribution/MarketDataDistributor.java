/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.livedata.server.LastKnownValueStore;
import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;

/**
 * Distributes market data to clients and keeps a history of what has been distributed.
 */
public class MarketDataDistributor {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataDistributor.class);

  /**
   * What data should be distributed, how and where.
   */
  private final DistributionSpecification _distributionSpec;
  /**
   * Which subscription this distributor belongs to.
   */
  private final Subscription _subscription;
  
  /** These listener(s) actually publish the data */ 
  private final Collection<MarketDataSender> _marketDataSenders;
  /**
   * Last known values of ALL fully normalized fields that were
   * sent to clients. This is not the last message as such
   * because the last message might not have included all the fields.
   * Instead, because the last value of ALL fields is stored,
   * this store provides a current snapshot of the entire state of the 
   * market data line.   
   */
  private final LastKnownValueStore _lastKnownValues;
  /** 
   * A history store to be used by the FieldHistoryUpdater normalization rule.
   * Fields stored in this history could either be completely unnormalized, 
   * partially normalized, or fully normalized.   
   */
  private final FieldHistoryStore _history = new FieldHistoryStore();
  /**
   * Stores how many normalized messages have been sent to clients.  
   */
  private final AtomicLong _numMessagesSent = new AtomicLong(0);
  /**
   * Whether this distributor is persistent. 
   * <p>
   * True = a persistent distributor, should survive a server restart.
   * False = a non-persistent distributor. Will die if the server is
   * restarted.
   */
  private boolean _persistent;
  /** 
   * When this distributor should stop distributing
   * data if no heartbeats are received from clients.
   * <p>
   * Stored as milliseconds from UTC epoch.
   * <p>
   * Null means the distributor should not expire.
   */
  private Long _expiry;

  /**
   * Creates an instance.
   * 
   * @param distributionSpec  What data should be distributed, how and where.
   * @param subscription  Which subscription this distributor belongs to.
   * @param marketDataSenderFactory  Used to create listener(s) that actually publish the data
   * @param persistent  Whether this distributor is persistent.
   * @param lkvStoreProvider The factory for LastKnownValue stores. 
   */
  public MarketDataDistributor(DistributionSpecification distributionSpec,
      Subscription subscription,
      MarketDataSenderFactory marketDataSenderFactory,
      boolean persistent,
      LastKnownValueStoreProvider lkvStoreProvider) {
    ArgumentChecker.notNull(distributionSpec, "Distribution spec");
    ArgumentChecker.notNull(subscription, "Subscription");
    ArgumentChecker.notNull(marketDataSenderFactory, "Market data sender factory");
    ArgumentChecker.notNull(lkvStoreProvider, "LKV Store Provider");
    
    _distributionSpec = distributionSpec;
    _subscription = subscription;
    _marketDataSenders = marketDataSenderFactory.create(this);
    if (_marketDataSenders == null) {
      throw new IllegalStateException("Null returned by " + marketDataSenderFactory);
    }
    setPersistent(persistent);
    
    _lastKnownValues = lkvStoreProvider.newInstance(distributionSpec.getMarketDataId(), distributionSpec.getNormalizationRuleSet().getId());
    
    // Initialize history with last known values.
    // This does nothing in the default state (where the LKV is empty) but
    // in case where the LKV is backed by a persistent store will prep the
    // current state based on the persistent version.
    _history.liveDataReceived(_lastKnownValues.getFields());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the distribution specification.
   * 
   * @return the distribution specification, not null
   */
  public DistributionSpecification getDistributionSpec() {
    return _distributionSpec;
  }

  /**
   * Gets the live data specification.
   * 
   * @return the specification of the data
   */
  public LiveDataSpecification getFullyQualifiedLiveDataSpecification() {
    return getDistributionSpec().getFullyQualifiedLiveDataSpecification();
  }

  /**
   * Gets the subscription details.
   * 
   * @return the subscription details
   */
  public Subscription getSubscription() {
    return _subscription;
  }

  /**
   * Gets the number of messages sent.
   * 
   * @return the message count
   */
  public long getNumMessagesSent() {
    return _numMessagesSent.get();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a snapshot of data, returning the latest value.
   * 
   * @return the latest value, not null
   */
  public LiveDataValueUpdateBean getSnapshot() {
    FudgeMsg lastKnownValues = getLastKnownValues();
    if (lastKnownValues == null) {
      return null;
    }
    return new LiveDataValueUpdateBean(
        getNumMessagesSent(), // 0-based as it should be 
        getDistributionSpec().getFullyQualifiedLiveDataSpecification(), 
        lastKnownValues);
  }

  private synchronized FudgeMsg getLastKnownValues() {
    // NOTE kirk 2012-07-12 -- I have to assume the sentinel is important
    // so I'm preserving it even though _lastKnownValues will never be null now.
    if (_lastKnownValues.isEmpty()) {
      return null;
    }
    return _lastKnownValues.getFields();
  }
  
  private synchronized void updateLastKnownValues(FudgeMsg lastKnownValue) {
    _lastKnownValues.updateFields(lastKnownValue);
  }

  //-------------------------------------------------------------------------
  /**
   * Normalizes the data.
   * 
   * @param msg  the message received from underlying market data API in its native format
   * @return the normalized message. Null if in the process of normalization,
   * the message became empty and therefore should not be sent.
   */
  private FudgeMsg normalize(FudgeMsg msg) {
    FudgeMsg normalizedMsg = _distributionSpec.getNormalizedMessage(msg, _subscription.getSecurityUniqueId(), _history);
    return normalizedMsg;
  }

  /**
   * Updates field history without sending any market data to field receivers. 
   * 
   * @param msg Unnormalized market data from underlying market data API.
   */
  public synchronized void updateFieldHistory(FudgeMsg msg) {
    FudgeMsg normalizedMsg = normalize(msg);
    if (normalizedMsg != null) {
      updateLastKnownValues(normalizedMsg);
    }
  }

  /**
   * Sends normalized market data to field receivers. 
   * <p>
   * Serialized to ensure a well-defined distribution order for this topic.
   * 
   * @param liveDataFields Unnormalized market data from underlying market data API.
   */
  public synchronized void distributeLiveData(FudgeMsg liveDataFields) {
    FudgeMsg normalizedMsg;
    try {
      normalizedMsg = normalize(liveDataFields);
    } catch (RuntimeException e) {
      s_logger.error("Normalizing " + liveDataFields + " to " + this + " failed.", e);
      return;
    }
    
    if (normalizedMsg != null) {
      updateLastKnownValues(normalizedMsg);
      
      LiveDataValueUpdateBean data = new LiveDataValueUpdateBean(
          getNumMessagesSent(), // 0-based as it should be
          getDistributionSpec().getFullyQualifiedLiveDataSpecification(),
          normalizedMsg);
      
      s_logger.debug("{}: Sending Live Data update {}", this, data);
      
      for (MarketDataSender sender : _marketDataSenders) {
        try {
          sender.sendMarketData(data);
        } catch (RuntimeException e) {
          s_logger.error(sender + " failed", e);
        }
      }
      
      _numMessagesSent.incrementAndGet();
    
    } else {
      s_logger.debug("{}: Not sending Live Data update (message extinguished).", this);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the expiry instant.
   * 
   * @return the millisecond instant from UTC epoch, or null if the distributor never expires
   */
  public synchronized Long getExpiry() {
    return _expiry;
  }

  /**
   * Sets the expiry instant.
   * 
   * @param expiry  the millisecond instant from UTC epoch, or null if the distributor never expires.
   */
  public synchronized void setExpiry(Long expiry) {
    _expiry = expiry;
  }

  /**
   * Extends the expiry instant to a number of milliseconds in the future.
   * 
   * @param timeoutExtensionMillis  the extension duration
   */
  public synchronized void extendExpiry(long timeoutExtensionMillis) {
    setExpiry(System.currentTimeMillis() + timeoutExtensionMillis);
  }

  /**
   * Checks if this has expired.
   * 
   * @return true if expired
   */
  public synchronized boolean hasExpired() {
    if (isPersistent()) {
      return false;      
    }
    if (getExpiry() == null) {
      return false;
    }
    return getExpiry() < System.currentTimeMillis();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this is persistent.
   * <p>
   * True = a persistent distributor, should survive a server restart.
   * False = a non-persistent distributor. Will die if the server is
   * restarted.
   * 
   * @return whether this distributor is persistent
   */
  public synchronized boolean isPersistent() {
    return _persistent;
  }

  /**
   * Sets the persistent flag.
   * 
   * @param persistent  whether this is persistent
   */
  public synchronized void setPersistent(boolean persistent) {
    _persistent = persistent;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "MarketDataDistributor[" + getDistributionSpec().toString() +  "]";    
  }

}
