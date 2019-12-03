/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeMsg;

import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import net.sf.ehcache.CacheManager;

/**
 *
 */
public class MockLiveDataServer extends StandardLiveDataServer {

  private final ExternalScheme _domain;
  private final List<String> _subscriptions = new ArrayList<>();
  private final List<String> _unsubscriptions = new ArrayList<>();
  private volatile int _numConnections; // = 0;
  private volatile int _numDisconnections; // = 0;
  private final Map<String, FudgeMsg> _uniqueId2MarketData;

  public MockLiveDataServer(final ExternalScheme domain, final CacheManager cacheManager) {
    this(domain, new ConcurrentHashMap<String, FudgeMsg>(), cacheManager);
  }

  public MockLiveDataServer(final ExternalScheme domain, final Map<String, FudgeMsg> uniqueId2Snapshot, final CacheManager cacheManager) {
    super(cacheManager);
    ArgumentChecker.notNull(domain, "Identification domain");
    ArgumentChecker.notNull(uniqueId2Snapshot, "Snapshot map");
    _domain = domain;
    _uniqueId2MarketData = uniqueId2Snapshot;
  }

  //-------------------------------------------------------------------------
  public void addMarketDataMapping(final String key, final FudgeMsg value) {
    _uniqueId2MarketData.put(key, value);
  }

  @Override
  public ExternalScheme getUniqueIdDomain() {
    return _domain;
  }

  @Override
  protected Map<String, Object> doSubscribe(final Collection<String> uniqueIds) {
    final Map<String, Object> returnValue = new HashMap<>();

    for (final String uniqueId : uniqueIds) {
      _subscriptions.add(uniqueId);
      returnValue.put(uniqueId, uniqueId);
    }

    return returnValue;
  }

  @Override
  protected void doUnsubscribe(final Collection<Object> subscriptionHandles) {
    for (final Object subscriptionHandle : subscriptionHandles) {
      _unsubscriptions.add((String) subscriptionHandle);
    }
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(final Collection<String> uniqueIds) {
    final Map<String, FudgeMsg> returnValue = new HashMap<>();

    for (final String uniqueId : uniqueIds) {
      FudgeMsg snapshot = _uniqueId2MarketData.get(uniqueId);
      if (snapshot == null) {
        snapshot = OpenGammaFudgeContext.getInstance().newMessage();
      }
      returnValue.put(uniqueId, snapshot);
    }

    return returnValue;
  }

  public void sendLiveDataToClient() {
    for (final Subscription subscription : getSubscriptions()) {
      final FudgeMsg marketData = doSnapshot(subscription.getSecurityUniqueId());
      liveDataReceived(subscription.getSecurityUniqueId(), marketData);
    }
  }

  public List<String> getActualSubscriptions() {
    return _subscriptions;
  }

  public List<String> getActualUnsubscriptions() {
    return _unsubscriptions;
  }

  @Override
  protected void doConnect() {
    _numConnections++;
  }

  @Override
  protected void doDisconnect() {
    _numDisconnections++;
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(
      final Subscription subscription) {
    return false;
  }

  public int getNumConnections() {
    return _numConnections;
  }

  public int getNumDisconnections() {
    return _numDisconnections;
  }

  @Override
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getNoNormalization().getId();
  }

}
