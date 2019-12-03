/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static com.opengamma.bbg.BloombergConstants.VALID_EQUITY_TYPES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.livedata.resolver.JmsTopicNameResolveRequest;
import com.opengamma.livedata.resolver.JmsTopicNameResolver;
import com.opengamma.util.ArgumentChecker;

/**
 *
 *
 */
public class BloombergJmsTopicNameResolver extends AbstractResolver<JmsTopicNameResolveRequest, String> implements JmsTopicNameResolver {

  private static final Logger LOGGER = LoggerFactory
  .getLogger(BloombergJmsTopicNameResolver.class);

  private final ReferenceDataProvider _referenceDataProvider;

  private static final Set<String> BBG_VALID_EQUITY_TYPES = VALID_EQUITY_TYPES;

  public BloombergJmsTopicNameResolver(final ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "Reference Data Provider");
    _referenceDataProvider = referenceDataProvider;
  }

  /**
   * @return the referenceDataProvider
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  @Override
  public Map<JmsTopicNameResolveRequest, String> resolve(final Collection<JmsTopicNameResolveRequest> requests) {
    final Map<JmsTopicNameResolveRequest, String> returnValue = new HashMap<>();

    final Map<String, Collection<JmsTopicNameResolveRequest>> lookupKey2Requests = new HashMap<>();

    for (final JmsTopicNameResolveRequest request : requests) {
      final ExternalId marketDataUniqueId = request.getMarketDataUniqueId();
      if (!marketDataUniqueId.getScheme().equals(ExternalSchemes.BLOOMBERG_BUID)) {
        LOGGER.info("No Bloomberg BUID found, was given: " + marketDataUniqueId);
        returnValue.put(request, null);
        continue;
      }

      final String lookupKey = BloombergDomainIdentifierResolver.toBloombergKey(marketDataUniqueId);

      Collection<JmsTopicNameResolveRequest> requestsForLookupKey = lookupKey2Requests.get(lookupKey);
      if (requestsForLookupKey == null) {
        requestsForLookupKey = new ArrayList<>();
        lookupKey2Requests.put(lookupKey, requestsForLookupKey);
      }
      requestsForLookupKey.add(request);
    }

    if (!lookupKey2Requests.keySet().isEmpty()) {
      final ReferenceDataProviderGetRequest rdRequest = ReferenceDataProviderGetRequest.createGet(
          lookupKey2Requests.keySet(), BloombergConstants.JMS_TOPIC_NAME_RESOLVER_FIELDS, true);
      final ReferenceDataProviderGetResult referenceData = _referenceDataProvider.getReferenceData(rdRequest);

      for (final Map.Entry<String, Collection<JmsTopicNameResolveRequest>> entry : lookupKey2Requests.entrySet()) {
        final String lookupKey = entry.getKey();
        final ReferenceData result = referenceData.getReferenceDataOrNull(lookupKey);

        for (final JmsTopicNameResolveRequest request : entry.getValue()) {
          final String jmsTopicName = getJmsTopicName(request, result);
          returnValue.put(request, jmsTopicName);
        }
      }
    }

    return returnValue;
  }

  private String getJmsTopicName(final JmsTopicNameResolveRequest request, final ReferenceData result) {
    if (result == null) {
      LOGGER.info("No reference data available for {}", request);
      return null;
    } else if (result.isIdentifierError()) {
      LOGGER.info("Failed to retrieve reference data for {}: {}", request, result.getErrors());
      return null;
    }
    final FudgeMsg resultFields = result.getFieldValues();

    final String prefix = "LiveData" + SEPARATOR + "Bloomberg" + SEPARATOR;
    final String suffix = request.getNormalizationRule().getJmsTopicSuffix();
    final String bbgUniqueId = request.getMarketDataUniqueId().getValue();
    final String defaultTopicName = prefix + bbgUniqueId + suffix;

    final String bbgSecurityType = resultFields.getString(BloombergConstants.FIELD_SECURITY_TYPE);
    if (bbgSecurityType == null) {
      return defaultTopicName;

    } else if (BBG_VALID_EQUITY_TYPES.contains(bbgSecurityType)) {

      final String bbgExchange = resultFields.getString(BloombergConstants.FIELD_PRIMARY_EXCHANGE_NAME);
      final String bbgTicker = resultFields.getString(BloombergConstants.FIELD_TICKER);

      if (bbgExchange == null || bbgTicker == null) {
        return defaultTopicName;
      }

      return prefix + "Equity" + SEPARATOR + bbgExchange + SEPARATOR + bbgTicker + suffix;

    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_US_DOMESTIC_BOND_SECURITY_TYPE)) {

      final String issuer = resultFields.getString(BloombergConstants.FIELD_ISSUER);
      final String cusip = resultFields.getString(BloombergConstants.FIELD_ID_CUSIP);

      if (issuer == null || cusip == null) {
        return defaultTopicName;
      }

      return prefix + "Bond" + SEPARATOR + issuer + SEPARATOR + cusip + suffix;

    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_GLOBAL_BOND_SECURITY_TYPE)) {

      final String issuer = resultFields.getString(BloombergConstants.FIELD_ISSUER);
      final String isin = resultFields.getString(BloombergConstants.FIELD_ID_ISIN);

      if (issuer == null || isin == null) {
        return defaultTopicName;
      }

      return prefix + "Bond" + SEPARATOR + issuer + SEPARATOR + isin + suffix;

    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE)) {

      final String underlyingTicker = resultFields.getString(BloombergConstants.FIELD_OPT_UNDL_TICKER);
      final String ticker = resultFields.getString(BloombergConstants.FIELD_TICKER);

      if (underlyingTicker == null || ticker == null) {
        return defaultTopicName;
      }

      return prefix + "EquityOption" + SEPARATOR + underlyingTicker + SEPARATOR + ticker + suffix;

    } else {
      return defaultTopicName;
    }
  }

}
