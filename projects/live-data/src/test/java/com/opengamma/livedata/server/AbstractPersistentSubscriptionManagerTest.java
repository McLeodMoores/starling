/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

@Test(groups = {TestGroup.UNIT, "ehcache"})
public class AbstractPersistentSubscriptionManagerTest {

  //TODO test async logic
  private final ExternalScheme _scheme = ExternalScheme.of("SomeScheme");
  private final String _normalizationRulesetId = StandardRules.getNoNormalization().getId();
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testNormalStartup() throws InterruptedException {
    final MockLiveDataServer server = new MockLiveDataServer(_scheme, _cacheManager);
    final TestPersistentSubscriptionManager subManager = new TestPersistentSubscriptionManager(server);
    final String ticker = "X";
    subManager.getPendingReads().add(Sets.newHashSet(getSubscription(ticker)));
    server.start();
    subManager.start();
    Thread.sleep(1000);
    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    subManager.stop();
    server.stop();
  }

  @Test
  public void testLateRefresh() throws InterruptedException {
    final MockLiveDataServer server = new MockLiveDataServer(_scheme, _cacheManager);
    final TestPersistentSubscriptionManager subManager = new TestPersistentSubscriptionManager(server);
    final String ticker = "X";
    subManager.getPendingReads().add(new HashSet<PersistentSubscription>());
    subManager.getPendingReads().add(Sets.newHashSet(getSubscription(ticker)));
    server.start();
    subManager.start();
    Thread.sleep(1000);
    assertEquals(new HashSet<String>(), server.getActiveSubscriptionIds());

    server.subscribe(ticker);

    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    final MarketDataDistributor marketDataDistributor = server.getMarketDataDistributor(ticker);
    assertEquals(false, marketDataDistributor.isPersistent());

    subManager.refresh();
    Thread.sleep(1000);
    assertEquals(true, marketDataDistributor.isPersistent());
    assertEquals(Sets.newHashSet(ticker), server.getActiveSubscriptionIds());
    subManager.stop();
    server.stop();
  }

  private PersistentSubscription getSubscription(final String ticker) {
    return new PersistentSubscription(getSpec(ticker));
  }

  private LiveDataSpecification getSpec(final String ticker) {
    return new LiveDataSpecification(_normalizationRulesetId, ExternalId.of(_scheme, ticker));
  }

  class TestPersistentSubscriptionManager extends AbstractPersistentSubscriptionManager  {

    TestPersistentSubscriptionManager(final StandardLiveDataServer server) {
      super(server);
    }

    private final Queue<Set<PersistentSubscription>> _pendingReads = new LinkedBlockingQueue<>();
    private final Queue<Set<PersistentSubscription>> _pendingWrites = new LinkedBlockingQueue<>();

    @Override
    protected void readFromStorage() {
      final Set<PersistentSubscription> remove = _pendingReads.remove();
      for (final PersistentSubscription persistentSubscription : remove) {
        addPersistentSubscription(persistentSubscription);
      }
    }

    @Override
    public void saveToStorage(final Set<PersistentSubscription> newState) {
      _pendingWrites.add(newState);
    }

    public Queue<Set<PersistentSubscription>> getPendingReads() {
      return _pendingReads;
    }

    public Queue<Set<PersistentSubscription>> getPendingWrites() {
      return _pendingWrites;
    }
  }

}
