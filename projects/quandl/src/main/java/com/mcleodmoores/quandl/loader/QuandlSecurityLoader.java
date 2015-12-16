/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import cern.colt.Arrays;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.scripts.Scriptable;


/**
 * Creates securities from a csv file containing full details, or a csv file containing codes and a {@link ConventionSource},
 * and writes them as xml files or stores them in the security master, depending on the options used.
 */
@Scriptable
public abstract class QuandlSecurityLoader extends AbstractTool<ToolContext> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlSecurityLoader.class);
  /** The write to file option */
  private static final String OUTPUT_FILE_OPTION = "file";
  /** The write to file flag */
  private static final String OUTPUT_FILE_FLAG = "f";
  /** The persist to database option */
  private static final String PERSIST_OPTION = "persist";
  /** The persist to database flag */
  private static final String PERSIST_FLAG = "p";
  /** The input file name option */
  private static final String INPUT_FILE_OPTION = "input-file";
  /** The input file name flag */
  private static final String INPUT_FILE_FLAG = "i";
  /** The use convention option */
  private static final String USE_CONVENTION_OPTION = "use-convention";
  /** The use convention flag */
  private static final String USE_CONVENTION_FLAG = "u";

  @Override
  protected Options createOptions(final boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createInputFileOption());
    options.addOption(createOutputFileOption());
    options.addOption(createPersistOption());
    options.addOption(createUseConventionOption());
    return options;
  }

  /**
   * Create the option to save the securities to a file.
   * @return The option
   */
  private static Option createOutputFileOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(OUTPUT_FILE_OPTION);
    OptionBuilder.withDescription("Save the securities to a file");
    return OptionBuilder.create(OUTPUT_FILE_FLAG);
  }

  /**
   * Creates the option to persist the securities to the security master.
   * @return The option
   */
  private static Option createPersistOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(PERSIST_OPTION);
    OptionBuilder.withDescription("Persist the securities to a database");
    return OptionBuilder.create(PERSIST_FLAG);
  }

  /**
   * Creates the option to supply an input file name.
   * @return The option
   */
  private static Option createInputFileOption() {
    OptionBuilder.hasArg(true);
    OptionBuilder.isRequired(true);
    OptionBuilder.withLongOpt(INPUT_FILE_OPTION);
    OptionBuilder.withDescription("Input file name");
    return OptionBuilder.create(INPUT_FILE_FLAG);
  }

  /**
   * Creates the option to use a convention source to fill in fields in the securities.
   * @return The option
   */
  private static Option createUseConventionOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(USE_CONVENTION_OPTION);
    OptionBuilder.withDescription("Use convention");
    return OptionBuilder.create(USE_CONVENTION_FLAG);
  }

  @Override
  protected void doRun() throws Exception {
    try (ToolContext toolContext = getToolContext()) {
      final CommandLine commandLine = getCommandLine();
      final boolean isPersist = commandLine.hasOption(PERSIST_FLAG);
      final boolean isSaveToFile = commandLine.hasOption(OUTPUT_FILE_FLAG);
      final Collection<ManageableSecurity> securities = new HashSet<>();
      final String inputFileName = commandLine.getOptionValue(INPUT_FILE_FLAG);
      try (InputStream resource = getClass().getResourceAsStream(inputFileName)) {
        if (resource == null) {
          LOGGER.error("Could not open file called {}", inputFileName);
          return;
        }
        try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
          String[] line = reader.readNext(); //ignore headers
          while ((line = reader.readNext()) != null) {
            if (commandLine.hasOption(USE_CONVENTION_FLAG)) {
              LOGGER.warn("Generating securities from {} and the convention source", inputFileName);
              final ConventionSource conventionSource = toolContext.getConventionSource();
              for (final String quandlCode : line) { //don't care if there's multiple elements on the same line
                final ManageableSecurity generatedSecurity = createSecurity(conventionSource, quandlCode);
                if (generatedSecurity != null) {
                  securities.add(generatedSecurity);
                }
              }
            } else {
              LOGGER.warn("Generating securities from {}", inputFileName);
              final ManageableSecurity generatedSecurity = createSecurity(line);
              if (generatedSecurity != null) {
                securities.add(generatedSecurity);
              }
            }
          }
        }
      }
      LOGGER.warn("Generated securities");
      if (isPersist) {
        LOGGER.warn("Persisting {} indices to security master", securities.size());
        final SecurityMaster securityMaster = toolContext.getSecurityMaster();
        for (final ManageableSecurity security : securities) {
          SecurityMasterUtils.addOrUpdateSecurity(securityMaster, security);
        }
        LOGGER.warn("Done");
      }
      if (isSaveToFile) {
        LOGGER.warn("Saving configurations to file");
        for (final ManageableSecurity security : securities) {
          final String quandlCode = security.getExternalIdBundle().getValue(QuandlConstants.QUANDL_CODE);
          if (quandlCode == null) {
            LOGGER.error("No QUANDL code found for index {}, skipping", security);
            continue;
          }
          final String fileName = quandlCode.replace("/", "_");
          final Path path = Paths.get(fileName);
          final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(security).getBytes();
          System.err.println(Arrays.toString(xmlString));
          try (final OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            out.write(xmlString);
          } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
          }
        }
        return;
      }
    }
  }

  /**
   * Creates a security from an array of string inputs. If the security cannot be created,
   * returns null
   * @param inputs The inputs
   * @return The security, can be null
   */
  protected abstract ManageableSecurity createSecurity(String[] inputs);

  /**
   * Creates a security from a Quandl code using {@link com.opengamma.core.convention.Convention}.
   * @param conventionSource The convention source, not null
   * @param quandlCode The Quandl code
   * @return The security, can be null
   */
  protected abstract ManageableSecurity createSecurity(ConventionSource conventionSource, String quandlCode);
}
