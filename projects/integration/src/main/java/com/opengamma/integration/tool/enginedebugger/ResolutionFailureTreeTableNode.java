/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.integration.tool.enginedebugger.node.TreeTableNode;

public class ResolutionFailureTreeTableNode  implements TreeTableNode {

  private static final String NAME = "Failure";
  private final ResolutionFailureImpl _node;
  private final List<Object> _children;

  public ResolutionFailureTreeTableNode(final ResolutionFailureImpl node) {
    _node = node;
    _children = new ArrayList<>(node.accept(new ResolutionFailureChildNodeCreatingVisitor(node)));
  }

  @Override
  public Object getChildAt(final int index) {
    return _children.get(index);
  }

  @Override
  public int getChildCount() {
    return _children.size();
  }

  @Override
  public int getIndexOfChild(final Object child) {
    return _children.indexOf(child);
  }

  @Override
  public Object getColumn(final int column) {
    if (column == 0) {
      return NAME;
    }
    return null;
  }
}
