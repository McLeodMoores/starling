/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.scripts.Scriptable;

/**
 * The exchange-traded security loader tool
 */
@Scriptable
public class ExchangeTradedSecurityLoaderTool extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeTradedSecurityLoaderTool.class);
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Populate time series */
  private static final String POPULATE_TIME_SERIES_OPT = "ts";

  private static final String DEFAULT_DATA_PROVIDER = "DEFAULT";
  private static final String DEFAULT_DATA_FIELD = "PX_LAST";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { //CSIGNORE
    new ExchangeTradedSecurityLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {
    final ToolContext context = getToolContext();

    final SecurityLoader loader = context.getSecurityLoader();

    final Set<ExternalIdBundle> externalIdBundles = new LinkedHashSet<>();
    final Set<ExternalId> externalIds = new LinkedHashSet<>();
    final File file = new File(getCommandLine().getOptionValue(FILE_NAME_OPT));

    if (file.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
          try {
            final ExternalId externalId = ExternalId.parse(line);
            externalIdBundles.add(externalId.toBundle());
            externalIds.add(externalId);
          } catch (final IllegalArgumentException iae) {
            LOGGER.error("Couldn't parse identifier {}, skipping", line);
          }
        }
      } catch (final IOException ioe) {
        LOGGER.error("Problem reading file");
        System.exit(1);
      }
    } else {
      LOGGER.error("File not found");
      System.exit(1);
    }

    LOGGER.info("Starting to load securities");
    final Map<ExternalIdBundle, UniqueId> loadSecurities = loader.loadSecurities(externalIdBundles);
    LOGGER.info("Loaded {} securities", loadSecurities.size());
    LOGGER.info("Finished loading securities");

    if (getCommandLine().hasOption(POPULATE_TIME_SERIES_OPT)) {
      // Load time series
      final HistoricalTimeSeriesLoader tsLoader = context.getHistoricalTimeSeriesLoader();
      final String dataProvider = getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, DEFAULT_DATA_PROVIDER);
      final String dataField = getCommandLine().getOptionValue(TIME_SERIES_DATAFIELD_OPT, DEFAULT_DATA_FIELD);

      LOGGER.info("Starting to load time series from data provider {} using field {}", dataProvider, dataField);
      final Map<ExternalId, UniqueId> loadTimeSeries = tsLoader.loadTimeSeries(externalIds, dataProvider, dataField, null, null);
      LOGGER.info("Loaded {} time series", loadTimeSeries.size());
      LOGGER.info("Finished loading time series");
    } else {
      LOGGER.info("Time series load not requested, skipping");
    }
    LOGGER.info("Done.");
  }

  @Override
  protected Options createOptions(final boolean contextProvided) {

    final Options options = super.createOptions(contextProvided);

    final Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (Text file, one ID per line)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);

    final Option populateTimeSeriesOption = new Option(
        POPULATE_TIME_SERIES_OPT, "time-series");
    options.addOption(populateTimeSeriesOption);
    populateTimeSeriesOption.setRequired(false);

    final Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider (default DEFAULT)");
    timeSeriesDataProviderOption.setRequired(false);
    options.addOption(timeSeriesDataProviderOption);

    final Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field (default PX_LAST)");
    timeSeriesDataFieldOption.setRequired(false);
    options.addOption(timeSeriesDataFieldOption);

    return options;
  }


}
