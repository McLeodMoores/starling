/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples;

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
import com.opengamma.examples.simulated.generator.ExampleIndexSecuritiesGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleCurrencyConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveConfigurationsLoader;
import com.opengamma.examples.simulated.loader.ExampleExchangeLoader;
import com.opengamma.examples.simulated.loader.ExampleExposureFunctionLoader;
import com.opengamma.examples.simulated.loader.ExampleFXImpliedCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleHolidayLoader;
import com.opengamma.examples.simulated.loader.ExampleLegalEntityLoader;
import com.opengamma.examples.simulated.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.simulated.tool.SyntheticSecuritiesGeneratorTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.scripts.Scriptable;

@Scriptable
public class FinmathDatabasePopulator extends AbstractTool<ToolContext> {

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext/toolcontext.properties";

  /** Logger. */
  /* package */static final Logger LOGGER = LoggerFactory.getLogger(FinmathDatabasePopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    LOGGER.info("Populating example database");
    new FinmathDatabasePopulator().invokeAndTerminate(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    loadExchanges();
    loadHolidays();
    loadLegalEntities();
    loadConventions();
    loadCurrencyConfiguration();
    loadTimeSeriesRating();
    loadSimulatedHistoricalData();
    loadViews();
    loadFunctionConfigurations();
    loadExposureFunctions();
    loadCurveConfigurations();
    loadIndexSecurities();
  }

  /**
   * Loads the function configurations.
   */
  private void loadFunctionConfigurations() {
    final Log log = new Log("Creating function configuration definitions");
    try {
      final FinmathFunctionConfigurationPopulator populator = new FinmathFunctionConfigurationPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
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

  private void loadConventions() {
    final Log log = new Log("Creating convention data");
    try {
      final ConventionMaster master = getToolContext().getConventionMaster();
      SyntheticInMemoryConventionMasterInitializer.INSTANCE.init(master);
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

  /**
   * Loads example exposure functions.
   */
  private void loadExposureFunctions() {
    final Log log = new Log("Creating exposure functions");
    try {
      final ExampleExposureFunctionLoader exposureFunctionLoader = new ExampleExposureFunctionLoader();
      exposureFunctionLoader.run(getToolContext());
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
   * Loads the example view definitions
   */
  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final FinmathViewsPopulator populator = new FinmathViewsPopulator();
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
      final ExampleHolidayLoader loader = new ExampleHolidayLoader();
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
      final ExampleLegalEntityLoader loader = new ExampleLegalEntityLoader();
      loader.run(getToolContext());
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
      securitiesGeneratorTool().run(getToolContext(), new ExampleIndexSecuritiesGeneratorTool(), true);
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

  /**
   * Creates a synthetic securities generator tool.
   * @return The tool
   */
  private static SyntheticSecuritiesGeneratorTool securitiesGeneratorTool() {
    return new SyntheticSecuritiesGeneratorTool();
  }
}
