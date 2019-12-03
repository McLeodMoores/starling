/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;

/**
 * Represents a call to {@link ViewResultListener#processTerminated(boolean)}.
 */
public class ProcessTerminatedCall implements Function<ViewResultListener, Object> {

  private final boolean _executionInterrupted;

  public ProcessTerminatedCall(final boolean executionInterrupted) {
    _executionInterrupted = executionInterrupted;
  }

  public boolean isExecutionInterrupted() {
    return _executionInterrupted;
  }

  @Override
  public Object apply(final ViewResultListener viewProcessListener) {
    viewProcessListener.processTerminated(isExecutionInterrupted());
    return null;
  }

}
