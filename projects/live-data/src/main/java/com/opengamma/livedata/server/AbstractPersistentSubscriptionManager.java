/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Stores persistent subscriptions in persistent storage so they're not lost if
 * the server crashes.
 * <p>
 * If you modify the list of persistent subscriptions in persistent storage by
 * editing the persistent storage (DB/file/whatever) using external tools while
 * the server is down, these changes will be reflected on the server the next
 * time it starts.
 * <p>
 * This beans depends-on the Live Data Server, and any Spring configuration must reflect
 * this. See <a href="http://jira.springframework.org/browse/SPR-2325">http://jira.springframework.org/browse/SPR-2325</a>.
 *
 */
public abstract class AbstractPersistentSubscriptionManager implements Lifecycle {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AbstractPersistentSubscriptionManager.class);

  /**
   * Default how often to save the persistent subscriptions to the database, milliseconds.
   */
  public static final long DEFAULT_SAVE_PERIOD = 60000L;

  private final StandardLiveDataServer _server;
  private final Timer _timer;
  private final long _savePeriod;
  private volatile SaveTask _saveTask;

  private Set<PersistentSubscription> _previousSavedState;
  private final Set<PersistentSubscription> _persistentSubscriptions = new HashSet<>();

  public AbstractPersistentSubscriptionManager(final StandardLiveDataServer server) {
    this(server, new Timer("PersistentSubscriptionManager Timer"),
        DEFAULT_SAVE_PERIOD);
  }

  public AbstractPersistentSubscriptionManager(final StandardLiveDataServer server,
      final Timer timer, final long savePeriod) {
    ArgumentChecker.notNull(server, "Live Data Server");
    ArgumentChecker.notNull(timer, "Timer");
    if (savePeriod <= 0) {
      throw new IllegalArgumentException("Please give positive save period");
    }

    _server = server;
    _timer = timer;
    _savePeriod = savePeriod;
  }

  private class SaveTask extends TimerTask {
    @Override
    public void run() {
      try {
        save();
      } catch (final RuntimeException e) {
        LOGGER.error("Saving persistent subscriptions to storage failed", e);
      }
    }
  }


  @Override
  public boolean isRunning() {
    return _saveTask != null;
  }

  @Override
  public void start() {
    refreshAsync(); //PLAT-1632
    //Safe after refresh queued to avoid empty save
    _saveTask = new SaveTask();
    _timer.schedule(_saveTask, _savePeriod, _savePeriod);
  }

  @Override
  public void stop() {
    _saveTask.cancel();
    _saveTask = null;
    waitForIdleTimer();
  }

  private void waitForIdleTimer() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);;
    LOGGER.info("Waiting for timer to be idle");
    try {
      _timer.schedule(new TimerTask() {
        @Override
        public void run() {
          countDownLatch.countDown();
        }
      }, 0);
      countDownLatch.await();
      LOGGER.info("Timer idle");
    } catch (final Exception ex) {
      LOGGER.error("Couldn't waiting for timer to be idle", ex);
    }
  }

  /**
   * This should mean that all the subscriptions become persistent eventually,
   *  and (importantly) none of them expire in the mean time.
   * Because of the implementation of updateServer.
   */
  private synchronized void refreshAsync() {
    refreshState();

    _timer.schedule(new TimerTask() {
      @Override
      public void run() {
        //We release the lock before here, so someone could sneak in and change things
        updateServer(true);
      }
    }, 0);
  }

  public synchronized void refresh() {
    refreshState();

    updateServer(true);
  }

  /**
   * Reads from all sources to our private state
   */
  private synchronized void refreshState() {
    LOGGER.debug("Refreshing persistent subscriptions from storage");

    clear();
    readFromStorage();
    readFromServer();

    LOGGER.info("Refreshed persistent subscriptions from storage. There are currently "
        + _persistentSubscriptions.size() + " persistent subscriptions.");
  }

  /**
   * Creates a persistent subscription on the server for any persistent
   * subscriptions which are not yet there.
   */
  private synchronized void updateServer(final boolean catchExceptions) {
    final Collection<LiveDataSpecification> specs = getSpecs(_persistentSubscriptions);
    final Set<LiveDataSpecification> persistentSubscriptionsToMake = new HashSet<>(specs);

    final OperationTimer operationTimer = new OperationTimer(LOGGER, "Updating server's persistent subscriptions {}", persistentSubscriptionsToMake.size());

    final int partitionSize = 50; //Aim is to make sure we can convert subscriptions quickly enough that nothing expires,
    // and to leave the server responsive, and make retrys not take too long

    final List<List<LiveDataSpecification>> partitions = Lists.partition(Lists.newArrayList(persistentSubscriptionsToMake), partitionSize);
    for (final List<LiveDataSpecification> partition : partitions) {

      final Map<LiveDataSpecification, MarketDataDistributor> marketDataDistributors = _server.getMarketDataDistributors(persistentSubscriptionsToMake);
      for (final Entry<LiveDataSpecification, MarketDataDistributor> distrEntry : marketDataDistributors.entrySet()) {
        if (distrEntry.getValue() != null) {
          //Upgrade or no/op should be fast, lets do it to avoid expiry
          createPersistentSubscription(catchExceptions, distrEntry.getKey());
          persistentSubscriptionsToMake.remove(distrEntry.getKey());
        }
      }


      final SetView<LiveDataSpecification> toMake = Sets.intersection(new HashSet<>(partition), persistentSubscriptionsToMake);
      if (!toMake.isEmpty()) {
        createPersistentSubscription(catchExceptions, toMake); //PLAT-1632
        persistentSubscriptionsToMake.removeAll(toMake);
      }
    }
    operationTimer.finished();
    LOGGER.info("Server updated");
  }

  private void createPersistentSubscription(final boolean catchExceptions, final LiveDataSpecification sub) {
    createPersistentSubscription(catchExceptions, Collections.singleton(sub));
  }

  private void createPersistentSubscription(final boolean catchExceptions, final Set<LiveDataSpecification> specs) {
    if (specs.isEmpty()) {
      return;
    }
    LOGGER.info("Creating {}", specs);
    try {
      final Collection<LiveDataSubscriptionResponse> results = _server.subscribe(specs, true);
      for (final LiveDataSubscriptionResponse liveDataSubscriptionResponse : results) {
        if (liveDataSubscriptionResponse.getSubscriptionResult() != LiveDataSubscriptionResult.SUCCESS) {
          LOGGER.warn("Failed to create persistent subscription {}", liveDataSubscriptionResponse);
        }
      }
    } catch (final RuntimeException e) {
      if (catchExceptions) {
        //This should be rare
        LOGGER.error("Creating a persistent subscription failed for " + specs, e);
        if (specs.size() > 1) {
          //  NOTE: have to retry here since _all_ of the subs will have failed
          for (final LiveDataSpecification spec : specs) {
            createPersistentSubscription(catchExceptions, spec);
          }
        }
      } else {
        throw e;
      }
    }
  }

  private static Collection<LiveDataSpecification> getSpecs(final Set<PersistentSubscription> subs) {
    final Collection<LiveDataSpecification> specs = new ArrayList<>();
    for (final PersistentSubscription sub : subs) {
      specs.add(sub.getFullyQualifiedSpec());
    }
    return specs;
  }

  public synchronized void save() {
    LOGGER.debug("Dumping persistent subscriptions to storage");

    clear();
    readFromServer();

    // Only save if changed
    if (_previousSavedState == null || !_previousSavedState.equals(_persistentSubscriptions)) {

      LOGGER.info("A change to persistent subscriptions detected, saving "
          + _persistentSubscriptions.size() + " subscriptions to storage.");
      saveToStorage(_persistentSubscriptions);
      _previousSavedState = new HashSet<>(_persistentSubscriptions);

    } else {
      LOGGER.debug("No changes to persistent subscriptions detected.");
    }

    LOGGER.debug("Dumped persistent subscriptions to storage");
  }

  public synchronized long getApproximateNumberOfPersistentSubscriptions() {
    return _persistentSubscriptions.size();
  }
  public synchronized Set<String> getPersistentSubscriptions() {
    clear();
    readFromServer();

    final HashSet<String> returnValue = new HashSet<>();
    for (final PersistentSubscription ps : _persistentSubscriptions) {
      returnValue.add(ps.getFullyQualifiedSpec().toString());
    }

    return returnValue;
  }

  public synchronized void addPersistentSubscription(final String securityUniqueId) {
    final LiveDataSpecification spec = getFullyQualifiedLiveDataSpec(securityUniqueId);
    addPersistentSubscription(new PersistentSubscription(spec));
    updateServer(false);
  }

  public synchronized boolean removePersistentSubscription(
      final String securityUniqueId) {
    final Subscription sub = _server.getSubscription(securityUniqueId);
    if (sub == null) {
      return false;
    }

    boolean removed = false;
    for (final MarketDataDistributor distributor : sub.getDistributors()) {
      removed = true;
      distributor.setPersistent(false);
    }

    save();
    return removed;
  }

  public LiveDataSpecification getFullyQualifiedLiveDataSpec(final String securityUniqueId) {
    return _server.getLiveDataSpecification(securityUniqueId);
  }

  private void clear() {
    _persistentSubscriptions.clear();
  }

  protected void addPersistentSubscription(final PersistentSubscription sub) {
    _persistentSubscriptions.add(sub);
  }

  /**
   * Refreshes persistent subscriptions from the latest status on the server.
   */
  private void readFromServer() {
    for (final Subscription sub : _server.getSubscriptions()) {
      for (final MarketDataDistributor distributor : sub.getDistributors()) {
        if (distributor.isPersistent()) {
          final PersistentSubscription ps = new PersistentSubscription(
              distributor.getFullyQualifiedLiveDataSpecification());
          addPersistentSubscription(ps);
        }
      }
    }
  }

  /**
   * Reads entries from persistent storage (DB, flat file, ...) and calls
   * {@link #addPersistentSubscription(PersistentSubscription)} for each one.
   */
  protected abstract void readFromStorage();

  /**
   * Saves entries to persistent storage (DB, flat file, ...)
   *
   * @param newState Entries to be saved
   */
  public abstract void saveToStorage(Set<PersistentSubscription> newState);

}
