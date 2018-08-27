/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import java.lang.reflect.Method;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.CombiningBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionSelector;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.AbstractInvalidFieldCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.AbstractValueCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import net.sf.ehcache.CacheManager;

/**
 * Test utilities for Bloomberg.
 */
public class BloombergLiveDataServerUtils {

  /**
   * Gets a reference data provider for a class, defined by a method.
   *
   * @param testMethod  the test method, not null
   * @return the data provider, not null
   */
  public static ReferenceDataProvider getCachingReferenceDataProvider(final Method testMethod) {
    return getCachingReferenceDataProvider(testMethod.getClass());
  }

  /**
   * Gets a reference data provider for a class.
   *
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  public static ReferenceDataProvider getCachingReferenceDataProvider(final Class<?> testClass) {
    final BloombergReferenceDataProvider brdp = getUnderlyingProvider();
    return getCachingReferenceDataProvider(brdp, testClass);
  }

  /**
   * Adds caching to a reference data provider.
   *
   * @param underlying  the underlying provider, not null
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  private static ReferenceDataProvider getCachingReferenceDataProvider(final ReferenceDataProvider underlying, final Class<?> testClass) {
    return MongoCachedReferenceData.makeMongoProvider(underlying, testClass);
  }

  /**
   * Creates a Bloomberg reference data provider, that has been started, for testing.
   *
   * @return the provider, not null
   */
  public static BloombergReferenceDataProvider getUnderlyingProvider() {
    final BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    final BloombergReferenceDataProvider brdp = new BloombergReferenceDataProvider(connector);
    brdp.start();
    return brdp;
  }

  /**
   * Stops the specified reference data provider, as best as possible.
   *
   * @param refDataProvider  the provider to stop, null ignored
   */
  public static void stopCachingReferenceDataProvider(final ReferenceDataProvider refDataProvider) {
    if (refDataProvider != null) {
      if (refDataProvider instanceof BloombergReferenceDataProvider) {
        final BloombergReferenceDataProvider bbgProvider = (BloombergReferenceDataProvider) refDataProvider;
        bbgProvider.stop();

      } else if (refDataProvider instanceof AbstractValueCachingReferenceDataProvider) {
        stopCachingReferenceDataProvider(((AbstractValueCachingReferenceDataProvider) refDataProvider).getUnderlying());

      } else if (refDataProvider instanceof AbstractInvalidFieldCachingReferenceDataProvider) {
        stopCachingReferenceDataProvider(((AbstractInvalidFieldCachingReferenceDataProvider) refDataProvider).getUnderlying());
      }
    }
  }

  //-------------------------------------------------------------------------
  public static BloombergLiveDataServer startTestServer(final Method testMethod) {
    return startTestServer(testMethod.getClass());
  }

  public static BloombergLiveDataServer startTestServer(final Class<?> testClass) {
    final ReferenceDataProvider refDataProvider = getCachingReferenceDataProvider(testClass);
    return getTestServer(refDataProvider);
  }

  public static void stopTestServer(final BloombergLiveDataServer testServer) {
    stopCachingReferenceDataProvider(testServer.getReferenceDataProvider());
    testServer.stop();
  }

  public static BloombergLiveDataServer getTestServer(final ReferenceDataProvider cachingRefDataProvider) {
    final FudgeMessageSender fudgeMessageSender = new FudgeMessageSender() {
      @Override
      public void send(final FudgeMsg message) {
        // do nothing
      }
      @Override
      public FudgeContext getFudgeContext() {
        return OpenGammaFudgeContext.getInstance();
      }
    };
    final BloombergLiveDataServer server = new BloombergLiveDataServer(BloombergTestUtils.getBloombergConnector(),
                                                                 cachingRefDataProvider,
                                                                 EHCacheUtils.createCacheManager(),
                                                                 fudgeMessageSender);
    final DistributionSpecificationResolver distributionSpecificationResolver = server.getDefaultDistributionSpecificationResolver();
    server.setDistributionSpecificationResolver(distributionSpecificationResolver);

    server.start();
    return server;
  }

  public static CombiningBloombergLiveDataServer startTestServer(final Class<?> testClass, final FakeSubscriptionSelector subscriptionSelector, final ReferenceDataProvider refDataProvider) {
    final ReferenceDataProvider cachingRefDataProvider = getCachingReferenceDataProvider(refDataProvider, testClass);
    final BloombergLiveDataServer underlying = getTestServer(cachingRefDataProvider);

    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    final FakeSubscriptionBloombergLiveDataServer fakeServer = new FakeSubscriptionBloombergLiveDataServer(underlying, ExternalSchemes.BLOOMBERG_BUID_WEAK, cacheManager);
    fakeServer.start();

    final CombiningBloombergLiveDataServer combinedServer = new CombiningBloombergLiveDataServer(fakeServer, underlying, subscriptionSelector, cacheManager);

    combinedServer.start();
    return combinedServer;
  }

  public static void stopTestServer(final StandardLiveDataServer server) {
    if (server instanceof BloombergLiveDataServer) {
      stopTestServer((BloombergLiveDataServer) server);
    } else if (server instanceof CombiningLiveDataServer) {
      stopTestServer(((CombiningBloombergLiveDataServer) server).getFakeServer());
    }
  }

}
