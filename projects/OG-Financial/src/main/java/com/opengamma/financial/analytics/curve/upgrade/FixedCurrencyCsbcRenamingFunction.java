/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.Objects;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;

/**
 * A renaming function for {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}
 * where the currency string is fixed upon construction. If {@link #_name} is null, the new name is
 * "[original name] [ISO currency string]. If {@link #_name} is not null, the new name is
 * "[original name] [_name] [ISO currency string]".
 *
 */
public class FixedCurrencyCsbcRenamingFunction implements Function2<String, String, String> {
  /** The currency */
  private final String _currency;
  /** A string containing additional information */
  private final String _name;

  /**
   * Sets the additional string to null.
   * @param currency  the currency string, not null
   */
  public FixedCurrencyCsbcRenamingFunction(final String currency) {
    this(currency, null);
  }

  /**
   * @param currency  the currency string, not null
   * @param name  the additional string, can be null
   */
  public FixedCurrencyCsbcRenamingFunction(final String currency, final String name) {
    ArgumentChecker.notNull(currency, "currency");
    _currency = currency;
    _name = name;
  }

  @Override
  public String apply(final String name, final String currency) {
    return _name == null ? name + " " + _currency : name + " " + _currency + " " + _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_currency);
    result = prime * result + Objects.hashCode(_name);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FixedCurrencyCsbcRenamingFunction)) {
      return false;
    }
    final FixedCurrencyCsbcRenamingFunction other = (FixedCurrencyCsbcRenamingFunction) obj;
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    return true;
  }

}
