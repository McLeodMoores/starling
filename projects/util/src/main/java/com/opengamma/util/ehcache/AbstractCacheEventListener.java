/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * A simple no-op implementation of {@link CacheEventListener} which can be extended cleanly.
 */
public abstract class AbstractCacheEventListener implements CacheEventListener {

  @Override
  public void dispose() {
  }

  @Override
  public void notifyElementEvicted(final Ehcache cache, final Element element) {
  }

  @Override
  public void notifyElementExpired(final Ehcache cache, final Element element) {
  }

  @Override
  public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
  }

  @Override
  public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
  }

  @Override
  public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
  }

  @Override
  public void notifyRemoveAll(final Ehcache cache) {
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

}
