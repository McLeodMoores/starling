/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.StartupUtils;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * The entry point for running OpenGamma batches.
 */
public class BatchJobRunner {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobRunner.class);

  static {
    StartupUtils.init();
  }

  /**
   * Date-time format: yyyyMMdd
   */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  static LocalDate parseDate(final String date) {
    return LocalDate.parse(date, DATE_FORMATTER);
  }

  static Instant parseTime(final String date) {
    return OffsetDateTime.parse(date, DATE_FORMATTER).toInstant();
  }

  private static RunCreationMode getRunCreationMode(final CommandLine line, final Properties configProperties, final String configPropertysFile) {
    final String runCreationMode = getProperty("runCreationMode", line, configProperties, configPropertysFile, false);
    if (runCreationMode != null) {
      if (runCreationMode.equalsIgnoreCase("auto")) {
        return RunCreationMode.AUTO;
      } else if (runCreationMode.equalsIgnoreCase("create_new")) {
        return RunCreationMode.CREATE_NEW;
      } else if (runCreationMode.equalsIgnoreCase("create_new_overwrite")) {
        return RunCreationMode.CREATE_NEW_OVERWRITE;
      } else if (runCreationMode.equalsIgnoreCase("reuse_existing")) {
        return RunCreationMode.REUSE_EXISTING;
      } else {
        throw new OpenGammaRuntimeException("Unrecognized runCreationMode. "
            + "Should be one of AUTO, ALWAYS, NEVER. "
            + "Was " + runCreationMode);
      }
    }
    return null;
  }

  private static LocalDate getObservationDate(final CommandLine line, final Properties configProperties, final String configPropertysFile) {
    final String observationDate = getProperty("observationDate", line, configProperties, configPropertysFile, false);
    if (observationDate != null) {
      return parseDate(observationDate);
    }
    return LocalDate.now();
  }

  private static Instant getValuationTime(final CommandLine line, final Properties configProperties, final String configPropertysFile) {
    final String observationDate = getProperty("valuationTime", line, configProperties, configPropertysFile, false);
    if (observationDate != null) {
      return parseTime(observationDate);
    }
    return Instant.now();
  }

  private static UniqueId getViewDefinitionUniqueId(final CommandLine line, final Properties configProperties) {
    final String view = getProperty("view", line, configProperties);
    if (view != null) {
      return UniqueId.parse(view);
    }
    throw new IllegalArgumentException("View definition unique Id is mandatory parameter");
  }

  /**
   * Creates an runs a batch job based on a properties file and configuration.
   *
   * @param args
   *          the command line arguments
   * @throws Exception
   *           if there is a problem
   */
  public static void main(final String[] args) throws Exception { // CSIGNORE
    if (args.length == 0) {
      usage();
      System.exit(-1);
    }

    CommandLine line = null;
    Properties configProperties = null;

    final String propertyFile = "batchJob.properties";

    String configPropertyFile = null;

    if (System.getProperty(propertyFile) != null) {
      configPropertyFile = System.getProperty(propertyFile);
      try {
        final FileInputStream fis = new FileInputStream(configPropertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
      } catch (final FileNotFoundException e) {
        LOGGER.error("The system cannot find " + configPropertyFile);
        System.exit(-1);
      }
    } else {
      try {
        final FileInputStream fis = new FileInputStream(propertyFile);
        configProperties = new Properties();
        configProperties.load(fis);
        fis.close();
        configPropertyFile = propertyFile;
      } catch (final FileNotFoundException e) {
        // there is no config file so we expect command line arguments
        try {
          final CommandLineParser parser = new PosixParser();
          line = parser.parse(getOptions(), args);
        } catch (final ParseException e2) {
          usage();
          System.exit(-1);
        }
      }
    }

    RunCreationMode runCreationMode = getRunCreationMode(line, configProperties, configPropertyFile);
    if (runCreationMode == null) {
      // default
      runCreationMode = RunCreationMode.AUTO;
    }

    final String engineURI = getProperty("engineURI", line, configProperties, configPropertyFile);

    final String brokerURL = getProperty("brokerURL", line, configProperties, configPropertyFile);

    final Instant valuationTime = getValuationTime(line, configProperties, configPropertyFile);
    final LocalDate observationDate = getObservationDate(line, configProperties, configPropertyFile);

    final UniqueId viewDefinitionUniqueId = getViewDefinitionUniqueId(line, configProperties);

    URI vpBase;
    try {
      vpBase = new URI(engineURI);
    } catch (final URISyntaxException ex) {
      throw new OpenGammaRuntimeException("Invalid URI", ex);
    }

    final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
    activeMQConnectionFactory.setWatchTopicAdvisories(false);

    final JmsConnectorFactoryBean jmsConnectorFactoryBean = new JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setConnectionFactory(activeMQConnectionFactory);
    jmsConnectorFactoryBean.setName("Masters");

    final JmsConnector jmsConnector = jmsConnectorFactoryBean.getObjectCreating();
    final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      final ViewProcessor vp = new RemoteViewProcessor(
          vpBase,
          jmsConnector,
          heartbeatScheduler);
      final ViewClient vc = vp.createViewClient(UserPrincipal.getLocalUser());

      final HistoricalMarketDataSpecification marketDataSpecification = MarketData.historical(observationDate, null);

      final ViewExecutionOptions executionOptions = ExecutionOptions.batch(valuationTime, marketDataSpecification, null);

      vc.attachToViewProcess(viewDefinitionUniqueId, executionOptions);
      vc.waitForCompletion();
    } finally {
      heartbeatScheduler.shutdown();
    }
  }

  private static String getProperty(final String property, final CommandLine line, final Properties configProperties) {
    return getProperty(property, line, configProperties, null);
  }

  private static String getProperty(final String propertyName, final CommandLine line, final Properties properties, final String configPropertysFile) {
    return getProperty(propertyName, line, properties, configPropertysFile, true);
  }

  private static String getProperty(final String propertyName, final CommandLine line, final Properties properties, final String configPropertysFile,
      final boolean required) {
    String optionValue = null;
    if (line != null) {
      optionValue = line.getOptionValue(propertyName);
      if (optionValue != null) {
        return optionValue;
      }
    }
    if (properties != null) {
      optionValue = properties.getProperty(propertyName);
      if (optionValue == null && required) {
        LOGGER.error("Cannot find property " + propertyName + " in " + configPropertysFile);
        System.exit(-1);
      }
    } else {
      if (required) {
        LOGGER.error("Cannot find option " + propertyName + " in command line arguments");
        System.exit(-1);
      }
    }
    return optionValue;
  }

  public static void usage() {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java [-DbatchJob.properties={property file}] com.opengamma.financial.batch.BatchJobRunner [options]", getOptions());
  }

  private static Options getOptions() {
    final Options options = new Options();

    // options.addOption("reason", true, "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");

    // options.addOption("observationTime", true, "Observation time - for example, LDN_CLOSE. Default - " + BatchJobParameters.AD_HOC_OBSERVATION_TIME + ".");
    options.addOption("observationDate", true, "Observation date (= run date). yyyyMMdd - for example, 20100621. Default - system clock date.");

    options.addOption("valuationTime", true, "Valuation time. HH:mm[:ss] - for example, 16:22:09. Default - system clock.");

    options.addOption("view", true, "View name in configuration database. You must specify this.");

    options.addOption("engineURI", true,
        "URI to remote OG engine - for example 'http://localhost:8080/jax/components/ViewProcessor/main'. You must specify this.");
    options.addOption("brokerURL", true, "URL to activeMQ broker - for example 'tcp://localhost:61616'. You must specify this.");

    // options.addOption("viewTime", true, "Time at which view should be loaded. HH:mm[:ss]. Default - system clock.");
    // options.addOption("snapshotObservationTime", true, "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as
    // observationTime.");
    // options.addOption("snapshotObservationDate", true, "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationDate");

    options.addOption("runCreationMode", true, "One of auto, create_new, create_new_overwrite, reuse_existing (case insensitive)."
        + " Specifies whether to create a new run in the database."
        + " See documentation of RunCreationMode Java enum to find out more. Default - auto.");

    // options.addOption("timeZone", true, "Time zone in which times on the command line are given. Default - system time zone.");

    return options;
  }

}
