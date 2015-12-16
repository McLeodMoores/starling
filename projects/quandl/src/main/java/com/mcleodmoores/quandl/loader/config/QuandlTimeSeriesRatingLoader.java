/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;

/**
 *
 */
public class QuandlTimeSeriesRatingLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new QuandlTimeSeriesRatingLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<>();
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, QuandlConstants.QUANDL_DATA_SOURCE_NAME, 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, QuandlConstants.QUANDL_DATA_SOURCE_NAME, 1));
    final HistoricalTimeSeriesRating ratingConfig = HistoricalTimeSeriesRating.of(rules);
    final ConfigItem<HistoricalTimeSeriesRating> config = ConfigItem.of(ratingConfig, DEFAULT_CONFIG_NAME, HistoricalTimeSeriesRating.class);
    ConfigMasterUtils.storeByName(configMaster, config);
  }

}
