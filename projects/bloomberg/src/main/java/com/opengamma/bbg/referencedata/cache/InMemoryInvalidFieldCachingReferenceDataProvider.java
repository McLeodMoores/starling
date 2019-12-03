/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;

/**
 * Decorates a reference data provider, adding caching of invalid field errors.
 * <p>
 * The cache is implemented using memory.
 */
public class InMemoryInvalidFieldCachingReferenceDataProvider extends AbstractInvalidFieldCachingReferenceDataProvider {

  /**
   * The in memory cache.
   */
  private static final ConcurrentMap<String, Set<String>> CACHE = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   *
   * @param underlying  the underlying provider, not null
   */
  public InMemoryInvalidFieldCachingReferenceDataProvider(final ReferenceDataProvider underlying) {
    super(underlying);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void saveInvalidFields(final String identifier, final Set<String> invalidFields) {
    CACHE.put(identifier, invalidFields);
  }

  @Override
  protected Map<String, Set<String>> loadInvalidFields(final Set<String> identifiers) {
    final Map<String, Set<String>> result = Maps.newHashMap();
    for (final String identifier : identifiers) {
      final Set<String> invalidFields = CACHE.get(identifier);
      result.put(identifier, invalidFields);
    }
    return result;
  }

}
