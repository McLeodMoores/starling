/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import com.opengamma.util.ArgumentChecker;

/**
 * {@link BeanDataSource} decorator that returns a value for one property and delegates to an underlying data source
 * for all other properties. Only supports basic properties (i.e. not properties whose values are maps, collections
 * or other beans).
 */
/* package */ class PropertyReplacingDataSource implements BeanDataSource {

  private final BeanDataSource _delegate;
  private final String _propertyName;
  private final String _value;

  /* package */ PropertyReplacingDataSource(final BeanDataSource delegate, final String propertyName, final String value) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(propertyName, "propertyName");
    ArgumentChecker.notNull(value, "value");
    _delegate = delegate;
    _propertyName = propertyName;
    _value = value;
  }

  @Override
  public Object getValue(final String propertyName) {
    if (_propertyName.equals(propertyName)) {
      return _value;
    } else {
      return _delegate.getValue(propertyName);
    }
  }

  @Override
  public List<?> getCollectionValues(final String propertyName) {
    return _delegate.getCollectionValues(propertyName);
  }

  @Override
  public Map<?, ?> getMapValues(final String propertyName) {
    return _delegate.getMapValues(propertyName);
  }

  @Override
  public String getBeanTypeName() {
    return _delegate.getBeanTypeName();
  }
}
