/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

/**
 * Simple wrapper for a function entry in the resolution failure tree.
 */
public class FunctionEntryNode extends AbstractTreeTableLeafNode {
  private static final String LABEL = "Function";
  private final Object _parent;
  private final String _functionName;

  public FunctionEntryNode(final Object parent, final String functionName) {
    _parent = parent;
    _functionName = functionName;
  }

  public Object getParent() {
    return _parent;
  }

  public String getFunctionName() {
    return _functionName;
  }

  public String getLabel() {
    return LABEL;
  }

  @Override
  public Object getColumn(final int column) {
    switch (column) {
      case 0:
        return LABEL;
      case 1:
        return getFunctionName();
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_functionName == null ? 0 : _functionName.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FunctionEntryNode)) {
      return false;
    }
    final FunctionEntryNode other = (FunctionEntryNode) obj;
    if (_functionName == null) {
      if (other._functionName != null) {
        return false;
      }
    } else if (!_functionName.equals(other._functionName)) {
      return false;
    }
    return true;
  }

}
