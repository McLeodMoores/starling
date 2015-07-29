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

import com.mcleodmoores.integration.simulatedexamples.populator.TestCurrencyConfigurationsLoader;
import com.mcleodmoores.integration.simulatedexamples.populator.TestCurveConfigurationsLoader;
import com.mcleodmoores.integration.simulatedexamples.populator.TestExposureFunctionsLoader;
import com.mcleodmoores.integration.simulatedexamples.populator.TestHolidaysLoader;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

@Scriptable
public class TestFinmathDatabasePopulator extends AbstractTool<ToolContext> {

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext/toolcontext.properties";

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TestFinmathDatabasePopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    LOGGER.info("Populating example database");
    new TestFinmathDatabasePopulator().invokeAndTerminate(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    //loadExchanges();
    loadHolidays();
    //loadLegalEntities();
    //loadConventions();
    loadCurrencyConfiguration();
    //loadTimeSeriesRating();
    //loadSimulatedHistoricalData();
    loadViews();
    loadFunctionConfigurations();
    loadExposureFunctions();
    loadCurveConfigurations();
    //loadIndexSecurities();
  }

  /**
   * Loads the function configurations.
   */
  private void loadFunctionConfigurations() {
    LOGGER.warn("Creating function configuration definitions");
    final TestFinmathFunctionConfigurationPopulator populator = new TestFinmathFunctionConfigurationPopulator();
    populator.run(getToolContext());
  }

  private void loadCurrencyConfiguration() {
    LOGGER.warn("Creating currency configurations");
    final TestCurrencyConfigurationsLoader currencyLoader = new TestCurrencyConfigurationsLoader();
    currencyLoader.run(getToolContext());
  }

//  private void loadTimeSeriesRating() {
//    LOGGER.warn("Creating timeseries configuration");
//    //TODO remove dependency on examples-simulated
//    final ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
//    timeSeriesRatingLoader.run(getToolContext());
//  }

//  private void loadSimulatedHistoricalData() {
//    LOGGER.warn("Creating simulated historical timeseries");
//    //TODO remove dependency on examples-simulated
//    final ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
//    historicalDataGenerator.run(getToolContext());
//  }

  /**
   * Loads exposure functions.
   */
  private void loadExposureFunctions() {
    LOGGER.warn("Creating exposure functions");
    final TestExposureFunctionsLoader exposureFunctionsLoader = new TestExposureFunctionsLoader();
    exposureFunctionsLoader.run(getToolContext());
  }

  /**
   * Loads curve construction configurations.
   */
  private void loadCurveConfigurations() {
    LOGGER.warn("Creating curve construction configurations");
    final TestCurveConfigurationsLoader loader = new TestCurveConfigurationsLoader();
    loader.run(getToolContext());
  }

  /**
   * Loads view definitions.
   */
  private void loadViews() {
    LOGGER.warn("Creating view definitions");
    final TestFinmathViewsPopulator populator = new TestFinmathViewsPopulator();
    populator.run(getToolContext());
  }

  /**
   * Loads holidays.
   */
  private void loadHolidays() {
    LOGGER.warn("Creating holiday data");
    final TestHolidaysLoader loader = new TestHolidaysLoader();
    loader.run(getToolContext());
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

//  /**
//   * Creates a synthetic securities generator tool.
//   * @return The tool
//   */
//  private static SyntheticSecuritiesGeneratorTool securitiesGeneratorTool() {
//    return new SyntheticSecuritiesGeneratorTool();
//  }
}
