/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.convention.SyntheticInMemoryConventionMasterInitializer;
import com.opengamma.examples.simulated.generator.ExampleEquityOptionPortfolioGeneratorTool;
import com.opengamma.examples.simulated.generator.ExampleMultiCountryPortfolioGeneratorTool;
import com.opengamma.examples.simulated.generator.ExampleOisPortfolioGeneratorTool;
import com.opengamma.examples.simulated.generator.SyntheticPortfolioGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleCurrencyConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveConfigurationsLoader;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.simulated.loader.ExampleExchangeLoader;
import com.opengamma.examples.simulated.loader.ExampleFXImpliedCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleFXImpliedCurveConfigurationsLoader;
import com.opengamma.examples.simulated.loader.ExampleFunctionConfigurationPopulator;
import com.opengamma.examples.simulated.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleHolidayLoader;
import com.opengamma.examples.simulated.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.simulated.loader.ExampleViewsPopulator;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.portfolio.PortfolioLoader;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the sources and masters that are required by the OpenGamma simulated views.
 */
@Scriptable
public class ExampleDatabasePopulator extends AbstractTool<ToolContext> {

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext/toolcontext.properties";
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "Swap Portfolio";
  /**
   * The name of Cap/Floor portfolio.
   */
  public static final String CAP_FLOOR_PORTFOLIO_NAME = "Cap/Floor Portfolio";
  /**
   * The name of the AUD swap portfolio.
   */
  public static final String AUD_SWAP_PORFOLIO_NAME = "AUD Swap Portfolio";
  /**
   * The name of the swaption portfolio.
   */
  public static final String SWAPTION_PORTFOLIO_NAME = "Swap / Swaption Portfolio";
  /**
   * The name of the mixed CMS portfolio.
   */
  public static final String MIXED_CMS_PORTFOLIO_NAME = "Constant Maturity Swap Portfolio";
  /**
   * The name of a vanilla FX option portfolio.
   */
  public static final String VANILLA_FX_OPTION_PORTFOLIO_NAME = "Vanilla FX Option Portfolio";
  /**
   * The name of a FX volatility swap portfolio.
   */
  public static final String FX_VOLATILITY_SWAP_PORTFOLIO_NAME = "FX Volatility Swap Portfolio";
  /**
   * The name of a EUR fixed income portfolio.
   */
  public static final String EUR_SWAP_PORTFOLIO_NAME = "EUR Fixed Income Portfolio";
  /**
   * The name of a mixed currency swaption portfolio.
   */
  public static final String MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME = "Swaption Portfolio";
  /**
   * The name of a FX forward portfolio.
   */
  public static final String FX_FORWARD_PORTFOLIO_NAME = "FX Forward Portfolio";
  /**
   * Equity options portfolio.
   */
  public static final String EQUITY_OPTION_PORTFOLIO_NAME = "Equity Option Portfolio";
  /**
   * Futures portfolio.
   */
  public static final String FUTURE_PORTFOLIO_NAME = "Futures Portfolio";
  /**
   * The name of an ER future portfolio.
   */
  public static final String ER_PORTFOLIO_NAME = "ER Portfolio";
  /**
   * The name of a US Government bond portfolio.
   */
  public static final String US_GOVERNMENT_BOND_PORTFOLIO_NAME = "US Government Bonds";
  /**
   * The name of an index portfolio.
   */
  public static final String INDEX_PORTFOLIO_NAME = "Index Portfolio";
  /**
   * The name of a bond total return swap portfolio.
   */
  public static final String BOND_TRS_PORTFOLIO_NAME = "Bond Total Return Swaps";
  /**
   * The name of an equity total return swap portfolio.
   */
  public static final String EQUITY_TRS_PORTFOLIO_NAME = "Equity Total Return Swaps";
  /**
   * The name of an OIS portfolio.
   */
  public static final String OIS_PORTFOLIO_NAME = "OIS Portfolio";
  /**
   * The name of a bond portfolio.
   */
  public static final String BONDS_PORTFOLIO_NAME = "Bond Portfolio";

