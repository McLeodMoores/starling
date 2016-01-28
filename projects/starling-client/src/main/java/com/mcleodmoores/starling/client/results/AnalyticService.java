/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * A simplified interface for accessing OpenGamma analytics. This object is for single calculation use and very lightweight.
 */
public class AnalyticService  {
  /** A position source */
  private final PositionSource _positionSource;
  /** A config source */
  private final ConfigSource _configSource;
  /** A view processor */
  private final ViewProcessor _viewProcessor;

  /**
   * Create an instance of the service.  It is important to note that this object is for single calculation use and very lightweight.
   * @param viewProcessor  the view processor, not null
   * @param positionSource  the position source, not null
   * @param configSource  the config source, not null
   */
  public AnalyticService(final ViewProcessor viewProcessor, final PositionSource positionSource, final ConfigSource configSource) {
    _positionSource = ArgumentChecker.notNull(positionSource, "positionSource");
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
    _viewProcessor = ArgumentChecker.notNull(viewProcessor, "viewProcessor");
  }

  /**
   * Create a job that calls back to a listener provided when start(ResultListener) is called.  The caller is responsible for
   * either explicitly closing the job with close() on completion or running the whole thing inside a try-with-resources block
   * (assuming the listener completes before the block)
   * @param viewKey  the key for the view to calculate, not null
   * @param valuationTime  the valuation time, not null
   * @param snapshotDate  the date from which data is to be retrieved, not null
   * @return an AsynchronousJob, on which the start method should be called with a ResultListener
   */
  public AsynchronousJob createAsynchronousJob(final ViewKey viewKey, final Instant valuationTime, final LocalDate snapshotDate) {
    return new AsynchronousJobImpl(viewKey, valuationTime, snapshotDate);
  }

  /**
   * Create a job that calls back to a listener provided when start(ResultListener) is called.  The caller is responsible for
   * either explicitly closing the job with close() on completion or running the whole thing inside a try-with-resources block
   * (assuming the listener completes before the block).
   * @param viewKey  the key for the view to calculate, not null
   * @param valuationTime  the valuation time, not null
   * @param snapshotDate  the date from which data is to be retrieved, not null
   * @return an SynchronousJob, on which the run method should be called
   */
  public SynchronousJob createSynchronousJob(final ViewKey viewKey, final Instant valuationTime, final LocalDate snapshotDate) {
    return new SynchronousJobImpl(viewKey, valuationTime, snapshotDate);
  }

  /**
   * Class representing a calculation job that is executed asynchronously - i.e. calling the start method will return immediately
   * and call back the supplied ResultListener.
   */
  public final class AsynchronousJobImpl implements AsynchronousJob {
    /** A view client */
    private ViewClient _viewClient;
    /** The view key */
    private final ViewKey _viewKey;
    /** The valuation time */
    private final Instant _valuationTime;
    /** The snapshot date */
    private final LocalDate _snapshotDate;

    /**
     * @param viewKey  the key for the view to calculate, not null
     * @param valuationTime  the valuation time, not null
     * @param snapshotDate  the date from which data is to be retrieved, not null
     */
    /* package */AsynchronousJobImpl(final ViewKey viewKey, final Instant valuationTime, final LocalDate snapshotDate) {
      _viewKey = ArgumentChecker.notNull(viewKey, "viewKey");
      _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
      _snapshotDate = ArgumentChecker.notNull(snapshotDate, "snapshotDate");
    }

    /**
     * Start the job notifying the listener when the result is available.  The caller is responsible for closing the
     * job by calling close() explicitly or using in a try-with-resources block.
     * @param resultListener  the result listener, to be called when the calculation is complete, not null
     */
    @Override
    public void start(final ResultListener resultListener) {
      if (_viewClient != null && _viewClient.isAttached()) {
        // if the job is being reused, we need to clean up the previous cycle.
        _viewClient.shutdown();
      }
      _viewClient = _viewProcessor.createViewClient(UserPrincipal.getLocalUser());
      _viewClient.setResultMode(ViewResultMode.FULL_ONLY);
      _viewClient.setResultListener(new AnalyticServiceResultListener(_viewClient, resultListener, this));
      final List<MarketDataSpecification> marketDataSpecificationList = Collections.<MarketDataSpecification>singletonList(
          new FixedHistoricalMarketDataSpecification(HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, _snapshotDate));
      final UniqueId viewDefId = ensureConfig(_viewKey);
      final ExecutionFlags flags = ExecutionFlags.none().awaitMarketData();
      _viewClient.attachToViewProcess(viewDefId, ExecutionOptions.singleCycle(_valuationTime, marketDataSpecificationList, flags.get()));
    }

