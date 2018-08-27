/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.RedisCachingSecuritySource;

import redis.clients.jedis.JedisPool;

/**
 *
 */
public class RedisCachingFinancialSecuritySource extends RedisCachingSecuritySource implements FinancialSecuritySource {
  private final FinancialSecuritySource _financialUnderlying;

  public RedisCachingFinancialSecuritySource(final FinancialSecuritySource underlying, final JedisPool jedisPool, final String redisPrefix, final FudgeContext fudgeContext) {
    super(underlying, jedisPool, redisPrefix, fudgeContext);
    _financialUnderlying = underlying;
  }

  /**
   * Gets the financialUnderlying.
   * @return the financialUnderlying
   */
  protected FinancialSecuritySource getFinancialUnderlying() {
    return _financialUnderlying;
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(final String issuerName) {
    final Collection<Security> results = getFinancialUnderlying().getBondsWithIssuerName(issuerName);
    processResults(results);
    return results;
  }

}
