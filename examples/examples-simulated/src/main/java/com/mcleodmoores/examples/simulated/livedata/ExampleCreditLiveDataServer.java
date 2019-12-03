/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.livedata;

import org.springframework.core.io.Resource;

import com.opengamma.examples.simulated.livedata.ExampleLiveDataServer;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.TerminatableJob;

import net.sf.ehcache.CacheManager;

/**
 *
 */
public class ExampleCreditLiveDataServer extends ExampleLiveDataServer {
  private final TerminatableJob _marketDataSimulatorJob = new StaticMarketDataJob();

  public ExampleCreditLiveDataServer(final CacheManager cacheManager, final Resource initialValuesFile) {
    super(cacheManager, initialValuesFile);
  }

  public ExampleCreditLiveDataServer(final CacheManager cacheManager, final Resource initialValuesFile, final double scalingFactor,
      final int maxMillisBetweenTicks) {
    super(cacheManager, initialValuesFile, scalingFactor, maxMillisBetweenTicks);
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return CdsRecoveryRateIdentifier.SAMEDAY_CDS_SCHEME;
  }

  @Override
  protected TerminatableJob getMarketDataSimulatorJob() {
    return _marketDataSimulatorJob;
  }

  /**
   * Provides market data that is not perturbed.
   */
  protected class StaticMarketDataJob extends SimulatedMarketDataJob {

    @Override
    protected double wiggleValue(final double value, final double centre) {
      return value;
    }
  }
}
