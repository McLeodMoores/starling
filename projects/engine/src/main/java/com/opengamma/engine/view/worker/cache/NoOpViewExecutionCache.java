/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * An No Op implementation of {@link ViewExecutionCache}, that never caches anything. Useful when requests are always presenting a unique
 * {@link ViewExecutionCacheKey} and there is effectively always a cache miss.
 */
public class NoOpViewExecutionCache implements ViewExecutionCache {
  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key) {
    return null;
  }

  @Override
  public void setCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key, final CompiledViewDefinitionWithGraphs viewDefinition) {
    // do nothing
  }

  @Override
  public void clear() {
    // do nothing
  }
}
