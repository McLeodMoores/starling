/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A wrapper for a map of value specification to value requirement that makes it easier to write
 * a tree table model
 */
public class ValueSpecificationToRequirementMapNode implements TreeTableNode {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValueSpecificationToRequirementMapNode.class);
  private static final Object NAME = "Map of ValueSpec->ValueReq";
  private final Map<ValueSpecification, ValueRequirement> _map;
  private final String _description;
  private final List<ValueSpecification> _keySet;
  private final Object _parent;

  public ValueSpecificationToRequirementMapNode(final Object parent, final Map<ValueSpecification, ValueRequirement> map, final String description) {
    _parent = parent;
    _map = map;
    _description = description;
    _keySet = new ArrayList<>(map.keySet());
  }

  public String getDescription() {
    return _description;
  }

  private int getSize() {
    return _keySet.size();
  }

  private SpecToRequirementEntryNode getEntry(final int index) {
    final ValueSpecification valueSpecification = _keySet.get(index);
    final ValueRequirement valueRequirement = _map.get(valueSpecification);
    return new SpecToRequirementEntryNode(valueSpecification, valueRequirement);
  }

  private int indexOf(final SpecToRequirementEntryNode entry) {
    return _keySet.indexOf(entry.getValueSpecification());
  }

  @Override
  public Object getChildAt(final int index) {
    return getEntry(index);
  }

  @Override
  public int getChildCount() {
    return getSize();
  }

  @Override
  public int getIndexOfChild(final Object child) {
    if (child instanceof SpecToRequirementEntryNode) {
      return indexOf((SpecToRequirementEntryNode) child);
    }
    return -1;
  }

  @Override
  public Object getColumn(final int column) {
    switch (column) {
      case 0:
        return NAME + (getChildCount() == 0 ? " (Empty)" : "");
      case 1:
        return _description;
    }
    return null;
  }
}
