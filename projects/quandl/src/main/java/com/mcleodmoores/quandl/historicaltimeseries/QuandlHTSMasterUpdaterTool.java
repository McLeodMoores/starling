/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.integration.tool.GUIFeedback;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.scripts.Scriptable;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Tool to load time-series information from Quandl.
 * <p>
 * This loads missing historical time-series data from Quandl.
 */
// TODO rename me
@Scriptable
public class QuandlHTSMasterUpdaterTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlHTSMasterUpdaterTool.class);

  /** Command line option. */
  private static final String RELOAD_OPTION = "reload";
  /** Command line option. */
  private static final String START_OPTION = "start";
  /** Command line option. */
  private static final String END_OPTION = "end";

  private final GUIFeedback _feedback;

  // -------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * <pre>
   * usage: java com.opengamma.bbg.loader.QuandlTimeSeriesTool [options]... [files]...
   *  -e,--end (yyyymmdd)                            End date
   *  -h,--help                                      Print this message
   *  -r,--reload                                    Reload historical data
   *  -s,--start (yyyymmdd)                          Start date
   * </pre>
   *
   * @param args
   *          the command line arguments
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Updating time-series data from Quandl");
    GUIFeedback feedback = null;
    try {
      feedback = new GUIFeedback("Updating time series database from Quandl");
      if (!new QuandlHTSMasterUpdaterTool(feedback).initAndRun(args, ToolContext.class)) {
        feedback.done("Could not update the time-series database - check that the server has been started");
      } else {
        ShutdownUtils.exit(0);
      }
    } catch (final java.awt.HeadlessException ex) {
      final boolean success = new QuandlHTSMasterUpdaterTool().initAndRun(args, ToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);

    } catch (final Exception ex) {
      GUIFeedback.shout(ex.getClass().getSimpleName() + " - " + ex.getMessage());
      LOGGER.error("Caught exception", ex);
      ex.printStackTrace();
    } finally {
      if (feedback != null) {
        try {
          feedback.close();
        } catch (final Exception e) {
          System.exit(0);
        }
      }
    }
    System.exit(1);
  }

  // -------------------------------------------------------------------------
  /**
   * Constructor when we have a GUIFeedback handler.
   * 
   * @param feedback
   *          the GUI feedback handler
   */
  public QuandlHTSMasterUpdaterTool(final GUIFeedback feedback) {
    _feedback = feedback;
  }

  /**
   * Constructor when we don't have a GUIFeedback handler.
   */
  public QuandlHTSMasterUpdaterTool() {
    _feedback = null;
  }

  @Override
  protected void doRun() throws Exception {
    final HistoricalTimeSeriesMaster historicalTimeSeriesMaster = getToolContext().getHistoricalTimeSeriesMaster();
    if (historicalTimeSeriesMaster == null) {
      throw new IllegalArgumentException("Historical timeseries master is missing in toolContext");
    }
    final HistoricalTimeSeriesProvider historicalTimeSeriesProvider = getToolContext().getHistoricalTimeSeriesProvider();
    if (historicalTimeSeriesProvider == null) {
      throw new IllegalArgumentException("Historical timeseries provider is missing in toolContext");
    }
    final AtomicInteger errors = new AtomicInteger();
    final AtomicInteger successes = new AtomicInteger();
    final QuandlHistoricalTimeSeriesUpdater loader = new QuandlHistoricalTimeSeriesUpdater(historicalTimeSeriesMaster, historicalTimeSeriesProvider) {

      private long _lastNotify;
      private int _toUpdate;

      @Override
      protected boolean checkForUpdates(final HistoricalTimeSeriesInfoDocument doc, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
          final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest) {
        final boolean result = super.checkForUpdates(doc, metaDataKeyMap, bbgTSRequest);
        if (result) {
          if (_feedback != null) {
            synchronized (_feedback) {
              _feedback.workRequired(1);
              _feedback.workCompleted(1);
              _toUpdate++;
              final long now = System.nanoTime();
              if (now - _lastNotify < 0 || now - _lastNotify > 5000000000L) {
                GUIFeedback.say("Found " + _toUpdate + " time series to update");
                _lastNotify = now;
              }
            }
          }
          return true;
        }
        if (_feedback != null) {
          synchronized (_feedback) {
            _feedback.workCompleted(1);
          }
        }
        return false;
      }

      @Override
      protected void checkForUpdates(final Collection<HistoricalTimeSeriesInfoDocument> documents, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
          final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest) {
        if (_feedback != null) {
          _feedback.workRequired(documents.size());
        }
        super.checkForUpdates(documents, metaDataKeyMap, bbgTSRequest);
        if (_feedback != null) {
          GUIFeedback.say("Updating " + _toUpdate + " time series");
          _lastNotify = System.nanoTime();
        }
      }

      @Override
      protected Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getTimeSeries(final String dataField, final LocalDate startDate,
          final LocalDate endDate, final String bbgDataProvider,
          final Set<ExternalIdBundle> identifierSet) {
        final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> result = super.getTimeSeries(dataField, startDate, endDate, bbgDataProvider, identifierSet);
        if (_feedback != null) {
          final int count = identifierSet.size();
          _toUpdate -= count;
          final long now = System.nanoTime();
          if (now - _lastNotify < 0 || now - _lastNotify > 5000000000L) {
            GUIFeedback.say(_toUpdate + " time series left to update");
            _lastNotify = now;
          }
          _feedback.workCompleted(count);
          successes.addAndGet(count);
        }
        return result;
      }

      @Override
      protected void errorLoading(final MetaDataKey timeSeries) {
        errors.incrementAndGet();
      }

    };
    configureOptions(getCommandLine(), loader);
    loader.run();
    if (_feedback != null) {
      final int errorCount = errors.get();
      if (errorCount > 0) {
        _feedback.done("Couldn't update " + errorCount + " database time series");
      } else {
        _feedback.done("Updated " + successes.get() + " database time series");
      }
    }
  }

  private static void configureOptions(final CommandLine line, final QuandlHistoricalTimeSeriesUpdater dataLoader) {
    if (line.hasOption(START_OPTION)) {
      final String startOption = line.getOptionValue(START_OPTION);
      try {
        final LocalDate startDate = DateUtils.toLocalDate(startOption);
        dataLoader.setStartDate(startDate);
      } catch (final Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse start date " + startOption, ex);
      }
    }
    if (line.hasOption(END_OPTION)) {
      final String endOption = line.getOptionValue(END_OPTION);
      try {
        final LocalDate endDate = DateUtils.toLocalDate(endOption);
        dataLoader.setEndDate(endDate);
      } catch (final Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse end date " + endOption, ex);
      }
    }
    dataLoader.setReload(line.hasOption(RELOAD_OPTION));
  }

  // -------------------------------------------------------------------------
  @Override
  protected Options createOptions(final boolean mandatoryConfigResource) {
    final Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createReloadOption());
    options.addOption(createStartOption());
    options.addOption(createEndOption());
    return options;
  }

  private static Option createReloadOption() {
    return new Option("r", RELOAD_OPTION, false, "Reload historical data");
  }

  private static Option createStartOption() {
    OptionBuilder.withLongOpt(START_OPTION);
    OptionBuilder.withDescription("Start date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("s");
  }

  private static Option createEndOption() {
    OptionBuilder.withLongOpt(END_OPTION);
    OptionBuilder.withDescription("End date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("e");
  }

}
