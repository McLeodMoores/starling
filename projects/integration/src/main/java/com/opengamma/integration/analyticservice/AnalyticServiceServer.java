/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Analytic service server.
 */
public class AnalyticServiceServer implements TradeListener, Lifecycle {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticServiceServer.class);

  private final ViewProcessor _viewProcessor;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigSource _configSource;
  private ObjectId _portfolioId;
  private UniqueId _viewId;
  private String _viewName;
  private UserPrincipal _user;
  private ViewClient _viewClient;
  private AnalyticResultReceiver _analyticResultReceiver;

  private final ReentrantLock _updateLock = new ReentrantLock();
  private final ViewExecutionOptions _viewExecutionOptions = ExecutionOptions.infinite(MarketData.live());
  private final boolean _usePrivateProcess = true;
  private String _providerIdName;
  private TradeProducer _tradeProducer;
  private final ExecutorService _tradeUpdaterExecutor = Executors.newSingleThreadExecutor();
  private final AtomicBoolean _isRunning = new AtomicBoolean(false);

  public AnalyticServiceServer(final ViewProcessor viewProcessor, final PositionMaster positionMaster, final PortfolioMaster portfolioMaster,
      final ConfigSource configSource) {
    ArgumentChecker.notNull(viewProcessor, "view processor");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(configSource, "configSource");

    _viewProcessor = viewProcessor;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configSource = configSource;
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  public UserPrincipal getUser() {
    return _user;
  }

  /**
   * Sets the user.
   *
   * @param user
   *          the user
   */
  public void setUser(final UserPrincipal user) {
    _user = user;
  }

  /**
   * Gets the viewProcessor.
   *
   * @return the viewProcessor
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Gets the positionMaster.
   *
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Gets the portfolioMaster.
   *
   * @return the portfolioMaster
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Gets the configSource.
   *
   * @return the configSource
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public String getProviderIdName() {
    return _providerIdName;
  }

  public void setProviderIdName(final String providerIdName) {
    this._providerIdName = providerIdName;
  }

  @Override
  public void tradeReceived(final Trade trade) {
    LOGGER.debug("Trade {} received", trade);
    if (trade != null) {
      _tradeUpdaterExecutor.submit(new TradeUpdaterTask(trade));
    }
  }

  /**
   * Gets the tradeProducer.
   *
   * @return the tradeProducer
   */
  public TradeProducer getTradeProducer() {
    return _tradeProducer;
  }

  /**
   * Sets the tradeProducer.
   *
   * @param tradeProducer
   *          the tradeProducer
   */
  public void setTradeProducer(final TradeProducer tradeProducer) {
    _tradeProducer = tradeProducer;
  }

  @Override
  public synchronized void start() {
    if (getViewName() == null) {
      throw new OpenGammaRuntimeException("Can not start analytic server because ViewName is missing");
    }
    if (getTradeProducer() == null) {
      throw new OpenGammaRuntimeException("Can not start analytic server because TradeProducer is missing");
    }
    final ViewDefinition viewDefinition = getConfigSource().getLatestByName(ViewDefinition.class, getViewName());
    if (viewDefinition == null) {
      throw new OpenGammaRuntimeException("Can not start analytic server because view [" + getViewName() + "] can not be loaded");
    }
    _portfolioId = viewDefinition.getPortfolioId().getObjectId();
    _viewId = viewDefinition.getUniqueId();
    setUpViewClient();
    _tradeProducer.addTradeListener(this);
    _isRunning.set(true);
  }

  private void setUpViewClient() {
    _updateLock.lock();
    try {
      LOGGER.debug("creating a view client to obtain view result");
      _viewClient = _viewProcessor.createViewClient(getUser());
      _viewClient.setResultMode(ViewResultMode.FULL_ONLY);
      _viewClient.setResultListener(new AbstractViewResultListener() {

        @Override
        public UserPrincipal getUser() {
          return getUser();
        }

        @Override
        public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
          if (_analyticResultReceiver != null) {
            _analyticResultReceiver.analyticReceived(fullResult.getAllResults());
          }
        }

      });
      _viewClient.attachToViewProcess(_viewId, _viewExecutionOptions, _usePrivateProcess);
      LOGGER.debug("view client attached ready to serve results");
    } finally {
      _updateLock.unlock();
    }
  }

  @Override
  public synchronized void stop() {
    _tradeUpdaterExecutor.shutdownNow();

    if (_tradeProducer != null) {
      _tradeProducer.removeTradeListener(this);
      _tradeProducer = null;
    }
    if (_viewClient != null) {
      _viewClient.pause();
      _viewClient.shutdown();
      _viewClient = null;
    }

    _isRunning.set(false);

  }

  @Override
  public synchronized boolean isRunning() {
    return _isRunning.get();
  }

  /**
   * Gets the analyticResultReceiver.
   *
   * @return the analyticResultReceiver
   */
  public AnalyticResultReceiver getAnalyticResultReceiver() {
    return _analyticResultReceiver;
  }

  /**
   * Sets the analyticResultReceiver.
   *
   * @param analyticResultReceiver
   *          the analyticResultReceiver
   */
  public void setAnalyticResultReceiver(final AnalyticResultReceiver analyticResultReceiver) {
    _analyticResultReceiver = analyticResultReceiver;
  }

  /**
   * Gets the viewName.
   *
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * Sets the viewName.
   *
   * @param viewName
   *          the viewName
   */
  public void setViewName(final String viewName) {
    _viewName = viewName;
  }

  private class TradeUpdaterTask implements Runnable {

    private final Trade _trade;

    TradeUpdaterTask(final Trade trade) {
      _trade = trade;
    }

    @Override
    public void run() {

      final PortfolioDocument portfolioDocument = getPortfolioMaster().get(_portfolioId, VersionCorrection.LATEST);
      LOGGER.debug("Updating portfolio {} with {}", portfolioDocument.getUniqueId(), _trade);
      final ManageablePortfolio portfolio = portfolioDocument.getPortfolio();
      final ManageablePortfolioNode root = portfolio.getRootNode();

      final ManageablePosition position = new ManageablePosition();
      position.getSecurityLink().setExternalId(_trade.getSecurityLink().getExternalId());
      position.setQuantity(_trade.getQuantity());
      final String providerIdStr = _trade.getAttributes().get(getProviderIdName());
      final ManageableTrade manageableTrade = new ManageableTrade(_trade);
      if (providerIdStr != null) {
        final ExternalId providerId = ExternalId.parse(providerIdStr);
        position.setProviderId(providerId);
        manageableTrade.setProviderId(providerId);
      }
      position.addTrade(manageableTrade);
      final PositionDocument addedPosition = getPositionMaster().add(new PositionDocument(position));
      root.addPosition(addedPosition.getUniqueId());

      final PortfolioDocument currentPortfolio = getPortfolioMaster().update(new PortfolioDocument(portfolio));
      LOGGER.info("Portfolio ID {} updated", currentPortfolio.getUniqueId());

      restartViewCalculation();
    }
  }

  private void restartViewCalculation() {
    _updateLock.lock();
    try {
      LOGGER.debug("kick starting computation");
      if (_viewClient != null) {
        _viewClient.detachFromViewProcess();
        _viewClient.attachToViewProcess(_viewId, _viewExecutionOptions, _usePrivateProcess);
        LOGGER.debug("view client shut down");
      }
    } finally {
      _updateLock.unlock();
    }
  }

}
