/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Abstract implementation of a provider of reference data.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
 */
public abstract class AbstractReferenceDataProvider implements ReferenceDataProvider {

  /**
   * Creates an instance.
   */
  public AbstractReferenceDataProvider() {
  }

  //-------------------------------------------------------------------------
  @Override
  public String getReferenceDataValue(final String identifier, final String dataField) {
    return getReferenceDataValues(ImmutableSet.of(identifier), dataField).get(identifier);
  }

  @Override
  public Map<String, String> getReferenceDataValues(final String identifier, final Iterable<String> dataFields) {
    final Set<String> fields = ImmutableSet.copyOf(dataFields);  // copy to avoid implementation bugs
    final Map<String, FudgeMsg> data = getReferenceData(ImmutableSet.of(identifier), dataFields);

    // extract field to value
    final Map<String, String> map = Maps.newHashMap();
    final FudgeMsg msg = data.get(identifier);
    if (msg != null) {
      for (final String field : fields) {
        final String value = msg.getString(field);
        if (value != null) {
          map.put(field, value);
        }
      }
    }
    return map;
  }

  @Override
  public Map<String, String> getReferenceDataValues(final Iterable<String> identifiers, final String dataField) {
    final Map<String, FudgeMsg> data = getReferenceData(identifiers, ImmutableSet.of(dataField));

    // extract identifier to value
    final Map<String, String> map = Maps.newHashMap();
    for (final String identifier : data.keySet()) {
      final String value = data.get(identifier).getString(dataField);
      if (value != null) {
        map.put(identifier, value);
      }
    }
    return map;
  }

  @Override
  public Map<String, FudgeMsg> getReferenceData(final Iterable<String> identifiers, final Iterable<String> dataFields) {
    final ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(identifiers, dataFields, true);
    return queryMap(request, this);
  }

  @Override
  public Map<String, FudgeMsg> getReferenceDataIgnoreCache(final Iterable<String> identifiers, final Iterable<String> dataFields) {
    final ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(identifiers, dataFields, false);
    return queryMap(request, this);
  }

  protected static Map<String, FudgeMsg> queryMap(final ReferenceDataProviderGetRequest request, final ReferenceDataProvider provider) {
    final Set<String> identifiers = ImmutableSet.copyOf(request.getIdentifiers()); // copy to avoid implementation bugs
    final Set<String> fields = ImmutableSet.copyOf(request.getFields()); // copy to avoid implementation bugs
    final ReferenceDataProviderGetResult result = provider.getReferenceData(request);
    // extract identifier to field-values
    final Map<String, FudgeMsg> map = Maps.newHashMap();
    for (final String identifier : identifiers) {
      final ReferenceData data = result.getReferenceDataOrNull(identifier);
      if (data != null) {
        // filter results by error list (if clause is an optimization)
        if (data.getErrors().size() == 0) {
          map.put(identifier, data.getFieldValues());
        } else {
          if (data.isIdentifierError() == false) {
            final MutableFudgeMsg values = OpenGammaFudgeContext.getInstance().newMessage(data.getFieldValues());
            for (final String field : fields) {
              if (data.isError(field)) {
                values.remove(field);
              }
            }
            map.put(identifier, values);
          }
        }
      }
    }
    return map;
  }

  @Override
  public ReferenceDataProviderGetResult getReferenceData(final ReferenceDataProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");

    // short-cut empty case
    if (request.getIdentifiers().isEmpty()) {
      return new ReferenceDataProviderGetResult();
    }
    if (request.getFields().isEmpty()) {
      final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
      for (final String identifier : request.getIdentifiers()) {
        result.addReferenceData(new ReferenceData(identifier));
      }
      return result;
    }

    // get time-series
    return doBulkGet(request);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reference data.
   *
   * @param request  the request, with a non-empty collections, not null
   * @return the result, not null
   */
  protected abstract ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request);

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