    /**
     * Shut down the service, releasing any underlying resources.
     * @throws Exception if there is a problem during shut down
     */
    @Override
    public void close() throws Exception {
      if (_viewClient != null) {
        _viewClient.shutdown();
      }
    }
  }

  /**
   * Class representing a calculation job that is executed synchronously - i.e. you can call the run method and once the result
   * is available it will return the result.
   */
  private final class SynchronousJobImpl implements SynchronousJob {
    /** The underlying asynchronous job */
    private final AsynchronousJob _asyncJob;

    /**
     * Start a calculation, note that this method can only be called once.
     * @param viewKey  the key for the view to calculate, not null
     * @param valuationTime  the valuation time, not null
     * @param snapshotDate  the date from which data is to be retrieved, not null
     */
    /* package */SynchronousJobImpl(final ViewKey viewKey, final Instant valuationTime, final LocalDate snapshotDate) {
      _asyncJob = new AsynchronousJobImpl(viewKey, valuationTime, snapshotDate);
    }

    /**
     * Run the job synchronously and return the ResultModel.  The caller must close the job afterwards to release any resources.
     * @return the result model
     */
    @Override
    public ResultModel run() {
      final SynchronousQueue<ResultModel> resultQueue = new SynchronousQueue<>();
      _asyncJob.start(new ViewResultListener(resultQueue));
      try {
        return resultQueue.take();
      } catch (final InterruptedException ie) {
        throw new OpenGammaRuntimeException("Computation interrupted", ie);
      }
    }

    @Override
    public void close() throws Exception {
      _asyncJob.close();
    }

    /**
     * A view result listener based on a synchronous queue.
     */
    private class ViewResultListener implements ResultListener {
      /** The result queue */
      private final SynchronousQueue<ResultModel> _resultQueue;

      /**
       * @param resultQueue  the result queue
       */
      /* package */ViewResultListener(final SynchronousQueue<ResultModel> resultQueue) {
        _resultQueue = resultQueue;
      }

      @Override
      public void calculationComplete(final ResultModel resultModel, final AsynchronousJob asynchronousJob) {
        try {
          _resultQueue.put(resultModel);
        } catch (final Exception e) {
          throw new OpenGammaRuntimeException("ViewResultListener exception", e);
        }
      }
    }
  }

  /**
   * Result listener to convert from ViewComputationResultModel to the new ResultModel object.
   */
  private class AnalyticServiceResultListener extends AbstractViewResultListener {
    /** The view client */
    private final ViewClient _viewClient;
    /** The result listener */
    private final ResultListener _resultListener;
    /** The job */
    private final AsynchronousJob _job;

    /**
     * @param viewClient  the view client
     * @param resultListener  the result listener
     * @param job  the job
     */
    public AnalyticServiceResultListener(final ViewClient viewClient, final ResultListener resultListener, final AsynchronousJob job) {
      _viewClient = viewClient;
      _resultListener = resultListener;
      _job = job;
    }
    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getLocalUser();
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      final ViewDefinition viewDefinition = _viewClient.getLatestViewDefinition();
      final ResultModel resultModel = new ResultModelImpl(fullResult, viewDefinition, _positionSource);
      _resultListener.calculationComplete(resultModel, _job);
    }
  }

  /**
   * Checks that the view definition is present in the config source.
   * @param viewKey  the view key
   * @return  the unique id of the view if available
   */
  /* package */UniqueId ensureConfig(final ViewKey viewKey) {
    ViewDefinition viewDef;
    try {
    if (viewKey.hasUniqueId()) {
      viewDef = _configSource.getConfig(ViewDefinition.class, viewKey.getUniqueId());
    } else {
      viewDef = _configSource.getSingle(ViewDefinition.class, viewKey.getName(), VersionCorrection.LATEST);
    }
    if (viewDef == null) {
      throw new OpenGammaRuntimeException("Could not find view " + viewKey);
    }
    } catch (final ClassCastException e) {
      throw e;
    }
    return viewDef.getUniqueId();
  }
}
