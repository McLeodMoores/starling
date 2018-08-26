/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.TreeTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugTraceTreeTableModel implements TreeTableModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(DebugTraceTreeTableModel.class);
  private TreeTableModel _underlying;
  
  public DebugTraceTreeTableModel(TreeTableModel underlying) {
    _underlying = underlying;
  }
  @Override
  public Object getRoot() {
    Object root = _underlying.getRoot();
    LOGGER.info("getRoot() called, returning {}", root.getClass());
    return root;
  }

  @Override
  public Object getChild(Object parent, int index) {
    Object child = _underlying.getChild(parent, index);
    LOGGER.info("getChild(parent={}, index={}) returning {}", parent.getClass() + "(" + parent.hashCode() + ")", index, child);
    return child;
  }

  @Override
  public int getChildCount(Object parent) {
    int childCount = _underlying.getChildCount(parent);
    LOGGER.info("getChildCount(parent={}) returning {}", parent.getClass() + "(" + parent.hashCode() + ")", childCount);
    return childCount;
  }

  @Override
  public boolean isLeaf(Object node) {
    boolean leaf = _underlying.isLeaf(node);
    LOGGER.info("isLeaf(node={}) returning {}", node.getClass() + "(" + node.hashCode() + ")", leaf);
    return leaf;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    LOGGER.info("valueForPathChanged(path={}, newValue={})", path, newValue);
    _underlying.valueForPathChanged(path, newValue);
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    int index = _underlying.getIndexOfChild(parent, child);
    LOGGER.info("getIndexOfChild(parent={}, child={}) reurning {}", parent.getClass() + "(" + parent.hashCode() + ")", child, index);
    return index;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    LOGGER.info("addTreeModelListener(l={})", l);
    _underlying.addTreeModelListener(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    LOGGER.info("removeTreeModelListener(l={})", l);
    _underlying.removeTreeModelListener(l);
  }

  @Override
  public Class<?> getColumnClass(int arg0) {
    Class<?> columnClass = _underlying.getColumnClass(arg0);
    LOGGER.info("getColumnClass(arg0={}) returning {}", arg0, columnClass);
    return columnClass;
  }

  @Override
  public int getColumnCount() {
    int columnCount = _underlying.getColumnCount();
    LOGGER.info("getColumnCount() returning {}", columnCount);
    return columnCount;
  }

  @Override
  public String getColumnName(int arg0) {
    String columnName = _underlying.getColumnName(arg0);
    LOGGER.info("getColumnName() returning {}", columnName);
    return columnName;
  }

  @Override
  public int getHierarchicalColumn() {
    int heirarchicalColumn = _underlying.getHierarchicalColumn();
    LOGGER.info("getHeirarchicalColumn() returning {}", heirarchicalColumn);
    return heirarchicalColumn;
  }

  @Override
  public Object getValueAt(Object arg0, int arg1) {
    Object valueAt = _underlying.getValueAt(arg0, arg1);
    LOGGER.info("getValueAt(arg0={}, arg1={}) returning {}", arg0.getClass() + "(" + arg0.hashCode() + ")", arg1, valueAt);
    return valueAt;
  }

  @Override
  public boolean isCellEditable(Object arg0, int arg1) {
    boolean editable = _underlying.isCellEditable(arg0, arg1);
    LOGGER.info("isCellEditable(arg0={}, arg1={})", arg0.getClass() + "(" + arg0.hashCode() + ")", arg1);
    return editable;
  }

  @Override
  public void setValueAt(Object arg0, Object arg1, int arg2) {
    _underlying.setValueAt(arg0, arg1, arg2);
    LOGGER.info("setValueAt(arg0={}, arg1={}, arg2={}", arg0.getClass(), arg1.getClass(), arg2);
  }

}
