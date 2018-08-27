/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master. The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class SingleConfigImportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SingleConfigImportTool.class);
  private static final long DEFAULT_MARK_BUFFER = 1000000; // 1MB should do it.

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new SingleConfigImportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final ToolContext toolContext = getToolContext();
    final ConfigMaster configMaster = toolContext.getConfigMaster();
    final ConfigSource configSource = toolContext.getConfigSource();
    final ConventionMaster conventionMaster = toolContext.getConventionMaster();
    final MarketDataSnapshotMaster marketDataSnapshotMaster = toolContext.getMarketDataSnapshotMaster();
    final SecurityMaster secMaster = toolContext.getSecurityMaster();
    final CommandLine commandLine = getCommandLine();
    @SuppressWarnings("unchecked")
    final
    List<String> fileList = commandLine.getArgList();
    for (final String file : fileList) {
      System.err.println(file);
    }
    final boolean verbose = commandLine.hasOption("verbose");
    if (commandLine.hasOption("load")) {
      checkForInvalidOption("type");
      final SingleConfigLoader configLoader = new SingleConfigLoader(secMaster, configMaster, configSource, conventionMaster, marketDataSnapshotMaster, commandLine.hasOption("do-not-update"));
      if (fileList.size() > 0) {
        boolean problems = false;
        for (final String fileName : fileList) {
          final File file = new File(fileName);
          if (!file.exists()) {
            LOGGER.error("Could not find file:" + fileName);
            problems = true;
          }
          if (!file.canRead()) {
            LOGGER.error("Not able to read file (permissions?):" + fileName);
            problems = true;
          }
        }
        if (problems) {
          LOGGER.error("Problems with one or more files, aborting.");
          System.exit(1);
        }
        try {
          for (final String fileName : fileList) {
            if (verbose) {
              LOGGER.info("Processing " + fileName);
            }
            FileInputStream inputStream;
            final File file = new File(fileName);
            if (file.isFile()) {
              if (file.getName().endsWith(".zip")) {
                final ZipFile zipFile = new ZipFile(file);
                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                  final ZipEntry zipEntry = entries.nextElement();
                  if (!zipEntry.isDirectory()) {
                    final String zipFileName = zipEntry.getName();
                    if (zipFileName.endsWith(".xml")) {
                      if (verbose) {
                        LOGGER.info("Processing file {} in zip archive", zipFileName);
                      }
                      final long fileSize = zipEntry.getSize();
                      storeObjectFromStream(configLoader, fileSize, zipFile.getInputStream(zipEntry));
                    } else {
                      LOGGER.warn("File {} not xml, skipping...", zipFileName);
                    }
                  }
                }
              } else if (file.getName().endsWith(".xml")) {
                inputStream = new FileInputStream(fileName);
                final long fileSize = file.length();
                storeObjectFromStream(configLoader, fileSize, inputStream);
              } else {
                LOGGER.error("File type not recognised, pass either a zip or xml file");
                System.exit(1);
              }
            } else {
              LOGGER.error("Path is not a file");
              System.exit(1);
            }
          }
        } catch (final IOException ioe) {
          if (verbose) {
            LOGGER.error("An I/O error occurred while processing a file (run with -v to see stack trace)");
          } else {
            LOGGER.error("An I/O error occurred while processing a file", ioe);
          }
        }
      } else {
        if (verbose) {
          LOGGER.info("No file name given, assuming STDIN");
        }
        configLoader.loadConfig(System.in);
      }
    } else {
      LOGGER.info("Specify -load to load a config");
    }
  }

  /**
   * @param commandLine
   * @param configLoader
   * @param fileName
   * @param inputStream
   * @return
   */
  private void storeObjectFromStream(final SingleConfigLoader configLoader, final long fileSize, final InputStream rawInputStream) {
    final InputStream inputStream = new BufferedInputStream(rawInputStream, (int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER));
    if (getCommandLine().hasOption("type")) {
      final List<String> types = getTypes();
      if (types.size() > 1) {
        LOGGER.error("More than one type specified");
        System.exit(1);
      }
      try {
        final Class<?> type = Class.forName(types.get(0));
        try {
          inputStream.mark((int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER));
          configLoader.loadConfig(inputStream, type);
          LOGGER.info("Config loaded successfully");
        } catch (final Exception e) {
          // try loading it as fudge!
          try {
            inputStream.reset();
            configLoader.loadFudgeConfig(inputStream);
            LOGGER.info("Config loaded successfully");
          } catch (final Exception fe) {
            LOGGER.error("Exception thrown when loading as both JodaXML and as FudgeXML");
            LOGGER.error("JodaXML trace", e);
            LOGGER.error("Fudge trace", e);
          }
        }
      } catch (final ClassNotFoundException ex) {
        LOGGER.error("Class {} not found", types.get(0));
        System.exit(1);
      }
    } else {
      try {
        inputStream.mark((int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER));
        configLoader.loadConfig(inputStream);
        LOGGER.info("Config loaded successfully as JodaXML");
      } catch (final Exception e) {
        try {
          // close it - we could use mark/reset, but this is simpler.
          inputStream.reset();
          configLoader.loadFudgeConfig(inputStream);
          LOGGER.info("Config loaded successfully as FudgeXML");
        } catch (final Exception fe) {
          LOGGER.error("Exception thrown when loading as both JodaXML and as FudgeXML");
          LOGGER.error("JodaXML trace", e);
          LOGGER.error("Fudge trace", e);
        }
      }
    }
  }

  private List<String> getTypes() {
    if (getCommandLine().hasOption("type")) {
      final String[] typeValues = getCommandLine().getOptionValues("type");
      return Arrays.asList(typeValues);
    } else {
      return Collections.emptyList();
    }
  }

  private void checkForInvalidOption(final String longOpt) {
    if (getCommandLine().hasOption(longOpt)) {
      System.err.println("Option " + longOpt + " is invalid in this context");
      System.exit(1);
    }
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createTypeOption());
    options.addOption(createLoadOption());
    options.addOption(createVerboseOption());
    options.addOption(createDontUpdateOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createTypeOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("full class name").withDescription("The type(s) you want to export").withLongOpt("type").create("t");
  }

  @SuppressWarnings("static-access")
  private Option createLoadOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Load from file to config database").withLongOpt("load").create("load");
  }

  @SuppressWarnings("static-access")
  private Option createDontUpdateOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Don't update configs that already exist").withLongOpt("do-not-update").create("n");
  }

  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Display extra error messages").withLongOpt("verbose").create("v");
  }

  @Override
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("single-config-import-tool.sh [file...]", options, true);
  }

}
