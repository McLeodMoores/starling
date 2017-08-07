/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader;

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

import com.mcleodmoores.examples.simulated.loader.config.ExamplesCurveConfigsLoader;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesExposureFunctionLoader;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesFunctionConfigsPopulator;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesFxImpliedCurveConfigsLoader;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesFxVolatilitySurfaceConfigsLoader;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesUsBondCurveConfigsLoader;
import com.mcleodmoores.examples.simulated.loader.config.ExamplesViewsPopulator;
import com.mcleodmoores.examples.simulated.loader.convention.ExamplesConventionMasterInitializer;
import com.mcleodmoores.examples.simulated.loader.data.ExampleHistoricalDataGeneratorTool;
import com.mcleodmoores.examples.simulated.loader.holiday.ExamplesCurrencyHolidayLoader;
import com.mcleodmoores.examples.simulated.loader.legalentity.SimulatedLegalEntityLoader;
import com.mcleodmoores.examples.simulated.loader.portfolio.SimulatedMultiCountryBondPortfolioGenerator;
import com.mcleodmoores.examples.simulated.loader.portfolio.SimulatedOisPortfolioGenerator;
import com.mcleodmoores.examples.simulated.loader.portfolio.SimulatedUsBondPortfolioGenerator;
import com.mcleodmoores.examples.simulated.loader.securities.SimulatedBondCurveSecuritiesGenerator;
import com.mcleodmoores.examples.simulated.loader.securities.SimulatedIndexSecuritiesGenerator;
import com.mcleodmoores.examples.simulated.loader.securities.SimulatedSecuritiesGenerator;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.examples.simulated.generator.SyntheticPortfolioGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleCurrencyConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.simulated.loader.ExampleExchangeLoader;
import com.opengamma.examples.simulated.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.SwapPortfolioGeneratorTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.portfolio.PortfolioLoader;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;

