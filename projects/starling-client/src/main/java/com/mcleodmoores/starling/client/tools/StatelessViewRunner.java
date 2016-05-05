/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.tools;

import com.mcleodmoores.starling.client.portfolio.FXForwardTradeFileParser;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.SynchronousJob;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.stateless.StatelessAnalyticService;
import com.mcleodmoores.starling.client.utils.PreviousBusinessDayTemporalAdjuster;
import com.mcleodmoores.starling.client.utils.ResultModelUtils;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.monitor.OperationTimer;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Utility to run views.
 */
@Scriptable
public class StatelessViewRunner extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatelessViewRunner.class);

  private static final String DATE_OPTION = "d";
  private static final String DATE_LONG = "date";
  private static final String DATE_DESCRIPTION = "The date (in current locale short form or Today/Yesterday/YesterdayBusiness) "
      + "of the scalar values, default to today";
  private static final String DATE_ARG_NAME = "date";
  private static final String TODAY = "TODAY";
  private static final String YESTERDAY = "YESTERDAY";
  private static final String YESTERDAY_BUSINESS = "YESTERDAYBUSINESS";

  private static final String VALUATION_DATE_OPTION = "vd";
  private static final String VALUATION_DATE_LONG = "valution-date";
  private static final String VALUATION_DATE_DESCRIPTION = "The valuation date (in current locale short form or Today/Yesterday/YesterdayBusiness) "
      + "of the scalar values, default to today";
  private static final String VALUATION_DATE_ARG_NAME = "date";

  private static final String TEST_OPTION = "t";
  private static final String TEST_LONG = "test";
  private static final String TEST_DESCRIPTION = "Don't persist the data, just parse it";

  private static final String DATEPATTERN_OPTION = "dp";
  private static final String DATEPATTERN_LONG = "date-pattern";
  private static final String DATEPATTERN_DESCRIPTION = "Provide a date parsing pattern";
  private static final String DATEPATTERN_ARG_NAME =  "The pattern, e.g. dd/MM/yyyy";

  private static final String VIEW_NAME_OPTION = "v";
  private static final String VIEW_NAME_LONG = "view";
  private static final String VIEW_NAME_DESCRIPTION = "The templte view";
  private static final String VIEW_NAME_ARG_NAME =  "The template view name";

  private static final String OUTPUT_FILE_OPTION = "f";
  private static final String OUTPUT_FILE_LONG = "file";
  private static final String OUTPUT_FILE_DESCRIPTION = "The output CSV file";
  private static final String OUTPUT_FILE_ARG_NAME = "filename";

  private static final String OVERWRITE_OPTION = "o";
  private static final String OVERWRITE_LONG = "overwrite";
  private static final String OVERWRITE_DESCRIPTION = "Overwrite existing file when outputting";

  private static final String PORTFOLIO_OPTION = "p";
  private static final String PORTFOLIO_LONG = "portfoio";
  private static final String PORTFOLIO_DESCRIPTION = "The porfolio to process";
  private static final String PORTFOLIO_ARG_NAME = "porfolio file name";

  private static final String CORRELATION_ID_SCHEME_OPTION = "s";
  private static final String CORRELATION_ID_SCHEME_LONG = "correlation-scheme";
  private static final String CORRELATION_ID_SCHEME_DESCRIPTION = "The scheme used for correlation ids";

  private DateTimeFormatter _dateFormatter;
  private LocalDate _date;
  private LocalDate _valuationDate;
  private String _portfolio;

  @Override
  protected void doRun() throws Exception {
    initDateFormatter();
    initDate();
    SimplePortfolio portfolio = loadPortfolio();
    String viewName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);
    String correlationIdScheme = getCommandLine().getOptionValue(CORRELATION_ID_SCHEME_OPTION);
    Instant valuationTime = _valuationDate != null ? Instant.from(_valuationDate.atStartOfDay(ZoneOffset.UTC)) : Instant.now();
    runView(viewName, portfolio, ExternalScheme.of(correlationIdScheme), _date, valuationTime);
  }

  protected SimplePortfolio loadPortfolio() {
    if (getCommandLine().hasOption(PORTFOLIO_OPTION)) {
      File file = new File(getCommandLine().getOptionValue(PORTFOLIO_OPTION));
      if (!file.exists()) {
        LOGGER.error("Portfolio file does not exist");
        System.exit(1);
      }
      if (file.isDirectory()) {
        LOGGER.error("Portfolio file is a directory, must be a file");
        System.exit(1);
      }
      FXForwardTradeFileParser fileParser = new FXForwardTradeFileParser("Temp");
      try {
        Map<String, SimplePortfolio> portfolioMap = fileParser.parseCSV(new FileReader(file));
        if (portfolioMap.size() > 1) {
          LOGGER.warn("Multiple portfolios read from file, only using first");
        }
        if (portfolioMap.size() == 0) {
          LOGGER.error("No portfolios in file");
          System.exit(1);
        }
        return portfolioMap.values().iterator().next();
      } catch (IOException ioe) {
        LOGGER.error("Error reading portfolio file", ioe);
        System.exit(1);
      }
    }
    return null;
  }

  void runView(final String viewName, final SimplePortfolio portfolio, final ExternalScheme scheme, final LocalDate snapshotDate, final Instant valuationTime) {
    OperationTimer timer = new OperationTimer(LOGGER, "Running view {} with market data on {}", viewName, snapshotDate);
    //CacheManager cacheManager = CacheManager.create("classpath:default-ehcache.xml");
    ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    PositionSource positionSource = getToolContext().getPositionSource();
    ConfigSource configSource = getToolContext().getConfigSource();
    ConfigMaster configMaster = getToolContext().getConfigMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecuritySource securitySource = getToolContext().getSecuritySource();
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();

    final StatelessAnalyticService service = new StatelessAnalyticService(portfolioMaster, positionMaster, positionSource, securityMaster, 
        securitySource, configMaster, configSource, viewProcessor);
    final SynchronousJob job = service.createSynchronousJob(ViewKey.of(viewName), portfolio, scheme, valuationTime, snapshotDate);
    ResultModel resultModel = job.run();
    timer.finished();
    ResultModelUtils.displayResult(resultModel);
    if (getCommandLine().hasOption(OUTPUT_FILE_OPTION)) {
      File file = new File(getCommandLine().getOptionValue(OUTPUT_FILE_OPTION));
      if (file.exists()) {
        if (getCommandLine().hasOption(OVERWRITE_OPTION)) {
          ResultModelUtils.saveResult(resultModel, file);
          LOGGER.info("File saved as {}, check parent dir if file not in CWD", file.getName());
        } else {
          LOGGER.error("Not writing output CSV file because it already exists, use --overwrite option to force");
        }
      } else {
        ResultModelUtils.saveResult(resultModel, file);
        LOGGER.info("File saved as {}, check parent dir if file not in CWD", file.getName());
      }
    }
  }

  private void initDateFormatter() {
    if (getCommandLine().hasOption(DATEPATTERN_OPTION)) {
      String datePattern = getCommandLine().getOptionValue(DATEPATTERN_OPTION);
      _dateFormatter = DateTimeFormatter.ofPattern(datePattern);
    } else {
      if (Locale.getDefault().getCountry().equals("US")) {
        _dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      } else {
        _dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      }
    }
  }

  private void initDate() {
    if (getCommandLine().hasOption(DATE_OPTION)) {
      String dateStr = getCommandLine().getOptionValue(DATE_OPTION);
      switch (dateStr.toUpperCase()) {
        case TODAY:
          _date = LocalDate.now();
          break;
        case YESTERDAY:
          _date = LocalDate.now().minusDays(1);
          break;
        case YESTERDAY_BUSINESS:
          _date = LocalDate.now().with(new PreviousBusinessDayTemporalAdjuster());
          break;
        default:
          try {
            _date = (LocalDate) _dateFormatter.parse(dateStr, LocalDate.FROM);
          } catch (DateTimeParseException dtpe) {
            LOGGER.error("Could not parse date {}, expected format is {}, try setting the locale via the --locale flag", dateStr, _dateFormatter.toString());
            System.exit(1);
          }
          break;
      }
    } else {
      _date = LocalDate.now();
    }
    if (getCommandLine().hasOption(VALUATION_DATE_OPTION)) {
      String dateStr = getCommandLine().getOptionValue(VALUATION_DATE_OPTION);
      switch (dateStr.toUpperCase()) {
        case TODAY:
          _valuationDate = LocalDate.now();
          break;
        case YESTERDAY:
          _valuationDate = LocalDate.now().minusDays(1);
          break;
        case YESTERDAY_BUSINESS:
          _valuationDate = LocalDate.now().with(new PreviousBusinessDayTemporalAdjuster());
          break;
        default:
          try {
            _valuationDate = (LocalDate) _dateFormatter.parse(dateStr, LocalDate.FROM);
          } catch (DateTimeParseException dtpe) {
            LOGGER.error("Could not parse date {}, expected format is {}, try setting the locale via the --locale flag", dateStr, _dateFormatter.toString());
            System.exit(1);
          }
          break;
      }
    } else {
      _valuationDate = LocalDate.now();
    }
  }

  @Override
  protected Options createOptions(boolean requiresConfigResource) {
    Options options = super.createOptions(requiresConfigResource);

    Option fileOption = new Option(OUTPUT_FILE_OPTION, OUTPUT_FILE_LONG, true, OUTPUT_FILE_DESCRIPTION);
    fileOption.setArgName(OUTPUT_FILE_ARG_NAME);
    fileOption.setArgs(1);
    fileOption.setRequired(false);
    options.addOption(fileOption);

    Option overwriteOption = new Option(OVERWRITE_OPTION, OVERWRITE_LONG, false, OVERWRITE_DESCRIPTION);
    overwriteOption.setRequired(false);
    options.addOption(overwriteOption);

    Option localeOption = new Option(DATEPATTERN_OPTION, DATEPATTERN_LONG, true, DATEPATTERN_DESCRIPTION);
    localeOption.setArgName(DATEPATTERN_ARG_NAME);
    localeOption.setArgs(1);
    localeOption.setRequired(false);
    options.addOption(localeOption);

    Option dateOption = new Option(DATE_OPTION, DATE_LONG, true, DATE_DESCRIPTION);
    dateOption.setArgName(DATE_ARG_NAME);
    dateOption.setArgs(1);
    dateOption.setRequired(false);
    options.addOption(dateOption);

    Option valuationDateOption = new Option(VALUATION_DATE_OPTION, VALUATION_DATE_LONG, true, VALUATION_DATE_DESCRIPTION);
    valuationDateOption.setArgName(VALUATION_DATE_ARG_NAME);
    valuationDateOption.setArgs(1);
    valuationDateOption.setRequired(false);
    options.addOption(valuationDateOption);

    Option viewNameOption = new Option(VIEW_NAME_OPTION, VIEW_NAME_LONG, true, VIEW_NAME_DESCRIPTION);
    viewNameOption.setArgName(VIEW_NAME_ARG_NAME);
    viewNameOption.setArgs(1);
    viewNameOption.setRequired(false);
    options.addOption(viewNameOption);

    Option testOption = new Option(TEST_OPTION, TEST_LONG, false, TEST_DESCRIPTION);
    testOption.setRequired(false);
    options.addOption(testOption);

    Option portfolioOption = new Option(PORTFOLIO_OPTION, PORTFOLIO_LONG, true, PORTFOLIO_DESCRIPTION);
    portfolioOption.setArgName(PORTFOLIO_ARG_NAME);
    portfolioOption.setArgs(1);
    portfolioOption.setRequired(true);
    options.addOption(portfolioOption);

    Option externalSchemeOption = new Option(CORRELATION_ID_SCHEME_OPTION, CORRELATION_ID_SCHEME_LONG, true, CORRELATION_ID_SCHEME_DESCRIPTION);
    externalSchemeOption.setArgs(1);
    externalSchemeOption.setRequired(true);
    options.addOption(externalSchemeOption);

    return options;
  }


  public static void main(String[] args) {
    StatelessViewRunner loader = new StatelessViewRunner();
    loader.invokeAndTerminate(args);
  }
}
