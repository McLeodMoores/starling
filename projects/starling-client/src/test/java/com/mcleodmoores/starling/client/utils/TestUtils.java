/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.utils;

import java.util.HashMap;

import com.mcleodmoores.starling.client.component.StarlingToolContext;
import com.mcleodmoores.starling.client.marketdata.DataField;
import com.mcleodmoores.starling.client.marketdata.MarketDataKey;
import com.mcleodmoores.starling.client.marketdata.MarketDataSet;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Utilities for unit tests.
 */
public final class TestUtils {
  /** 
   * Id scheme name for unit tests.
   */
  public static final ExternalScheme TEST_SUITE = ExternalScheme.of("TEST_SUITE");
  /** The logging configuration file */
  private static final String LOGGING_CONFIG = "logback.configurationFile";
  /** The default logging set up */
  private static final String DEFAULT_LOGGING_FILE = "com/opengamma/util/warn-logback.xml";

  /**
   * Restricted constructor for utilities class.
   */
  private TestUtils() {
  }
  
  /**
   * Gets a test tool context.
   * @return  the test tool context
   */
  public static ToolContext getToolContext() {
    if (System.getProperty(LOGGING_CONFIG) == null) {
      System.setProperty(LOGGING_CONFIG, DEFAULT_LOGGING_FILE);
    }
    final OpenGammaComponentServer server = new OpenGammaComponentServer();
    final ComponentManager componentManager = server.createManager("classpath:/inmemory/inmemory.properties",
        new HashMap<String, String>());
    final ComponentRepository repository = componentManager.getRepository();
    componentManager.init();
    componentManager.start();
    return repository.getInstance(StarlingToolContext.class, "tool");
  }

  /**
   * Creates a test data set containing USD zero rates, IR futures, FX rates and AUD and NZD FX forward rates.
   * @return  the test data set
   */
  public static MarketDataSet createTestDataSet() {
    final MarketDataSet dataSet = MarketDataSet.empty();
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZEROO/N").toBundle(), DataField.of("Market_Value")), 0.01);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO7D").toBundle(), DataField.of("Market_Value")), 0.011);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO1M").toBundle(), DataField.of("Market_Value")), 0.013);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO3M").toBundle(), DataField.of("Market_Value")), 0.015);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO1Y").toBundle(), DataField.of("Market_Value")), 0.02);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO2Y").toBundle(), DataField.of("Market_Value")), 0.022);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO3Y").toBundle(), DataField.of("Market_Value")), 0.023);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO5Y").toBundle(), DataField.of("Market_Value")), 0.024);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDZERO10Y").toBundle(), DataField.of("Market_Value")), 0.025);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDJPY").toBundle(), DataField.of("Market_Value")), 120);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "USDCHF").toBundle(), DataField.of("Market_Value")), 0.92);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "EURUSD").toBundle(), DataField.of("Market_Value")), 1.14);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "GBPUSD").toBundle(), DataField.of("Market_Value")), 1.58);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD").toBundle(), DataField.of("Market_Value")), 0.81);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD").toBundle(), DataField.of("Market_Value")), 0.75);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CADUSD").toBundle(), DataField.of("Market_Value")), 0.83);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDM15").toBundle(), DataField.of("Market_Value")), 0.9972);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDU15").toBundle(), DataField.of("Market_Value")), 0.9962);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDZ15").toBundle(), DataField.of("Market_Value")), 0.9946);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDH16").toBundle(), DataField.of("Market_Value")), 0.9929);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDM16").toBundle(), DataField.of("Market_Value")), 0.9908);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDU16").toBundle(), DataField.of("Market_Value")), 0.9886);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDZ16").toBundle(), DataField.of("Market_Value")), 0.9865);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDH17").toBundle(), DataField.of("Market_Value")), 0.9847);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDM17").toBundle(), DataField.of("Market_Value")), 0.983);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDU17").toBundle(), DataField.of("Market_Value")), 0.9814);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDZ17").toBundle(), DataField.of("Market_Value")), 0.9799);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDH18").toBundle(), DataField.of("Market_Value")), 0.9788);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDM18").toBundle(), DataField.of("Market_Value")), 0.9777);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDU18").toBundle(), DataField.of("Market_Value")), 0.9767);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDZ18").toBundle(), DataField.of("Market_Value")), 0.9757);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDH19").toBundle(), DataField.of("Market_Value")), 0.9749);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDM19").toBundle(), DataField.of("Market_Value")), 0.974);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDU19").toBundle(), DataField.of("Market_Value")), 0.9733);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "CME/EDZ19").toBundle(), DataField.of("Market_Value")), 0.9725);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSDOVERNIGHT").toBundle(), DataField.of("Market_Value")), 0.809859);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD7D").toBundle(), DataField.of("Market_Value")), 0.809953);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD14D").toBundle(), DataField.of("Market_Value")), 0.809663);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD21D").toBundle(), DataField.of("Market_Value")), 0.809332);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD1M").toBundle(), DataField.of("Market_Value")), 0.809004);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD2M").toBundle(), DataField.of("Market_Value")), 0.808492);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD3M").toBundle(), DataField.of("Market_Value")), 0.80709);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD4M").toBundle(), DataField.of("Market_Value")), 0.80572);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD5M").toBundle(), DataField.of("Market_Value")), 0.80456);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD6M").toBundle(), DataField.of("Market_Value")), 0.80332);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD7M").toBundle(), DataField.of("Market_Value")), 0.80212);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD8M").toBundle(), DataField.of("Market_Value")), 0.79998);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD9M").toBundle(), DataField.of("Market_Value")), 0.79896);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD10M").toBundle(), DataField.of("Market_Value")), 0.79794);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD11M").toBundle(), DataField.of("Market_Value")), 0.79706);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD1Y").toBundle(), DataField.of("Market_Value")), 0.7962);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD2Y").toBundle(), DataField.of("Market_Value")), 0.7884);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "AUDUSD5Y").toBundle(), DataField.of("Market_Value")), 0.7718);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSDOVERNIGHT").toBundle(), DataField.of("Market_Value")), 0.749924);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD7D").toBundle(), DataField.of("Market_Value")), 0.749468);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD14D").toBundle(), DataField.of("Market_Value")), 0.748962);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD21D").toBundle(), DataField.of("Market_Value")), 0.748451);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD1M").toBundle(), DataField.of("Market_Value")), 0.747665);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD2M").toBundle(), DataField.of("Market_Value")), 0.745415);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD3M").toBundle(), DataField.of("Market_Value")), 0.743245);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD4M").toBundle(), DataField.of("Market_Value")), 0.74124);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD5M").toBundle(), DataField.of("Market_Value")), 0.739105);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD6M").toBundle(), DataField.of("Market_Value")), 0.737015);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD7M").toBundle(), DataField.of("Market_Value")), 0.734815);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD8M").toBundle(), DataField.of("Market_Value")), 0.73305);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD9M").toBundle(), DataField.of("Market_Value")), 0.73119);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD10M").toBundle(), DataField.of("Market_Value")), 0.72921);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD11M").toBundle(), DataField.of("Market_Value")), 0.727535);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD1Y").toBundle(), DataField.of("Market_Value")), 0.725825);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD2Y").toBundle(), DataField.of("Market_Value")), 0.70738);
    dataSet.put(MarketDataKey.of(ExternalId.of(TEST_SUITE, "NZDUSD5Y").toBundle(), DataField.of("Market_Value")), 0.6654);
    return dataSet;
  }
}