@Scriptable
public class ExamplesDatabasePopulator extends AbstractTool<ToolContext> {

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext/toolcontext.properties";
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "Swap Portfolio";
  /**
   * The name of Cap/Floor portfolio
   */
  public static final String CAP_FLOOR_PORTFOLIO_NAME = "Cap/Floor Portfolio";
  /**
   * The name of the AUD swap portfolio
   */
  public static final String AUD_SWAP_PORFOLIO_NAME = "AUD Swap Portfolio";
  /**
   * The name of the swaption portfolio
   */
  public static final String SWAPTION_PORTFOLIO_NAME = "Swap / Swaption Portfolio";
  /**
   * The name of the mixed CMS portfolio
   */
  public static final String MIXED_CMS_PORTFOLIO_NAME = "Constant Maturity Swap Portfolio";
  /**
   * The name of a vanilla FX option portfolio
   */
  public static final String VANILLA_FX_OPTION_PORTFOLIO_NAME = "Vanilla FX Option Portfolio";
  /**
   * The name of a FX volatility swap portfolio
   */
  public static final String FX_VOLATILITY_SWAP_PORTFOLIO_NAME = "FX Volatility Swap Portfolio";
  /**
   * The name of a EUR fixed income portfolio
   */
  public static final String EUR_SWAP_PORTFOLIO_NAME = "EUR Fixed Income Portfolio";
  /**
   * The name of a mixed currency swaption portfolio
   */
  public static final String MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME = "Swaption Portfolio";
  /**
   * The name of a FX forward portfolio.
   */
  public static final String FX_FORWARD_PORTFOLIO_NAME = "FX Forward Portfolio";
  /**
   * Equity options portfolio
   */
  public static final String EQUITY_OPTION_PORTFOLIO_NAME = "Equity Option Portfolio";
  /**
   * Futures portfolio
   */
  public static final String FUTURE_PORTFOLIO_NAME = "Futures Portfolio";
  /**
   * The name of an ER future portfolio.
   */
  public static final String ER_PORTFOLIO_NAME = "ER Portfolio";
  /**
   * The name of a US Government bond portfolio.
   */
  public static final String US_GOVERNMENT_BOND_PORTFOLIO_NAME = "Government Bonds";
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
  /* package */static final Logger LOGGER = LoggerFactory.getLogger(ExamplesDatabasePopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Populating example database");
    new ExamplesDatabasePopulator().invokeAndTerminate(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    loadBondCurveSecurities();
    loadIndexSecurities();
    loadExchanges();
    loadHolidays();
    loadLegalEntities();
    loadConventions();
    //loadCurveAndSurfaceDefinitions();
    //    loadCurveCalculationConfigurations();
    loadTimeSeriesRating();
    loadSimulatedHistoricalData();
    loadMultiCurrencySwapPortfolio();
    // SWAP
    // Swap + swaption w/cubes and comparison btwn black + various SABRS
    // AUD swap
    // EUR swap desk
    // equities
    // equity options
    // futures
    // FX options + FX forwards
    // CDS with new curve configs
    // GBP corporate bonds

    loadAudSwapPortfolio();
    //    loadSwaptionParityPortfolio();
    //    loadMixedCMPortfolio();
    loadVanillaFxOptionPortfolio();
    loadEquityPortfolio();
    //    loadEquityOptionPortfolio();
    loadFuturePortfolio();
    //    loadBondPortfolio();
    //    loadSwaptionPortfolio();
    //    loadEURFixedIncomePortfolio();
    loadFXForwardPortfolio();
    //    loadERFuturePortfolio();
    //    loadFXVolatilitySwapPortfolio();
    loadOisPortfolio();
    loadMultiCountryBondPortfolio();
    loadViews();
    loadFunctionConfigurations();
    loadExposureFunctions();
    loadCurveConfigurations();
    loadFxVolatilitySurfaceConfigurations();
    loadFxImpliedCurveCalculationConfigurations();
    loadUsBondCurveConfigurations(); // bond curve configurations to use a bond curve
  }

  /**
   * Loads the function configurations.
   */
  private void loadFunctionConfigurations() {
    final Log log = new Log("Creating function configuration definitions");
    try {
      final ExamplesFunctionConfigsPopulator populator = new ExamplesFunctionConfigsPopulator();
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
   * Creates a simulated securities generator tool.
   * @return The tool
   */
  private static SimulatedSecuritiesGenerator securitiesGeneratorTool() {
    return new SimulatedSecuritiesGenerator();
  }

  /**
   * Logging helper. All stages must go through this. When run as part of the Windows install, the logger is customized to recognize messages
   * formatted in this fashion and route them towards the progress indicators.
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
   * Loads
   */
  private void loadConventions() {
    final Log log = new Log("Creating convention data");
    try {
      final ConventionMaster conventionMaster = getToolContext().getConventionMaster();
      final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
      ExamplesConventionMasterInitializer.INSTANCE.init(conventionMaster, securityMaster);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurrencyConfiguration() {
    final Log log = new Log("Creating FX definitions");
    try {
      final ExampleCurrencyConfigurationLoader currencyLoader = new ExampleCurrencyConfigurationLoader();
      currencyLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFxVolatilitySurfaceConfigurations() {
    final Log log = new Log("Creating FX volatility surface definitions");
    try {
      final ExamplesFxVolatilitySurfaceConfigsLoader loader = new ExamplesFxVolatilitySurfaceConfigsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException e) {
      log.fail(e);
    }
  }

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

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      final URL resource = ExampleEquityPortfolioLoader.class.getResource("equityOptions.zip");
      final String file = unpackJar(resource);
      final PortfolioLoader equityOptionLoader = new PortfolioLoader(getToolContext(), EQUITY_OPTION_PORTFOLIO_NAME, null,
          file, true,
          true, true, false, true, false, null);
      equityOptionLoader.execute();
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

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

  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi-currency swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAP_PORTFOLIO_NAME,
          new SwapPortfolioGeneratorTool(true, ExternalSchemes.OG_SYNTHETIC_TICKER), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadAudSwapPortfolio() {
    final Log log = new Log("Creating example AUD swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), AUD_SWAP_PORFOLIO_NAME, "AUDSwap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSwaptionParityPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), SWAPTION_PORTFOLIO_NAME, "SwaptionParity", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadMixedCMPortfolio() {
    final Log log = new Log("Creating example mixed CM portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MIXED_CMS_PORTFOLIO_NAME, "MixedCM", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

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

  private void loadSwaptionPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEURFixedIncomePortfolio() {
    final Log log = new Log("Creating example EUR fixed income portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), EUR_SWAP_PORTFOLIO_NAME, "EURFixedIncome", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFXForwardPortfolio() {
    final Log log = new Log("Creating example FX forward portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FX_FORWARD_PORTFOLIO_NAME, "FxForward", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadERFuturePortfolio() {
    final Log log = new Log("Creating example ER future portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), ER_PORTFOLIO_NAME, "ERFutureForCurve", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a portfolio of US government bonds.
   */
  private void loadBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), US_GOVERNMENT_BOND_PORTFOLIO_NAME, new SimulatedUsBondPortfolioGenerator(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a portfolio of bond TRS.
   */
  private void loadBondTotalReturnSwapPortfolio() {
    final Log log = new Log("Creating example bond total return swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), BOND_TRS_PORTFOLIO_NAME, "BondTotalReturnSwap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a portfolio of equity TRS.
   */
  private void loadEquityTotalReturnSwapPortfolio() {
    final Log log = new Log("Creating example equity total return swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), EQUITY_TRS_PORTFOLIO_NAME, "EquityTotalReturnSwap", true, null);
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
      portfolioGeneratorTool().run(getToolContext(), OIS_PORTFOLIO_NAME, new SimulatedOisPortfolioGenerator(), true, null);
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
      portfolioGeneratorTool().run(getToolContext(), BONDS_PORTFOLIO_NAME, new SimulatedMultiCountryBondPortfolioGenerator(), true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads example exposure functions.
   */
  private void loadExposureFunctions() {
    final Log log = new Log("Creating exposure functions");
    try {
      final ExamplesExposureFunctionLoader exposureFunctionLoader = new ExamplesExposureFunctionLoader();
      exposureFunctionLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a list of bond securities used in curves.
   */
  private void loadBondCurveSecurities() {
    final Log log = new Log("Creating bond curve securities");
    try {
      securitiesGeneratorTool().run(getToolContext(), new SimulatedBondCurveSecuritiesGenerator(), true);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads a list of index securities.
   */
  private void loadIndexSecurities() {
    final Log log = new Log("Creating example indices");
    try {
      securitiesGeneratorTool().run(getToolContext(), new SimulatedIndexSecuritiesGenerator(), true);
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
      final ExamplesCurveConfigsLoader loader = new ExamplesCurveConfigsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  //  /**
  //   * Loads example Ugandan bond curve construction configurations.
  //   */
  //  private void loadUgandanBondCurveConfigurations() {
  //    final Log log = new Log("Creating Ugandan bond curve construction configurations");
  //    try {
  //      final ExampleUgandanBondCurveConfigurationsLoader loader = new ExampleUgandanBondCurveConfigurationsLoader();
  //      loader.run(getToolContext());
  //      log.done();
  //    } catch (final RuntimeException t) {
  //      log.fail(t);
  //    }
  //  }

  private void loadFxImpliedCurveCalculationConfigurations() {
    final Log log = new Log("Creating FX implied curve construction configurations");
    try {
      final ExamplesFxImpliedCurveConfigsLoader loader = new ExamplesFxImpliedCurveConfigsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads simulated US bond curve construction configurations.
   */
  private void loadUsBondCurveConfigurations() {
    final Log log = new Log("Creating US bond curve construction configurations");
    try {
      final ExamplesUsBondCurveConfigsLoader loader = new ExamplesUsBondCurveConfigsLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Loads the example view definitions
   */
  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExamplesViewsPopulator populator = new ExamplesViewsPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

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

  private void loadHolidays() {
    final Log log = new Log("Creating holiday data");
    try {
      // note that this holiday loader uses the currency pairs configuration, so the call to this method has
      // been moved into this method to avoid having to remember this ordering
      loadCurrencyConfiguration();
      final ExamplesCurrencyHolidayLoader loader = new ExamplesCurrencyHolidayLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }


  /**
   * Loads hard-coded legal entity data.
   */
  private void loadLegalEntities() {
    final Log log = new Log("Creating legal entity data");
    try {
      final SimulatedLegalEntityLoader loader = new SimulatedLegalEntityLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  //-------------------------------------------------------------------------
  // workaround for poor handling of resources, see PLAT-3919
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
