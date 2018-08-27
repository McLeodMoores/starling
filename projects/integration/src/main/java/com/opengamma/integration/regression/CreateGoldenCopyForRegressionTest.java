/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class CreateGoldenCopyForRegressionTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateGoldenCopyForRegressionTest.class);

  private final String _dbDumpDir;
  private final Instant _valuationTime;
  private final String _baseWorkingDir;
  private final String _baseVersion;
  private final String _baseDbConfigFile;
  private final String _baseClasspath;

  private final String _serverConfigFile;
  private final String _logbackConfig;



  public CreateGoldenCopyForRegressionTest(final String projectName,
                                           final String serverConfigFile,
                                            final String dbDumpDir,
                                           final String logbackConfigFile,
                                           final Instant valuationTime,
                                           final String baseWorkingDir,
                                           final String baseVersion,
                                           final String baseDbConfigFile) {
    _dbDumpDir = dbDumpDir;
    _baseWorkingDir = baseWorkingDir;
    _baseVersion = baseVersion;
    _baseDbConfigFile = baseDbConfigFile;
    _serverConfigFile = serverConfigFile;
    _logbackConfig = "-Dlogback.configurationFile=" + logbackConfigFile;
    _baseClasspath = "config:lib/" + projectName + "-" + baseVersion + ".jar";
    _valuationTime = valuationTime;
  }

  public Map<Pair<String, String>, CalculationResults> run() {
    // TODO store the results in memory for now, serialize to disk/cache when it's an actual problem
    // TODO fail if there are any view defs or snapshots with duplicate names
    return runTest(_baseWorkingDir, _baseClasspath, _baseVersion, _baseDbConfigFile);
  }

  private Map<Pair<String, String>, CalculationResults> runTest(final String workingDir,
                                                                final String classpath,
                                                                final String version,
                                                                final String dbPropsFile) {
    // don't use the config file to be sure we don't accidentally clobber a real database
    final Properties dbProps = RegressionUtils.loadProperties(dbPropsFile);
    if (_dbDumpDir != null) {
      RegressionUtils.createEmptyDatabase(dbPropsFile, workingDir, classpath, _logbackConfig);
      RegressionUtils.restoreDatabase(workingDir, classpath, dbProps, _serverConfigFile, _logbackConfig, _dbDumpDir);
    }
    return runViews(workingDir, classpath, version, _valuationTime, dbProps);
  }

  private Map<Pair<String, String>, CalculationResults> runViews(final String workingDir,
                                                                 final String classpath,
                                                                 final String version,
                                                                 final Instant valuationTime,
                                                                 final Properties dbProps) {
    // TODO don't hard-code the port
    final int port = 8080;
    final String serverUrl = "http://localhost:" + port;

    // start the server again to run the tests
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, _serverConfigFile, dbProps, _logbackConfig);
         RemoteServer server = RemoteServer.create(serverUrl)) {
      final Map<Pair<String, String>, CalculationResults> allResults = Maps.newHashMap();
      final Collection<Pair<String, String>> viewAndSnapshotNames = getViewAndSnapshotNames(server.getConfigMaster(),
                                                                                      server.getMarketDataSnapshotMaster());
      final ViewRunner viewRunner = new ViewRunner(server.getConfigMaster(),
                                             server.getViewProcessor(),
                                             server.getPositionSource(),
                                             server.getSecuritySource(),
                                             server.getMarketDataSnapshotMaster());
      for (final Pair<String, String> names : viewAndSnapshotNames) {
        final String viewName = names.getFirst();
        final String snapshotName = names.getSecond();
        final CalculationResults results = viewRunner.run(version, viewName, snapshotName, valuationTime);
        allResults.put(names, results);
      }
      return allResults;
    }
  }

  private static Collection<Pair<String, String>> getViewAndSnapshotNames(final ConfigMaster configMaster,
                                                                          final MarketDataSnapshotMaster snapshotMaster) {
    final List<Pair<String, String>> viewAndSnapshotNames = Lists.newArrayList();
    final MarketDataSnapshotSearchRequest snapshotRequest = new MarketDataSnapshotSearchRequest();
    // TODO this isn't great but is necessary because of PLAT-4793
    snapshotRequest.setIncludeData(true);
    final MarketDataSnapshotSearchResult snapshotResult = snapshotMaster.search(snapshotRequest);
    for (final ManageableMarketDataSnapshot snapshot : snapshotResult.getSnapshots()) {
      final String basisViewName = snapshot.getBasisViewName();
      if (basisViewName != null) {
        final ConfigSearchRequest<ViewDefinition> configRequest = new ConfigSearchRequest<>(ViewDefinition.class);
        configRequest.setName(basisViewName);
        final ConfigSearchResult<ViewDefinition> configResult = configMaster.search(configRequest);
        if (configResult.getValues().size() > 1) {
          LOGGER.warn("Multiple view definitions found with the same name '{}'", basisViewName);
          continue;
        }
        final String viewDefName = configResult.getSingleValue().getName();
        viewAndSnapshotNames.add(Pairs.of(viewDefName, snapshot.getName()));
      }
    }
    return viewAndSnapshotNames;
  }
}
