/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.server.ExpirationManager;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.livedata.server.SubscriptionListener;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.livedata.test.LiveDataClientTestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class CombiningBloombergLiveDataServerTest {

  private static final UserPrincipal TEST_USER = UserPrincipal.getTestUser();

  private CombiningBloombergLiveDataServer _server;
  private JmsLiveDataClient _liveDataClient;
  private BloombergReferenceDataProvider _underlying;
  private UnitTestingReferenceDataProvider _unitTestingProvider;

  @BeforeMethod
  public void setUpClass() {
    _underlying = BloombergLiveDataServerUtils.getUnderlyingProvider();
    _unitTestingProvider = new UnitTestingReferenceDataProvider(_underlying);
    _server = BloombergLiveDataServerUtils.startTestServer(
      CombiningBloombergLiveDataServerTest.class,
      new UnionFakeSubscriptionSelector(
        new BySchemeFakeSubscriptionSelector(ExternalSchemes.BLOOMBERG_BUID_WEAK, ExternalSchemes.BLOOMBERG_TICKER_WEAK),
        new ByTypeFakeSubscriptionSelector("SWAPTION VOLATILITY")),
      _unitTestingProvider);
    _liveDataClient = LiveDataClientTestUtils.getJmsClient(_server);
    _unitTestingProvider.reset();
  }

  @AfterMethod
  public void tearDownClass() {
    BloombergLiveDataServerUtils.stopTestServer(_server);
    _liveDataClient.stop();
    _underlying.stop();
  }

  //-------------------------------------------------------------------------
  @Test
  public void testFakeSubscribe() throws Exception {
    final ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "CZPFGQFC Curncy");
    final ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "USPFJD5W Curncy");
    final ExternalId workingStrong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USPFJD5W Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(broken, working, workingStrong);
    final int repeats = 2;
    final CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size() * repeats, 1 * repeats);
    for (int i = 0; i < repeats; i++) {
      subscribe(_liveDataClient, listener, instruments);
      unsubscribe(_liveDataClient, listener, instruments);
      _unitTestingProvider.rejectAllfurtherRequests();
    }

    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));

    LiveDataSubscriptionResponse workingSub = null;
    LiveDataSubscriptionResponse workingStrongSub = null;
    for (final LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(working)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        workingSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(broken)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
      } else if (response.getRequestedSpecification().getIdentifiers().contains(workingStrong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        workingStrongSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertEquals(workingSub.getFullyQualifiedSpecification(), workingStrongSub.getFullyQualifiedSpecification());
    assertEquals(workingSub.getTickDistributionSpecification(), workingStrongSub.getTickDistributionSpecification());
    final List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    final List<LiveDataValueUpdate> workingUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, working));
    assertEquals(allUpdates, workingUpdates);
    assertFalse(_unitTestingProvider.hadToRejectRequests()); // Necessary, since exceptions are expected from the live data service
  }

  //-------------------------------------------------------------------------
  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testRealSubscribe() throws Exception {
    final ExternalId strong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(strong);
    final CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);

    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(60000));
    unsubscribe(_liveDataClient, listener, instruments);
    for (final LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(strong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
      }
    }

    final List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    final List<LiveDataValueUpdate> stronUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, strong));
    assertEquals(allUpdates, stronUpdates);
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testMixedSubscribe() throws Exception {
    final ExternalId strong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    final ExternalId weak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "GBP Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(strong, weak);
    final CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);

    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    unsubscribe(_liveDataClient, listener, instruments);

    LiveDataSubscriptionResponse strongSub = null;
    LiveDataSubscriptionResponse weakSub = null;
    for (final LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(strong)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        strongSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(weak)) {
        assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.SUCCESS);
        weakSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertFalse(strongSub.getFullyQualifiedSpecification().equals(weakSub.getFullyQualifiedSpecification()));
    assertFalse(strongSub.getTickDistributionSpecification().equals(weakSub.getTickDistributionSpecification()));

    final List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    final List<LiveDataValueUpdate> stronUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, strong));
    final List<LiveDataValueUpdate> weakUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, weak));
    assertEquals(1, weakUpdates.size());
    assertEquals(allUpdates.size(), weakUpdates.size() + stronUpdates.size());
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testBrokenSubscribe() throws Exception {
    final ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USSV15F Curncy"); //Broken
    final ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(broken, working);
    final CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);

    subscribe(_liveDataClient, listener, instruments);
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    unsubscribe(_liveDataClient, listener, instruments);

    LiveDataSubscriptionResponse strongSub = null;
    LiveDataSubscriptionResponse weakSub = null;
    for (final LiveDataSubscriptionResponse response : listener.getSubscriptionResponses()) {
      if (response.getRequestedSpecification().getIdentifiers().contains(working)) {
        assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getSubscriptionResult());
        strongSub = response;
      } else if (response.getRequestedSpecification().getIdentifiers().contains(broken)) {
        assertEquals(LiveDataSubscriptionResult.NOT_PRESENT, response.getSubscriptionResult());
        weakSub = response;
      } else {
        throw new Exception("Unexpected subscription response");
      }
    }
    assertFalse(strongSub.getFullyQualifiedSpecification().equals(weakSub.getFullyQualifiedSpecification()));
    assertFalse(strongSub.getTickDistributionSpecification().equals(weakSub.getTickDistributionSpecification()));

    final List<LiveDataValueUpdate> allUpdates = listener.getValueUpdates();
    final List<LiveDataValueUpdate> brokenUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, broken));
    final List<LiveDataValueUpdate> workingUpdates = listener.getValueUpdates(getLiveDataSpec(_liveDataClient, working));
    assertEquals(0, brokenUpdates.size());
    assertEquals(allUpdates.size(), brokenUpdates.size() + workingUpdates.size());
  }

  @Test
  public void testExpiration() throws Exception {
    final int period = 15000;
    final ExpirationManager expirationManager = _server.getExpirationManager();
    expirationManager.stop();
    expirationManager.setCheckPeriod(15000);
    expirationManager.setTimeoutExtension(15000);
    expirationManager.start();

    final AtomicInteger combinedSubs = countSubscriptions(_server);
    final AtomicInteger fakeSubs = countSubscriptions(_server.getFakeServer());
    final AtomicInteger realSubs = countSubscriptions(_server.getRealServer());

    final ExternalId weak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "GBP Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(weak);
    final CollectingLiveDataListener listener = new CollectingLiveDataListener(1, 1);

    subscribe(_liveDataClient, listener, instruments);

    assertEquals(1, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());
    assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
    for (int i = 0; i < 3; i++) {
      expirationManager.extendPublicationTimeout(ImmutableSet.of(getLiveDataSpec(_liveDataClient, weak)));
      Thread.sleep(period / 2);
    }
    assertEquals(1, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());

    unsubscribe(_liveDataClient, listener, instruments);

    Thread.sleep(period * 2);
    assertEquals(0, combinedSubs.get());
    assertEquals(combinedSubs.get(), fakeSubs.get());
    assertEquals(0, realSubs.get());
  }

  private AtomicInteger countSubscriptions(final StandardLiveDataServer server) {
    final AtomicInteger fakeSubs = new AtomicInteger(0);
    server.addSubscriptionListener(new SubscriptionListener() {
      @Override
      public void unsubscribed(final Subscription subscription) {
        fakeSubs.decrementAndGet();
      }
      @Override
      public void subscribed(final Subscription subscription) {
        fakeSubs.incrementAndGet();
      }
    });
    return fakeSubs;
  }

  private void subscribe(final LiveDataClient liveDataClient, final LiveDataListener listener, final Collection<ExternalId> instruments) {
    final Collection<LiveDataSpecification> specs = getLiveDataSpecs(liveDataClient, instruments);
    liveDataClient.subscribe(TEST_USER, specs, listener);
  }

  private void unsubscribe(final LiveDataClient liveDataClient, final LiveDataListener listener, final Collection<ExternalId> instruments) {
    final Collection<LiveDataSpecification> specs = getLiveDataSpecs(liveDataClient, instruments);
    liveDataClient.unsubscribe(TEST_USER, specs, listener);
  }

  private Collection<LiveDataSpecification> getLiveDataSpecs(final LiveDataClient liveDataClient,
      final Collection<ExternalId> instruments) {
    final Collection<LiveDataSpecification> specs = new ArrayList<>(instruments.size());
    for (final ExternalId instrument : instruments) {
      specs.add(getLiveDataSpec(liveDataClient, instrument));
    }
    return specs;
  }

  private LiveDataSpecification getLiveDataSpec(final LiveDataClient liveDataClient, final ExternalId id) {
    final LiveDataSpecification requestedSpecification = new LiveDataSpecification(
        liveDataClient.getDefaultNormalizationRuleSetId(), id);
    return requestedSpecification;
  }

  @Test(groups = {"bbgSubscriptionTests" }, enabled = false)
  public void testRepeatedSubscriptions_BBG_80() throws Exception {
    final ExternalId broken = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USSV15F Curncy"); //Broken
    final ExternalId working = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GBP Curncy");
    final ExternalId workingWeak = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "USPFJD5W Curncy");
    final ExternalId workingStrong = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "USPFJD5W Curncy");

    final List<ExternalId> instruments = Lists.newArrayList(broken, working, workingWeak, workingStrong);
    for (int i = 0; i < 10; i++) {
      final CollectingLiveDataListener listener = new CollectingLiveDataListener(instruments.size(), 3);

      subscribe(_liveDataClient, listener, instruments);
      assertTrue(listener.waitUntilEnoughUpdatesReceived(30000));
      unsubscribe(_liveDataClient, listener, instruments);
      _unitTestingProvider.rejectAllfurtherRequests();
    }
    assertFalse(_unitTestingProvider.hadToRejectRequests());
  }

  //-------------------------------------------------------------------------
  public static class UnitTestingReferenceDataProvider extends AbstractReferenceDataProvider {
    private final ReferenceDataProvider _underlying;
    private final java.util.concurrent.atomic.AtomicBoolean _locked = new java.util.concurrent.atomic.AtomicBoolean();
    private final java.util.concurrent.atomic.AtomicBoolean _broken = new java.util.concurrent.atomic.AtomicBoolean();

    public UnitTestingReferenceDataProvider(final ReferenceDataProvider underlying) {
      _underlying = underlying;
    }

    public void reset() {
      _locked.set(false);
      _broken.set(false);
    }

    public void rejectAllfurtherRequests() {
      _locked.set(true);
    }

    public boolean hadToRejectRequests() {
      return _broken.get();
    }

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
      if (_locked.get()) {
        _broken.set(true);
      }
      assertFalse(_locked.get());
      return _underlying.getReferenceData(request);
    }
  }

}
