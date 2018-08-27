/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.event;

import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.id.UniqueId;

/**
 * Registered listeners for registering and unregistering ViewProcessorEventListener and sending notifications to
 * registrants.
 *  <p>
 * There is one of these per ViewProcessor. It is a composite listener.
 */
public class ViewProcessorEventListenerRegistry implements ViewProcessorEventListener {

  /**
   * The set of listeners.
   */
  private final CopyOnWriteArraySet<ViewProcessorEventListener> _listeners = new CopyOnWriteArraySet<>();

  @Override
  public void notifyViewProcessAdded(final UniqueId viewProcessId) {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessAdded(viewProcessId);
    }
  }

  @Override
  public void notifyViewAutomaticallyStarted(final UniqueId viewProcessId, final String autoStartName) {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewAutomaticallyStarted(viewProcessId, autoStartName);
    }
  }

  @Override
  public void notifyViewProcessRemoved(final UniqueId viewProcessId) {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessRemoved(viewProcessId);
    }
  }

  @Override
  public void notifyViewClientAdded(final UniqueId viewClientId) {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewClientAdded(viewClientId);
    }
  }

  @Override
  public void notifyViewClientRemoved(final UniqueId viewClientId) {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewClientRemoved(viewClientId);
    }
  }

  /**
   * Adds a listener to the notification service. No guarantee is made that listeners will be
   * notified in the order they were added.
   *
   * @param viewProcessorEventListener the listener to add. Can be null, in which case nothing happens
   * @return true if the listener is being added and was not already added
   */
  public final boolean registerListener(final ViewProcessorEventListener viewProcessorEventListener) {
    if (viewProcessorEventListener == null) {
      return false;
    }
    return _listeners.add(viewProcessorEventListener);
  }

  /**
   * Removes a listener from the notification service.
   *
   * @param viewProcessorEventListener the listener to remove
   * @return true if the listener was present
   */
  public final boolean unregisterListener(final ViewProcessorEventListener viewProcessorEventListener) {
    return _listeners.remove(viewProcessorEventListener);
  }

  @Override
  public void notifyViewProcessorStarted() {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessorStarted();
    }
  }

  @Override
  public void notifyViewProcessorStopped() {
    for (final ViewProcessorEventListener listener : _listeners) {
      listener.notifyViewProcessorStopped();
    }
  }

}
