/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BBG_WEEKLY_INDEX_OPTIONS_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_EQUITY_INDEX_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_UNDL_SPOT_TICKER;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/** Creates EquityFutureSecurity from fields loaded from Bloomberg. */
public class EquityFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EquityFutureLoader.class);

  /** The fields to load from Bloomberg */
  private static final Set<String> BLOOMBERG_EQUITY_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH,
      FIELD_CRNCY,
      FIELD_MARKET_SECTOR_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_UNDL_SPOT_TICKER,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT,
      FIELD_FUTURES_CATEGORY));

  /** The set of valid Bloomberg 'Futures Category Types' that will map to EquityFutureSecurity. */
  public static final Set<String> VALID_SECURITY_TYPES = ImmutableSet.of(
      BLOOMBERG_EQUITY_INDEX_TYPE,
      BBG_WEEKLY_INDEX_OPTIONS_TYPE); // THIS IS IFFY - 2EH3 INDEX, FOR EXAMPLE, HAS A FUTURE CATEGORY OF WEEKLY INDEX OPTIONS, THOUGH IT JUST AN ALIAS FOR ESH3
  // INDEX WHICH IS EQUITY INDEX);
  // TODO: Answer this: Are Equity Index Futures EquityFutureSecurity or IndexFutureSecurity? - See EquityFutureLoader, too

  /**
   * Creates an instance.
   *
   * @param referenceDataProvider
   *          the provider, not null
   */
  public EquityFutureLoader(final ReferenceDataProvider referenceDataProvider) {
    super(LOGGER, referenceDataProvider, SecurityType.EQUITY_FUTURE);
  }

  // -------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    final String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    final String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    final String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    final String currencyStr = fieldData.getString(FIELD_CRNCY);
    final String underlyingTicker = fieldData.getString(FIELD_UNDL_SPOT_TICKER);
    final String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    final String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    final String unitAmount = fieldData.getString(FIELD_FUT_VAL_PT);

    if (!isValidField(bbgUnique)) {
      LOGGER.warn("bbgUnique is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(expiryDate)) {
      LOGGER.warn("expiry date is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      LOGGER.warn("futures trading hours is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      LOGGER.warn("settlement exchange is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(currencyStr)) {
      LOGGER.info("currency is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(category)) {
      LOGGER.info("category is null, cannot construct EquityFutureSecurity");
      return null;
    }
    ExternalId underlying = null;
    if (underlyingTicker != null) {
      underlying = ExternalSchemes.bloombergTickerSecurityId(underlyingTicker + " " + marketSector);
    }

    final Currency currency = Currency.parse(currencyStr);

    final Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }

    // FIXME: Case - treatment of Settlement Date
    LOGGER.warn("Creating EquityFutureSecurity - settlementDate set equal to expiryDate. Missing lag.");
    final ZonedDateTime settlementDate = expiry.getExpiry();

    final EquityFutureSecurity security = new EquityFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, Double.valueOf(unitAmount),
        settlementDate, underlying, category);
    security.setName(name);
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_EQUITY_FUTURE_FIELDS;
  }

}
