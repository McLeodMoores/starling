/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Creates a {@link CurrencyMatrix} and {@link CurrencyPairs} configuration and writes them as xml files
 * or stores them in the config master, depending on the options used. The currency matrix name must be
 * specified, but the currency pairs name is set to {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS}, as this
 * value is hard-coded in many OpenGamma integration functions.
 */
@Scriptable
public class QuandlCurrencyConfigurationsLoader extends AbstractTool<ToolContext> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCurrencyConfigurationsLoader.class);
  /** The write to file option */
  private static final String FILE_OPTION = "file";
  /** The write to file flag */
  private static final String FILE_FLAG = "f";
  /** The persist to database option */
  private static final String PERSIST_OPTION = "persist";
  /** The persist to database flag */
  private static final String PERSIST_FLAG = "p";
  /** The currency matrix name option */
  private static final String CURRENCY_MATRIX_NAME_OPTION = "currency-matrix-name";
  /** The currency matrix name flag */
  private static final String CURRENCY_MATRIX_NAME_FLAG = "cm";
  /** The currency pairs name option */
  private static final String CURRENCY_PAIRS_NAME_OPTION = "currency-pairs-name";
  /** The currency pairs name flag */
  private static final String CURRENCY_PAIRS_NAME_FLAG = "cp";

  /**
   * Main method to run the tool.
   * @param args The arguments
   */
  public static void main(final String[] args) {
    new QuandlCurrencyConfigurationsLoader().invokeAndTerminate(args);
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createFileOption());
    options.addOption(createPersistOption());
    options.addOption(createCurrencyMatrixNameOption());
    options.addOption(createCurrencyPairsNameOption());
    return options;
  }

  /**
   * Create the option to save the configurations to file.
   * @return The option
   */
  private static Option createFileOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(FILE_OPTION);
    OptionBuilder.withDescription("Save the configurations to a file");
    return OptionBuilder.create(FILE_FLAG);
  }

  /**
   * Creates the option to persist the configurations to the config master.
   * @return The option
   */
  private static Option createPersistOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(PERSIST_OPTION);
    OptionBuilder.withDescription("Persist the configurations to a database");
    return OptionBuilder.create(PERSIST_FLAG);
  }

  /**
   * Creates the currency matrix name option.
   * @return The option
   */
  private static Option createCurrencyMatrixNameOption() {
    OptionBuilder.hasArg(true);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(CURRENCY_MATRIX_NAME_OPTION);
    OptionBuilder.withDescription("The name of the currency matrix configuration");
    return OptionBuilder.create(CURRENCY_MATRIX_NAME_FLAG);
  }

  /**
   * Creates the currency pairs name option.
   * @return The option
   */
  private static Option createCurrencyPairsNameOption() {
    OptionBuilder.hasArg(true);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(CURRENCY_PAIRS_NAME_OPTION);
    OptionBuilder.withDescription("The name of the currency pairs configuration");
    return OptionBuilder.create(CURRENCY_PAIRS_NAME_FLAG);
  }

  @Override
  protected void doRun() throws Exception {
    try (ToolContext toolContext = getToolContext()) {
      final CommandLine commandLine = getCommandLine();
      final boolean isPersist = commandLine.hasOption(PERSIST_FLAG);
      final boolean isSaveToFile = commandLine.hasOption(FILE_FLAG);
      final CurrencyPairs currencyPairs = QuandlCurrencyPairsGenerator.createConfiguration();
      final CurrencyMatrix currencyMatrix = QuandlCurrencyMatrixGenerator.createConfiguration(currencyPairs);
      final String currencyMatrixName;
      if (commandLine.hasOption(CURRENCY_MATRIX_NAME_FLAG)) {
        currencyMatrixName = commandLine.getOptionValue(CURRENCY_MATRIX_NAME_FLAG);
      } else {
        currencyMatrixName = "QUANDL_LIVE_DATA";
      }
      String currencyPairsName;
      if (commandLine.hasOption(CURRENCY_PAIRS_NAME_FLAG)) {
        currencyPairsName = commandLine.getOptionValue(CURRENCY_PAIRS_NAME_FLAG);
      } else {
        currencyPairsName = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;
      }
      LOGGER.warn("Generated CurrencyMatrix and CurrencyPairs configuration");
      if (isPersist) {
        LOGGER.warn("Persisting configurations to config master");
        final ConfigMaster configMaster = toolContext.getConfigMaster();
        final ConfigItem<?> pairsItem = ConfigItem.of(currencyPairs, currencyPairsName, CurrencyPairs.class);
        final ConfigItem<?> matrixItem = ConfigItem.of(currencyMatrix, currencyMatrixName, CurrencyMatrix.class);
        ConfigMasterUtils.storeByName(configMaster, pairsItem);
        ConfigMasterUtils.storeByName(configMaster, matrixItem);
      }
      if (isSaveToFile) {
        LOGGER.warn("Saving configurations to file");
        Path path = Paths.get(currencyPairsName + ".xml");
        final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(currencyPairs).getBytes();
        try (final OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
          out.write(xmlString);
        } catch (final IOException e) {
          LOGGER.warn(e.getMessage());
        }
        path = Paths.get(currencyMatrixName + ".xml");
        final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
        final MutableFudgeMsg configurationMsg = serializer.objectToFudgeMsg(currencyMatrix);
        try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(),
            new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW))))) {
          fudgeMsgWriter.writeMessage(configurationMsg);
          fudgeMsgWriter.close();
        } catch (final IOException e) {
          LOGGER.warn(e.getMessage());
        }
        return;
      }
    }
  }
}
