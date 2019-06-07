/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.ResolvedValueCallback.ResolvedValueCallbackChain;
import com.opengamma.engine.value.ValueRequirement;

/* package */class AggregateResolvedValueProducer extends AbstractResolvedValueProducer implements ResolvedValueCallbackChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateResolvedValueProducer.class);

  private int _pendingTasks = 1;
  private boolean _wantResult = true;
  private final List<ResolutionPump> _pumps = new ArrayList<>();

  AggregateResolvedValueProducer(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  /**
   * Returns the number of pending tasks. The caller must hold the monitor.
   *
   * @return the number of pending tasks
   */
  protected int getPendingTasks() {
    return _pendingTasks;
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    LOGGER.debug("Failed on {} for {}", value, this);
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      if (_pendingTasks == Integer.MIN_VALUE) {
        // We were discarded after we requested the callback
        LOGGER.debug("Failed resolution after discard of {}", this);
        return;
      }
      assert _pendingTasks > 0;
      if (--_pendingTasks == 0) {
        if (_wantResult) {
          LOGGER.debug("Pumping underlying after last input failed for {}", this);
          pumps = pumpImpl();
        } else {
          LOGGER.debug("No pending tasks after last input failed for {} but no results requested", this);
        }
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("{} pending tasks for {}", _pendingTasks, this);
        }
      }
    }
    storeFailure(failure);
    pumpImpl(context, pumps);
  }

  /**
   * Tests if the result about to be pushed from {@link #resolved} can be considered the "last result". The result has come from the last pending task. The
   * default behavior is to return true but a sub-class that hooks the {@link #finished} call to introduce more productions must return false to avoid an
   * intermediate last result being passed to the consumer of this aggregate.
   * <p>
   * This is called holding the monitor.
   *
   * @return true if the result really is the last result, false otherwise
   */
  protected boolean isLastResult() {
    return true;
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    do {
      LOGGER.debug("Received {} for {}", value, valueRequirement);
      boolean wantedResult = false;
      final boolean lastResult;
      synchronized (this) {
        if (_pendingTasks == Integer.MIN_VALUE) {
          // We were discarded after we requested the callback
          LOGGER.debug("Successful resolution after discard of {}", this);
          break;
        }
        assert _pendingTasks > 0;
        if (_wantResult) {
          LOGGER.debug("Clearing \"want result\" flag for {}", this);
          wantedResult = true;
          _wantResult = false;
        }
        lastResult = pump == null && _pendingTasks == 1 && _pumps.isEmpty() && isLastResult();
      }
      // Note that the lastResult indicator isn't 100% if there are concurrent calls to resolved. The "last" condition may
      // not be seen. The alternative would be to serialize the calls through pushResult so that we can guarantee spotting
      // the final one.
      if (pushResult(context, value, lastResult)) {
        Collection<ResolutionPump> pumps = null;
        synchronized (this) {
          if (_pendingTasks == Integer.MIN_VALUE) {
            // We were discarded while the result was handled
            LOGGER.debug("Discard of {} while pushing result", this);
            break;
          }
          assert _pendingTasks > 0;
          if (pump != null) {
            _pumps.add(pump);
          }
          if (--_pendingTasks == 0) {
            if (_wantResult && !lastResult) {
              LOGGER.debug("Pumping underlying after last input resolved for {}", this);
              pumps = pumpImpl();
            } else {
              LOGGER.debug("No pending tasks after last input resolved for {} but no further results requested", this);
            }
          }
        }
        pumpImpl(context, pumps);
      } else {
        if (wantedResult) {
          synchronized (this) {
            if (_pendingTasks == Integer.MIN_VALUE) {
              // We were discarded while the result was rejected
              LOGGER.debug("Discard of {} while pushing rejected result", this);
              break;
            }
            assert _pendingTasks > 0;
            LOGGER.debug("Reinstating \"want result\" flag for {}", this);
            _wantResult = true;
          }
        }
        if (pump != null) {
          context.pump(pump);
        } else {
          context.failed(this, valueRequirement, null);
        }
      }
      return;
    } while (false);
    if (pump != null) {
      context.close(pump);
    }
  }

  @Override
  public void recursionDetected() {
    // No-op by default
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      assert _pendingTasks >= 0;
      if (_pendingTasks == 0) {
        LOGGER.debug("Pumping underlying since no pending tasks for {}", this);
        pumps = pumpImpl();
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Deferring pump while {} task(s) pending for {}", _pendingTasks, this);
        }
        _wantResult = true;
      }
    }
    pumpImpl(context, pumps);
  }

  // Caller must hold the monitor
  private Collection<ResolutionPump> pumpImpl() {
    if (_pumps.isEmpty()) {
      return Collections.emptyList();
    }
    final List<ResolutionPump> pumps = new ArrayList<>(_pumps);
    _pumps.clear();
    _pendingTasks = pumps.size();
    _wantResult = true;
    return pumps;
  }

  private void pumpImpl(final GraphBuildingContext context, final Collection<ResolutionPump> pumps) {
    if (pumps != null) {
      if (pumps.isEmpty()) {
        // We have nothing to pump, so must have finished (failed)
        LOGGER.debug("Finished {}", this);
        finished(context);
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Pumping {} origin tasks from {}", pumps.size(), this);
        }
        for (final ResolutionPump pump : pumps) {
          context.pump(pump);
        }
      }
    }
  }

  public void addProducer(final GraphBuildingContext context, final ResolvedValueProducer producer) {
    synchronized (this) {
      if (_pendingTasks == Integer.MIN_VALUE) {
        LOGGER.debug("Discarded before fallback producer {} added to {}", producer, this);
        return;
      }
      assert _pendingTasks >= 0;
      if (_pendingTasks == 0) {
        _wantResult = true;
      }
      _pendingTasks++;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("{} pending tasks for {}", _pendingTasks, this);
      }
    }
    producer.addCallback(context, this);
  }

  public void start(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      assert _pendingTasks >= 1;
      if (--_pendingTasks == 0) {
        if (_wantResult) {
          LOGGER.debug("Pumping underlying after startup tasks completed for {}", this);
          pumps = pumpImpl();
        } else {
          LOGGER.debug("Startup tasks completed for {} but no further results requested", this);
        }
      }
    }
    pumpImpl(context, pumps);
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    assert _pendingTasks <= 1;
    super.finished(context);
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (count == 0) {
      List<ResolutionPump> pumps;
      synchronized (this) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Releasing {} - with {} pumped inputs", this, _pumps.size());
        }
        // If _pendingTasks > 0 then there may be calls to failure or resolved from one or more of them. Setting _pendingTasks to
        // Integer.MIN_VALUE means we can detect these and discard them. Our reference count is zero so nothing subscribing to us
        // cares.
        _pendingTasks = Integer.MIN_VALUE;
        if (_pumps.isEmpty()) {
          return count;
        }
        pumps = new ArrayList<>(_pumps);
        _pumps.clear();
      }
      for (final ResolutionPump pump : pumps) {
        context.close(pump);
      }
    }
    return count;
  }

  @Override
  public String toString() {
    return "AGGREGATE" + getObjectId();
  }

}
