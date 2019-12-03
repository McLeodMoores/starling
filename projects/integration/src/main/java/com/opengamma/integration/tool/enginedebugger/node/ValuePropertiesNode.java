/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.value.ValueProperties;

public class ValuePropertiesNode implements TreeTableNode {

  private static final String NAME = "ValueProperties";
  private final Object _parent;
  private final ValueProperties _constraints;

  public ValuePropertiesNode(final Object parent, final ValueProperties constraints) {
    _parent = parent;
    _constraints = constraints;
  }

  @Override
  public Object getChildAt(final int index) {
    if (_constraints != null && _constraints.getProperties() != null) {
      final List<String> propertyKeyList = new ArrayList<>(_constraints.getProperties());
      return new ValuePropertyNode(propertyKeyList.get(index), _constraints.getValues(propertyKeyList.get(index)).toString());
    }
    return null;
  }

  @Override
  public int getChildCount() {
    if (_constraints != null && _constraints.getProperties() != null) {
      return _constraints.getProperties().size();
    }
    return 0;
  }

  @Override
  public int getIndexOfChild(final Object child) {
    if (_constraints != null && _constraints.getProperties() != null) {
      if (child instanceof ValuePropertyNode) {
        final ValuePropertyNode childVP = (ValuePropertyNode) child;
        final List<String> propertyKeyList = new ArrayList<>(_constraints.getProperties());
        final int index = propertyKeyList.indexOf(childVP.getName());
        if (index >= 0) {
          if (_constraints.getValues(childVP.getName()).toString().equals(childVP.getValue())) {
            return index;
          }
        }
      }
    }
    return -1;
  }

  @Override
  public Object getColumn(final int column) {
    if (column == 0) {
      return NAME + (getChildCount() == 0 ? " (empty)" : "");
    }
    return null;
  }

}
