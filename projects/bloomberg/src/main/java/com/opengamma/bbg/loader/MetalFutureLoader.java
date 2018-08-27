/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BBG_BASE_METAL_TYPE;
import static com.opengamma.bbg.BloombergConstants.BBG_PRECIOUS_METAL_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_CONT_SIZE;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_UNITS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_UNDL_SPOT_TICKER;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for a Metal Future from Bloomberg.
 */
public class MetalFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MetalFutureLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_METAL_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH, // trading exchange
      FIELD_CRNCY,
      FIELD_FUTURES_CATEGORY,
      FIELD_FUT_TRADING_UNITS,
      FIELD_PARSEKYABLE_DES,
      FIELD_FUT_CONT_SIZE,
      FIELD_UNDL_SPOT_TICKER,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT));

  /**
   * The valid Bloomberg future categories for Metal Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = Collections.unmodifiableSet(Sets.newHashSet(
      BBG_PRECIOUS_METAL_TYPE,
      BBG_BASE_METAL_TYPE));

  private static final Map<String, ExternalId> SPOT_MAP = new HashMap<>();

  static {
    SPOT_MAP.put("GOLD", ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "GOLDS Comdty")); // or GOLDS Comdty?
  }

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public MetalFutureLoader(final ReferenceDataProvider referenceDataProvider) {
    super(LOGGER, referenceDataProvider, SecurityType.METAL_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    final String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    final String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    final String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    final String currencyStr = fieldData.getString(FIELD_CRNCY);
    final String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    final Double unitNumber = fieldData.getDouble(FIELD_FUT_CONT_SIZE);
    final String unitName = fieldData.getString(FIELD_FUT_TRADING_UNITS);
    final String underlyingTicker = fieldData.getString(FIELD_UNDL_SPOT_TICKER);
    final String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final double unitAmount = Double.valueOf(fieldData.getString(FIELD_FUT_VAL_PT));

    if (!isValidField(bbgUnique)) {
      LOGGER.warn("bbgUnique is null, cannot construct metal future security");
      return null;
    }
    if (!isValidField(expiryDate)) {
      LOGGER.warn("expiry date is null, cannot construct metal future security");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      LOGGER.warn("futures trading hours is null, cannot construct metal index future security");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      LOGGER.warn("settlement exchange is null, cannot construct metal future security");
      return null;
    }
    if (!isValidField(currencyStr)) {
      LOGGER.info("currency is null, cannot construct metal future security");
      return null;
    }
    if (!isValidField(category)) {
      LOGGER.info("category is null, cannot construct metal future security");
      return null;
    }
    if (!isValidField(unitName)) {
      LOGGER.info("unitName is null, cannot construct metal future security");
      return null;
    }
    if (unitNumber == null) {
      LOGGER.info("unitNumber is null, cannot construct metal future security");
      return null;
    }
    ExternalId underlying = null;
    if (underlyingTicker != null) {
      underlying = ExternalSchemes.bloombergTickerSecurityId(underlyingTicker);
    } else {
      for (final Map.Entry<String, ExternalId> entry : SPOT_MAP.entrySet()) {
        if (name.contains(entry.getKey())) {
          underlying = entry.getValue();
          break;
        }
      }
    }
    final Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }
    final Currency currency = Currency.parse(currencyStr);
    final MetalFutureSecurity security = new MetalFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, category);
    security.setUnitNumber(unitNumber);
    security.setUnitName(unitName);
    security.setUnderlyingId(underlying);
    security.setName(name);
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_METAL_FUTURE_FIELDS;
  }

}
