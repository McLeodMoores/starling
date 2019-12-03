/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.examples.bloomberg.loader;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

/**
 *
 */
public class PortfolioLoaderHelper {
  /**
   * Logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioLoaderHelper.class);

  /**
   * Raw security type for libor rates
   */
  private static final String LIBOR_RATE_SECURITY_TYPE = "LIBOR_RATE";

  /** File name option flag. */
  public static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag. */
  public static final String PORTFOLIO_NAME_OPT = "n";
  /** Run mode option flag. */
  public static final String RUN_MODE_OPT = "r";
  /** Write option flag. */
  public static final String WRITE_OPT = "w";
  /** Standard date-time formatter for the input. */
  public static final DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output. */
  public static final DateTimeFormatter OUTPUT_DATE_FORMATTER;
  /** Command-line options. */
  public static final Options OPTIONS;
  /** Standard rate formatter. */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");
  /** Standard notional formatter. */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");

  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
    OPTIONS = PortfolioLoaderHelper.getOptions();
  }

  public static Options getOptions() {
    final Options options = new Options();
    buildOptions(options);
    return options;
  }

  /**
   * Builds the set of options.
   *
   * @param options
   *          the options to add to, not null
   */
  public static void buildOptions(final Options options) {
    final Option filenameOption = new Option(FILE_NAME_OPT, "filename", true, "The path to the CSV file of cash details");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);

    final Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPT, "name", true, "The name of the portfolio");
    portfolioNameOption.setRequired(true);
    options.addOption(portfolioNameOption);

    // Option runModeOption = new Option(RUN_MODE_OPT, "runmode", true, "The run mode: shareddev, standalone");
    // runModeOption.setRequired(true);
    // options.addOption(runModeOption);

    final Option writeOption = new Option(WRITE_OPT, "write", false, "Actually persists the portfolio to the database");
    options.addOption(writeOption);
  }

  public static void usage(final String loaderName) {
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.setWidth(100);
    helpFormatter.printHelp(loaderName, OPTIONS);
  }

  public static void normaliseHeaders(final String[] headers) {
    for (int i = 0; i < headers.length; i++) {
      headers[i] = headers[i].toLowerCase();
    }
  }

  public static String getWithException(final Map<String, String> fieldValueMap, final String fieldName) {
    final String result = fieldValueMap.get(fieldName);
    if (result == null) {
      LOGGER.error("{}", fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  public static LocalDate getDateWithException(final Map<String, String> fieldValueMap, final String fieldName) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), CSV_DATE_FORMATTER);
  }

}
