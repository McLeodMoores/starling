/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.tool.enginedebugger.node.AbstractFailureWithRequirementsNode;

public class BlacklistSuppressedNode extends AbstractFailureWithRequirementsNode {

  private static final String NAME = "BlacklistSuppressed";

  public BlacklistSuppressedNode(final Object parent, final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> requirements) {
    super(parent, valueRequirement, function, desiredOutput, requirements, "Requirements");
  }

  @Override
  public Object getColumn(final int column) {
    if (column == 0) {
      return NAME;
    }
    return null;
  }

  @Override
  public boolean equals(final Object o) {
    final boolean result = super.equals(o);
    return result && o instanceof BlacklistSuppressedNode;
  }

  @Override
  public int hashCode() {
    return BlacklistSuppressedNode.class.hashCode();
  }

}
