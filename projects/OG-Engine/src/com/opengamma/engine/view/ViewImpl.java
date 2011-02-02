/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.ViewRecalculationJob;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientImpl;
import com.opengamma.engine.view.client.ViewDeltaResultCalculator;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.ViewPermission;
import com.opengamma.engine.view.permission.ViewPermissionException;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

// REVIEW jonathan 2010-09-13 -- rather than throwing an exception if the view hasn't been initialised when attempting
// an operation that needs it to be, should we just initialise it / tear it down automatically based on clients being
// connected? This is much like kicking off live data processing automatically. One of the reasons for not initialising
// all views immediately is the potential high memory requirement of an entire view processor's collection of
// initialised views. Similarly, we should also tear down views when they are no longer used to avoid the same problem
// occurring over time; this should be done automatically based on some heuristic (0 clients and possibly a timeout).

/**
 * A view represents a {@link ViewDefinition} in the context of a {@link ViewProcessor}; this is everything required
 * to perform computations and listen to the output.
 */
public class ViewImpl implements ViewInternal, Lifecycle, LiveDataSnapshotListener {

  private static final Logger s_logger = LoggerFactory.getLogger(View.class);

  private static final String CLIENT_UID_PREFIX = "Client";

  private final ViewDefinition _definition;
  private final Timer _clientResultTimer;
  private final ViewProcessingContext _processingContext;

  private final String _uidScheme;
  private final AtomicLong _uidCount = new AtomicLong();

  private final Semaphore _viewLock = new Semaphore(1);

  // REVIEW jonathan 2010-09-11 -- using ConcurrentHashMap so that getClient doesn't need to acquire the lock. This
  // will be a frequent call for remote processes where the only reference to the client is its ID. No harm in
  // providing access to a client that's in the middle of shutting down or something; a reference could just as easily
  // have been stored elsewhere for arbitrary access.
  private final Map<UniqueIdentifier, ViewClient> _clientMap = new ConcurrentHashMap<UniqueIdentifier, ViewClient>();

  private final Set<ViewClient> _liveComputationClients = new HashSet<ViewClient>();

  private volatile ViewCalculationState _calculationState = ViewCalculationState.NOT_INITIALIZED;
  
  /**
   * Latest live mode dependency graphs.
   * It's basically an optimization so the dep graphs
   * don't need to be rebuilt every cycle. 
   * Batch run dep graphs are not saved in this variable.  
   */
  private volatile ViewEvaluationModel _viewEvaluationModel;
  
  private volatile ViewRecalculationJob _recalcJob;
  private volatile Thread _recalcThread;

  // REVIEW jonathan 2010-09-11 -- Use an AtomicReference to prevent the need to acquire the lock just to query the
  // latest result.
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();
  private final Set<ComputationResultListener> _resultListeners = new CopyOnWriteArraySet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new CopyOnWriteArraySet<DeltaComputationResultListener>();

  /**
   * Constructs an instance.
   * 
   * @param definition  the view definition, not null
   * @param viewProcessingContext  the processing context, a wrapper around the data structures required by the view
   *                               which allows a view to exist without a view processor
   * @param clientResultTimer  a timer for use when rate-limiting is being applied to client results
   */
  public ViewImpl(ViewDefinition definition, ViewProcessingContext viewProcessingContext, Timer clientResultTimer) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(viewProcessingContext, "viewProcessingContext");
    ArgumentChecker.notNull(clientResultTimer, "clientResultTimer");

