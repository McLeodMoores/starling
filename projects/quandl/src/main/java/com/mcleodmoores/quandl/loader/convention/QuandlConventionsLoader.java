/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Creates conventions and writes them as xml files or stores them in the config/security master, depending on the options used.
 */
@Scriptable
public class QuandlConventionsLoader extends AbstractTool<ToolContext> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlConventionsLoader.class);
  /** The write to file option */
  private static final String FILE_OPTION = "file";
  /** The write to file flag */
  private static final String FILE_FLAG = "f";
  /** The persist to database option */
  private static final String PERSIST_OPTION = "persist";
  /** The persist to database flag */
  private static final String PERSIST_FLAG = "p";
  /** The convention loaders to use when running this tool */
  private static final Set<ConventionsLoader<? extends ManageableConvention>> LOADERS = new HashSet<>();

  static {
    LOADERS.add(QuandlIborIndexConventionsLoader.INSTANCE);
    LOADERS.add(QuandlOvernightIndexConventionsLoader.INSTANCE);
    LOADERS.add(QuandlStirFutureConventionsLoader.INSTANCE);
    LOADERS.add(QuandlFedFundsFutureConventionsLoader.INSTANCE);
    LOADERS.add(VanillaFixedIborSwapLegConventionsLoader.INSTANCE);
    LOADERS.add(VanillaFixedOvernightIndexSwapLegConventionsLoader.INSTANCE);
    LOADERS.add(VanillaIborSwapLegConventionsLoader.INSTANCE);
    LOADERS.add(VanillaOvernightIndexSwapLegConventionsLoader.INSTANCE);
  }

  /**
   * Main method to run the tool.
   * @param args  the arguments
   */
  public static void main(final String[] args) {
    new QuandlConventionsLoader().invokeAndTerminate(args);
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
   * @return  the option
   */
  private static Option createFileOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(FILE_OPTION);
    OptionBuilder.withDescription("Save the indices to a set of files");
    return OptionBuilder.create(FILE_FLAG);
  }

  /**
   * Creates the option to persist the configurations to the config master.
   * @return  the option
   */
  private static Option createPersistOption() {
    OptionBuilder.hasArg(false);
    OptionBuilder.isRequired(false);
    OptionBuilder.withLongOpt(PERSIST_OPTION);
    OptionBuilder.withDescription("Persist the indices to a database");
    return OptionBuilder.create(PERSIST_FLAG);
  }

  @Override
  protected void doRun() throws Exception {
    try (ToolContext toolContext = getToolContext()) {
      final CommandLine commandLine = getCommandLine();
      final boolean isPersist = commandLine.hasOption(PERSIST_FLAG);
      final boolean isSaveToFile = commandLine.hasOption(FILE_FLAG);
      final Set<ManageableConvention> conventions = new HashSet<>();
      for (final ConventionsLoader<? extends ManageableConvention> loader : LOADERS) {
        conventions.addAll(loader.loadConventionsFromFile());
      }
      final ConventionMasterInitializer initializer = new ConventionsPopulator(conventions);
      LOGGER.warn("Generated {} conventions", conventions.size());
      if (isPersist) {
        LOGGER.warn("Persisting {} conventions", conventions.size());
        final SecurityMaster securityMaster = toolContext.getSecurityMaster();
        final ConventionMaster conventionMaster = toolContext.getConventionMaster();
        initializer.init(conventionMaster, securityMaster);
        LOGGER.warn("Done");
      }
      if (isSaveToFile) {
        final SecurityMaster securityMaster = new InMemorySecurityMaster();
        final ConventionMaster conventionMaster = new InMemoryConventionMaster();
        initializer.init(conventionMaster, securityMaster);
        LOGGER.warn("Saving configurations to file");
        final SecuritySearchResult search = securityMaster.search(new SecuritySearchRequest());
        for (final ManageableSecurity security : search.getSecurities()) {
          final String quandlCode = security.getExternalIdBundle().getValue(QuandlConstants.QUANDL_CODE);
          if (quandlCode == null) {
            LOGGER.error("No QUANDL code found for index {}, skipping", security);
            continue;
          }
          final Path path = Paths.get(quandlCode);
          final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(security).getBytes();
          try (final OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
            out.write(xmlString);
          } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
          }
        }
        final ConventionSearchResult search2 = conventionMaster.search(new ConventionSearchRequest());
        for (final ManageableConvention convention : search2.getConventions()) {
          String code = convention.getExternalIdBundle().getValue(QuandlConstants.QUANDL_CODE);
          if (code == null) {
            code = convention.getExternalIdBundle().getValue(ExternalScheme.of("CONVENTION"));
          }
          if (code == null) {
            LOGGER.error("No QUANDL/CONVENTION code found for convention {}, skipping", convention);
            continue;
          }
          final Path path = Paths.get(code);
          final byte[] xmlString = JodaBeanSer.PRETTY.xmlWriter().write(convention).getBytes();
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
}
