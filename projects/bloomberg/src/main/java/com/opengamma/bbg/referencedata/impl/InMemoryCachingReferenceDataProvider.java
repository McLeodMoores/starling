/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.AbstractValueCachingReferenceDataProvider;

/**
 * In memory caching reference data provider
 */
public class InMemoryCachingReferenceDataProvider extends AbstractValueCachingReferenceDataProvider implements ReferenceDataProvider {

  private final Map<String, ReferenceData> _refDataMap = Maps.newHashMap();

  public InMemoryCachingReferenceDataProvider(final ReferenceDataProvider underlying) {
    this(underlying, Collections.<String, ReferenceData>emptyMap());
  }

  public InMemoryCachingReferenceDataProvider(final ReferenceDataProvider underlying, final Map<String, ReferenceData> prePopulated) {
    super(underlying);
    _refDataMap.putAll(prePopulated);
  }

  @Override
  protected synchronized Map<String, ReferenceData> loadFieldValues(final Set<String> identifiers) {
    final Map<String, ReferenceData> filtered = Maps.filterKeys(_refDataMap, Predicates.in(identifiers));
    return ImmutableMap.copyOf(filtered);
  }

  @Override
  protected synchronized void saveFieldValues(final ReferenceData result) {
    _refDataMap.put(result.getIdentifier(), result);
  }

  /**
   * An immutable map containing the records accessed by this instance
   * @return a map of reference data objects
   */
  public synchronized ImmutableMap<String, ReferenceData> getDataAccessed() {
    return ImmutableMap.copyOf(_refDataMap);
  }

  /**
   * Adds the given data to the map
   * @param refData the data to add
   */
  public synchronized void addToCache(final Map<String, ReferenceData> refData) {
    _refDataMap.putAll(refData);
  }


}
