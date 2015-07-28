/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Objects;

import com.opengamma.util.result.Function2;

/**
 *
 * The default renaming function for {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}.
 * If {@link #_name} is null, the new name is "[original name] [ISO currency string].
 * If {@link #_name} is not null, the new name is "[original name] [_name] [ISO currency string]".
 *
 */
public class DefaultCsbcRenamingFunction implements Function2<String, String, String> {
  /** A string containing additional information */
  private final String _name;

  /**
   * Sets the additional string to null.
   */
  public DefaultCsbcRenamingFunction() {
    this(null);
  }

  /**
   * @param name  the additional string, can be null
   */
  public DefaultCsbcRenamingFunction(final String name) {
    _name = name;
  }

  @Override
  public String apply(final String name, final String currency) {
    return _name == null ? name + " " + currency : name + " " + currency + " " + _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_name);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultCsbcRenamingFunction)) {
      return false;
    }
    final DefaultCsbcRenamingFunction other = (DefaultCsbcRenamingFunction) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
