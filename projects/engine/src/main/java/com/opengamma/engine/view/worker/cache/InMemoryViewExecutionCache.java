/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * A trivial in-memory cache. This should be used for test infrastructure only; a cache that can spool to disk would probably be better.
 */
public class InMemoryViewExecutionCache implements ViewExecutionCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryViewExecutionCache.class);

  /**
   * The buffer to hold compiled view definitions.
   */
  private final Map<ViewExecutionCacheKey, CompiledViewDefinitionWithGraphs> _compiledViewDefinitions = CacheBuilder.newBuilder().softValues()
      .<ViewExecutionCacheKey, CompiledViewDefinitionWithGraphs> build()
      .asMap();

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key) {
    final CompiledViewDefinitionWithGraphs viewDefinition = _compiledViewDefinitions.get(key);
    if (LOGGER.isDebugEnabled()) {
      if (viewDefinition == null) {
        LOGGER.debug("Cache miss CompiledViewDefinitionWithGraphs for {}", key);
      } else {
        LOGGER.debug("Cache hit CompiledViewDefinitionWithGraphs for {}", key);
      }
    }
    return viewDefinition;
  }

  @Override
  public void setCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key, final CompiledViewDefinitionWithGraphs viewDefinition) {
    LOGGER.info("Storing CompiledViewDefinitionWithGraphs for {}", key);
    _compiledViewDefinitions.put(key, viewDefinition);
  }

  @Override
  public void clear() {
    LOGGER.info("Clearing all CompiledViewDefinitionWithGraphs");
    _compiledViewDefinitions.clear();
  }

}
