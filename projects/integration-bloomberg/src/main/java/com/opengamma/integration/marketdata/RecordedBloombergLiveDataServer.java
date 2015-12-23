/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeMsg;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.bbg.livedata.AbstractBloombergLiveDataServer;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.replay.BloombergTick;
import com.opengamma.bbg.replay.BloombergTickReceiver;
import com.opengamma.bbg.replay.BloombergTicksReplayer;
import com.opengamma.bbg.replay.BloombergTicksReplayer.Mode;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

import net.sf.ehcache.CacheManager;

/**
 * Exposes a window of recorded Bloomberg data in an infinite loop to simulate a live data server.
 */
public class RecordedBloombergLiveDataServer extends AbstractBloombergLiveDataServer {

  private final BloombergTicksReplayer _tickReplayer;
  private final ReferenceDataProvider _referenceDataProvider;

  /**
   * Used to keep track of subscriptions ourself in a way that can be queried efficiently, every time a tick is
   * replayed, to see whether it is required.
   */
  private final ConcurrentMap<String, Object> _subscriptions = new ConcurrentHashMap<String, Object>();

  /**
   * Creates an instance, parsing the given times from ISO-8601 strings.
   *
   * @param rootTickPath  the recorded ticks directory
   * @param dataStart  the tick start time
   * @param dataEnd  the tick end time
   * @param referenceDataProvider  a source of reference data
   * @param cacheManager  the cache manager, not null
   */
  public RecordedBloombergLiveDataServer(final String rootTickPath, final String dataStart, final String dataEnd, final ReferenceDataProvider referenceDataProvider, final CacheManager cacheManager) {
    this(rootTickPath, ZonedDateTime.parse(dataStart), ZonedDateTime.parse(dataEnd), referenceDataProvider, cacheManager);
  }

  /**
   * Creates an instance.
   *
   * @param rootTickPath  the recorded ticks directory
   * @param dataStart  the tick start time
   * @param dataEnd  the tick end time
   * @param referenceDataProvider  a source of reference data
   * @param cacheManager  the cache manager, not null
   */
  public RecordedBloombergLiveDataServer(final String rootTickPath, final ZonedDateTime dataStart, final ZonedDateTime dataEnd, final ReferenceDataProvider referenceDataProvider,
      final CacheManager cacheManager) {
    super(cacheManager);
    final BloombergTickReceiver tickReceiver = new BloombergTickReceiver() {
      @Override
      public void tickReceived(final BloombergTick msg) {
        RecordedBloombergLiveDataServer.this.tickReceived(msg.getBuid(), msg.getFields());
      }
    };

    setEntitlementChecker(new LiveDataEntitlementChecker() {
      @Override
      public Map<LiveDataSpecification, Boolean> isEntitled(final UserPrincipal user, final Collection<LiveDataSpecification> requestedSpecifications) {
        final Map<LiveDataSpecification, Boolean> results = new HashMap<LiveDataSpecification, Boolean>();
        for (final LiveDataSpecification requestedSpec : requestedSpecifications) {
          results.put(requestedSpec, true);
        }
        return results;
      }

      @Override
      public boolean isEntitled(final UserPrincipal user, final LiveDataSpecification requestedSpecification) {
        return true;
      }
    });

    _tickReplayer = new BloombergTicksReplayer(Mode.ORIGINAL_LATENCY, rootTickPath, tickReceiver, dataStart, dataEnd, true, Collections.<String>emptySet());
    _referenceDataProvider = referenceDataProvider;
  }

  @Override
  protected void doConnect() {
    _tickReplayer.start();
  }

  @Override
  protected void doDisconnect() {
    _tickReplayer.stop();
  }

  @Override
  protected Map<String, Object> doSubscribe(final Collection<String> uniqueIds) {
    final Map<String, Object> subscriptions = new HashMap<String, Object>();
    for (final String uniqueId : uniqueIds) {
      subscriptions.put(uniqueId, uniqueId);
    }
    _subscriptions.putAll(subscriptions);
    return subscriptions;
  }

  @Override
  protected void doUnsubscribe(final Collection<Object> subscriptionHandles) {
    for (final Object subscriptionHandle : subscriptionHandles) {
      _subscriptions.remove(subscriptionHandle);
    }
  }

  @Override
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  private void tickReceived(final String buid, final FudgeMsg fields) {
    if (_subscriptions.containsKey(buid)) {
      liveDataReceived(buid, fields);
    }
  }

}
