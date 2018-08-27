/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.integration.tool.enginedebugger.node.TreeTableNode;

/**
 * Tree-table model for browsing ResolutionFailure structures
 */
public class ResolutionFailureTreeTableModel extends AbstractTreeTableModel {

  private static final Object LIST_NAME = "Events";

  public ResolutionFailureTreeTableModel(final List<ResolutionFailure> rootFailures) {
    super(topLevelNodes(rootFailures));
  }

  private static List<ResolutionFailureTreeTableNode> topLevelNodes(final List<ResolutionFailure> failures) {
    final List<ResolutionFailureTreeTableNode> results = new ArrayList<>();
    for (final ResolutionFailure failure : failures) {
      results.add(new ResolutionFailureTreeTableNode((ResolutionFailureImpl) failure));
    }
    return results;
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public Object getValueAt(final Object node, final int column) {
    if (node instanceof TreeTableNode) {
      final TreeTableNode failureNode = (TreeTableNode) node;
      return failureNode.getColumn(column);
    } else if (node instanceof List) {
      if (column == 0) {
        return LIST_NAME;
      } else {
        return null;
      }
    } else if (node instanceof String) {
      switch (column) {
        case 0:
          return node;
        default:
          return null;
      }
    }
    return node.getClass().toString() + "(" + node.hashCode() + ")";
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent instanceof TreeTableNode) {
      final TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getChildAt(index);
    } else if (parent instanceof List) {
      final List<?> failures = (List<?>) parent;
      return failures.get(index);
    }
    return null;
  }

  @Override
  public int getChildCount(final Object parent) {
    if (parent instanceof TreeTableNode) {
      final TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getChildCount();
    } else if (parent instanceof List) {
      final List<?> failures = (List<?>) parent;
      return failures.size();
    }
    return 0;
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    if (parent instanceof TreeTableNode) {
      final TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getIndexOfChild(child);
    } else if (parent instanceof List) {
      final List<?> failures = (List<?>) parent;
      return failures.indexOf(child);
    }
    return -1;
  }

}
