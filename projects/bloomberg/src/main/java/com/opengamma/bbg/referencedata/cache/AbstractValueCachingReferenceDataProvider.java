/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Abstract reference data provider decorator that caches field values.
 * <p>
 * It is recommended to use a cache over the underlying provider to avoid excess queries on Bloomberg.
 */
public abstract class AbstractValueCachingReferenceDataProvider extends AbstractReferenceDataProvider {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValueCachingReferenceDataProvider.class);
  /**
   * Constant used when field not available.
   */
  private static final String FIELD_NOT_AVAILABLE_NAME = "NOT_AVAILABLE_FIELD";

  /**
   * The underlying provider.
   */
  private final ReferenceDataProvider _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   *
   * @param underlying
   *          the underlying provider, not null
   */
  protected AbstractValueCachingReferenceDataProvider(final ReferenceDataProvider underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   *
   * @param underlying
   *          the underlying provider, not null
   * @param fudgeContext
   *          the Fudge context, not null
   */
  protected AbstractValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the underlying provider.
   *
   * @return the underlying provider, not null
   */
  public ReferenceDataProvider getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   *
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  // -------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
    // if use-cache is false, then do not cache
    if (request.isUseCache() == false) {
      return getUnderlying().getReferenceData(request);
    }

    // load from cache
    final Map<String, ReferenceData> cachedResults = loadFieldValues(request.getIdentifiers());

    // filter the request removing known invalid fields
    final Map<Set<String>, Set<String>> identifiersByFields = buildUnderlyingRequestGroups(request, cachedResults);

    // process everything that remains
    ReferenceDataProviderGetResult resolvedResults = loadAndPersistUnknownFields(cachedResults, identifiersByFields);
    resolvedResults = stripUnwantedFields(resolvedResults, request.getFields());
    return resolvedResults;
  }

  protected ReferenceDataProviderGetResult stripUnwantedFields(final ReferenceDataProviderGetResult resolvedResults, final Set<String> fields) {
    final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    for (final ReferenceData unstippedDataResult : resolvedResults.getReferenceData()) {
      final String identifier = unstippedDataResult.getIdentifier();
      final ReferenceData strippedDataResult = new ReferenceData(identifier);
      strippedDataResult.getErrors().addAll(unstippedDataResult.getErrors());
      final MutableFudgeMsg strippedFields = getFudgeContext().newMessage();
      final FudgeMsg unstrippedFieldData = unstippedDataResult.getFieldValues();
      // check requested fields
      for (final String requestField : fields) {
        final List<FudgeField> fudgeFields = unstrippedFieldData.getAllByName(requestField);
        for (final FudgeField fudgeField : fudgeFields) {
          strippedFields.add(requestField, fudgeField.getValue());
        }
      }
      strippedDataResult.setFieldValues(strippedFields);
      result.addReferenceData(strippedDataResult);
    }
    return result;
  }

  protected ReferenceDataProviderGetResult loadAndPersistUnknownFields(
      final Map<String, ReferenceData> cachedResults,
      final Map<Set<String>, Set<String>> identifiersByFields) {

    // TODO kirk 2009-10-23 -- Also need to maintain securities we don't need to put back in the database.
    final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    // REVIEW kirk 2009-10-23 -- Candidate for scatter/gather.
    for (final Map.Entry<Set<String>, Set<String>> entry : identifiersByFields.entrySet()) {
      final Set<String> requestedIdentifiers = entry.getValue();
      final Set<String> requestedFields = entry.getKey();
      assert !requestedIdentifiers.isEmpty();
      if (entry.getKey().isEmpty()) {
        LOGGER.debug("Satisfied entire request for securities {} from cache", requestedIdentifiers);
        for (final String securityKey : requestedIdentifiers) {
          result.addReferenceData(cachedResults.get(securityKey));
        }
        continue;
      }
      LOGGER.info("Loading {} fields for {} securities from underlying", entry.getKey().size(), requestedIdentifiers.size());
      final ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(requestedIdentifiers, requestedFields, false);
      final ReferenceDataProviderGetResult loadedResult = getUnderlying().getReferenceData(underlyingRequest);
      for (final String identifier : requestedIdentifiers) {
        final ReferenceData cachedResult = cachedResults.get(identifier);
        ReferenceData freshResult = loadedResult.getReferenceDataOrNull(identifier);
        freshResult = freshResult != null ? freshResult : new ReferenceData(identifier);

        final ReferenceData resolvedResult = getCombinedResult(requestedFields, cachedResult, freshResult);
        saveFieldValues(resolvedResult);
        result.addReferenceData(resolvedResult);
      }
    }
    return result;
  }

  private ReferenceData getCombinedResult(final Set<String> requestedFields, final ReferenceData cachedResult, final ReferenceData freshResult) {
    MutableFudgeMsg unionFieldData = null;
    if (cachedResult == null) {
      unionFieldData = getFudgeContext().newMessage();
    } else {
      unionFieldData = getFudgeContext().newMessage(cachedResult.getFieldValues());
    }
    final Set<String> returnedFields = new HashSet<>();
    for (final FudgeField freshField : freshResult.getFieldValues().getAllFields()) {
      unionFieldData.add(freshField);
      returnedFields.add(freshField.getName());
    }

    // cache not available fields as well
    final Set<String> notAvaliableFields = Sets.newTreeSet(requestedFields);
    notAvaliableFields.removeAll(returnedFields);

    // add list of not available fields
    for (final String notAvailableField : notAvaliableFields) {
      unionFieldData.add(FIELD_NOT_AVAILABLE_NAME, notAvailableField);
    }

    // create combined result
    final ReferenceData resolvedResult = new ReferenceData(freshResult.getIdentifier(), unionFieldData);
    for (final ReferenceDataError error : freshResult.getErrors()) {
      if (resolvedResult.getErrors().contains(error) == false) {
        resolvedResult.getErrors().add(error);
      }
    }
    return resolvedResult;
  }

  /**
   * Examines and groups the request using the known invalid fields.
   *
   * @param request
   *          the request, not null
   * @param cachedResults
   *          the cached results, keyed by identifier, not null
   * @return the map of field-set to identifier-set, not null
   */
  protected Map<Set<String>, Set<String>> buildUnderlyingRequestGroups(final ReferenceDataProviderGetRequest request,
      final Map<String, ReferenceData> cachedResults) {
    final Map<Set<String>, Set<String>> result = Maps.newHashMap();
    for (final String identifier : request.getIdentifiers()) {
      // select known invalid fields for the identifier
      final ReferenceData cachedResult = cachedResults.get(identifier);

      // calculate the missing fields that must be queried from the underlying
      Set<String> missingFields = null;
      if (cachedResult == null) {
        missingFields = Sets.newHashSet(request.getFields());
      } else {
        missingFields = Sets.newHashSet(Sets.difference(request.getFields(), cachedResult.getFieldValues().getAllFieldNames()));
        // remove known not available fields from missingFields
        final List<String> notAvailableFieldNames = getNotAvailableFields(cachedResult);
        for (final String field : notAvailableFieldNames) {
          missingFields.remove(field);
        }
      }

      // build the grouped result map, keyed from field-set to identifier-set
      Set<String> resultIdentifiers = result.get(missingFields);
      if (resultIdentifiers == null) {
        resultIdentifiers = Sets.newTreeSet();
        result.put(missingFields, resultIdentifiers);
      }
      resultIdentifiers.add(identifier);
    }
    return result;
  }

  private List<String> getNotAvailableFields(final ReferenceData cachedResult) {
    final List<FudgeField> notAvailableFields = cachedResult.getFieldValues().getAllByName(FIELD_NOT_AVAILABLE_NAME);
    final List<String> notAvailableFieldNames = new ArrayList<>(notAvailableFields.size());
    for (final FudgeField field : notAvailableFields) {
      notAvailableFieldNames.add((String) field.getValue());
    }
    return notAvailableFieldNames;
  }

  // -------------------------------------------------------------------------
  /**
   * Loads the field values from the cache.
   *
   * @param identifiers
   *          the identifiers to find errors for, not null
   * @return the map of reference data keyed by identifier, not null
   */
  protected abstract Map<String, ReferenceData> loadFieldValues(Set<String> identifiers);

  /**
   * Saves the field value into the cache.
   *
   * @param result
   *          the result to save, not null
   */
  protected abstract void saveFieldValues(ReferenceData result);

  // -------------------------------------------------------------------------
  /**
   * Refreshes the cache.
   *
   * @param identifiers
   *          the identifiers, not null
   */
  public void refresh(final Set<String> identifiers) {
    // TODO bulk queries
    final Map<String, ReferenceData> cachedResults = loadFieldValues(identifiers);

    final Map<Set<String>, Set<String>> identifiersByFields = Maps.newHashMap();

    for (final String identifier : identifiers) {
      final ReferenceData cachedResult = cachedResults.get(identifier);
      if (cachedResult == null) {
        continue; // nothing to refresh
      }
      final Set<String> fields = new HashSet<>();
      fields.addAll(cachedResult.getFieldValues().getAllFieldNames());
      fields.addAll(getNotAvailableFields(cachedResult));
      fields.remove(FIELD_NOT_AVAILABLE_NAME);
      Set<String> secsForTheseFields = identifiersByFields.get(fields);
      if (secsForTheseFields == null) {
        secsForTheseFields = new HashSet<>();
        identifiersByFields.put(fields, secsForTheseFields);
      }
      secsForTheseFields.add(identifier);
    }

    for (final Entry<Set<String>, Set<String>> entry : identifiersByFields.entrySet()) {
      final Set<String> identifiersForTheseFields = entry.getValue();
      final Set<String> fields = entry.getKey();

      final ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(identifiersForTheseFields, fields, false);
      final ReferenceDataProviderGetResult underlyingResult = _underlying.getReferenceData(underlyingRequest);
      for (final ReferenceData refData : underlyingResult.getReferenceData()) {
        final ReferenceData previousResult = cachedResults.get(refData.getIdentifier());
        final ReferenceData resolvedResult = getCombinedResult(fields, new ReferenceData(refData.getIdentifier()), refData);
        if (differentCachedResult(previousResult, resolvedResult)) {
          saveFieldValues(resolvedResult);
        }
      }
    }
  }

  private boolean differentCachedResult(final ReferenceData previousResult, final ReferenceData resolvedResult) {
    if (previousResult.getIdentifier().equals(resolvedResult.getIdentifier()) == false) {
      throw new OpenGammaRuntimeException("Attempting to compare two different securities " + previousResult + " " + resolvedResult);
    }
    // TODO better, non ordered comparison
    if (previousResult.getFieldValues().toString().equals(resolvedResult.getFieldValues().toString())) {
      return false;
    }
    return true;
  }

}
