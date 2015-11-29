/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An implementation of a reference data provider that gets reference data from an underlying map
 * (identifier -> map(data field -> reference data)). The code is copied from <code>MockReferenceDataProvider</code>.
 */
public class MapReferenceDataProvider extends AbstractReferenceDataProvider {
  /** A map from identifier to map of data field to reference data */
  private Map<String, Multimap<String, String>> _refDataMap = new HashMap<>();

  /**
   * Creates an instance that populates the reference data map.
   * @param referenceData  the reference data, not null
   */
  public MapReferenceDataProvider(final Map<String, Multimap<String, String>> referenceData) {
    _refDataMap = ArgumentChecker.notNull(referenceData, "referenceData");
  }

  @Override
  protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (final String identifier : request.getIdentifiers()) {
      if (_refDataMap.containsKey(identifier)) {
        // known security
        final ReferenceData refData = new ReferenceData(identifier);
        final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();

        final Multimap<String, String> fieldMap = _refDataMap.get(identifier);
        if (fieldMap != null) {
          // security actually has data
          for (final String field : request.getFields()) {
            final Collection<String> values = fieldMap.get(field);
            if (values == null || values.isEmpty()) {
              throw new OpenGammaRuntimeException("Field not found: " + field + " in " + fieldMap.keySet());
            }
            for (final String value : values) {
              if (value != null) {
                if (value.contains("=")) {
                  final MutableFudgeMsg submsg = OpenGammaFudgeContext.getInstance().newMessage();
                  submsg.add(StringUtils.substringBefore(value, "="), StringUtils.substringAfter(value, "="));
                  msg.add(field, submsg);
                } else {
                  msg.add(field, value);
                }
              }
            }
          }
        }
        refData.setFieldValues(msg);
        result.addReferenceData(refData);

      } else {
        // security wasn't marked as known
        throw new OpenGammaRuntimeException("Security not found: " + identifier + " in " + _refDataMap.keySet());
      }
    }
    return result;
  }

}
