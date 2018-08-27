/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A simple implementation of {@link MultivaluedMap} where keys and values are
 * instances of String.
 */
public class MultivaluedMapImpl extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = 1L;

  public MultivaluedMapImpl() {
  }

  public MultivaluedMapImpl(final MultivaluedMap<String, String> that) {
    for (final Map.Entry<String, List<String>> e : that.entrySet()) {
      this.put(e.getKey(), new ArrayList<>(e.getValue()));
    }
  }

  @Override
  public final void putSingle(final String key, final String value) {
    final List<String> l = getList(key);
    l.clear();
    if (value != null) {
      l.add(value);
    } else {
      l.add("");
    }
  }

  @Override
  public final void add(final String key, final String value) {
    final List<String> l = getList(key);
    if (value != null) {
      l.add(value);
    } else {
      l.add("");
    }
  }

  @Override
  public final String getFirst(final String key) {
    final List<String> values = get(key);
    if (values != null && values.size() > 0) {
      return values.get(0);
    } else {
      return null;
    }
  }

  public final void addFirst(final String key, final String value) {
    final List<String> l = getList(key);
    if (value != null) {
      l.add(0, value);
    } else {
      l.add(0, "");
    }
  }

  public final <A> List<A> get(final String key, final Class<A> type) {
    Constructor<A> c = null;
    try {
      c = type.getConstructor(String.class);
    } catch (final Exception ex) {
      throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
    }

    ArrayList<A> l = null;
    final List<String> values = get(key);
    if (values != null) {
      l = new ArrayList<>();
      for (final String value : values) {
        try {
          l.add(c.newInstance(value));
        } catch (final Exception ex) {
          l.add(null);
        }
      }
    }
    return l;
  }

  public final void putSingle(final String key, final Object value) {
    final List<String> l = getList(key);
    l.clear();
    if (value != null) {
      l.add(value.toString());
    } else {
      l.add("");
    }
  }

  public final void add(final String key, final Object value) {
    final List<String> l = getList(key);
    if (value != null) {
      l.add(value.toString());
    } else {
      l.add("");
    }
  }

  private List<String> getList(final String key) {
    List<String> l = get(key);
    if (l == null) {
      l = new LinkedList<>();
      put(key, l);
    }
    return l;
  }

  public final <A> A getFirst(final String key, final Class<A> type) {
    final String value = getFirst(key);
    if (value == null) {
      return null;
    }
    Constructor<A> c = null;
    try {
      c = type.getConstructor(String.class);
    } catch (final Exception ex) {
      throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
    }
    A retVal = null;
    try {
      retVal = c.newInstance(value);
    } catch (final Exception ex) {
    }
    return retVal;
  }

  @SuppressWarnings("unchecked")
  public final <A> A getFirst(final String key, final A defaultValue) {
    final String value = getFirst(key);
    if (value == null) {
      return defaultValue;
    }

    final Class<A> type = (Class<A>) defaultValue.getClass();

    Constructor<A> c = null;
    try {
      c = type.getConstructor(String.class);
    } catch (final Exception ex) {
      throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
    }
    A retVal = defaultValue;
    try {
      retVal = c.newInstance(value);
    } catch (final Exception ex) {
    }
    return retVal;
  }

}
