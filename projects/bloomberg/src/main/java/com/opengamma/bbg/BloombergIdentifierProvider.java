/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_FIRST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_EXPIRE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_TYPE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Loads identifier bundle with dates for a given identifier.
 */
public class BloombergIdentifierProvider implements ExternalIdResolver {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergIdentifierProvider.class);

  /**
   * The Bloomberg data.
   */
  private final ReferenceDataProvider _bbgRefDataProvider;
  /**
   * The requested Bloomberg fields.
   */
  private final Set<String> _bbgFields = Sets.newHashSet(
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_PARSEKYABLE_DES,
      FIELD_FUT_FIRST_TRADE_DT,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_SECURITY_TYPE,
      FIELD_SECURITY_DES,
      FIELD_MARKET_SECTOR_DES,
      FIELD_OPT_EXPIRE_DT);

  /**
   * Creates a new instance.
   *
   * @param refDataProvider
   *          the Bloomberg data, not null
   */
  public BloombergIdentifierProvider(final ReferenceDataProvider refDataProvider) {
    ArgumentChecker.notNull(refDataProvider, "refDataProvider");
    _bbgRefDataProvider = refDataProvider;
  }

  // -------------------------------------------------------------------------
  @Override
  public Map<ExternalId, ExternalIdBundleWithDates> getExternalIds(final Set<ExternalId> unresolvedIdentifiers) {
    final Map<ExternalId, ExternalIdBundleWithDates> result = new HashMap<>(unresolvedIdentifiers.size());

    final Map<String, ExternalId> bloombergKeys = new HashMap<>(unresolvedIdentifiers.size());
    for (final ExternalId identifier : unresolvedIdentifiers) {
      final String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(identifier);
      if (bloombergKey != null) {
        bloombergKeys.put(bloombergKey, identifier);
      } else {
        LOGGER.warn("cannot resolve {} to bloomberg identifier", bloombergKey);
      }
    }

    final ReferenceDataProviderGetRequest refDataRequest = ReferenceDataProviderGetRequest.createGet(bloombergKeys.keySet(), _bbgFields, true);
    final ReferenceDataProviderGetResult refDataResult = _bbgRefDataProvider.getReferenceData(refDataRequest);

    for (final ReferenceData refData : refDataResult.getReferenceData()) {
      final String securityDes = refData.getIdentifier();

      // check no exceptions
      if (refData.isIdentifierError()) {
        final List<ReferenceDataError> errors = refData.getErrors();
        if (errors != null && errors.size() > 0) {
          for (final ReferenceDataError error : errors) {
            LOGGER.warn("Exception looking up {}/{} - {}",
                new Object[] { securityDes, _bbgFields, error });
          }
          continue;
        }
      }

      // check same security was returned
      final String refSec = refData.getIdentifier();
      if (!securityDes.equals(refSec)) {
        LOGGER.warn("Returned security {} not the same as searched security {}", refSec, securityDes);
        continue;
      }
      // get field data
      final FudgeMsg fieldData = refData.getFieldValues();

      final String securityType = fieldData.getString(FIELD_SECURITY_TYPE);
      if (securityType != null && securityType.toUpperCase().contains(" FUTURE")) {
        final String firstTradeStr = fieldData.getString(FIELD_FUT_FIRST_TRADE_DT);
        final LocalDate firstTradeDate = parseDate(firstTradeStr);
        final String lastTradeStr = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
        final LocalDate lastTradeDate = parseDate(lastTradeStr);
        if (lastTradeDate.isBefore(firstTradeDate)) {
          LOGGER.warn("Reference data for {} indicates last trade date ({}) before first trade date ({}) - ignoring",
              new Object[] { securityDes, lastTradeDate, firstTradeDate });
          continue;
        }
      }

      try {
        final ExternalIdBundleWithDates bundleWithDates = parseIdentifiers(fieldData);
        if (bundleWithDates != null) {
          result.put(bloombergKeys.get(securityDes), bundleWithDates);
        }
      } catch (final Exception e) {
        LOGGER.error("Error parsing identifiers for security " + securityDes, e);
        throw new OpenGammaRuntimeException("Error parsing identifiers for security " + securityDes, e);
      }
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * Parses the response to a bundle.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier bundle, null if not available
   */
  private ExternalIdBundleWithDates parseIdentifiers(final FudgeMsg fieldData) {
    ArgumentChecker.notNull(fieldData, "fieldData");

    final Set<ExternalIdWithDates> identifiers = new HashSet<>();
    final ExternalIdWithDates buid = makeBuidIdentifier(fieldData);
    if (buid != null) {
      identifiers.add(buid);
    }
    final ExternalIdWithDates cusip = makeCusipIdentifier(fieldData);
    if (cusip != null) {
      identifiers.add(cusip);
    }
    final ExternalIdWithDates isin = makeIsinIdentifier(fieldData);
    if (isin != null) {
      identifiers.add(isin);
    }
    final ExternalIdWithDates sedol1 = makeSedol1Identifier(fieldData);
    if (sedol1 != null) {
      identifiers.add(sedol1);
    }
    final ExternalIdWithDates ticker = makeTickerIdentifier(fieldData);
    if (ticker != null) {
      identifiers.add(ticker);
    }
    return identifiers.isEmpty() ? null : new ExternalIdBundleWithDates(identifiers);
  }

  // -------------------------------------------------------------------------
  /**
   * Extract and build a ticker identifier.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier, null if not found
   */
  private ExternalIdWithDates makeTickerIdentifier(final FudgeMsg fieldData) {
    ExternalIdWithDates result = null;
    if (fieldData != null) {
      final String securityType = fieldData.getString(FIELD_SECURITY_TYPE);
      if (securityType != null) {
        if (securityType.contains("GOVERNMENT")) {
          return result;
        }
        if (securityType.contains("Option")) {
          final String securityDes = fieldData.getString(FIELD_SECURITY_DES);
          final String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
          final String expiry = fieldData.getString(FIELD_OPT_EXPIRE_DT);
          final LocalDate validTo = parseDate(expiry);
          final LocalDate validFrom = null;
          if (BloombergDataUtils.isValidField(securityDes)) {
            if (BloombergDataUtils.isValidField(marketSector)) {
              final ExternalId tickerId = ExternalSchemes.bloombergTickerSecurityId(securityDes + " " + marketSector);
              result = ExternalIdWithDates.of(tickerId, validFrom, validTo);
            }
          }
        } else {
          final String securityIdentifier = fieldData.getString(FIELD_PARSEKYABLE_DES);
          final Pair<LocalDate, LocalDate> futureValidFromTo = getFutureValidFromTo(fieldData);
          if (BloombergDataUtils.isValidField(securityIdentifier)) {
            final ExternalId tickerId = ExternalSchemes.bloombergTickerSecurityId(BloombergDataUtils.removeDuplicateWhiteSpace(securityIdentifier, " "));
            result = ExternalIdWithDates.of(tickerId, futureValidFromTo.getFirst(), futureValidFromTo.getSecond());
          }
        }
      }
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * Extract and build a SEDOL1 identifier.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier, null if not found
   */
  private ExternalIdWithDates makeSedol1Identifier(final FudgeMsg fieldData) {
    final String sedol1 = fieldData.getString(FIELD_ID_SEDOL1);
    if (!BloombergDataUtils.isValidField(sedol1)) {
      return null;
    }
    final ExternalId sedol1Id = ExternalSchemes.sedol1SecurityId(sedol1);
    return ExternalIdWithDates.of(sedol1Id, null, null);
  }

  /**
   * Extract and build a ISIN identifier.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier, null if not found
   */
  private ExternalIdWithDates makeIsinIdentifier(final FudgeMsg fieldData) {
    final String isin = fieldData.getString(FIELD_ID_ISIN);
    if (!BloombergDataUtils.isValidField(isin)) {
      return null;
    }
    final ExternalId isinId = ExternalSchemes.isinSecurityId(isin);
    return ExternalIdWithDates.of(isinId, null, null);
  }

  /**
   * Extract and build a CUSIP identifier.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier, null if not found
   */
  private ExternalIdWithDates makeCusipIdentifier(final FudgeMsg fieldData) {
    final String cusip = fieldData.getString(FIELD_ID_CUSIP);
    if (!BloombergDataUtils.isValidField(cusip)) {
      return null;
    }
    final Pair<LocalDate, LocalDate> validFromTo = getFutureValidFromTo(fieldData);
    final ExternalId cusipId = ExternalSchemes.cusipSecurityId(cusip);
    return ExternalIdWithDates.of(cusipId, validFromTo.getFirst(), validFromTo.getSecond());
  }

  /**
   * Extract and build a BIUD identifier.
   *
   * @param fieldData
   *          the Bloomberg field data, not null
   * @return the identifier, null if not found
   */
  private ExternalIdWithDates makeBuidIdentifier(final FudgeMsg fieldData) {
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    if (!BloombergDataUtils.isValidField(bbgUnique)) {
      return null;
    }
    final ExternalId buid = ExternalSchemes.bloombergBuidSecurityId(bbgUnique);
    return ExternalIdWithDates.of(buid, null, null);
  }

  // -------------------------------------------------------------------------
  /**
   * Parses a Bloomberg formatted date.
   *
   * @param dateStr
   *          the date, not null
   * @return the parsed date, null if not available
   */
  private LocalDate parseDate(final String dateStr) {
    if (!BloombergDataUtils.isValidField(dateStr)) {
      return null;
    }
    try {
      return LocalDate.parse(dateStr);
    } catch (final DateTimeParseException ex) {
      LOGGER.warn("valid from date not in yyyy-mm-dd format - {}", dateStr);
      return null;
    }
  }

  private Pair<LocalDate, LocalDate> getFutureValidFromTo(final FudgeMsg fieldData) {
    final String validFromStr = fieldData.getString(FIELD_FUT_FIRST_TRADE_DT);
    LocalDate validFrom = parseDate(validFromStr);
    final String validToStr = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    final LocalDate validTo = parseDate(validToStr);
    if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
      validFrom = null;
    }
    return Pairs.of(validFrom, validTo);
  }

}
