/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Cache factory.
 * @param <D>  the document type
 */
public class UidToDocumentCacheEntryFactory<D extends AbstractDocument> implements CacheEntryFactory {

  /** The underlying master. */
  private final AbstractChangeProvidingMaster<D> _underlying;

  public UidToDocumentCacheEntryFactory(final AbstractChangeProvidingMaster<D> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  @Override
  public Object createEntry(final Object key) throws Exception {
    return _underlying.get((UniqueId) key);
  }

}
