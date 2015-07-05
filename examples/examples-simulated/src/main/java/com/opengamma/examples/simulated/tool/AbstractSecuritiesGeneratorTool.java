/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.tool;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.examples.simulated.generator.SecuritiesGenerator;
import com.opengamma.financial.generator.InMemorySecurityPersister;
import com.opengamma.financial.generator.MasterSecurityPersister;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SecurityPersister;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for tools that generate securities.
 */
public abstract class AbstractSecuritiesGeneratorTool {
  /** Command line option to specify to write to the database masters. */
  public static final String WRITE_OPT = "write";
  /** Command line option to specifying the asset class to generate the portfolio for. */
  public static final String GENERATOR_OPT = "generator";
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(SyntheticSecuritiesGeneratorTool.class);
  /** The tool context */
  private ToolContext _toolContext;
  /** The class context */
  private Class<? extends AbstractSecuritiesGeneratorTool> _classContext;
  /** The object context */
  private AbstractSecuritiesGeneratorTool _objectContext;
  /** The security persister */
  private SecurityPersister _securityPersister;

  /**
   * Sets the class context to the sub-class.
   */
  public AbstractSecuritiesGeneratorTool() {
    _classContext = getClass();
  }

  /**
   * The securities generator creates the list of securities.
   * @return The securities generator
   */
  public abstract SecuritiesGenerator createSecuritiesGenerator();

  /**
   * Creates a list of {@link ManageableSecurity} from the securities generator.
   * @return The list of securities
   */
  public List<ManageableSecurity> createSecurities() {
    return createSecuritiesGenerator().createManageableSecurities();
  }

