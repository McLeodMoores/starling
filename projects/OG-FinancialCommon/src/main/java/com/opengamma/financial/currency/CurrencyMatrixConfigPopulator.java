/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.currency;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generates and stores currency matrix configurations into the configuration database.
 */
public final class CurrencyMatrixConfigPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyMatrixConfigPopulator.class);

  /**
   * Bloomberg currency matrix config name.
   */
  public static final String BLOOMBERG_LIVE_DATA = "BloombergLiveData";

  /**
   * Synthetic currency matrix config name.
   */
  public static final String SYNTHETIC_LIVE_DATA = "SyntheticLiveData";

  /**
   * Restricted constructor.
   */
  private CurrencyMatrixConfigPopulator() {
  }

  /**
   * Creates a currency matrix that uses synthetic tickers. This configuration should only be used for tests and simulated examples.
   * This method assumes that there is a {@link CurrencyPairs} configuration called {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS} stored in the master.
   * @param configMaster  the config master, not null
   */
  public static void populateSyntheticCurrencyMatrix(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    final CurrencyPairs currencies =
        new MasterConfigSource(configMaster).getSingle(CurrencyPairs.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, VersionCorrection.LATEST);
    storeCurrencyMatrix(configMaster, SYNTHETIC_LIVE_DATA, createSyntheticConversionMatrix(currencies));
  }

  /**
   * Creates a currency matrix that uses Bloomberg tickers. This method assumes that there is a {@link CurrencyPairs} configuration called
   * {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS} stored in the master.
   * @param configMaster  the config master, not null
   */
  public static void populateBloombergCurrencyMatrix(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    final CurrencyPairs currencies =
        new MasterConfigSource(configMaster).getSingle(CurrencyPairs.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, VersionCorrection.LATEST);
    storeCurrencyMatrix(configMaster, BLOOMBERG_LIVE_DATA, createBloombergConversionMatrix(currencies));
  }

  /**
   * Creates a synthetic currency matrix and a Bloomberg ticker currency matrix. This method assumes that there is a {@link CurrencyPairs} configuration called
   * {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS} stored in the master.
   * @param cfgMaster  the config master, not null
   * @return  the config master populated with the configurations
   * @deprecated  the currency matrices should be generated as required
   */
  @Deprecated
  public static ConfigMaster populateCurrencyMatrixConfigMaster(final ConfigMaster cfgMaster) {
    ArgumentChecker.notNull(cfgMaster, "cfgMaster");
    final CurrencyPairs currencies =
        new MasterConfigSource(cfgMaster).getSingle(CurrencyPairs.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, VersionCorrection.LATEST);
    storeCurrencyMatrix(cfgMaster, BLOOMBERG_LIVE_DATA, createBloombergConversionMatrix(currencies));
    storeCurrencyMatrix(cfgMaster, SYNTHETIC_LIVE_DATA, createSyntheticConversionMatrix(currencies));
    return cfgMaster;
  }

  /**
   * Stores a currency matrix in the config master.
   * @param cfgMaster  the config master
   * @param name  the configuration name
   * @param currencyMatrix  the matrix
   */
  private static void storeCurrencyMatrix(final ConfigMaster cfgMaster, final String name, final CurrencyMatrix currencyMatrix) {
    final ConfigItem<CurrencyMatrix> doc = ConfigItem.of(currencyMatrix, name, CurrencyMatrix.class);
    doc.setName(name);
    ConfigMasterUtils.storeByName(cfgMaster, doc);
  }

  /**
   * Generates a currency matrix that contains Bloomberg tickers.
   * @param currencies  the currency pairs, not null
   * @return  the matrix
   */
  public static CurrencyMatrix createBloombergConversionMatrix(final CurrencyPairs currencies) {
    ArgumentChecker.notNull(currencies, "currencies");
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    for (final CurrencyPair pair : currencies.getPairs()) {
      matrix.setLiveData(pair.getCounter(), pair.getBase(),
          new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalSchemes.bloombergTickerSecurityId(pair.getBase().getCode()
              + pair.getCounter().getCode() + " Curncy")));
    }
    dumpMatrix(matrix);
    return matrix;
  }

  /**
   * Generates a currency matrix that contains synthetic tickers. This matrix should only be used for tests and simulated examples.
   * @param currencies  the currency pairs, not null
   * @return  the matrix
   */
  public static CurrencyMatrix createSyntheticConversionMatrix(final CurrencyPairs currencies) {
    ArgumentChecker.notNull(currencies, "currencies");
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    final Currency commonCross = Currency.USD;
    for (final CurrencyPair pair : currencies.getPairs()) {
      if (commonCross.equals(pair.getBase()) || commonCross.equals(pair.getCounter())) {
        matrix.setLiveData(pair.getCounter(), pair.getBase(),
            new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalSchemes.syntheticSecurityId(pair.getBase().getCode()
                + pair.getCounter().getCode())));
      }
    }
    for (final CurrencyPair pair : currencies.getPairs()) {
      if (!commonCross.equals(pair.getBase()) && !commonCross.equals(pair.getCounter())) {
        matrix.setCrossConversion(pair.getCounter(), pair.getBase(), commonCross);
      }
    }
    dumpMatrix(matrix);
    return matrix;

  }

  /**
   * Prints a logging message showing the structure of a currency matrix.
   * @param matrix  the matrix, not null
   */
  public static void dumpMatrix(final CurrencyMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    final StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (final Currency x : matrix.getTargetCurrencies()) {
      sb.append('\t').append(x.getCode());
    }
    for (final Currency y : matrix.getSourceCurrencies()) {
      sb.append('\n').append(y.getCode());
      for (final Currency x : matrix.getTargetCurrencies()) {
        sb.append('\t').append(matrix.getConversion(y, x));
      }
    }
    LOGGER.debug("Currency matrix = {}", sb);
  }

}
