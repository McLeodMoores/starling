/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_FIELDS_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_REFERENCE_DATA_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_SECURITIES_REQUEST;
import static com.opengamma.bbg.BloombergConstants.EID_DATA;
import static com.opengamma.bbg.BloombergConstants.ERROR_INFO;
import static com.opengamma.bbg.BloombergConstants.FIELD_DATA;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCEPTIONS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID;
import static com.opengamma.bbg.BloombergConstants.RESPONSE_ERROR;
import static com.opengamma.bbg.BloombergConstants.SECURITY;
import static com.opengamma.bbg.BloombergConstants.SECURITY_DATA;
import static com.opengamma.bbg.BloombergConstants.SECURITY_ERROR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.google.common.base.CharMatcher;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.AbstractBloombergStaticDataProvider;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Provider of reference-data from the Bloomberg data source.
 */
public class BloombergReferenceDataProvider extends AbstractReferenceDataProvider implements Lifecycle {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergReferenceDataProvider.class);

  /**
   * Implementation class.
   */
  private final BloombergReferenceDataRequestService _refDataService;

  /**
   * Creates an instance.
   * <p>
   * This will use the statistics tool in the connector.
   *
   * @param bloombergConnector
   *          the bloomberg connector, not null
   */
  public BloombergReferenceDataProvider(final BloombergConnector bloombergConnector) {
    this(ArgumentChecker.notNull(bloombergConnector, "bloombergConnector"), bloombergConnector.getReferenceDataStatistics());
  }

  /**
   * Creates an instance with statistics gathering.
   *
   * @param bloombergConnector
   *          the Bloomberg connector, not null
   * @param statistics
   *          the statistics to collect, not null
   */
  public BloombergReferenceDataProvider(final BloombergConnector bloombergConnector, final BloombergReferenceDataStatistics statistics) {
    _refDataService = new BloombergReferenceDataRequestService(bloombergConnector, statistics);
  }

  // -------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
    return _refDataService.doBulkGet(request);
  }

  // -------------------------------------------------------------------------
  @Override
  public void start() {
    _refDataService.start();
  }

  @Override
  public void stop() {
    _refDataService.stop();
  }

  @Override
  public boolean isRunning() {
    return _refDataService.isRunning();
  }

  // -------------------------------------------------------------------------
  /**
   * Loads reference-data from Bloomberg.
   */
  static class BloombergReferenceDataRequestService extends AbstractBloombergStaticDataProvider {
    /**
     * Bloomberg statistics.
     */
    private final BloombergReferenceDataStatistics _statistics;

    BloombergReferenceDataRequestService(final BloombergConnector bloombergConnector) {
      this(ArgumentChecker.notNull(bloombergConnector, "bloombergConnector"), bloombergConnector.getReferenceDataStatistics());
    }

    /**
     * Creates an instance.
     *
     * @param bloombergConnector
     *          the bloomberg connector, not null
     * @param statistics
     *          the bloomberg reference data statistics, not null
     */
    BloombergReferenceDataRequestService(final BloombergConnector bloombergConnector, final BloombergReferenceDataStatistics statistics) {
      super(bloombergConnector, BloombergConstants.REF_DATA_SVC_NAME);
      ArgumentChecker.notNull(statistics, "statistics");
      _statistics = statistics;
    }

    // -------------------------------------------------------------------------
    @Override
    protected Logger getLogger() {
      return LOGGER;
    }

    // -------------------------------------------------------------------------
    /**
     * Get reference-data from Bloomberg.
     *
     * @param request
     *          the request, not null
     * @return the reference-data result, not null
     */
    ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
      final Set<String> identifiers = request.getIdentifiers();
      final Set<String> dataFields = request.getFields();
      validateIdentifiers(identifiers);
      validateFields(dataFields);

      ensureStarted();
      getLogger().debug("Getting reference data for {}, fields {}", identifiers, dataFields);

      final Request bbgRequest = createRequest(identifiers, dataFields);
      _statistics.recordStatistics(identifiers, dataFields);
      ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
      try {
        final List<Element> resultElements = submitRequest(bbgRequest).get();
        if (resultElements == null || resultElements.isEmpty()) {
          getLogger().warn("Unable to get a Bloomberg response for {} fields for {}", dataFields, identifiers);
        } else {
          result = parse(identifiers, dataFields, resultElements);
        }
      } catch (InterruptedException | ExecutionException ex) {
        getLogger().warn(String.format("Unable to get a Bloomberg response fields:[%s] for security[%s]", dataFields, identifiers), ex);
      }
      return result;
    }

    // -------------------------------------------------------------------------
    /**
     * Checks that all the identifiers are valid.
     *
     * @param identifiers
     *          the set of identifiers, not null
     */
    private void validateIdentifiers(final Set<String> identifiers) {
      final Set<String> excluded = new HashSet<>();
      for (final String identifier : identifiers) {
        if (StringUtils.isEmpty(identifier)) {
          throw new IllegalArgumentException("Must not have any null or empty identifiers");
        }
        if (CharMatcher.ASCII.matchesAllOf(identifier) == false) {
          // [BBG-93] - The C++ interface is declared as UChar, so this just enforces that restriction
          excluded.add(identifier);
        }
      }
      if (excluded.size() > 0) {
        final String message = MessageFormatter.format("Request contains invalid identifiers {} from ({})", excluded, identifiers).getMessage();
        getLogger().error(message);
        throw new OpenGammaRuntimeException(message);
      }
    }

    /**
     * Checks that all the fields are valid.
     *
     * @param fields
     *          the set of fields, not null
     */
    private void validateFields(final Set<String> fields) {
      final Set<String> excluded = new HashSet<>();
      for (final String field : fields) {
        if (StringUtils.isEmpty(field)) {
          throw new IllegalArgumentException("Must not have any null or empty fields");
        }
        if (CharMatcher.ASCII.matchesAllOf(field) == false) {
          excluded.add(field);
        }
      }
      if (excluded.size() > 0) {
        final String message = MessageFormatter.format("Request contains invalid fields {} from ({})", excluded, fields).getMessage();
        getLogger().error(message);
        throw new OpenGammaRuntimeException(message);
      }
    }

    // -------------------------------------------------------------------------
    /**
     * Creates the Bloomberg request.
     *
     * @param identifiers
     *          the identifiers, not null
     * @param dataFields
     *          the datafields, not null
     * @return the bloomberg request, not null
     */
    private Request createRequest(final Set<String> identifiers, final Set<String> dataFields) {
      // create request
      final Request request = getService().createRequest(BLOOMBERG_REFERENCE_DATA_REQUEST);
      final Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);

      // identifiers
      for (final String identifier : identifiers) {
        if (StringUtils.isEmpty(identifier)) {
          throw new IllegalArgumentException("Must not have any null or empty securities");
        }
        securitiesElem.appendValue(identifier);
      }

      // fields
      final Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
      for (final String dataField : dataFields) {
        if (StringUtils.isEmpty(dataField)) {
          throw new IllegalArgumentException("Must not have any null or empty fields");
        }
        if (dataField.equals(BloombergConstants.FIELD_EID_DATA) == false) {
          fieldElem.appendValue(dataField);
        }
      }
      request.set("returnEids", true);
      return request;
    }

    /**
     * Performs the main work to parse the result from Bloomberg.
     * <p>
     * This is part of {@link #getFields(Set, Set)}.
     *
     * @param securityKeys
     *          the set of securities, not null
     * @param fields
     *          the set of fields, not null
     * @param resultElements
     *          the result elements from Bloomberg, not null
     * @return the parsed result, not null
     */
    private ReferenceDataProviderGetResult parse(final Set<String> securityKeys, final Set<String> fields, final List<Element> resultElements) {
      final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
      for (final Element resultElem : resultElements) {
        if (resultElem.hasElement(RESPONSE_ERROR)) {
          final Element responseError = resultElem.getElement(RESPONSE_ERROR);
          final String category = responseError.getElementAsString(BloombergConstants.CATEGORY);
          if ("LIMIT".equals(category)) {
            getLogger().error("Limit reached {}", responseError);
          }
          throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + fields + " fields for " + securityKeys + ": " + responseError);
        }

        final Element securityDataArray = resultElem.getElement(SECURITY_DATA);
        final int numSecurities = securityDataArray.numValues();
        for (int iSecurityElem = 0; iSecurityElem < numSecurities; iSecurityElem++) {
          final Element securityElem = securityDataArray.getValueAsElement(iSecurityElem);
          final String securityKey = securityElem.getElementAsString(SECURITY);
          final ReferenceData refData = new ReferenceData(securityKey);
          if (securityElem.hasElement(SECURITY_ERROR)) {
            final Element securityError = securityElem.getElement(SECURITY_ERROR);
            parseIdentifierError(refData, securityKey, securityError);
          }
          if (securityElem.hasElement(FIELD_DATA)) {
            parseFieldData(refData, securityElem.getElement(FIELD_DATA));
          }
          if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
            final Element fieldExceptions = securityElem.getElement(FIELD_EXCEPTIONS);
            parseFieldExceptions(refData, fieldExceptions);
          }
          if (securityElem.hasElement(EID_DATA)) {
            parseEidData(refData, securityElem.getElement(EID_DATA));
          }
          result.addReferenceData(refData);
        }
      }
      return result;
    }

    /**
     * Processes an error affecting the whole identifier.
     *
     * @param refData
     *          the per identifier reference data result, not null
     * @param securityKey
     *          the security identifier, not null
     * @param element
     *          the bloomberg element, not null
     */
    private void parseIdentifierError(final ReferenceData refData, final String securityKey, final Element element) {
      final ReferenceDataError error = buildError(null, element);
      if (error.isEntitlementError()) {
        getLogger().warn("Bloomberg referenceData security error: {} {}", securityKey, error.getMessage());
      } else {
        getLogger().warn("Bloomberg referenceData security error: {} {}", securityKey, element);
      }
      refData.addError(error);
    }

    /**
     * Processes the field data.
     *
     * @param refData
     *          the per identifier reference data result, not null
     * @param element
     *          the bloomberg element, not null
     */
    private void parseFieldData(final ReferenceData refData, final Element element) {
      final FudgeMsg fieldData = BloombergDataUtils.parseElement(element);
      refData.setFieldValues(fieldData);
    }

    /**
     * Processes the an error affecting a single field on a one identifier.
     *
     * @param refData
     *          the per identifier reference data result, not null
     * @param fieldExceptionArray
     *          the bloomberg data, not null
     */
    private void parseFieldExceptions(final ReferenceData refData, final Element fieldExceptionArray) {
      final int numExceptions = fieldExceptionArray.numValues();
      if (numExceptions > 0) {
        getLogger().warn("Bloomberg referenceData field exceptions: {}", fieldExceptionArray);
      }
      for (int i = 0; i < numExceptions; i++) {
        final Element exceptionElem = fieldExceptionArray.getValueAsElement(i);
        final String fieldId = exceptionElem.getElementAsString(FIELD_ID);
        final ReferenceDataError error = buildError(fieldId, exceptionElem.getElement(ERROR_INFO));
        refData.addError(error);
      }
    }

    /**
     * Processes the EID data.
     *
     * @param refData
     *          the per identifier reference data result, not null
     * @param eidElement
     *          the bloomberg element, not null
     */
    private void parseEidData(final ReferenceData refData, final Element eidElement) {
      for (int i = 0; i < eidElement.numValues(); i++) {
        refData.getEidValues().add(eidElement.getValueAsInt32(i));
      }
      if (refData.getFieldValues() instanceof MutableFudgeMsg) {
        final MutableFudgeMsg fieldValues = (MutableFudgeMsg) refData.getFieldValues();
        for (final Integer eid : refData.getEidValues()) {
          fieldValues.add(BloombergConstants.EID_DATA.toString(), eid);
        }
      }
    }

    /**
     * Creates an instance from a Bloomberg element.
     *
     * @param field
     *          the field, null if linked to the identifier rather than a field
     * @param element
     *          the element, not null
     * @return the error, not null
     */
    private ReferenceDataError buildError(final String field, final Element element) {
      return new ReferenceDataError(
          field,
          element.getElementAsInt32(BloombergConstants.CODE),
          element.getElementAsString(BloombergConstants.CATEGORY),
          element.getElementAsString(BloombergConstants.SUBCATEGORY),
          element.getElementAsString(BloombergConstants.MESSAGE));
    }

  }

}
