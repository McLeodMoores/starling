/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import com.opengamma.id.UniqueId;

/**
 * An entry in a list for a given view definition.
 */
public final class ViewEntry {
  private final UniqueId _uniqueId;
  private final String _name;

  private ViewEntry(final UniqueId uniqueId, final String name) {
    _uniqueId = uniqueId;
    _name = name;
  }

  public static ViewEntry of(final UniqueId uniqueId, final String name) {
    return new ViewEntry(uniqueId, name);
  }

  public UniqueId getUniqueId() {
    return _uniqueId;
  }
  public String getName() {
    return _name;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof ViewEntry)) {
      return false;
    }
    final ViewEntry o = (ViewEntry) other;
    if (!o.getName().equals(getName())) {
      return false;
    }
    return o.getUniqueId().equals(getUniqueId());
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }
}
