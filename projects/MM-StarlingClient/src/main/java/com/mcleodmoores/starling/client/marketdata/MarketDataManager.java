/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.starling.client.results.ViewKey;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.spec.AlwaysAvailableMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.PrimitiveResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data manager class to allow save or update and load operations on MarketDataSets.
 */
public class MarketDataManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataManager.class);
  private static final String TIME_SERIES_KEY_RESOLVER = "DEFAULT_TSS_CONFIG";
  private static final String DEFAULT_OBSERVATION_TIME = "DEFAULT";
  private final HistoricalTimeSeriesMaster _htsMaster;
  private final HistoricalTimeSeriesSource _htsSource;
  private final SecuritySource _secSource;
  private final ViewProcessor _viewProcessor;
  private final HistoricalTimeSeriesResolver _htsResolver;
  private final ConfigSource _configSource;
  // copied from PrimitiveResolver because it's private.
  private static final String SCHEME_PREFIX = "ExternalId-";

  /**
   * Public constructor to create an instance of the market data manager.
   * @param htsMaster  a historical time series master, not null
   * @param htsSource  a historical time series source, not null
   * @param secSource  a security source, not null
   * @param htsResolver  a historical time series resolver, not null
   * @param configSource  a config source, not null
   * @param viewProcessor  a view processor, not null
   */
  public MarketDataManager(final HistoricalTimeSeriesMaster htsMaster, final HistoricalTimeSeriesSource htsSource,
                           final SecuritySource secSource, final HistoricalTimeSeriesResolver htsResolver,
                           final ConfigSource configSource, final ViewProcessor viewProcessor) {
    _htsMaster = ArgumentChecker.notNull(htsMaster, "htsMaster");
    _htsSource = ArgumentChecker.notNull(htsSource, "htsSource");
    _secSource = ArgumentChecker.notNull(secSource, "secSource");
    _htsResolver = ArgumentChecker.notNull(htsResolver, "htsResolver");
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
    _viewProcessor = ArgumentChecker.notNull(viewProcessor, "viewProcessor");
  }

  /**
   * Public constructor to create an instance of a market data manager.
   * @param toolContext  the tool context
   */
  public MarketDataManager(final ToolContext toolContext) {
    this(toolContext.getHistoricalTimeSeriesMaster(),
         toolContext.getHistoricalTimeSeriesSource(),
         toolContext.getSecuritySource(),
         toolContext.getHistoricalTimeSeriesResolver(),
         toolContext.getConfigSource(),
         toolContext.getViewProcessor());
  }

  /**
   * Save or update the provided market data set on the provided date.
   * @param marketDataSet  the set of market data to save or update
   * @param date  the date on which to save the data
   */
  public void saveOrUpdate(final MarketDataSet marketDataSet, final LocalDate date) {
    final HistoricalTimeSeriesMasterUtils masterUtils = new HistoricalTimeSeriesMasterUtils(_htsMaster);
    for (final Map.Entry<MarketDataKey, Object> entry : marketDataSet.entrySet()) {
      final MarketDataKey marketDataKey = entry.getKey();
      if (entry.getValue() instanceof Double) {
        masterUtils.writeTimeSeriesPoint(marketDataKey.getExternalIdBundle().toString(), marketDataKey.getSource().getName(),
            marketDataKey.getProvider().getName(),
            marketDataKey.getField().getName(),
            DEFAULT_OBSERVATION_TIME,
            marketDataKey.getExternalIdBundle(),
            date,
            (Double) entry.getValue());
      } else if (entry.getValue() instanceof LocalDateDoubleTimeSeries) {
        masterUtils.writeTimeSeries(marketDataKey.getExternalIdBundle().toString(),  marketDataKey.getSource().getName(),
            marketDataKey.getProvider().getName(),
            marketDataKey.getField().getName(),
            DEFAULT_OBSERVATION_TIME,
            marketDataKey.getExternalIdBundle(),
            (LocalDateDoubleTimeSeries) entry.getValue());
      }
    }
  }

  /**
   * Listens for the result and pipes it back.
   */
  private class CompileResultListener extends AbstractViewResultListener {

    private final SynchronousQueue<CompiledViewDefinition> _queue;

    public CompileResultListener(final SynchronousQueue<CompiledViewDefinition> queue) {
      _queue = queue;
    }
    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getLocalUser();
    }

    @Override
    public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
      try {
        _queue.put(compiledViewDefinition);
      } catch (final InterruptedException e) {
        LOGGER.error("View compilation queue interrupted", e);
      }
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      try {
        LOGGER.error("View compilation failed", exception);
        _queue.put(null);
      } catch (final InterruptedException e) {
        LOGGER.error("View compilation queue interrupted", e);
      }
    }

  }

  /**
   * Determine the required market data for a given view at a given valuation time.  This method presently only works
   * if market data is already available.
   * @param viewKey  the key for the view, not null
   * @param valuationTime  the valuation time, which may influence market data requirements, not null
   * @return a map of market data keys to meta data
   */
  public Map<MarketDataKey, MarketDataMetaData> getRequiredData(final ViewKey viewKey, final Instant valuationTime) {
    ArgumentChecker.notNull(viewKey, "viewKey");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    final Map<MarketDataKey, MarketDataMetaData> keys = new HashMap<>();
    final ViewClient viewClient = _viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    viewClient.setResultMode(ViewResultMode.FULL_ONLY);
    final List<MarketDataSpecification> marketDataSpecificationList = Collections
        .<MarketDataSpecification>singletonList(AlwaysAvailableMarketDataSpecification.builder().build());
    final UniqueId viewDefId = ensureConfig(viewKey.getName());
    final ExecutionFlags flags = ExecutionFlags.none().compileOnly().runAsFastAsPossible();  //.ignoreCompilationValidity();
    final SynchronousQueue<CompiledViewDefinition> queue = new SynchronousQueue<>();
    viewClient.setResultListener(new CompileResultListener(queue));
    viewClient.attachToViewProcess(viewDefId, ExecutionOptions.singleCycle(valuationTime, marketDataSpecificationList, flags.get()));
    try {
      final CompiledViewDefinition compiledViewDefinition = queue.take(); // wait for listener to get called back
      for (final ValueSpecification valueSpecification : compiledViewDefinition.getMarketDataRequirements()) {
        final ComputationTargetType targetType = valueSpecification.getTargetSpecification().getType();
        if (targetType.equals(ComputationTargetType.SECURITY)) {
          final Security security = _secSource.get(valueSpecification.getTargetSpecification().getUniqueId());
          final ExternalIdBundle bundle = security.getExternalIdBundle();
          keys.put(resolveKey(valueSpecification.getValueName(), bundle), ScalarMarketDataMetaData.INSTANCE);
        } else if (targetType.equals(ComputationTargetType.PRIMITIVE)) {
          if (valueSpecification.getTargetSpecification().getUniqueId().getValue().startsWith("ExternalId-")) {
            final ExternalIdBundle bundle = PrimitiveResolver.resolveExternalIds(valueSpecification.getTargetSpecification().getUniqueId(), SCHEME_PREFIX);
            if (valueSpecification.getValueName().equals(ValueRequirementNames.HISTORICAL_TIME_SERIES)) {
              final TimeSeriesMarketDataMetaData.Builder builder = TimeSeriesMarketDataMetaData.builder();
              final ValueProperties properties = valueSpecification.getProperties();
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)) {
                builder.adjust(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY));
              }
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)) {
                builder.ageLimit(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY));
              }
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)) {
                builder.startDate(LocalDate.parse(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)));
              }
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY)) {
                final String includeStart = properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
                builder.includeStart(includeStart.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE));
              }
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)) {
                builder.endDate(LocalDate.parse(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)));
              }
              if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY)) {
                final String includeEnd = properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
                builder.includeEnd(includeEnd.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE));
              }
              keys.put(resolveKey(valueSpecification.getValueName(), bundle), builder.build());
            } else if (valueSpecification.getValueName().startsWith("Market_")) {
              keys.put(resolveKey(valueSpecification.getValueName(), bundle), ScalarMarketDataMetaData.INSTANCE);
            }
          } else {
            final ExternalIdBundle bundle = PrimitiveResolver.resolveExternalIds(valueSpecification.getTargetSpecification().getUniqueId(), SCHEME_PREFIX);
            if (bundle == null) {
              LOGGER.error("couldn't resolve id {}", valueSpecification.getTargetSpecification().getUniqueId());
            }
            // valueSpecification.getTargetSpecification().getUniqueId()
            keys.put(resolveKey(valueSpecification.getValueName(), bundle), ScalarMarketDataMetaData.INSTANCE);
          }
        }
      }
      return keys;
    } catch (final InterruptedException ie) {
      throw new OpenGammaRuntimeException("View Compilation interrupted while waiting");
    }
  }

  private MarketDataKey resolveKey(final String normalizedFieldName, final UniqueId uid) {
    return resolveKey(normalizedFieldName, _htsSource.getExternalIdBundle(uid));
  }

  private MarketDataKey resolveKey(final String normalizedFieldName, final ExternalIdBundle bundle) {
    if (_htsResolver != null) {
      final HistoricalTimeSeriesResolutionResult resolutionResult = _htsResolver.resolve(bundle, null, null, null, normalizedFieldName, null);
      final ManageableHistoricalTimeSeriesInfo info = resolutionResult.getHistoricalTimeSeriesInfo();
      return MarketDataKey.builder().externalIdBundle(bundle).field(DataField.of(info.getDataField())).source(DataSource.of(info.getDataSource()))
          .provider(DataProvider.of(info.getDataProvider())).build();
    } else {
      return MarketDataKey.builder().externalIdBundle(bundle).field(DataField.of(normalizedFieldName)).build();
    }
  }

  private UniqueId ensureConfig(final String viewName) {
    final ViewDefinition viewDef = _configSource.getSingle(ViewDefinition.class, viewName, VersionCorrection.LATEST);
    return viewDef.getUniqueId();
  }
}
