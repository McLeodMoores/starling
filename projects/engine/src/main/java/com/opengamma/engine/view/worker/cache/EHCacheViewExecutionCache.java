/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.MapMaker;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * An EH-Cache based implementation of {@link ViewExecutionCache}.
 */
public class EHCacheViewExecutionCache implements ViewExecutionCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(EHCacheViewExecutionCache.class);

  private static final String COMPILED_VIEW_DEFINITIONS = "compiledViewDefinitions";

  private static final Map<Serializable, EHCacheViewExecutionCache> INSTANCE_TO_IDENTIFIER = new MapMaker().weakValues().makeMap();

  private static final AtomicInteger NEXT_IDENTIFIER = new AtomicInteger(0);

  private final Serializable _identifier;

  private final CacheManager _cacheManager;

  private final ComputationTargetResolver _targetResolver;

  private final ConcurrentMap<ViewExecutionCacheKey, CompiledViewDefinitionWithGraphs> _compiledViewDefinitionsFrontCache = new MapMaker().weakValues()
      .makeMap();

  private final Cache _compiledViewDefinitions;

  /**
   * Creates a new instance.
   *
   * @param cacheManager
   *          the cache manager, not null
   * @param targetResolver
   *          the target resolver for portfolio and view definition objects, not null
   */
  public EHCacheViewExecutionCache(final CacheManager cacheManager, final ComputationTargetResolver targetResolver) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _identifier = NEXT_IDENTIFIER.getAndIncrement();
    _cacheManager = cacheManager;
    _targetResolver = targetResolver;
    EHCacheUtils.addCache(_cacheManager, COMPILED_VIEW_DEFINITIONS);
    _compiledViewDefinitions = EHCacheUtils.getCacheFromManager(_cacheManager, COMPILED_VIEW_DEFINITIONS);
    INSTANCE_TO_IDENTIFIER.put(_identifier, this);
  }

  /**
   * Creates a new instance.
   *
   * @param cacheManager
   *          the cache manager, not null
   * @param cfs
   *          the compiled function service, holding a computation target resolver, not null
   */
  public EHCacheViewExecutionCache(final CacheManager cacheManager, final CompiledFunctionService cfs) {
    this(cacheManager, cfs.getFunctionCompilationContext().getRawComputationTargetResolver());
  }

  /**
   * Creates a new instance.
   *
   * @param cacheManager
   *          the cache manager, not null
   * @param configSource
   *          not used
   * @param cfs
   *          the compiled function service, holding a computation target resolver, not null
   * @deprecated Kept for compatibility with existing Spring config files only
   */
  @Deprecated
  public EHCacheViewExecutionCache(final CacheManager cacheManager, final ConfigSource configSource, final CompiledFunctionService cfs) {
    this(cacheManager, cfs.getFunctionCompilationContext().getRawComputationTargetResolver());
  }

  /**
   * For testing only.
   */
  /* package */void clearFrontCache() {
    _compiledViewDefinitionsFrontCache.clear();
  }

  protected Serializable instance() {
    return _identifier;
  }

  protected static EHCacheViewExecutionCache instance(final Serializable identifier) {
    return INSTANCE_TO_IDENTIFIER.get(identifier);
  }

  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /* package */static final class CompiledViewDefinitionWithGraphsReader implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Serializable _parent;
    private final VersionCorrection _versionCorrection;
    private final String _compilationId;
    private final UniqueId _viewDefinition;
    private final Collection<DependencyGraph> _graphs;
    private final Map<ComputationTargetReference, UniqueId> _resolutions;
    private final UniqueId _portfolio;
    private final long _functionInitId;
    private final Collection<CompiledViewCalculationConfiguration> _calcConfigs;
    private final Instant _validFrom;
    private final Instant _validTo;

    CompiledViewDefinitionWithGraphsReader(final EHCacheViewExecutionCache parent, final CompiledViewDefinitionWithGraphs viewDef) {
      _parent = parent.instance();
      _versionCorrection = viewDef.getResolverVersionCorrection();
      _compilationId = viewDef.getCompilationIdentifier();
      _viewDefinition = viewDef.getViewDefinition().getUniqueId();
      final Collection<DependencyGraphExplorer> graphs = viewDef.getDependencyGraphExplorers();
      _graphs = new ArrayList<>(graphs.size());
      for (final DependencyGraphExplorer explorer : graphs) {
        _graphs.add(explorer.getWholeGraph());
      }
      _resolutions = viewDef.getResolvedIdentifiers();
      _portfolio = viewDef.getPortfolio().getUniqueId();
      _functionInitId = ((CompiledViewDefinitionWithGraphsImpl) viewDef).getFunctionInitId();
      _calcConfigs = new ArrayList<>(viewDef.getCompiledCalculationConfigurations());
      _validFrom = viewDef.getValidFrom();
      _validTo = viewDef.getValidTo();
    }

    private Object readResolve() {
      final EHCacheViewExecutionCache parent = instance(_parent);
      final ViewDefinition viewDefinition = (ViewDefinition) parent.getTargetResolver()
          .resolve(new ComputationTargetSpecification(ComputationTargetType.of(ViewDefinition.class), _viewDefinition), VersionCorrection.LATEST).getValue();
      final Portfolio portfolio = (Portfolio) parent.getTargetResolver().resolve(
          new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, _portfolio), _versionCorrection).getValue();
      final CompiledViewDefinitionWithGraphsImpl compiledViewDef = new CompiledViewDefinitionWithGraphsImpl(_versionCorrection, _compilationId, viewDefinition,
          _graphs, _resolutions, portfolio, _functionInitId,
          _calcConfigs, _validFrom, _validTo);
      return parent.new CompiledViewDefinitionWithGraphsHolder(compiledViewDef);
    }

  }

  /* package */final class CompiledViewDefinitionWithGraphsHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CompiledViewDefinitionWithGraphs _viewDefinition;

    CompiledViewDefinitionWithGraphsHolder(final CompiledViewDefinitionWithGraphs viewDefinition) {
      _viewDefinition = viewDefinition;
    }

    public CompiledViewDefinitionWithGraphs get() {
      return _viewDefinition;
    }

    private Object writeReplace() {
      return new CompiledViewDefinitionWithGraphsReader(EHCacheViewExecutionCache.this, _viewDefinition);
    }

  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key) {
    CompiledViewDefinitionWithGraphs graphs = _compiledViewDefinitionsFrontCache.get(key);
    if (graphs != null) {
      LOGGER.debug("Front cache hit CompiledViewDefinitionWithGraphs for {}", key);
      return graphs;
    }
    final Element element = _compiledViewDefinitions.get(key);
    if (element != null) {
      LOGGER.debug("EHCache hit CompiledViewDefinitionWithGraphs for {}", key);
      graphs = ((CompiledViewDefinitionWithGraphsHolder) element.getObjectValue()).get();
      final CompiledViewDefinitionWithGraphs existing = _compiledViewDefinitionsFrontCache.putIfAbsent(key, graphs);
      if (existing != null) {
        graphs = existing;
      }
    } else {
      LOGGER.debug("EHCache miss CompiledViewDefinitionWithGraphs for {}", key);
    }
    return graphs;
  }

  @Override
  public void setCompiledViewDefinitionWithGraphs(final ViewExecutionCacheKey key, final CompiledViewDefinitionWithGraphs viewDefinition) {
    final CompiledViewDefinitionWithGraphs existing = _compiledViewDefinitionsFrontCache.put(key, viewDefinition);
    if (existing != null) {
      if (existing == viewDefinition) {
        return;
      }
    }
    LOGGER.info("Storing CompiledViewDefinitionWithGraphs for {}", key);
    _compiledViewDefinitions.put(new Element(key, new CompiledViewDefinitionWithGraphsHolder(viewDefinition)));
  }

  @Override
  public void clear() {
    _compiledViewDefinitionsFrontCache.clear();
    LOGGER.info("Clearing all CompiledViewDefinitionWithGraphs");
    _compiledViewDefinitions.removeAll();
  }

}
