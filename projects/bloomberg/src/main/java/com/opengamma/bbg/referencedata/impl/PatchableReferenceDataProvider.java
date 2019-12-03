/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A decorator for a ReferenceDataProvider that allows you to override the results
 * from the underlying provider e.g. if you have extra information from another source.
 */
public class PatchableReferenceDataProvider extends AbstractReferenceDataProvider {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(PatchableReferenceDataProvider.class);

  private final Map<Pair<String, String>, Object> _patches = new HashMap<>();
  private final Set<String> _securities = new HashSet<>();
  private final ReferenceDataProvider _underlying;

  /**
   * Creates an instance.
   *
   * @param underlying  the underlying source of reference data
   */
  public PatchableReferenceDataProvider(final ReferenceDataProvider underlying) {
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets an override or replacement value.
   *
   * @param security  the Bloomberg security identifier
   * @param field  the Bloomberg field name
   * @param result  the object to return as a result (must be possible to Fudge encode with standard OG dictionary)
   */
  public void setPatch(final String security, final String field, final Object result) {
    _patches.put(Pairs.of(security, field), result);
    _securities.add(security);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
    final ReferenceDataProviderGetResult rawResult = _underlying.getReferenceData(request);
    final ReferenceDataProviderGetResult newResult = new ReferenceDataProviderGetResult();

    for (final ReferenceData refData : rawResult.getReferenceData()) {
      final String identifier = refData.getIdentifier();
      if (_securities.contains(identifier)) {
        final FudgeMsg fieldData = refData.getFieldValues();
        final MutableFudgeMsg alteredFieldData = OpenGammaFudgeContext.getInstance().newMessage(fieldData);
        for (final String field : request.getFields()) {
          if (_patches.containsKey(Pairs.of(identifier, field))) {
            if (alteredFieldData.hasField(field)) {
              alteredFieldData.remove(field);
            }
            alteredFieldData.add(field, _patches.get(Pairs.of(identifier, field)));
            refData.removeErrors(field);
          }
        }
        LOGGER.debug("Patching {} with {}", new Object[] {fieldData, alteredFieldData });
        refData.setFieldValues(alteredFieldData);
      }
      newResult.addReferenceData(refData);
    }
    return newResult;
  }

}
