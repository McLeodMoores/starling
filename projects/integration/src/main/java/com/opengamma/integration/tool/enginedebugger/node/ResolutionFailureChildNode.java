/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger.node;

import com.opengamma.engine.depgraph.ResolutionFailureImpl;

public class ResolutionFailureChildNode {
  private final Object[] _children;
  private final ResolutionFailureImpl _parent;

  public ResolutionFailureChildNode(final ResolutionFailureImpl parent, final Object[] children) {
    _parent = parent;
    _children = children;
  }
}
