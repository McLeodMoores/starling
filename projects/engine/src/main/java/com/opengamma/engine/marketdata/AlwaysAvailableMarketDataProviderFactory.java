/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.spec.AlwaysAvailableMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Factory for building {@link AlwaysAvailableMarketDataProvider}s.
 */
public class AlwaysAvailableMarketDataProviderFactory implements MarketDataProviderFactory {

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser, final MarketDataSpecification marketDataSpec) {
    if (marketDataSpec instanceof AlwaysAvailableMarketDataSpecification) {
      return new AlwaysAvailableMarketDataProvider();
    }
    throw new IllegalStateException("Asking AlwaysAvailableMarketDataProviderFactory for instance using incompatible spec type " + marketDataSpec);
  }

}
