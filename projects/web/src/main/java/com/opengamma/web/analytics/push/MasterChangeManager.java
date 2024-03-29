/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Dispatches notifications to listeners when data changes in a master.
 */
public class MasterChangeManager {

  /** Listeners for changes in data in a master */
  private final Set<MasterChangeListener> _listeners = new CopyOnWriteArraySet<>();

  /**
   * Creates a new instance that will receive change events from the change providers and dispatch them to its
   * listeners.
   * @param changeProviders Providers of change events from masters keyed by the type of the master which
   * produces their events.
   */
  public MasterChangeManager(final Map<MasterType, ChangeProvider> changeProviders) {
    for (final Map.Entry<MasterType, ChangeProvider> entry : changeProviders.entrySet()) {
      final MasterType masterType = entry.getKey();
      final ChangeProvider changeProvider = entry.getValue();
      changeProvider.changeManager().addChangeListener(new ChangeListener() {
        @Override
        public void entityChanged(final ChangeEvent event) {
          MasterChangeManager.this.entityChanged(masterType);
        }
      });
    }
  }

  /* package */ void addChangeListener(final MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /* package */ void removeChangeListener(final MasterChangeListener listener) {
    _listeners.remove(listener);
  }

  private void entityChanged(final MasterType masterType) {
    for (final MasterChangeListener listener : _listeners) {
      listener.masterChanged(masterType);
    }
  }
}
