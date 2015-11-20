/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.util.NamedInstance;

/**
 * An interface for named instances of {@link ExposureFunction}. Classes that implement this interface have
 * additional methods that take a {@link FunctionExecutionContext} or {@link FunctionCompilationContext}.
 * This eliminates the need to pass the source in through the constructor and so allows implementations to be
 * obtained from a {@link com.opengamma.util.NamedInstanceFactory}.
 */
public interface NamedExposureFunction extends ExposureFunction, NamedInstance {

  @Override
  List<ExternalId> getIds(Trade trade);

  /**
   * Returns the identifiers, specific to the implementation of the exposure function, that the exposure function will
   * use to determine which curve construction configuration to use.
   * @param trade The trade to retrieve identifiers from, not null
   * @param context The context that provides access to {@link com.opengamma.core.Source} implementations, not null
   * @return The identifiers used to look up the curve construction configuration.
   */
  List<ExternalId> getIds(Trade trade, FunctionExecutionContext context);

  /**
   * Returns the identifiers, specific to the implementation of the exposure function, that the exposure function will
   * use to determine which curve construction configuration to use.
   * @param trade The trade to retrieve identifiers from, not null
   * @param context The context that provides access to {@link com.opengamma.core.Source} implementations, not null
   * @return The identifiers used to look up the curve construction configuration.
   */
  List<ExternalId> getIds(Trade trade, FunctionCompilationContext context);
}