    _definition = definition;
    _processingContext = viewProcessingContext;
    _clientResultTimer = clientResultTimer;
    _uidScheme = "View-" + definition.getName();
  }

  private void lock() {
    try {
      _viewLock.acquire();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
  }

  private void unlock() {
    _viewLock.release();
  }

  // -------------------------------------------------------------------------
  @Override
  public void init() {
    // Caller MUST NOT hold the semaphore
    init(Instant.now());
  }

  @Override
  public void init(final InstantProvider initializationInstant) {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      if (getCalculationState() != ViewCalculationState.NOT_INITIALIZED) {
        // Ignore the repeated call because users of the view might attempt initialization first, just in case it
        // it hasn't already been initialized.
        return;
      }

      // Set to the initialized state because we're holding the lock, so this change is not visible externally, and
      // some steps below call methods which do a general initialization check. We carefully control the order here to
      // ensure that such calls are valid.
      setCalculationState(ViewCalculationState.STOPPED);

      OperationTimer timer = new OperationTimer(s_logger, "Initializing view {}", getDefinition().getName());

      final ViewCompilationServices viewCompilation = getProcessingContext().asCompilationServices();
      setViewEvaluationModel(ViewDefinitionCompiler.compile(getDefinition(), viewCompilation, initializationInstant));
      addLiveDataSubscriptions();

      timer.finished();
    } catch (Throwable t) {
      // Reset the state
      setCalculationState(ViewCalculationState.NOT_INITIALIZED);
      setViewEvaluationModel(null);
      s_logger.warn("The view failed to initialise", t);
      throw new OpenGammaRuntimeException("The view failed to initialize", t);
    } finally {
      unlock();
    }
  }

  @Override
  public void reinit() {
    // Caller MUST NOT hold the semaphore
    reinit(Instant.now());
  }

  @Override
  public void reinit(final InstantProvider initializationInstant) {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      ViewCalculationState calculationState = getCalculationState();
      if (calculationState == ViewCalculationState.NOT_INITIALIZED) {
        s_logger.debug("Skipping reinitialization of view '{}' since it has not been initialized", getName());
        return;
      }

      OperationTimer timer = new OperationTimer(s_logger, "Reinitializing view {}", getDefinition().getName());

      if (calculationState == ViewCalculationState.RUNNING) {
        terminateCalculationJob();
        // Connected clients (and they do exist because we're running) will not receive any further results until a new
        // calculation job is started. This may cause a delay, but the clients will remain connected.
      }

      // Recompile the view definition, replacing old live data subscriptions with new ones (perhaps the functions are
      // now configured different so that different live data is required).
      removeLiveDataSubscriptions();
      final ViewCompilationServices viewCompilation = getProcessingContext().asCompilationServices();
      setViewEvaluationModel(ViewDefinitionCompiler.compile(getDefinition(), viewCompilation, initializationInstant));
      addLiveDataSubscriptions();

      if (calculationState == ViewCalculationState.RUNNING) {
        // Start the calcluation job again so that results continue without interruption.
        startCalculationJob();
      }

      timer.finished();

    } finally {
      unlock();
    }
  }

  // Lifecycle
  // ------------------------------------------------------------------------
  @Override
  public void start() {
    // Lifecycle method - nothing to start
  }

  /**
   * Terminates the view, which shuts down any existing clients and stops new clients from being created, thus
   * preventing live computations from ever restarting. This should be used only when the view has reached the end of
   * its life, for example because the view processor is shutting down.
   */
  @Override
  public void stop() {
    // Caller MUST NOT hold the semaphore
    final Set<ViewClient> currentClients;
    lock();
    try {
      currentClients = new HashSet<ViewClient>(_clientMap.values());
      _clientMap.clear();
    } finally {
      unlock();
    }
    // Don't hold semaphore while calling out
    for (ViewClient client : currentClients) {
      client.shutdown();
    }
    lock();
    try {
      // Shutting down every client should have removed all live computation clients and stopped live computation
      setCalculationState(ViewCalculationState.TERMINATED);
      if (getViewEvaluationModel() != null) {
        removeLiveDataSubscriptions();
        setViewEvaluationModel(null);
      }
    } finally {
      unlock();
    }
  }

  @Override
  public boolean isRunning() {
    return getCalculationState() != ViewCalculationState.TERMINATED;
  }

  @Override
  public void suspend() {
    s_logger.info("Suspending view {}", getName());
    lock();
    if (getRecalcJob() != null) {
      s_logger.debug("Suspending calculation job");
      getRecalcJob().terminate();
      if (getRecalcThread().getState() == Thread.State.TIMED_WAITING) {
        // In this case it might be waiting on a recalculation pass. Interrupt it.
        s_logger.debug("Interrupting calculation job thread");
        getRecalcThread().interrupt();
      }
      setRecalcJob(null);
      try {
        s_logger.debug("Waiting for calculation thread to finish");
        getRecalcThread().wait();
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted waiting for calculation thread");
        throw new OpenGammaRuntimeException("Couldn't suspend view", e);
      } finally {
        setRecalcThread(null);
      }
    }
    s_logger.info("View {} suspended", getName());
  }

  @Override
  public void resume() {
    s_logger.info("Resuming view {}", getName());
    unlock();
  }

  // View
  // -------------------------------------------------------------------------
  @Override
  public String getName() {
    return getDefinition().getName();
  }

  @Override
  public ViewDefinition getDefinition() {
    // Injected - can access regardless of calculation state
    return _definition;
  }

  @Override
  public Portfolio getPortfolio() {
    assertInitialized();
    return getViewEvaluationModel().getPortfolio();
  }

  @Override
  public ViewClient createClient(UserPrincipal credentials) {
    ArgumentChecker.notNull(credentials, "credentials");
    assertInitialized();
    getProcessingContext().getPermissionProvider().assertPermission(ViewPermission.ACCESS, credentials, this);
    UniqueIdentifier id = generateClientIdentifier();
    ViewClient client = new ViewClientImpl(id, this, credentials, _clientResultTimer);
    _clientMap.put(id, client);
    return client;
  }

  @Override
  public ViewClient getClient(UniqueIdentifier id) {
    return _clientMap.get(id);
  }

  /**
   * Checks that the given user has access to every market data line required to compute the results of the view, and
   * throws an exception if this is not the case.
   * 
   * @param user  the user
   * @throws ViewPermissionException  if any entitlement problems are found
   */
  @Override
  public void assertAccessToLiveDataRequirements(UserPrincipal user) {
    s_logger.info("Checking that {} is entitled to the results of {}", user, this);
    assertInitialized();
    Collection<LiveDataSpecification> requiredLiveData = getRequiredLiveDataSpecifications();
    Map<LiveDataSpecification, Boolean> entitlements = getProcessingContext().getLiveDataEntitlementChecker().isEntitled(user, requiredLiveData);
    ArrayList<LiveDataSpecification> failures = new ArrayList<LiveDataSpecification>();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (entry.getValue().booleanValue() == false) {
        failures.add(entry.getKey());
      }
    }

    if (!failures.isEmpty()) {
      s_logger.warn("User {} is not entitled to the output of {} because they do not have permission to {}", new Object[] {user, this, failures.get(0)});
      throw new ViewPermissionException(user + " is not entitled to the output of " + this + " because they do not have permissions to " + failures.get(0));
    }
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    return _latestResult.get();
  }

  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    assertInitialized();
    ViewEvaluationModel viewEvaluationModel = getViewEvaluationModel();
    Map<ValueRequirement, ValueSpecification> requiredSpecs = viewEvaluationModel.getAllLiveDataRequirements();
    return requiredSpecs.keySet();
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    assertInitialized();
    return getViewEvaluationModel().getAllSecurityTypes();
  }

  @Override
  public void runOneCycle() {
    SingleComputationCycle cycle;
    lock();
    try {
      assertInitialized();
      long valuationTime = System.currentTimeMillis();
      cycle = createCycleImpl(valuationTime, getLiveDataSnapshotProvider(), true);
    } finally {
      unlock();
    }

    runOneCycleImpl(cycle, _resultListeners);
  }
  
  /**
   * This method is useful in tests
   *  
   * @param valuationTime valuation time
   */
  public void runOneCycle(long valuationTime) {
    SingleComputationCycle cycle;
    lock();
    try {
      assertInitialized();
      cycle = createCycleImpl(valuationTime, getLiveDataSnapshotProvider(), true);
    } finally {
      unlock();
    }
    
    runOneCycleImpl(cycle, _resultListeners);
  }

  @Override
  public void runOneCycle(long valuationTime, LiveDataSnapshotProvider snapshotProvider, ComputationResultListener listener) {
    // Caller MUST NOT hold the semaphore
    SingleComputationCycle cycle;
    lock();
    try {
      assertInitialized();
      cycle = createCycleImpl(valuationTime, snapshotProvider, false);
    } finally {
      unlock();
    }
    
    Set<ComputationResultListener> resultListeners = 
      listener == null 
        ? Collections.<ComputationResultListener>emptySet() 
        : Collections.singleton(listener);
    
    runOneCycleImpl(cycle, resultListeners);
  }
  
  private void runOneCycleImpl(SingleComputationCycle cycle, Set<ComputationResultListener> resultListeners) {
    cycle.prepareInputs();
    try {
      cycle.executePlans();
    } catch (InterruptedException e) {
      s_logger.warn("Interrupted while attempting to run a single computation cycle. No results will be output.");
      cycle.releaseResources();
      return;
    }

    if (!resultListeners.isEmpty()) {
      cycle.populateResultModel();
      for (ComputationResultListener resultListener : resultListeners) {
        resultListener.computationResultAvailable(cycle.getResultModel());
      }
    }
    
    cycle.releaseResources();
  }

  @Override
  public LiveDataInjector getLiveDataOverrideInjector() {
    return getProcessingContext().getLiveDataOverrideInjector();
  }

  @Override
  public boolean isLiveComputationRunning() {
    return getCalculationState() == ViewCalculationState.RUNNING;
  }

  // ViewInternal
  // -------------------------------------------------------------------------
  @Override
  public ViewProcessingContext getProcessingContext() {
    // Injected - can access at any time
    return _processingContext;
  }

  @Override
  public ViewEvaluationModel getViewEvaluationModel() {
    assertInitialized();
    return _viewEvaluationModel;
  }

  protected void setViewEvaluationModel(final ViewEvaluationModel viewEvaluationModel) {
    _viewEvaluationModel = viewEvaluationModel;
  }

  private void recalculationPerformedImpl(ViewComputationResultModel result) {
    // Caller MUST hold the semaphore

    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It holds the lock and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden. Have to factor in some type of win there.

    // We swap these first so that in the callback the view is consistent.
    ViewResultModel previousResult = _latestResult.get();
    _latestResult.set(result);
    for (ComputationResultListener resultListener : _resultListeners) {
      resultListener.computationResultAvailable(result);
    }
    if (!_deltaListeners.isEmpty()) {
      ViewDeltaResultModel deltaModel = ViewDeltaResultCalculator.computeDeltaModel(getDefinition(), previousResult, result);
      for (DeltaComputationResultListener deltaListener : _deltaListeners) {
        deltaListener.deltaResultAvailable(deltaModel);
      }
    }
  }

  @Override
  public void recalculationPerformed(ViewComputationResultModel result) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Recalculation Performed called.");
    lock();
    try {
      recalculationPerformedImpl(result);
    } finally {
      unlock();
    }
  }

  private SingleComputationCycle createCycleImpl(final long valuationTime, LiveDataSnapshotProvider snapshotProvider, boolean liveCycle) {
    // Caller MUST hold the semaphore
    boolean recompile = false;
    long functionInitId = getProcessingContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    if (functionInitId == getViewEvaluationModel().getFunctionInitId()) {
      if (getViewEvaluationModel().isValidFor(valuationTime)) {
        s_logger.debug("View {} still valid at {}", getDefinition().getName(), Instant.ofEpochMillis(valuationTime));
        recompile = false;
      } else {
        recompile = true;
      }
    } else {
      recompile = true;
    }
    
    ViewEvaluationModel viewEvaluationModel = getViewEvaluationModel();
    if (recompile) {
      final OperationTimer timer = new OperationTimer(s_logger, "Re-compiling view {} for {}", getDefinition().getName(), Instant.ofEpochMillis(valuationTime));
      
      // TODO for "expired" also read "re-initialized"
      // [ENG-253] Incremental compilation - could remove nodes from the dep graph that require "expired" functions and then rebuild to fill in the gaps
      // [ENG-253] Incremental compilation - could at least only rebuild the dep graphs that have "expired" and reuse the others
      final Set<ValueRequirement> previousRequirement = getRequiredLiveData();
      viewEvaluationModel = ViewDefinitionCompiler.compile(getDefinition(), getProcessingContext().asCompilationServices(), Instant.ofEpochMillis(valuationTime));
      
      // if this is a BATCH (historical, one-off) cycle, we do not want to update the 
      // live data subscriptions or the evaluation model. If we updated them, they would need
      // to be reset again on the next live cycle
      if (liveCycle) {
        setViewEvaluationModel(viewEvaluationModel);
        updateLiveDataSubscriptions(previousRequirement);
      }
      
      timer.finished();
    }
    SingleComputationCycle cycle = new SingleComputationCycle(this, snapshotProvider, valuationTime);
    return cycle;
  }

  @Override
  public SingleComputationCycle createCycle() {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      long valuationTime = System.currentTimeMillis();
      SingleComputationCycle cycle = createCycleImpl(valuationTime, getLiveDataSnapshotProvider(), true);
      return cycle;
    } finally {
      unlock();
    }
  }

  @Override
  public ViewPermissionProvider getPermissionProvider() {
    assertInitialized();
    return getProcessingContext().getPermissionProvider();
  }

  // -------------------------------------------------------------------------
  @Override
  public String toString() {
    return "View[" + getName() + "]";
  }

  // -------------------------------------------------------------------------
  /**
   * Adds a listener to updates of the full set of computation results.
   * 
   * @param resultListener  the listener to add
   * @return  <code>true</code> if the listener was newly added, or <code>false</code> if the listener was already
   *          present. 
   */
  public boolean addResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    if (_resultListeners.contains(resultListener)) {
      // Avoid the permission check
      return false;
    }
    assertInitialized();
    getProcessingContext().getPermissionProvider().assertPermission(ViewPermission.READ_RESULTS, resultListener.getUser(), this);
    return _resultListeners.add(resultListener);
  }

  /**
   * Removes a listener from updates of the full set of computation results.
   * 
   * @param resultListener  the listener to remove
   * @return  <code>true</code> if the listener was removed, or <code>false</code> if the listener was not known.
   */
  public boolean removeResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    return _resultListeners.remove(resultListener);
  }

  /**
   * Adds a listener to delta updates of the computation results.
   * 
   * @param deltaListener  the listener to add
   * @return  <code>true</code> if the listener was newly added, or <code>false</code> if the listener was already
   *          present. 
   */
  public boolean addDeltaResultListener(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    if (_deltaListeners.contains(deltaListener)) {
      // Avoid the permission check
      return false;
    }
    assertInitialized();
    getProcessingContext().getPermissionProvider().assertPermission(ViewPermission.READ_RESULTS, deltaListener.getUser(), this);
    return _deltaListeners.add(deltaListener);
  }

  /**
   * Removes a listener from delta updates of the computation results.
   * 
   * @param deltaListener  the listener to remove
   * @return  <code>true</code> if the listener was removed, or <code>false</code> if the listener was not known.
   */
  public boolean removeDeltaResultListener(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    return _deltaListeners.remove(deltaListener);
  }

  // -------------------------------------------------------------------------
  /**
   * Part of initialization. Adds live data subscriptions to the view.
   */
  private void addLiveDataSubscriptions() {
    final LiveDataSnapshotProvider snapshotProvider = getLiveDataSnapshotProvider();
    snapshotProvider.addListener(this);
    addLiveDataSubscriptions(getRequiredLiveData());
  }

  /**
   * Part of shutdown. Removes live data subscriptions for the view.
   */
  // final LiveDataSnapshotProvider snapshotProvider = getLiveDataSnapshotProvider();
  private void removeLiveDataSubscriptions() {
    // [ENG-251] TODO snapshotProvider.removeListener(this);
    removeLiveDataSubscriptions(getRequiredLiveData());
  }

  /**
   * Part of recompilation of functions. Changes live data subscriptions for the view.
   */
  private void updateLiveDataSubscriptions(final Set<ValueRequirement> previousLiveData) {
    final Set<ValueRequirement> newLiveDataRequirements = getRequiredLiveData();
    final Set<ValueRequirement> unusedLiveData = Sets.difference(previousLiveData, newLiveDataRequirements);
    if (!unusedLiveData.isEmpty()) {
      s_logger.debug("{} unused data requirements: {}", unusedLiveData.size(), unusedLiveData);
      removeLiveDataSubscriptions(unusedLiveData);
    }
    final Set<ValueRequirement> newLiveData = Sets.difference(newLiveDataRequirements, previousLiveData);
    if (!newLiveData.isEmpty()) {
      s_logger.debug("{} new live data requirements: {}", newLiveData.size(), newLiveData);
      // [ENG-250] TODO asserting permissions on live subscribers or force them to pause
      addLiveDataSubscriptions(newLiveData);
    }
  }

  private void addLiveDataSubscriptions(final Set<ValueRequirement> requiredLiveData) {
    // Caller MAY hold the semaphore
    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} live data subscriptions for portfolio {}", requiredLiveData.size(), getDefinition().getPortfolioId());
    getLiveDataSnapshotProvider().addSubscription(getDefinition().getLiveDataUser(), requiredLiveData);
    timer.finished();
  }

  private void removeLiveDataSubscriptions(final Set<ValueRequirement> requiredLiveData) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} live data subscriptions for portfolio {}", requiredLiveData.size(), getDefinition().getPortfolioId());
    // [ENG-251] TODO getLiveDataSnapshotProvider().removeSubscription(getDefinition().getLiveDataUser(), requiredLiveData);
    timer.finished();
  }

  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {
  }

  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
  }

  @Override
  public void valueChanged(ValueRequirement value) {
    ValueSpecification valueSpecification = new ValueSpecification(value, LiveDataSourcingFunction.UNIQUE_ID);
    Map<ValueRequirement, ValueSpecification> liveDataRequirements = getViewEvaluationModel().getAllLiveDataRequirements();
    ViewRecalculationJob recalcJob = getRecalcJob();
    if (recalcJob != null && liveDataRequirements.containsKey(valueSpecification)) {
      recalcJob.liveDataChanged();
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the current calculation state.
   * 
   * @return the current calculation state
   */
  private ViewCalculationState getCalculationState() {
    return _calculationState;
  }

  /**
   * Sets the current calculation state.
   * 
   * @param calculationState  the new calculation state
   */
  private void setCalculationState(ViewCalculationState calculationState) {
    _calculationState = calculationState;
  }

  /**
   * Sets the current recalculation job.
   * 
   * @return  the current recalculation job
   */
  protected ViewRecalculationJob getRecalcJob() {
    return _recalcJob;
  }

  /**
   * Sets the current recalculation job
   * 
   * @param recalcJob  the current recalculation job
   */
  private void setRecalcJob(ViewRecalculationJob recalcJob) {
    _recalcJob = recalcJob;
  }

  /**
   * Gets the current recalculation thread
   * 
   * @return  the current recalculation thread
   */
  protected Thread getRecalcThread() {
    return _recalcThread;
  }

  /**
   * Sets the current recalculation thread
   * 
   * @param recalcThread  the current recalculation thread
   */
  private void setRecalcThread(Thread recalcThread) {
    _recalcThread = recalcThread;
  }
  
  /**
   * Gets the live data snapshot provider used for all live mode calculations.
   * 
   * @return the live data snapshot provider, not null
   */
  @Override
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return getProcessingContext().getLiveDataSnapshotProvider();
  }

  // -------------------------------------------------------------------------
  /**
   * Notifies the view that a client expects to receive live computation results. This method ensures that live
   * processing has been started.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same client have no effect.
   * 
   * @param client  the client expecting to receive live computation results
   */
  public void addLiveComputationClient(ViewClient client) {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      assertInitialized();
      if (_liveComputationClients.size() == 0) {
        startLiveComputation();
      }
      _liveComputationClients.add(client);
    } finally {
      unlock();
    }
  }

  /**
   * Notifies the view that a client no longer expects to receive live computation results. Removal of the last such
   * client will cause live processing to stop.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same client have no effect.
   * 
   * @param client  the client no longer expecting live computation results 
   */
  public void removeLiveComputationClient(ViewClient client) {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      assertInitialized();
      if (_liveComputationClients.remove(client) && _liveComputationClients.size() == 0) {
        stopLiveComputation();
      }
    } finally {
      unlock();
    }
  }

  /**
   * Starts a background job which runs a computation cycle, waits for changes to the dependency graph's inputs, and
   * repeats this task until live computation is stopped. With continuously-changing inputs, computation cycles will
   * run continuously.
   */
  private void startLiveComputation() {
    // Caller MUST hold the semaphore
    s_logger.info("Starting live computation on view {}...", this);
    switch (getCalculationState()) {
      case STOPPED:
        // Normal state of play. Continue as normal.
        break;
      case NOT_INITIALIZED:
        throw new IllegalStateException("Must call init() before starting.");
      case RUNNING:
        throw new IllegalStateException("Already running.");
      case TERMINATED:
        throw new IllegalStateException("A terminated view cannot be used.");
    }
    startCalculationJob();
    s_logger.info("Started.");
  }

  /**
   * Instructs the background computation job to finish. The background job might actually terminate asynchronously,
   * but any outstanding result will be discarded. Live computation may be started again immediately.
   */
  private void stopLiveComputation() {
    // Caller MUST hold the semaphore
    s_logger.info("Stopping live computation on view {}...", this);
    if (getCalculationState() != ViewCalculationState.RUNNING) {
      throw new IllegalStateException("Cannot stop a view that is not running. Currently in state: " + getCalculationState());
    }
    terminateCalculationJob();
    setCalculationState(ViewCalculationState.STOPPED);
    s_logger.info("Stopped.");
  }

  // -------------------------------------------------------------------------
  private Collection<LiveDataSpecification> getRequiredLiveDataSpecifications() {
    Set<LiveDataSpecification> returnValue = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : getRequiredLiveData()) {
      LiveDataSpecification liveDataSpec = requirement.getRequiredLiveData(getProcessingContext().getSecuritySource());
      returnValue.add(liveDataSpec);
    }
    return returnValue;
  }

  private void assertInitialized() {
    ViewCalculationState state = getCalculationState();
    boolean isInitialized = state != ViewCalculationState.NOT_INITIALIZED;
    if (!isInitialized) {
      throw new IllegalStateException("The view has not been initialized");
    }
  }

  private UniqueIdentifier generateClientIdentifier() {
    return UniqueIdentifier.of(_uidScheme, CLIENT_UID_PREFIX + "-" + _uidCount.getAndIncrement());
  }

  private void startCalculationJob() {
    ViewRecalculationJob recalcJob = new ViewRecalculationJob(this);
    Thread recalcThread = new Thread(recalcJob, "Recalc Thread for " + getDefinition().getName());

    setRecalcJob(recalcJob);
    setRecalcThread(recalcThread);
    setCalculationState(ViewCalculationState.RUNNING);
    recalcThread.start();
  }

  private void terminateCalculationJob() {
    if (getRecalcJob() == null) {
      return;
    }

    getRecalcJob().terminate();
    if (getRecalcThread().getState() == Thread.State.TIMED_WAITING) {
      // In this case it might be waiting on a recalculation pass. Interrupt it.
      getRecalcThread().interrupt();
    }

    // Let go of the job/thread and allow it to die on its own. A computation cycle might be taking place on this
    // thread, but it will not update the view with its result because it has been terminated. As far as the view is
    // concerned, live computation has now stopped, and it may be started again immediately in a new thread. There is
    // no need to slow things down by waiting for the thread to die.
    setRecalcJob(null);
    setRecalcThread(null);
  }

}
