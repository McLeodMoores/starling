/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.util.List;
import java.util.Objects;

import com.opengamma.core.position.Trade;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link ExposureFunction} that implements {@link NamedExposureFunction}.
 */
public class ExposureFunctionAdapter implements NamedExposureFunction {
  /** The underlying exposure function */
  private final ExposureFunction _exposureFunction;

  /**
   * Creates an instance.
   * @param exposureFunction The underlying exposure function, not null
   */
  public ExposureFunctionAdapter(final ExposureFunction exposureFunction) {
    ArgumentChecker.notNull(exposureFunction, "exposureFunction");
    _exposureFunction = exposureFunction;
  }

  @Override
  public String getName() {
    return _exposureFunction.getName();
  }

  @Override
  public List<ExternalId> getIds(final Trade trade) {
    return _exposureFunction.getIds(trade);
  }

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionExecutionContext executionContext) {
    return getIds(trade);
  }

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionCompilationContext compilationContext) {
    return getIds(trade);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _exposureFunction.getName().hashCode();
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
    if (!(obj instanceof ExposureFunctionAdapter)) {
      return false;
    }
    final ExposureFunctionAdapter other = (ExposureFunctionAdapter) obj;
    return Objects.equals(_exposureFunction.getName(), other._exposureFunction.getName());
  }

}
