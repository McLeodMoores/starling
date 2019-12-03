/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BBG_CORN;
import static com.opengamma.bbg.BloombergConstants.BBG_FOODSTUFF;
import static com.opengamma.bbg.BloombergConstants.BBG_LIVESTOCK;
import static com.opengamma.bbg.BloombergConstants.BBG_SOY;
import static com.opengamma.bbg.BloombergConstants.BBG_WHEAT;
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
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for an Agricultural Future from Bloomberg.
 */
public final class AgricultureFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AgricultureFutureLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_AGRICULTURE_FUTURE_FIELDS = ImmutableSet.of(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH, // trading exchange
      FIELD_CRNCY,
      FIELD_FUTURES_CATEGORY,
      FIELD_FUT_TRADING_UNITS,
      FIELD_PARSEKYABLE_DES,
      FIELD_FUT_CONT_SIZE,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT);

  /**
   * The valid Bloomberg future categories for Agriculture Futures.
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = ImmutableSet.of(BBG_CORN, BBG_WHEAT, BBG_SOY, BBG_LIVESTOCK, BBG_FOODSTUFF);

  /**
   * Creates an instance.
   * 
   * @param referenceDataProvider
   *          the provider, not null
   */
  public AgricultureFutureLoader(final ReferenceDataProvider referenceDataProvider) {
    super(LOGGER, referenceDataProvider, SecurityType.AGRICULTURE_FUTURE);
  }

  // -------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    final String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    final String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    final String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    final String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    final Double unitNumber = fieldData.getDouble(FIELD_FUT_CONT_SIZE);
    final String currencyStr = fieldData.getString(FIELD_CRNCY);
    final String futureCategory = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    final String unitName = fieldData.getString(FIELD_FUT_TRADING_UNITS);
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final double unitAmount = Double.valueOf(fieldData.getString(FIELD_FUT_VAL_PT));

    // validate params
    if (!isValidField(bbgUnique)) {
      LOGGER.warn("bbgUnique is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(name)) {
      LOGGER.warn("name is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(expiryDate)) {
      LOGGER.warn("expiry date is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      LOGGER.warn("futures trading hours is null, cannot construct bond future security");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      LOGGER.warn("settlement exchange is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(currencyStr)) {
      LOGGER.info("currency is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(futureCategory)) {
      LOGGER.info("futureCategory is null, cannot construct agriculture future security");
      return null;
    }
    if (!isValidField(unitName)) {
      LOGGER.info("unitName is null, cannot construct agriculture future security");
      return null;
    }
    if (unitNumber == null) {
      LOGGER.info("unitNumber is null, cannot construct agriculture future security");
      return null;
    }
    // decode string params
    final Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }

    final Currency currency = Currency.parse(currencyStr);
    final AgricultureFutureSecurity security = new AgricultureFutureSecurity(expiry, micExchangeCode, micExchangeCode,
        currency, unitAmount, futureCategory);
    security.setUnitNumber(unitNumber);
    security.setUnitName(unitName);
    security.setName(name);
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_AGRICULTURE_FUTURE_FIELDS;
  }

}
