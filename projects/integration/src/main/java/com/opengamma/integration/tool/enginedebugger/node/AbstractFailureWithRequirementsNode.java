/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Extended abstract failure node that adds a map as standard.
 */
public abstract class AbstractFailureWithRequirementsNode extends AbstractFailureNode {
  private final ValueSpecificationToRequirementMapNode _valueSpecificationToRequirementMap;

  public AbstractFailureWithRequirementsNode(final Object parent, final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final String mapName) {
    super(parent, valueRequirement, function, desiredOutput);
    _valueSpecificationToRequirementMap = new ValueSpecificationToRequirementMapNode(this, satisfied, mapName);
  }

  @Override
  public Object getChildAt(final int index) {
    switch (index) {
      case 3:
        return _valueSpecificationToRequirementMap;
      default:
        return super.getChildAt(index);
    }
  }

  @Override
  public int getIndexOfChild(final Object child) {
    if (child.equals(_valueSpecificationToRequirementMap)) {
      return 3;
    } else {
      return super.getIndexOfChild(child);
    }
  }

  @Override
  public int getChildCount() {
    return 4;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_valueSpecificationToRequirementMap == null ? 0 : _valueSpecificationToRequirementMap.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof AbstractFailureWithRequirementsNode)) {
      return false;
    }
    final AbstractFailureWithRequirementsNode other = (AbstractFailureWithRequirementsNode) obj;
    if (_valueSpecificationToRequirementMap == null) {
      if (other._valueSpecificationToRequirementMap != null) {
        return false;
      }
    } else if (!_valueSpecificationToRequirementMap.equals(other._valueSpecificationToRequirementMap)) {
      return false;
    }
    return true;
  }

}
