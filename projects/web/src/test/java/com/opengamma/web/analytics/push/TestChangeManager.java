/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;

/**
 *
 */
public class TestChangeManager implements ChangeManager, ChangeProvider {

  private final List<ChangeListener> _listeners = new CopyOnWriteArrayList<ChangeListener>();

  @Override
  public void addChangeListener(ChangeListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
    throw new UnsupportedOperationException("removeChangeListener not implemented");
  }

  @Override
  public void entityChanged(ChangeType type, ObjectId objectId, Instant versonFrom, Instant versionTo, Instant versionInstant) {
    ChangeEvent event = new ChangeEvent(type, objectId, versonFrom, versionTo, versionInstant);
    for (ChangeListener listener : _listeners) {
      listener.entityChanged(event);
    }
  }

  @Override
  public ChangeManager changeManager() {
    return this;
  }
}