  /**
   * Runs this tool given a tool context and command line options. The -s option
   * is required (this sets the name of the securities generator), and -w will
   * save the securities to the security master.
   * @param context The tool context, not null
   * @param commandLine The command line options, not null
   */
  public void run(final ToolContext context, final CommandLine commandLine) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(commandLine, "commandLine");
    run(context, commandLine.getOptionValue(GENERATOR_OPT), commandLine.hasOption(WRITE_OPT));
  }

  /**
   * Runs this tool given a tool context, the securities generator name and a flag
   * indicating whether or not to write the securities to the database.
   * @param context The tool context, not null
   * @param generatorName The securities generator name, not null
   * @param write True if the securities are to be written to the security master
   */
  public void run(final ToolContext context, final String generatorName, final boolean write) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(generatorName, "generatorName");
    final AbstractSecuritiesGeneratorTool instance = getInstance(getClassContext(), generatorName);
    instance.setToolContext(context);
    if (write) {
      LOGGER.info("Creating database security writer");
      instance.setSecurityPersister(new MasterSecurityPersister(context.getSecurityMaster()));
    } else {
      LOGGER.info("Using dummy security writer");
      final InMemorySecurityPersister securityPersister = new InMemorySecurityPersister();
      instance.setSecurityPersister(securityPersister);
    }
    final List<ManageableSecurity> securities = instance.createSecurities();
    if (write) {
      LOGGER.info("Writing securities to the database");
      for (final ManageableSecurity security : securities) {
        final SecuritySearchRequest request = new SecuritySearchRequest();
        request.setName(security.getName());
        final SecuritySearchResult result = context.getSecurityMaster().search(request);
        SecurityDocument document = result.getFirstDocument();
        if (document != null) {
          LOGGER.warn("Overwriting security {}", document.getUniqueId());
          document.setSecurity(security);
          context.getSecurityMaster().update(document);
        } else {
          document = new SecurityDocument(security);
          context.getSecurityMaster().add(document);
        }
      }
    }
  }

  /**
   * Runs this tool given a tool context, the securities generator name and a flag
   * indicating whether or not to write the securities to the database.
   * @param context The tool context, not null
   * @param instance The securities generator, not null
   * @param write True if the securities are to be written to the security master
   */
  public void run(final ToolContext context, final AbstractSecuritiesGeneratorTool instance, final boolean write) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(instance, "generator");
    instance.setToolContext(context);
    if (write) {
      LOGGER.info("Creating database security writer");
      instance.setSecurityPersister(new MasterSecurityPersister(context.getSecurityMaster()));
    } else {
      LOGGER.info("Using dummy security writer");
      final InMemorySecurityPersister securityPersister = new InMemorySecurityPersister();
      instance.setSecurityPersister(securityPersister);
    }
    final List<ManageableSecurity> securities = instance.createSecurities();
    if (write) {
      LOGGER.info("Writing securities to the database");
      for (final ManageableSecurity security : securities) {
        final SecuritySearchRequest request = new SecuritySearchRequest();
        request.setName(security.getName());
        final SecuritySearchResult result = context.getSecurityMaster().search(request);
        SecurityDocument document = result.getFirstDocument();
        if (document != null) {
          LOGGER.warn("Overwriting security {}", document.getUniqueId());
          document.setSecurity(security);
          context.getSecurityMaster().update(document);
        } else {
          document = new SecurityDocument(security);
          context.getSecurityMaster().add(document);
        }
      }
    }
  }

  /**
   * Gets the tool context.
   * @return The tool context
   */
  public ToolContext getToolContext() {
    return _toolContext;
  }

  /**
   * Sets the tool context.
   * @param toolContext The tool context
   */
  public void setToolContext(final ToolContext toolContext) {
    _toolContext = toolContext;
  }

  /**
   * Gets the security persister.
   * @return The security persister
   */
  public SecurityPersister getSecurityPersister() {
    return _securityPersister;
  }

  /**
   * Sets the security persister.
   * @param securityPersister The security persister, not null
   */
  public void setSecurityPersister(final SecurityPersister securityPersister) {
    ArgumentChecker.notNull(securityPersister, "securityPersister");
    _securityPersister = securityPersister;
  }

  /**
   * Configures the tool context. If the tool context has been set, sets<p>
   * <ul>
   * <li> config source
   * <li> convention source
   * <li> convention bundle source
   * <li> holiday source
   * <li> historical source
   * <li> exchange master
   * <li> region source
   * <li> legal entity source
   * <li> security master
   * <li> historical time series master
   * </ul>
   * @param securityGenerator The security generator, not null if the tool context is not null
   */
  protected final void configure(final SecurityGenerator<?> securityGenerator) {
    if (getToolContext() != null) {
      ArgumentChecker.notNull(securityGenerator, "securityGenerator");
      securityGenerator.setConfigSource(getToolContext().getConfigSource());
      securityGenerator.setConventionSource(getToolContext().getConventionSource());
      securityGenerator.setConventionBundleSource(getToolContext().getConventionBundleSource());
      securityGenerator.setHolidaySource(getToolContext().getHolidaySource());
      securityGenerator.setHistoricalSource(getToolContext().getHistoricalTimeSeriesSource());
      securityGenerator.setExchangeMaster(getToolContext().getExchangeMaster());
      securityGenerator.setRegionSource(getToolContext().getRegionSource());
      securityGenerator.setLegalEntitySource(getToolContext().getLegalEntitySource());
      securityGenerator.setSecurityMaster(getToolContext().getSecurityMaster());
      securityGenerator.setHistoricalTimeSeriesMaster(getToolContext().getHistoricalTimeSeriesMaster());
    }
    configureChain(securityGenerator);
  }

  /**
   * Configures the chain for this tool and security generator.
   * @param securityGenerator The security generator, not null if the object context is not null
   */
  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    if (getObjectContext() != null) {
      ArgumentChecker.notNull(securityGenerator, "securityGenerator");
      getObjectContext().configureChain(securityGenerator);
    }
  }

  /**
   * Configures the chain for this tool by setting the tool context and security persister.
   * @param tool The tool, not null
   */
  protected void configure(final AbstractSecuritiesGeneratorTool tool) {
    if (getToolContext() != null) {
      ArgumentChecker.notNull(tool, "tool");
      tool.setToolContext(getToolContext());
    }
    if (getSecurityPersister() != null) {
      ArgumentChecker.notNull(tool, "tool");
      tool.setSecurityPersister(getSecurityPersister());
    }
  }

  /**
   * Sets the class context and the object context for this tool.
   * @param classContext The class context
   * @param objectContext The object context
   */
  private void setContext(final Class<? extends AbstractSecuritiesGeneratorTool> classContext, final AbstractSecuritiesGeneratorTool objectContext) {
    _classContext = classContext;
    _objectContext = objectContext;
  }

  /**
   * Gets the class context.
   * @return The class context
   */
  private Class<? extends AbstractSecuritiesGeneratorTool> getClassContext() {
    return _classContext;
  }

  /**
   * Gets the object context.
   * @return The object context
   */
  private AbstractSecuritiesGeneratorTool getObjectContext() {
    return _objectContext;
  }

  /**
   * Gets an instance of the tool given its class and name. If the generator name is not a
   * full path (i.e. contains a "."), this tool looks in the directory of this class for a
   * class named [generatorName]SecuritiesGeneratorTool or [generatorName]SecurityGeneratorTool.
   * If the class is not found, tries the superclass of the required tool.
   * @param clazz The class of the required securities generator tool
   * @param generatorName The name
   * @return The tool
   * @throws OpenGammaRuntimeException If the securities generator cannot be found or instantiated
   */
  private AbstractSecuritiesGeneratorTool getInstance(final Class<?> clazz, final String generatorName) {
    if (!AbstractSecuritiesGeneratorTool.class.isAssignableFrom(clazz)) {
      throw new OpenGammaRuntimeException("Couldn't find securites generator tool class for " + generatorName);
    }
    try {
      final String className;
      String alternativeClassName = null;
      final int i = generatorName.indexOf('.');
      if (i < 0) {
        className = clazz.getPackage().getName() + "." + generatorName + "SecuritiesGeneratorTool";
        alternativeClassName = clazz.getPackage().getName() + "." + generatorName + "SecurityGeneratorTool";
      } else {
        className = generatorName;
      }
      Class<?> instanceClass;
      try {
        LOGGER.debug("Trying class {}", className);
        instanceClass = Class.forName(className);
      } catch (final ClassNotFoundException e) {
        try {
          LOGGER.debug("Trying class {}", alternativeClassName);
          instanceClass = Class.forName(alternativeClassName);
        } catch (final ClassNotFoundException e1) {
          return getInstance(clazz.getSuperclass(), generatorName);
        }
      }
      LOGGER.info("Loading {}", className);
      final AbstractSecuritiesGeneratorTool tool = (AbstractSecuritiesGeneratorTool) instanceClass.newInstance();
      tool.setContext(getClassContext(), this);
      return tool;
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Couldn't create securities generator tool instance for " + generatorName, e);
    }
  }

  /**
   * Sets the required field on an option to true.
   * @param option The option
   * @return The option with the required field set to true
   */
  private static Option required(final Option option) {
    option.setRequired(true);
    return option;
  }

  /**
   * Sets the options for this tool: -s for the security generator name, -w to write to the
   * security master.
   * @param options The options
   */
  public void createOptions(final Options options) {
    options.addOption(required(new Option("s", GENERATOR_OPT, true, "selects the security generator")));
    options.addOption(new Option("w", WRITE_OPT, false, "writes the securities to the master"));
  }
}
