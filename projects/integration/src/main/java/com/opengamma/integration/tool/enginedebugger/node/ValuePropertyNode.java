/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

/**
 * container for a value property, purely for use by the tree table model.
 */
public class ValuePropertyNode extends AbstractTreeTableLeafNode {
  private static final String NAME = "ValueProperty";
  private final String _name;
  private final String _value;

  public ValuePropertyNode(final String name, final String value) {
    _name = name;
    _value = value;
  }

  public String getName() {
    return _name;
  }

  public String getValue() {
    return _value;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof ValuePropertyNode)) {
      return false;
    }
    final ValuePropertyNode o = (ValuePropertyNode) other;
    return _name.equals(o.getName()) && _value.equals(o.getValue());
  }

  @Override
  public int hashCode() {
    return _name.hashCode() * _value.hashCode();
  }

  @Override
  public Object getColumn(final int column) {
    switch (column) {
      case 0:
        return NAME;
      case 1:
        return getName();
      case 2:
        return getValue();
    }
    return null;
  }


}
