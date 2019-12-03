/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link TypeBasedMarketDataProviderResolver}.
 */
public class TypeBasedMarketDataProviderResolverFactoryBean extends SingletonFactoryBean<TypeBasedMarketDataProviderResolver> {

  private final Map<Class<?>, MarketDataProviderFactory> _providers = new HashMap<>();

  public Map<Class<?>, MarketDataProviderFactory> getTypesAndProviders() {
    return _providers;
  }

  public void setTypesAndProviders(final Map<Class<?>, MarketDataProviderFactory> typesAndProviders) {
    _providers.clear();
    _providers.putAll(typesAndProviders);
  }

  @Override
  protected TypeBasedMarketDataProviderResolver createObject() {
    final TypeBasedMarketDataProviderResolver resolver = new TypeBasedMarketDataProviderResolver();
    for (final Map.Entry<Class<?>, MarketDataProviderFactory> typeAndProvider : getTypesAndProviders().entrySet()) {
      resolver.addProvider(typeAndProvider.getKey(), typeAndProvider.getValue());
    }
    return resolver;
  }

}
