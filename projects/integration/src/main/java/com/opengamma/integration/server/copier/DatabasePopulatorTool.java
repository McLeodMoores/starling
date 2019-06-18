/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server.copier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.config.ConfigLoader;
import com.opengamma.integration.tool.config.ConfigSaver;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.PortfolioSearchIterator;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 *
 */
public class DatabasePopulatorTool extends AbstractTool<ToolContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePopulatorTool.class);
  /**
   * Demo function configuration object name.
   */
  public static final String DEMO_FUNCTION = "DEMO_FUNCTIONS";
  /**
   * URL of opengamma server to copy data from
   */
  private final String _serverUrl;
  private final ExecutorService _executorService = Executors.newFixedThreadPool(10);
  private final ExecutorCompletionService<UniqueId> _completionService = new ExecutorCompletionService<>(_executorService);
  private final List<HistoricalTimeSeriesInfoDocument> _tsList = Lists.newArrayList();

  public DatabasePopulatorTool(final String serverUrl) {
    ArgumentChecker.notNull(serverUrl, "serverUrl");
    _serverUrl = serverUrl;
  }

  @Override
  protected void doRun() throws Exception {
    final ToolContext toolContext = getToolContext();
    loadSecurity(toolContext.getSecurityMaster());
    loadPortfolio(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(), toolContext.getSecurityMaster(), toolContext.getSecuritySource());
    loadConfig(toolContext.getConfigMaster(), toolContext.getPortfolioMaster());
    loadHistoricalTimeSeries(toolContext.getHistoricalTimeSeriesMaster());
    loadSnapshot(toolContext.getMarketDataSnapshotMaster());
    loadFunctionConfiguration(toolContext.getConfigMaster());
    _executorService.shutdown();
  }

  protected void loadFunctionConfiguration(final ConfigMaster configMaster) {
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final FunctionConfigurationSource functionConfigSource = getToolContext().getFunctionConfigSource();
        final FunctionConfigurationDefinition definition = FunctionConfigurationDefinition.of(DEMO_FUNCTION, functionConfigSource);
        final ConfigItem<FunctionConfigurationDefinition> config = ConfigItem.of(definition, DEMO_FUNCTION, FunctionConfigurationDefinition.class);
        ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
      }
    };
    final String[] args = { "-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadSecurity(final SecurityMaster demoSecurityMaster) {
    LOGGER.info("loading securities");
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final SecurityMaster remotesecurityMaster = getToolContext().getSecurityMaster();
        for (final SecurityDocument securityDocument : SecuritySearchIterator.iterable(remotesecurityMaster, new SecuritySearchRequest())) {
          securityDocument.setUniqueId(null);
          demoSecurityMaster.add(securityDocument);
        }
      }
    };
    final String[] args = { "-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadPortfolio(final PortfolioMaster demoPortfolioMaster, final PositionMaster demoPositionMaster,
      final SecurityMaster demoSecurityMaster, final SecuritySource demoSecuritySource) {
    LOGGER.info("loading portfolios");
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final PortfolioMaster remotePortfolioMaster = getToolContext().getPortfolioMaster();
        final PositionSource remotePositionSource = getToolContext().getPositionSource();

        final PortfolioSearchRequest request = new PortfolioSearchRequest();
        request.setDepth(0);
        for (final PortfolioDocument portfolioDocument : PortfolioSearchIterator.iterable(remotePortfolioMaster, request)) {
          final Portfolio portfolio = remotePositionSource.getPortfolio(portfolioDocument.getUniqueId(), VersionCorrection.LATEST);
          Portfolio resolvePortfolio = null;
          try {
            resolvePortfolio = PortfolioCompiler.resolvePortfolio(portfolio, _executorService, getToolContext().getSecuritySource());
          } catch (final Exception ex) {
            LOGGER.warn(String.format("Error resolving porfolio %s", portfolio.getName()), ex);
            continue;
          }
          final SavePortfolio savePortfolio = new SavePortfolio(_executorService, demoPortfolioMaster, demoPositionMaster);
          savePortfolio.savePortfolio(resolvePortfolio, true);
        }
      }
    };
    final String[] args = { "-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadConfig(final ConfigMaster configMaster, final PortfolioMaster portfolioMaster) {
    LOGGER.info("loading configs");
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final ConfigMaster remoteConfigMaster = getToolContext().getConfigMaster();
        final PortfolioMaster remotePortfolioMaster = getToolContext().getPortfolioMaster();
        final ConfigSaver configSaver = new ConfigSaver(remoteConfigMaster, remotePortfolioMaster, new ArrayList<String>(), new ArrayList<String>(), true, true,
            ConfigSearchSortOrder.VERSION_FROM_INSTANT_DESC);
        final ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        final PrintStream outputStream = new PrintStream(byteArrayOutput);
        configSaver.saveConfigs(outputStream);
        final ConfigLoader configLoader = new ConfigLoader(configMaster, portfolioMaster, true, true, true);
        configLoader.loadConfig(new ByteArrayInputStream(byteArrayOutput.toByteArray()));
      }
    };
    final String[] args = { "-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadHistoricalTimeSeries(final HistoricalTimeSeriesMaster htsMaster) {
    LOGGER.info("loading timeseries");
    final OperationTimer timer = new OperationTimer(LOGGER, "Loading time series");
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final HistoricalTimeSeriesMaster remoteHtsMaster = getToolContext().getHistoricalTimeSeriesMaster();
        for (final HistoricalTimeSeriesInfoDocument infoDoc : HistoricalTimeSeriesInfoSearchIterator.iterable(remoteHtsMaster,
            new HistoricalTimeSeriesInfoSearchRequest())) {
          final ObjectId timeSeriesObjectId = infoDoc.getInfo().getTimeSeriesObjectId();
          final ManageableHistoricalTimeSeries timeSeries = remoteHtsMaster.getTimeSeries(timeSeriesObjectId, VersionCorrection.LATEST);
          _tsList.add(infoDoc);
          _completionService.submit(new Callable<UniqueId>() {

            @Override
            public UniqueId call() throws Exception {
              try {
                final ManageableHistoricalTimeSeriesInfo added = htsMaster.add(infoDoc).getInfo();
                htsMaster.updateTimeSeriesDataPoints(added.getTimeSeriesObjectId(), timeSeries.getTimeSeries());
                return added.getUniqueId();
              } catch (final Exception ex) {
                ex.printStackTrace();
                return null;
              }
            }
          });
        }
      }
    };
    final String[] args = { "-c", getServerUrl() };
    remoteServerTool.initAndRun(args, ToolContext.class);
    for (int i = 0; i < _tsList.size(); i++) {
      try {
        _completionService.take();
      } catch (final Exception ex) {
        throw new OpenGammaRuntimeException("Error writing TS to remote master", ex);
      }
    }
    timer.finished();
  }

  protected void loadSnapshot(final MarketDataSnapshotMaster marketDataSnapshotMaster) {
    LOGGER.info("loading market data snapshots");
    final AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final MarketDataSnapshotMaster remoteSnapshotMaster = getToolContext().getMarketDataSnapshotMaster();
        final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
        for (final MarketDataSnapshotDocument snapshotDocument : MarketDataSnapshotSearchIterator.iterable(remoteSnapshotMaster, request)) {
          marketDataSnapshotMaster.add(snapshotDocument);
        }
      }
    };
    final String[] args = { "-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  /**
   * Gets the serverUrl.
   * 
   * @return the serverUrl
   */
  public String getServerUrl() {
    return _serverUrl;
  }

}
