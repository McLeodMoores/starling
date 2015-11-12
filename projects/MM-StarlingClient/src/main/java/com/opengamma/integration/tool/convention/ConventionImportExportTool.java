package com.opengamma.integration.tool.convention;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read conventions from a text file and store them in the convention master.
 * Adapted from the OpenGamma <code>ConfigImportExportTool</code>
 */
@Scriptable
public class ConventionImportExportTool extends AbstractTool<ToolContext> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionImportExportTool.class);

  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ConventionImportExportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    try (final ToolContext toolContext = getToolContext()) {
      final ConventionMaster conventionMaster = toolContext.getConventionMaster();
      final CommandLine commandLine = getCommandLine();
      final
      List<String> fileList = commandLine.getArgList();
      for (final String file : fileList) {
        System.err.println(file);
      }
      final boolean verbose = commandLine.hasOption("verbose");
      if (commandLine.hasOption("load")) {
        checkForInvalidOption("type");
        checkForInvalidOption("name");
        checkForInvalidOption("save");
        checkForInvalidOption("sort-by-name");
        final boolean persist = !commandLine.hasOption("do-not-persist"); // NOTE: inverted logic here
        final ConventionLoader conventionLoader = new ConventionLoader(conventionMaster, persist, verbose);
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
          for (final String fileName : fileList) {
            if (verbose) {
              LOGGER.info("Processing " + fileName);
            }
            try (final FileInputStream inputStream = new FileInputStream(fileName)) {
              conventionLoader.loadConvention(inputStream);
            } catch (final IOException ioe) {
              if (verbose) {
                LOGGER.error("An I/O error occurred while processing a file (run with -v to see stack trace)");
              } else {
                LOGGER.error("An I/O error occurred while processing a file", ioe);
              }
            }
          }
        } else {
          if (verbose) {
            LOGGER.info("No file name given, assuming STDIN");
          }
          conventionLoader.loadConvention(System.in);
        }
      } else if (commandLine.hasOption("save")) {
        if (verbose) {
          LOGGER.info("Save option active");
        }
        checkForInvalidOption("do-not-persist");
        final List<String> types = getTypes();
        final List<String> names = getNames();
        PrintStream outputStream;
        if (fileList.size() == 1) {
          try {
            outputStream = new PrintStream(new FileOutputStream(fileList.get(0)));
          } catch (final FileNotFoundException ex) {
            LOGGER.error("Couldn't find file " + fileList.get(0));
            System.exit(1);
            return;
          }
        } else {
          outputStream = System.out;
        }
        ConventionSearchSortOrder order = ConventionSearchSortOrder.VERSION_FROM_INSTANT_DESC;
        if (commandLine.hasOption("sort-by-name")) {
          order = ConventionSearchSortOrder.NAME_ASC;
        }
        final ConventionSaver conventionSaver = new ConventionSaver(conventionMaster, names, types, verbose, order);
        conventionSaver.saveConventions(outputStream);
        System.out.println("Warning: file may have been created in installation base directory");
      }
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private List<String> getTypes() {
    if (getCommandLine().hasOption("type")) {
      final String[] typeValues = getCommandLine().getOptionValues("type");
      return Arrays.asList(typeValues);
    }
    return Collections.emptyList();
  }

  private List<String> getNames() {
    if (getCommandLine().hasOption("name")) {
      final String[] nameValues = getCommandLine().getOptionValues("name");
      return Arrays.asList(nameValues);
    }
    return Collections.emptyList();
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
    options.addOption(createSearchOption());
    options.addOption(createLoadOption());
    options.addOption(createSaveOption());
    options.addOption(createDoNotPersistOption());
    options.addOption(createVerboseOption());
    options.addOption(createSortOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createTypeOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("full class name").withDescription("The type(s) you want to export").withLongOpt("type").create("t");
  }

  @SuppressWarnings("static-access")
  private Option createSearchOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("name search string").withDescription("The name(s) you want to search for (globbing available)").withLongOpt("name")
        .create("n");
  }

  @SuppressWarnings("static-access")
  private Option createLoadOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Load from file to convention database").withLongOpt("load").create("load");
  }

  @SuppressWarnings("static-access")
  private Option createSaveOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Save to file from convention database").withLongOpt("save").create("save");
  }

  @SuppressWarnings("static-access")
  private Option createDoNotPersistOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Simulate writing rather than actually writing to DB").withLongOpt("do-not-persist").create("d");
  }

  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Display extra error messages").withLongOpt("verbose").create("v");
  }

  @SuppressWarnings("static-access")
  private Option createSortOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Sort output by config name (default=most recent first)").withLongOpt("sort-by-name").create("s");
  }

  @Override
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("convention-import-export-tool.sh [file...]", options, true);
  }

}
