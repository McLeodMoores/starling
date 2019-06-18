/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.examples.bloomberg.loader;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;

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

  public static void persistLiborRawSecurities(final Set<Currency> currencies, final ToolContext toolContext) {
    final SecurityMaster securityMaster = toolContext.getSecurityMaster();
    final byte[] rawData = new byte[] { 0 };
    final StringBuilder sb = new StringBuilder();
    sb.append("Created ").append(currencies.size()).append(" libor securities:\n");
    for (final Currency ccy : currencies) {
      final ConventionBundle swapConvention = getSwapConventionBundle(ccy, toolContext.getConventionBundleSource());
      final ConventionBundle liborConvention = getLiborConventionBundle(swapConvention, toolContext.getConventionBundleSource());
      sb.append("\t").append(liborConvention.getIdentifiers()).append("\n");
      final RawSecurity rawSecurity = new RawSecurity(LIBOR_RATE_SECURITY_TYPE, rawData);
      rawSecurity.setExternalIdBundle(liborConvention.getIdentifiers());
      final SecurityDocument secDoc = new SecurityDocument();
      secDoc.setSecurity(rawSecurity);
      securityMaster.add(secDoc);
    }
    LOGGER.info(sb.toString());
  }

  private static ConventionBundle getSwapConventionBundle(final Currency ccy, final ConventionBundleSource conventionSource) {
    final ConventionBundle swapConvention = conventionSource
        .getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap convention for " + ccy.getCode());
    }
    return swapConvention;
  }

  private static ConventionBundle getLiborConventionBundle(final ConventionBundle swapConvention, final ConventionBundleSource conventionSource) {
    final ConventionBundle liborConvention = conventionSource.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      throw new OpenGammaRuntimeException("Couldn't get libor convention for " + swapConvention.getSwapFloatingLegInitialRate());
    }
    return liborConvention;
  }
}
