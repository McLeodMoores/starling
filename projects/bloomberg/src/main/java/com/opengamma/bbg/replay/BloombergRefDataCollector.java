/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConnectorFactoryBean;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.cli.BloombergCliOptions;
import com.opengamma.bbg.cli.BloombergCliOptions.Builder;
import com.opengamma.bbg.livedata.LoggingReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A collector of Bloomberg data.
 */
public class BloombergRefDataCollector implements Lifecycle {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergRefDataCollector.class);
  /** Fudge context. */
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * The watch list file.
   */
  private final File _watchListFile;
  /**
   * The fields file.
   */
  private final File _fieldsFile;
  /**
   * The reference data provider.
   */
  private final LoggingReferenceDataProvider _loggingRefDataProvider;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * Whether the service has started.
   */
  private final AtomicBoolean _started = new AtomicBoolean();

  /**
   * Create an instance.
   *
   * @param fudgeContext
   *          the fudgeContext, not null
   * @param watchListFile
   *          the watch list file, not null
   * @param refDataProvider
   *          the reference data provider, not null
   * @param fieldsFile
   *          the file containing the fields
   * @param outputFile
   *          the output file, not null
   */
  public BloombergRefDataCollector(final FudgeContext fudgeContext, final File watchListFile, final ReferenceDataProvider refDataProvider,
      final File fieldsFile, final File outputFile) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(watchListFile, "watch list file");
    ArgumentChecker.notNull(refDataProvider, "reference data provider");
    ArgumentChecker.notNull(fieldsFile, "fields input file");
    ArgumentChecker.notNull(outputFile, "out put file");
    _fudgeContext = fudgeContext;
    _watchListFile = watchListFile;
    _fieldsFile = fieldsFile;
    _loggingRefDataProvider = new LoggingReferenceDataProvider(refDataProvider, _fudgeContext, outputFile);
  }

  /**
   * Create an instance.
   *
   * @param watchListFile
   *          the watch list file, not null
   * @param refDataProvider
   *          the reference data provider, not null
   * @param fieldsFile
   *          the file containing the fields
   * @param outputFile
   *          the output file, not null
   */
  public BloombergRefDataCollector(final File watchListFile, final ReferenceDataProvider refDataProvider, final File fieldsFile, final File outputFile) {
    this(FUDGE_CONTEXT, watchListFile, refDataProvider, fieldsFile, outputFile);
  }

  // -------------------------------------------------------------------------
  @Override
  public synchronized void start() {
    LOGGER.info("starting bloombergRefDataCollector");
    if (isRunning()) {
      LOGGER.info("bloombergRefDataCollector is already running");
      return;
    }
    _started.set(true);
    _loggingRefDataProvider.getReferenceData(loadSecurities(), loadFields());
    _started.set(false);
  }

  private Set<String> loadFields() {
    final Set<String> fields = Sets.newHashSet();
    LineIterator it;
    try {
      it = FileUtils.lineIterator(_fieldsFile);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("IOException when reading " + _fieldsFile, ex);
    }
    try {
      while (it.hasNext()) {
        final String line = it.nextLine();
        if (StringUtils.isBlank(line) || line.charAt(0) == '#') {
          continue;
        }
        fields.add(line);
      }
    } finally {
      LineIterator.closeQuietly(it);
    }
    return fields;
  }

  private Set<String> loadSecurities() {
    final Set<String> bloombergKeys = Sets.newHashSet();
    try {
      for (final ExternalId identifier : BloombergDataUtils.identifierLoader(new FileReader(_watchListFile))) {
        bloombergKeys.add(BloombergDomainIdentifierResolver.toBloombergKey(identifier));
      }
    } catch (final FileNotFoundException ex) {
      throw new OpenGammaRuntimeException(_watchListFile + " cannot be found", ex);
    }
    return bloombergKeys;
  }

  @Override
  public synchronized void stop() {
    LOGGER.info("stopping bloombergRefDataCollector");
    _started.set(false);
  }

  @Override
  public synchronized boolean isRunning() {
    return _started.get();
  }

  // -------------------------------------------------------------------------
  /**
   * Main entry point from command line.
   *
   * @param args
   *          the args
   */
  public static void main(final String[] args) { // CSIGNORE
    final BloombergCliOptions bbgOptions = createOptions();
    processCommandLineOptions(args, bbgOptions);
  }

  private static void processCommandLineOptions(final String[] args, final BloombergCliOptions bbgOptions) {
    final CommandLine cmdLine = bbgOptions.parse(args);
    if (cmdLine == null) {
      bbgOptions.printUsage(BloombergRefDataCollector.class);
      return;
    }
    if (cmdLine.getOptionValue(BloombergCliOptions.HELP_OPTION) != null) {
      bbgOptions.printUsage(BloombergRefDataCollector.class);
      return;
    }
    final String dataFieldFile = cmdLine.getOptionValue(BloombergCliOptions.FIELDS_FILE_OPTION);
    final String identifiersFile = cmdLine.getOptionValue(BloombergCliOptions.IDENTIFIERS_OPTION);
    final String outputFile = cmdLine.getOptionValue(BloombergCliOptions.OUPUT_OPTION);
    final String host = cmdLine.getOptionValue(BloombergCliOptions.HOST_OPTION);
    String port = cmdLine.getOptionValue(BloombergCliOptions.PORT_OPTION);

    if (port == null) {
      port = BloombergConstants.DEFAULT_PORT;
    }

    LOGGER.info("loading ref data with host: {} port: {} fields: {} identifies: {} outputfile {}",
        new Object[] { host, port, dataFieldFile, identifiersFile, outputFile });

    final BloombergConnectorFactoryBean factory = new BloombergConnectorFactoryBean("BloombergRefDataCollector", host, Integer.valueOf(port));
    final BloombergConnector bloombergConnector = factory.getObjectCreating();
    final BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(bloombergConnector);
    refDataProvider.start();

    final BloombergRefDataCollector refDataCollector = new BloombergRefDataCollector(new File(identifiersFile), refDataProvider, new File(dataFieldFile),
        new File(outputFile));
    refDataCollector.start();
  }

  private static BloombergCliOptions createOptions() {
    final Builder builder = new BloombergCliOptions.Builder()
        .withDataFieldsFile(true)
        .withIdentifiers(true)
        .withOutput(true)
        .withHost(true)
        .withPort(false);
    return builder.build();
  }

}
