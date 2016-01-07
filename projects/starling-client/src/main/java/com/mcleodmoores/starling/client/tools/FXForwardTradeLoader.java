package com.mcleodmoores.starling.client.tools;

import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.mcleodmoores.starling.client.portfolio.FXForwardTradeFileParser;
import com.mcleodmoores.starling.client.portfolio.PortfolioManager;
import com.mcleodmoores.starling.client.utils.TablePrinter;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Created by jim on 14/05/15.
 */
@Scriptable
public class FXForwardTradeLoader extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FXForwardTradeLoader.class);

  private static final String INPUT_FILE_OPTION = "f";
  private static final String INPUT_FILE_LONG = "file";
  private static final String INPUT_FILE_DESCRIPTION = "The input CSV file";
  private static final String INPUT_FILE_ARG_NAME = "filename";

  private static final String DATE_OPTION = "d";
  private static final String DATE_LONG = "date";
  private static final String DATE_DESCRIPTION = "The date (in current locale short form or Today/Yesterday/YesterdayBusiness) of the scalar values, default to today";
  private static final String DATE_ARG_NAME = "date";

  private static final String TEST_OPTION = "t";
  private static final String TEST_LONG = "test";
  private static final String TEST_DESCRIPTION = "Don't persist the data, just parse it";

  private static final String DATEPATTERN_OPTION = "dp";
  private static final String DATEPATTERN_LONG = "date-pattern";
  private static final String DATEPATTERN_DESCRIPTION = "Provide a date parsing pattern";
  private static final String DATEPATTERN_ARG_NAME =  "The pattern, e.g. dd/MM/yyyy";

  private static final String PORTFOLIO_OPTION = "p";
  private static final String PORTFOLIO_LONG = "portfolio";
  private static final String PORTFOLIO_DESCRIPTION = "The portfolio name to import to";
  private static final String PORTFOLIO_ARG_NAME =  "The portfolio name (overrides Portfolio column)";


  @Override
  protected void doRun() throws Exception {
    final DateTimeFormatter formatter = initDateFormatter();
    final String inputFile = getCommandLine().getOptionValue(INPUT_FILE_OPTION);
    final File file = new File(inputFile);
    if (file.exists() && file.canRead() && !file.isDirectory()) {
      String defaultPortfolioName = null;
      if (getCommandLine().hasOption(PORTFOLIO_OPTION)) {
        defaultPortfolioName = getCommandLine().getOptionValue(PORTFOLIO_OPTION);
      }
      final FXForwardTradeFileParser parser = new FXForwardTradeFileParser(defaultPortfolioName);
      final Map<String, SimplePortfolio> portfolios = parser.parseCSV(new FileReader(file));
      if (!getCommandLine().hasOption(TEST_OPTION)) {
        final PortfolioManager portfolioManager = new PortfolioManager(getToolContext());
        for (final SimplePortfolio portfolio : portfolios.values()) {
          LOGGER.info("Saving portfolio {}", portfolio.getName());
          portfolioManager.savePortfolio(portfolio);
        }
        LOGGER.info("Data saved.");
      } else {
        for (final SimplePortfolio portfolio : portfolios.values()) {
          System.out.println(TablePrinter.toPrettyPrintedString(portfolio));
        }
        LOGGER.info("Test mode, data parsed but not saved");
      }
    } else {
      System.err.println("Trade file " + inputFile + " does not exist, cannot be read or is a directory");
    }
  }

  private DateTimeFormatter initDateFormatter() {
    DateTimeFormatter formatter;
    if (getCommandLine().hasOption(DATEPATTERN_OPTION)) {
      final String datePattern = getCommandLine().getOptionValue(DATEPATTERN_OPTION);
      formatter = DateTimeFormatter.ofPattern(datePattern);
    } else {
      if (Locale.getDefault().getCountry().equals("US")) {
        formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      } else {
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      }
    }
    JodaBeanUtils.stringConverter().register(LocalDate.class, new LocalDateStringConverter(formatter));
    return formatter;
  }

  /**
   * LocalDate String converter for JodaBeans.
   */
  private class LocalDateStringConverter implements StringConverter<LocalDate> {
    private final DateTimeFormatter _formatter;

    public LocalDateStringConverter(final DateTimeFormatter formatter) {
      _formatter = formatter;
    }

    @Override public LocalDate convertFromString(final Class<? extends LocalDate> cls, final String str) {
      return LocalDate.from(_formatter.parse(str));
    }

    @Override public String convertToString(final LocalDate object) {
      return _formatter.format(object);
    }
  }

  @Override
  protected Options createOptions(final boolean requiresConfigResource) {
    final Options options = super.createOptions(requiresConfigResource);

    final Option fileOption = new Option(INPUT_FILE_OPTION, INPUT_FILE_LONG, true, INPUT_FILE_DESCRIPTION);
    fileOption.setArgName(INPUT_FILE_ARG_NAME);
    fileOption.setArgs(1);
    fileOption.setRequired(true);
    options.addOption(fileOption);

    final Option localeOption = new Option(DATEPATTERN_OPTION, DATEPATTERN_LONG, true, DATEPATTERN_DESCRIPTION);
    localeOption.setArgName(DATEPATTERN_ARG_NAME);
    localeOption.setArgs(1);
    localeOption.setRequired(false);
    options.addOption(localeOption);

    final Option dateOption = new Option(DATE_OPTION, DATE_LONG, true, DATE_DESCRIPTION);
    dateOption.setArgName(DATE_ARG_NAME);
    dateOption.setArgs(1);
    dateOption.setRequired(false);
    options.addOption(dateOption);

    final Option portfolioOption = new Option(PORTFOLIO_OPTION, PORTFOLIO_LONG, true, PORTFOLIO_DESCRIPTION);
    portfolioOption.setArgName(PORTFOLIO_ARG_NAME);
    portfolioOption.setArgs(1);
    portfolioOption.setRequired(false);
    options.addOption(portfolioOption);

    final Option testOption = new Option(TEST_OPTION, TEST_LONG, false, TEST_DESCRIPTION);
    testOption.setRequired(false);
    options.addOption(testOption);

    return options;
  }

  public static void main(final String[] args) {
    final FXForwardTradeLoader loader = new FXForwardTradeLoader();
    loader.invokeAndTerminate(args);
  }
}
