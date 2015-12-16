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
import java.util.Collection;

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

import com.mcleodmoores.quandl.loader.config.QuandlCurveConfigurationsGenerator.Configurations;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Creates {@link com.opengamma.financial.analytics.curve.CurveConstructionConfiguration}s,
 * {@link com.opengamma.financial.analytics.curve.AbstractCurveDefinition}s and {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper}
 * and writes them as xml files or stores them in the config master, depending on the options used.
 */
@Scriptable
public class QuandlCurveConfigurationsLoader extends AbstractTool<ToolContext> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCurveConfigurationsLoader.class);
  /** The write to file option */
  private static final String FILE_OPTION = "file";
  /** The write to file flag */
  private static final String FILE_FLAG = "f";
  /** The persist to database option */
  private static final String PERSIST_OPTION = "persist";
  /** The persist to database flag */
  private static final String PERSIST_FLAG = "p";

  /**
   * Main method to run the tool.
   * @param args The arguments
   */
  public static void main(final String[] args) {
    new QuandlCurveConfigurationsLoader().invokeAndTerminate(args);
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createFileOption());
    options.addOption(createPersistOption());
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

  @Override
  protected void doRun() throws Exception {
    try (final ToolContext toolContext = getToolContext()) {
      final CommandLine commandLine = getCommandLine();
      final boolean isPersist = commandLine.hasOption(PERSIST_FLAG);
      final boolean isSaveToFile = commandLine.hasOption(FILE_FLAG);
      final Configurations configs = QuandlCurveConfigurationsGenerator.createConfigurations();
      final Collection<CurveConstructionConfiguration> cccs = configs.getCurveConstructionConfigurations();
      final Collection<AbstractCurveDefinition> acds = configs.getAbstractCurveDefinitions();
      final Collection<CurveNodeIdMapper> cnims = configs.getCurveNodeIdMappers();
      LOGGER.warn("Generated {} CurveConstructionConfigurations", cccs.size());
      LOGGER.warn("Generated {} AbstractCurveDefinitions", acds.size());
      LOGGER.warn("Generated {} CurveNodeIdMappers", cnims.size());
      if (isPersist) {
        LOGGER.warn("Persisting {} CurveConstructionConfigurations to config master", cccs.size());
        final ConfigMaster configMaster = toolContext.getConfigMaster();
        for (final CurveConstructionConfiguration configuration : cccs) {
          final ConfigItem<?> configItem = ConfigItem.of(configuration, configuration.getName(), CurveConstructionConfiguration.class);
          ConfigMasterUtils.storeByName(configMaster, configItem);
        }
        LOGGER.warn("Persisting {} AbstractCurveDefinitions to config master", acds.size());
        for (final AbstractCurveDefinition configuration : acds) {
          final ConfigItem<?> configItem = ConfigItem.of(configuration, configuration.getName(), configuration.getClass());
          ConfigMasterUtils.storeByName(configMaster, configItem);
        }
        LOGGER.warn("Persisting {} CurveNodeIdMappers to config master", cnims.size());
        for (final CurveNodeIdMapper configuration : cnims) {
          final ConfigItem<?> configItem = ConfigItem.of(configuration, configuration.getName(), CurveNodeIdMapper.class);
          ConfigMasterUtils.storeByName(configMaster, configItem);
        }
      }
      if (isSaveToFile) {
        LOGGER.warn("Saving {} CurveConstructionConfigurations to file", cccs.size());
        for (final CurveConstructionConfiguration configuration : cccs) {
          final Path path = Paths.get(configuration.getName() + ".xml");
          final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(configuration).getBytes();
          try (final OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            out.write(xmlString);
          } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
          }
        }
        LOGGER.warn("Saving {} AbstractCurveDefinitions to file", acds.size());
        for (final AbstractCurveDefinition configuration : acds) {
          final Path path = Paths.get(configuration.getName() + ".xml");
          final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(configuration).getBytes();
          try (final OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            out.write(xmlString);
          } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
          }
        }
        LOGGER.warn("Saving {} CurveNodeIdMappers to file", cnims.size());
        for (final CurveNodeIdMapper configuration : cnims) {
          final Path path = Paths.get(configuration.getName() + ".xml");
          final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
          final MutableFudgeMsg configurationMsg = serializer.objectToFudgeMsg(configuration);
          try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(),
              new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW))))) {
            fudgeMsgWriter.writeMessage(configurationMsg);
            fudgeMsgWriter.close();
          } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
          }
        }
        return;
      }
    }
  }
}
