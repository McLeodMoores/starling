/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.hts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.scripts.Scriptable;

/**
 * A tool to load a list of historical timeseries from the system's hts loader (e.g. Quandl/Bloomberg).  File contains only the ids you want to load, not the data itself.
 * For raw data loading, see TimeSeriesLoaderTool.
 */
@Scriptable
public class HistoricalTimeSeriesLoaderTool extends AbstractTool<IntegrationToolContext> {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesLoaderTool.class);
  /** File name option flag */
  public static final String FILE_NAME_OPT = "f";
  /** Time series data source option flag*/
  public static final String TIME_SERIES_DATASOURCE_OPT = "s";
  /** Time series data provider option flag*/
  public static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  public static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Time series ID scheme option flag*/
  public static final String TIME_SERIES_IDSCHEME_OPT = "i";
  /** Default value for the data provider */
  private static final String DEFAULT_DATA_PROVIDER = "DEFAULT";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new HistoricalTimeSeriesLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    String fileName = getCommandLine().getOptionValue(FILE_NAME_OPT);
    String dataProvider = getCommandLine().hasOption(TIME_SERIES_DATAPROVIDER_OPT) ? getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT) : DEFAULT_DATA_PROVIDER;
    String dataField = getCommandLine().getOptionValue(TIME_SERIES_DATAFIELD_OPT);
    HistoricalTimeSeriesLoader loader = getToolContext().getHistoricalTimeSeriesLoader();
    try {
      File file = new File(fileName);
      if (file.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
          String line;
          Set<ExternalId> ids = new LinkedHashSet<>();
          while ((line = reader.readLine()) != null) {
            if (line.contains("~")) {
              ids.add(ExternalId.parse(line.trim()));
            } else {
              if (getCommandLine().hasOption(TIME_SERIES_IDSCHEME_OPT)) {
                ids.add(ExternalId.of(getCommandLine().getOptionValue(TIME_SERIES_IDSCHEME_OPT), line.trim()));
              } else {
                s_logger.error("Time series id {} does not have a scheme, and ID scheme option not set, so cannot be loaded, skipping.", line);
              }
            }
          }
          HistoricalTimeSeriesLoaderRequest req = HistoricalTimeSeriesLoaderRequest.create(ids, dataProvider, dataField, null, null);
          loader.loadTimeSeries(req);
        }
      } else {
        s_logger.error("File {} does not exist", fileName);
      }
    } catch (Exception e) {
      
    }
  }

  @Override
  protected  Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider (defaults to DEFAULT)");
    timeSeriesDataProviderOption.setOptionalArg(true);
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field");
    options.addOption(timeSeriesDataFieldOption);
    
    Option timeSeriesIdSchemeOption = new Option(
        TIME_SERIES_IDSCHEME_OPT, "scheme", true, "The time series ID scheme (e.g. RIC, if omitted, assumes included in file IDs)");
    timeSeriesIdSchemeOption.setOptionalArg(true);
    options.addOption(timeSeriesIdSchemeOption);
        
    return options;
  }

}
