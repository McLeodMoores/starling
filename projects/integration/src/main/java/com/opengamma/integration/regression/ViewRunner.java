/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 */
/* package */ class ViewRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewRunner.class);

  private final ConfigMaster _configMaster;
  private final ViewProcessor _viewProcessor;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final MarketDataSnapshotMaster _snapshotMaster;

  /* package */ ViewRunner(final ConfigMaster configMaster,
                           final ViewProcessor viewProcessor,
                           final PositionSource positionSource,
                           final SecuritySource securitySource,
                           final MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
    _configMaster = configMaster;
    _viewProcessor = viewProcessor;
    _positionSource = positionSource;
    _snapshotMaster = snapshotMaster;
  }

  // TODO convert to a tool
  /* TODO inputs
  working directories (and versions?) for each of the two servers
  views/snapshots/valuation times
    is it possible to infer it all? run each view using the snapshots which have it as their basis view?
    either have a fixed valuation time or use Instant.now() (but only once for all runs)
    where does the DB dump live? presumably with the test config. separate repo / project? part of integration tests repo?
    or checked into the main repo?

    after 'mvn install' the server is in
    $PROJECT_DIR/server/target/server-dir
   */
  public static void main(final String[] args) throws Exception {
    final Instant valuationTime = Instant.now();
    final int serverHttpPort = 8080;
    final String workingDir = System.getProperty("user.dir");
    final String configFile = "classpath:fullstack/fullstack-examplessimulated-bin.properties";
    final String projectName = "examples-simulated";
    final String version = "1.0.0-SNAPSHOT";
    final String serverJar = projectName + "-" + version + ".jar";
    final String classpath = "config:lib/" + serverJar;
    final String logbackConfig = "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml";
    try (ServerProcess ignored = ServerProcess.start(workingDir, classpath, configFile, new Properties(), logbackConfig);
         RemoteServer server = RemoteServer.create("http://localhost:" + serverHttpPort))  {
      final ViewRunner viewRunner = new ViewRunner(server.getConfigMaster(),
                                             server.getViewProcessor(),
                                             server.getPositionSource(),
                                             server.getSecuritySource(),
                                             server.getMarketDataSnapshotMaster());
      final CalculationResults results1 = viewRunner.run(
          version,
          "AUD Swaps (3m / 6m basis) (1)",
          "AUD Swaps (3m / 6m basis) (1)/2013-09-27T12:17:45.587Z", valuationTime);
      final CalculationResults results2 = viewRunner.run(
          version,
          "AUD Swaps (3m / 6m basis) (1)",
          "AUD Swaps (3m / 6m basis) (1)/2013-09-27T12:17:45.587Z", valuationTime);
      final CalculationDifference difference = CalculationDifference.between(results1, results2, 0.001d);
      System.out.println(difference);
    }
  }

  public CalculationResults run(final String version, final String viewName, final String snapshotName, final Instant valuationTime) {
    ArgumentChecker.notNull(viewName, "viewName");
    ArgumentChecker.notNull(snapshotName, "snapshotName");
    final UniqueId snapshotId = getSnapshotId(snapshotName);
    final UniqueId viewDefId = getViewDefinitionId(viewName);
    LOGGER.info("Running view {} using snapshot {} at valuation time {}", viewName, snapshotName, valuationTime);
    final List<MarketDataSpecification> marketDataSpecs =
        Lists.<MarketDataSpecification>newArrayList(UserMarketDataSpecification.of(snapshotId));
    final ViewCycleExecutionOptions cycleOptions =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(valuationTime)
            .setMarketDataSpecifications(marketDataSpecs) // TODO multiple snapshots without rebuilding the graph?
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.triggersEnabled().get();
    final ArbitraryViewCycleExecutionSequence sequence =
        new ArbitraryViewCycleExecutionSequence(ImmutableList.of(cycleOptions));
    final ViewExecutionOptions executionOptions = ExecutionOptions.of(sequence, cycleOptions, flags);

    final ViewProcessor viewProcessor = _viewProcessor;
    final ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    final Listener listener = new Listener(_positionSource, _securitySource, snapshotName, version);
    viewClient.setResultListener(listener);
    viewClient.setResultMode(ViewResultMode.FULL_ONLY);
    System.out.println("attaching to view process for view definition '" + viewName + "' with snapshot '" + snapshotName + "'");
    viewClient.attachToViewProcess(viewDefId, executionOptions, true);
    System.out.println("waiting for completion");
    try {
      viewClient.waitForCompletion();
      System.out.println("view client completed");
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted waiting for view client to complete", e);
    }
    viewClient.shutdown();
    return listener.getResults();
  }

  private UniqueId getSnapshotId(final String snapshotName) {
    //String snapshotTime = "2013-09-27T12:17:45.587Z";
    //String snapshotName = snapshotName + "/" + snapshotTime;
    final MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setName(snapshotName);
    snapshotSearchRequest.setIncludeData(false);
    final MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    return snapshotSearchResult.getSingleSnapshot().getUniqueId();
  }

  private UniqueId getViewDefinitionId(final String viewDefName) {
    final ConfigSearchRequest<ViewDefinition> configSearchRequest = new ConfigSearchRequest<>(ViewDefinition.class);
    configSearchRequest.setName(viewDefName);
    final ConfigSearchResult<ViewDefinition> configSearchResult = _configMaster.search(configSearchRequest);
    return configSearchResult.getSingleValue().getValue().getUniqueId();
  }
}

class Listener extends AbstractViewResultListener {

  private final AtomicReference<CompiledViewDefinition> _viewDef = new AtomicReference<>();
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final CountDownLatch _latch = new CountDownLatch(1);
  private final String _version;
  private final String _snapshotName;

  private CalculationResults _results;

  Listener(final PositionSource positionSource,
           final SecuritySource securitySource,
           final String snapshotName,
           final String version) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notEmpty(snapshotName, "snapshotName");
    ArgumentChecker.notEmpty(version, "version");
    _version = version;
    _securitySource = securitySource;
    _snapshotName = snapshotName;
    _positionSource = positionSource;
  }

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }

  @Override
  public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
    System.out.println("view def compiled");
    _viewDef.set(compiledViewDefinition);
  }

  @Override
  public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
    System.out.println("view def compilation failed " + exception);
  }

  @Override
  public void processTerminated(final boolean executionInterrupted) {
    System.out.println("process terminated");
  }

  @Override
  public void processCompleted() {
    System.out.println("process completed");
  }

  @Override
  public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
    try {
      System.out.println("cycle completed - building CalculationResults object");
      _results = CalculationResults.create(fullResult, _viewDef.get(), _snapshotName, fullResult.getViewCycleExecutionOptions().getValuationTime(),
                                           _version, _positionSource, _securitySource);
      System.out.println("built CalculationResults object");
    } finally {
      _latch.countDown();
    }
  }

  public CalculationResults getResults() {
    try {
      _latch.await();
    } catch (final InterruptedException e) {
      // not going to happen
      throw new OpenGammaRuntimeException("unexpected exception", e);
    }
    return _results;
  }
}
