/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;

/**
 * Change listener that logs actions. A map of object id (so versions are
 * ignored) to list of change events is maintained but version information is
 * discarded.
 *
 * Used for testing.
 */
public final class TestChangeListener implements ChangeListener {

  /**
   * Static constructor.
   *
   * @return an instance
   */
  public static TestChangeListener of() {
    return new TestChangeListener();
  }

  private final Map<ObjectId, List<ChangeType>> _events = new HashMap<>();

  private TestChangeListener() {
  }

  @Override
  public void entityChanged(final ChangeEvent event) {
    List<ChangeType> events = _events.get(event.getObjectId());
    if (events == null) {
      events = new ArrayList<>();
      _events.put(event.getObjectId(), events);
    }
    events.add(event.getType());
  }

  /**
   * Gets all changes for an object id or throw an exception if there are no
   * events for this id.
   *
   * @param oid
   *          the object identifier
   * @return a list of changes
   */
  public List<ChangeType> getChangeType(final ObjectId oid) {
    final List<ChangeType> events = _events.get(oid);
    if (events == null) {
      throw new IllegalArgumentException("No events for " + oid);
    }
    return events;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_events == null ? 0 : _events.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TestChangeListener)) {
      return false;
    }
    final TestChangeListener other = (TestChangeListener) obj;
    return ObjectUtils.equals(_events, other._events);
  }

}
