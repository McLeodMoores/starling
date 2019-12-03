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

  private final List<ChangeListener> _listeners = new CopyOnWriteArrayList<>();

  @Override
  public void addChangeListener(final ChangeListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(final ChangeListener listener) {
    throw new UnsupportedOperationException("removeChangeListener not implemented");
  }

  @Override
  public void entityChanged(final ChangeType type, final ObjectId objectId, final Instant versonFrom, final Instant versionTo, final Instant versionInstant) {
    final ChangeEvent event = new ChangeEvent(type, objectId, versonFrom, versionTo, versionInstant);
    for (final ChangeListener listener : _listeners) {
      listener.entityChanged(event);
    }
  }

  @Override
  public ChangeManager changeManager() {
    return this;
  }
}