  /** Logger. */
  /* package */static final Logger LOGGER = LoggerFactory.getLogger(ExampleDatabasePopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Populating example database");
    new ExampleDatabasePopulator().invokeAndTerminate(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    loadExchanges();
    loadHolidays();
    loadConventions();
    loadCurrencyConfiguration();
    loadCurveAndSurfaceDefinitions();
    loadCurveCalculationConfigurations();
    loadTimeSeriesRating();
    loadSimulatedHistoricalData();
    loadMultiCurrencySwapPortfolio();
    loadAudSwapPortfolio();
    loadSwaptionParityPortfolio();
    loadMixedCMPortfolio();
    loadVanillaFxOptionPortfolio();
    loadEquityPortfolio();
    loadEquityOptionPortfolio();
    loadFuturePortfolio();
    loadSwaptionPortfolio();
    loadEurFixedIncomePortfolio();
    loadFxForwardPortfolio();
    loadErFuturePortfolio();
    loadFXVolatilitySwapPortfolio();
    loadOisPortfolio();
    loadMultiCountryBondPortfolio();
    loadUsBondPortfolio();
    loadFxImpliedCurveCalculationConfigurations();
    loadViews();
    loadFunctionConfigurations();
    loadFxImpliedCurveConfigurations();
    loadCurveConfigurations();
  }

  /**
   * Loads the function configurations.
   */
  private void loadFunctionConfigurations() {
    final Log log = new Log("Creating function configuration definitions");
    try {
      final ExampleFunctionConfigurationPopulator populator = new ExampleFunctionConfigurationPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Creates a synthetic portfolio generator tool.
   * @return The tool
   */
  private static SyntheticPortfolioGeneratorTool portfolioGeneratorTool() {
    final SyntheticPortfolioGeneratorTool tool = new SyntheticPortfolioGeneratorTool();
    tool.setCounterPartyGenerator(new StaticNameGenerator(AbstractPortfolioGeneratorTool.DEFAULT_COUNTER_PARTY));
    return tool;
  }

  /**
   * Logging helper. All stages must go through this. When run as part of the Windows install, the logger is customized to recognize messages
   * formatted in this fashion and route them towards the
   * progress indicators.
   */
  private static final class Log {
    /** The string */
    private final String _str;

    /**
     * Create an instance
     * @param str The string
     */
    /* package */Log(final String str) {
      LOGGER.info("{}", str);
      _str = str;
    }

    /**
     * Appends a finished message.
     */
    /* package */void done() {
      LOGGER.debug("{} - finished", _str);
    }

    /**
     * Appends an error message.
     * @param e The error
     */
    /* package */void fail(final RuntimeException e) {
      LOGGER.error("{} - failed - {}", _str, e.getMessage());
      throw e;
    }

  }

  /**
   * Loads conventions into an in-memory {@link ConventionMaster}.
   */
  private void loadConventions() {
    final Log log = new Log("Creating convention data");
    try {
      final ConventionMaster master = getToolContext().getConventionMaster();
      SyntheticInMemoryConventionMasterInitializer.INSTANCE.init(master, getToolContext().getSecurityMaster());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads currency pairs and matrices into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadCurrencyConfiguration() {
    final Log log = new Log("Creating FX conventions");
    try {
      final ExampleCurrencyConfigurationLoader currencyLoader = new ExampleCurrencyConfigurationLoader();
      currencyLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads legacy curve configurations and volatility surface definitions into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadCurveAndSurfaceDefinitions() {
    final Log log = new Log("Creating curve and surface definitions");
    try {
      final ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
      curveLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads legacy curve calculation configurations into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadCurveCalculationConfigurations() {
    final Log log = new Log("Creating curve calculation configurations");
    try {
      final ExampleCurveConfigurationLoader curveConfigLoader = new ExampleCurveConfigurationLoader();
      curveConfigLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads legacy FX-implied curve configurations into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadFxImpliedCurveCalculationConfigurations() {
    final Log log = new Log("Creating FX-implied curve calculation configurations");
    try {
      final ExampleFXImpliedCurveConfigurationLoader curveConfigLoader = new ExampleFXImpliedCurveConfigurationLoader();
      curveConfigLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads time series ratings into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadTimeSeriesRating() {
    final Log log = new Log("Creating Timeseries configuration");
    try {
      final ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
      timeSeriesRatingLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads simulated historical time series start points into the {@link com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster}.
   */
  private void loadSimulatedHistoricalData() {
    final Log log = new Log("Creating simulated historical timeseries");
    try {
      final ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
      historicalDataGenerator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads an equity portfolio.
   */
  private void loadEquityPortfolio() {
    final Log log = new Log("Creating example equity portfolio");
    try {
      final ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
      equityLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads an equity option portfolio.
   */
  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), EQUITY_OPTION_PORTFOLIO_NAME, new ExampleEquityOptionPortfolioGeneratorTool(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a futures portfolio.
   */
  private void loadFuturePortfolio() {
    final Log log = new Log("Creating example future portfolio");
    try {
      final URL resource = ExampleEquityPortfolioLoader.class.getResource("futures.zip");
      final String file = unpackJar(resource);
      final PortfolioLoader futureLoader = new PortfolioLoader(getToolContext(), FUTURE_PORTFOLIO_NAME, null,
          file, true, true, true, false, true, false, null);
      futureLoader.execute();
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a swap portfolio.
   */
  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi currency swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAP_PORTFOLIO_NAME, "Swap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads an AUD swap portfolio.
   */
  private void loadAudSwapPortfolio() {
    final Log log = new Log("Creating example AUD swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), AUD_SWAP_PORFOLIO_NAME, "AUDSwap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a swaption portfolio.
   */
  private void loadSwaptionParityPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), SWAPTION_PORTFOLIO_NAME, "SwaptionParity", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a portfolio containing constant-maturity swaps, cap-floors and cap-floor CMS spreads.
   */
  private void loadMixedCMPortfolio() {
    final Log log = new Log("Creating example mixed CM portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MIXED_CMS_PORTFOLIO_NAME, "MixedCM", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a vanilla FX option portfolio.
   */
  private void loadVanillaFxOptionPortfolio() {
    final Log log = new Log("Creating example vanilla FX option portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), VANILLA_FX_OPTION_PORTFOLIO_NAME, "VanillaFXOption", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a portfolio of FX volatility swaps.
   */
  private void loadFXVolatilitySwapPortfolio() {
    final Log log = new Log("Creating example FX volatility swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FX_VOLATILITY_SWAP_PORTFOLIO_NAME, "FXVolatilitySwap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a swaption portfolio.
   */
  private void loadSwaptionPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a EUR fixed-income portfolio.
   */
  private void loadEurFixedIncomePortfolio() {
    final Log log = new Log("Creating example EUR fixed income portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), EUR_SWAP_PORTFOLIO_NAME, "EURFixedIncome", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a FX forward portfolio.
   */
  private void loadFxForwardPortfolio() {
    final Log log = new Log("Creating example FX forward portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FX_FORWARD_PORTFOLIO_NAME, "FxForward", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads an EUR interest-rate future portfolio.
   */
  private void loadErFuturePortfolio() {
    final Log log = new Log("Creating example ER future portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), ER_PORTFOLIO_NAME, "ERFutureForCurve", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads an example OIS portfolio.
   */
  private void loadOisPortfolio() {
    final Log log = new Log("Creating example OIS portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), OIS_PORTFOLIO_NAME, new ExampleOisPortfolioGeneratorTool(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Adds a portfolio containing government bonds.
   */
  private void loadMultiCountryBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), BONDS_PORTFOLIO_NAME, new ExampleMultiCountryPortfolioGeneratorTool(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Adds a portfolio containing US government bonds.
   */
  private void loadUsBondPortfolio() {
    final Log log = new Log("Creating example US bond portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), US_GOVERNMENT_BOND_PORTFOLIO_NAME, new ExampleMultiCountryPortfolioGeneratorTool(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads example FX implied curve construction configurations.
   */
  private void loadFxImpliedCurveConfigurations() {
    final Log log = new Log("Creating FX implied curve construction configurations");
    try {
      final ExampleFXImpliedCurveConfigurationsLoader loader = new ExampleFXImpliedCurveConfigurationsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads example curve construction configurations.
   */
  private void loadCurveConfigurations() {
    final Log log = new Log("Creating curve construction configurations");
    try {
      final ExampleCurveConfigurationsLoader loader = new ExampleCurveConfigurationsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads the example view definitions into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExampleViewsPopulator populator = new ExampleViewsPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads exchanges into the {@link com.opengamma.master.config.ConfigMaster}.
   */
  private void loadExchanges() {
    final Log log = new Log("Creating exchange data");
    try {
      final ExampleExchangeLoader loader = new ExampleExchangeLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads holidays into the {@link com.opengamma.master.holiday.HolidayMaster}.
   */
  private void loadHolidays() {
    final Log log = new Log("Creating holiday data");
    try {
      final ExampleHolidayLoader loader = new ExampleHolidayLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Workaround for poor handling of resources.
   * @param resource The resource.
   * @return The file name
   */
  private static String unpackJar(final URL resource) {
    String file = resource.getPath();
    if (file.contains(".jar!/")) {
      LOGGER.info("Unpacking zip file located within a jar file: {}", resource);
      String jarFileName = StringUtils.substringBefore(file, "!/");
      if (jarFileName.startsWith("file:/")) {
        jarFileName = jarFileName.substring(5);
        if (SystemUtils.IS_OS_WINDOWS) {
          jarFileName = StringUtils.stripStart(jarFileName, "/");
        }
      } else if (jarFileName.startsWith("file:/")) {
        jarFileName = jarFileName.substring(6);
      }
      jarFileName = StringUtils.replace(jarFileName, "%20", " ");
      String innerFileName = StringUtils.substringAfter(file, "!/");
      innerFileName = StringUtils.replace(innerFileName, "%20", " ");
      LOGGER.info("Unpacking zip file found jar file: {}", jarFileName);
      LOGGER.info("Unpacking zip file found zip file: {}", innerFileName);
      try (JarFile jar = new JarFile(jarFileName)) {
        final JarEntry jarEntry = jar.getJarEntry(innerFileName);
        try (InputStream in = jar.getInputStream(jarEntry)) {
          final File tempFile = File.createTempFile("simulated-examples-database-populator-", ".zip");
          tempFile.deleteOnExit();
          try (OutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
          }
          file = tempFile.getCanonicalPath();
        }
      } catch (final IOException ex) {
        throw new OpenGammaRuntimeException("Unable to open file within jar file: " + resource, ex);
      }
      LOGGER.debug("Unpacking zip file extracted to: {}", file);
    }
    return file;
  }

}
