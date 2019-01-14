/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;

/**
 * Change manager that logs actions. Lists of added and removed
 * {@link ChangeListener}s are maintained and a map of object ids (so versions
 * are ignored) and change types of changed entities are recorded.
 *
 * Used for testing.
 */
public class TestChangeManager implements ChangeManager {

  /**
   * Static constructor.
   *
   * @param name
   *          the name of the change manager
   * @return the change manager
   */
  public static TestChangeManager of(final String name) {
    return new TestChangeManager(name);
  }

  private final String _name;
  private final List<ChangeListener> _added = new ArrayList<>();
  private final List<ChangeListener> _removed = new ArrayList<>();
  private final List<ChangeListener> _current = new ArrayList<>();
  private final Map<ObjectId, List<ChangeType>> _events = new HashMap<>();

  private TestChangeManager(final String name) {
    _name = name;
  }

  @Override
  public void addChangeListener(final ChangeListener listener) {
    _added.add(listener);
    _current.add(listener);
  }

  @Override
  public void removeChangeListener(final ChangeListener listener) {
    _removed.add(listener);
    _current.remove(listener);
  }

  @Override
  public void entityChanged(final ChangeType type, final ObjectId oid, final Instant versionFrom, final Instant versionTo, final Instant versionInstant) {
    for (final ChangeListener listener : _current) {
      listener.entityChanged(new ChangeEvent(type, oid, versionFrom, versionTo, versionInstant));
    }
    List<ChangeType> events = _events.get(oid);
    if (events == null) {
      events = new ArrayList<>();
      _events.put(oid, events);
    }
    events.add(type);
  }

  /**
   * Gets all listeners that have been added over the lifetime of this object.
   *
   * @return the listeners
   */
  public List<ChangeListener> getAllAddedListeners() {
    return _added;
  }

  /**
   * Gets all listeners that have been removed over the lifetime of this object.
   *
   * @return the listeners
   */
  public List<ChangeListener> getAllRemovedListeners() {
    return _removed;
  }

  /**
   * Gets all listeners currently available for this manager.
   *
   * @return the listeners
   */
  public List<ChangeListener> getAllCurrentListeners() {
    return _current;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the events that have been listened to.
   *
   * @return the events
   */
  public Map<ObjectId, List<ChangeType>> getUniqueOidsWithEvents() {
    return _events;
  }

  @Override
  public String toString() {
    return _name;
  }
}
