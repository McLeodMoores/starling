/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import net.sf.ehcache.CacheManager;

/**
 * Abstract cache.
 *
 * @param <A>  the first type
 * @param <B>  the second type
 * @deprecated  typo in class name. Use {@link HierarchicalEHCache}.
 */
@Deprecated
public abstract class HierarhicalEHCache<A, B> extends HierarchicalEHCache<A, B> {

  /**
   * @param cacheManager  the cache manager
   */
  public HierarhicalEHCache(final CacheManager cacheManager) {
    super(cacheManager);
  }
}
