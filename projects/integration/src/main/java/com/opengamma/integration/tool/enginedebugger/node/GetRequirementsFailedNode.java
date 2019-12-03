/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Node representing a failure in getRequirements() during graph building.
 */
public class GetRequirementsFailedNode extends AbstractFailureNode {

  private static final String NAME = "GetRequirementsFailed";

  public GetRequirementsFailedNode(final Object parent, final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput) {
    super(parent, valueRequirement, function, desiredOutput);
  }

  @Override
  public Object getColumn(final int column) {
    if (column == 0) {
      return NAME;
    } else if (column == 1) {
      return _functionEntry.getFunctionName();
    }
    return null;
  }

  @Override
  public boolean equals(final Object o) {
    final boolean result = super.equals(o);
    return result && o instanceof GetRequirementsFailedNode;
  }

}
