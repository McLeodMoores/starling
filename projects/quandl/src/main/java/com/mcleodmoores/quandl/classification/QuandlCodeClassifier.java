/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Classifies Quandl data based on the code.
 */
public class QuandlCodeClassifier {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCodeClassifier.class);
  /** The cache key */
  private static final String CACHE_KEY = "quandl-classifier-cache";
  /** The security type resolver */
  private static final QuandlSecurityTypeResolver SECURITY_TYPE_RESOLVER = new QuandlSecurityTypeResolver();
  /** Cache for the security types */
  private final Cache _cache;

  /**
   * Constructs an instance.
   * @param cacheManager  the cache manager, not null
   */
  public QuandlCodeClassifier(final CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    EHCacheUtils.addCache(cacheManager, CACHE_KEY);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_KEY);
  }

  /**
   * Gets the normalization factor for a given security. The normalization factor is the multiple
   * by which the market data is greater than the normalized value.
   * @param quandlCode The Quandl code of the security, not null
   * @return The normalization factor, or throws an exception if the normalization factor cannot be found
   */
  public Integer getNormalizationFactor(final String quandlCode) {
    ArgumentChecker.notNull(quandlCode, "buid");
    Element e = _cache.get(quandlCode);
    if (e != null) {
      LOGGER.debug("Obtained normalization factor for security " + quandlCode + " from cache");
      return (Integer) e.getObjectValue();
    }
    final Integer normalizationFactor = getNormalizationFactorCore(quandlCode);
    LOGGER.debug("Generated normalization factor {} for security {}", normalizationFactor, quandlCode);
    e = new Element(quandlCode, normalizationFactor);
    _cache.put(e);
    return normalizationFactor;
  }

  /**
   * Gets the normalization factor.
   * @param quandlCode The code
   * @return The normalization factor, or null if the security could not be classified.
   */
  private static Integer getNormalizationFactorCore(final String quandlCode) {
    final ExternalIdBundle idBundle = ExternalIdBundle.of(QuandlConstants.QUANDL_CODE, quandlCode);
    final QuandlSecurityType securityType = SECURITY_TYPE_RESOLVER.getSecurityType(Collections.singleton(idBundle)).get(idBundle);
    if (securityType == null) {
      LOGGER.warn("Unable to determine security type for {}", quandlCode);
      return null;
    }
    switch (securityType) {
      case CASH:
        return 100;
      case RATE_FUTURE:
        return 100;
      case SWAP:
        return 100;
      default:
        return 1;
    }
  }
}
