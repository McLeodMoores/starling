/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.results.ViewKey;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
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
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.AlwaysAvailableHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data manager class to allow save or update and load operations on MarketDataSets.
 */
public class MarketDataManager {
  /** The logger */
  /* package */ static final Logger LOGGER = LoggerFactory.getLogger(MarketDataManager.class);
  /** The default observation time */
  private static final String DEFAULT_OBSERVATION_TIME = "DEFAULT";
  /** The value requirement names that indicate that a time series has been requested */
  private static final Set<String> TIME_SERIES_REQUIREMENT_NAMES = new HashSet<>();
  static {
    TIME_SERIES_REQUIREMENT_NAMES.add(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    TIME_SERIES_REQUIREMENT_NAMES.add(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
    TIME_SERIES_REQUIREMENT_NAMES.add(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
  }
  /** The time series master */
  private final HistoricalTimeSeriesMaster _htsMaster;
  /** The security source */
  private final SecuritySource _secSource;
  /** The view processor */
  private final ViewProcessor _viewProcessor;
  /** The historical time series resolver */
  private final HistoricalTimeSeriesResolver _htsResolver;
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * Public constructor to create an instance of the market data manager.
   * @param htsMaster  a historical time series master, not null
   * @param secSource  a security source, not null
   * @param htsResolver  a historical time series resolver, not null
   * @param configSource  a config source, not null
   * @param viewProcessor  a view processor, not null
   */
  public MarketDataManager(final HistoricalTimeSeriesMaster htsMaster, final SecuritySource secSource, final HistoricalTimeSeriesResolver htsResolver,
                           final ConfigSource configSource, final ViewProcessor viewProcessor) {
    _htsMaster = ArgumentChecker.notNull(htsMaster, "htsMaster");
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
    this(ArgumentChecker.notNull(toolContext, "toolContext").getHistoricalTimeSeriesMaster(),
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
      } else {
        LOGGER.warn("Object to save or update {} was not a value or time series; ignoring", entry.getValue());
      }
    }
  }

  /**
   * Listens for the result and pipes it back.
   */
  private class CompileResultListener extends AbstractViewResultListener {
    /** The queue */
    private final SynchronousQueue<CompiledViewDefinition> _queue;

    /**
     * @param queue  the queue
     */
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
   * Determine the required market data for a given view at a given valuation time.
   * @param viewKey  the key for the view, not null
   * @param valuationTime  the valuation time, which may influence market data requirements, not null
   * @return the market data information
   */
  public MarketDataInfo getRequiredDataForView(final ViewKey viewKey, final Instant valuationTime) {
    ArgumentChecker.notNull(viewKey, "viewKey");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    final ViewClient viewClient = _viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    viewClient.setResultMode(ViewResultMode.FULL_ONLY);
    final List<MarketDataSpecification> marketDataSpecificationList = Collections.<MarketDataSpecification>singletonList(AlwaysAvailableMarketDataSpecification.builder().build());
    final UniqueId viewDefId = ensureConfig(viewKey.getName());
    final ExecutionFlags flags = ExecutionFlags.none().fetchMarketDataOnly().compileOnly().runAsFastAsPossible();
    final SynchronousQueue<CompiledViewDefinition> queue = new SynchronousQueue<>();
    viewClient.setResultListener(new CompileResultListener(queue));
    viewClient.attachToViewProcess(viewDefId, ExecutionOptions.singleCycle(valuationTime, marketDataSpecificationList, flags.get()));
    final MarketDataInfo marketDataInfo = MarketDataInfo.empty();
    final LocalDate valuationDate = ZonedDateTime.ofInstant(valuationTime, ZoneOffset.UTC).toLocalDate();
    try {
      final CompiledViewDefinition compiledViewDefinition = queue.take(); // wait for listener to get called back
      for (final ValueSpecification valueSpecification : compiledViewDefinition.getMarketDataRequirements()) {
        final ComputationTargetType targetType = valueSpecification.getTargetSpecification().getType();
        if (targetType == ComputationTargetType.SECURITY) {
          final Security security = _secSource.get(valueSpecification.getTargetSpecification().getUniqueId());
          final ExternalIdBundle bundle = security.getExternalIdBundle();
          final MarketDataKey marketDataKey = MarketDataKey.of(bundle, DataField.of(valueSpecification.getValueName()));
          // don't care if a point is requested twice
          marketDataInfo.addScalarInfo(marketDataKey, ScalarMarketDataMetaData.INSTANCE);
        } else if (targetType == ComputationTargetType.PRIMITIVE) {
          final ExternalIdBundle bundle = PrimitiveResolver.resolveExternalIds(valueSpecification.getTargetSpecification().getUniqueId(), PrimitiveResolver.SCHEME_PREFIX);
          if (bundle == null) {
            LOGGER.error("Could not resolve {} ignoring", valueSpecification.getTargetSpecification().getUniqueId());
          } else {
            final MarketDataKey marketDataKey = MarketDataKey.of(bundle, DataField.of(valueSpecification.getValueName()));
            marketDataInfo.addScalarInfo(marketDataKey, ScalarMarketDataMetaData.INSTANCE);
          }
        }
      }
      if (compiledViewDefinition instanceof CompiledViewDefinitionWithGraphs) {
        final CompiledViewDefinitionWithGraphs withGraphs = (CompiledViewDefinitionWithGraphs) compiledViewDefinition;
        for (final DependencyGraphExplorer explorer : withGraphs.getDependencyGraphExplorers()) {
          for (int i = 0; i < explorer.getWholeGraph().getRootCount(); i++) {
            final DependencyNode rootNode = explorer.getWholeGraph().getRootNode(i);
            getHistoricalTimeSeriesRequirements(_htsMaster, rootNode, marketDataInfo, valuationDate);
          }
        }
      }
      return marketDataInfo;
    } catch (final InterruptedException ie) {
      throw new OpenGammaRuntimeException("View Compilation interrupted while waiting: " + ie.getMessage());
    }
  }

  /**
   * Crawls the graph looking for nodes referencing one of the value requirement names indicating that a time series has been requested.
   * This is not yet exhaustive - some functions call into the time series source every time execute() is called and should be rewritten.
   * @param htsMaster  the time series master
   * @param node  a graph node
   * @param marketDataInfo  known market data requirements
   * @param valuationDate  the valuation date
   */
  private void getHistoricalTimeSeriesRequirements(final HistoricalTimeSeriesMaster htsMaster, final DependencyNode node, final MarketDataInfo marketDataInfo, final LocalDate valuationDate) {
    if (node.getInputCount() == 0 && node.getTarget().getType() == ComputationTargetType.PRIMITIVE && TIME_SERIES_REQUIREMENT_NAMES.contains(node.getOutputValue(0).getValueName())) {
      final ValueSpecification spec = node.getOutputValue(0);
      final TimeSeriesMarketDataMetaData metaData = createTimeSeriesMetaData(spec.getProperties(), valuationDate);
      final UniqueId tsUid = spec.getTargetSpecification().getUniqueId();
      try {
        final HistoricalTimeSeriesInfoDocument tsInfoDocument = htsMaster.get(tsUid);
        if (tsInfoDocument != null) {
          final ManageableHistoricalTimeSeriesInfo info = tsInfoDocument.getInfo();
          final ExternalIdBundle bundle = info.getExternalIdBundle().toBundle();
          final MarketDataKey key = MarketDataKey.builder().externalIdBundle(bundle).field(DataField.of(info.getDataField())).source(DataSource.of(info.getDataSource()))
              .provider(DataProvider.of(info.getDataProvider())).build();
          marketDataInfo.addTimeSeriesInfo(key, metaData);
          return;
        }
      } catch (final DataNotFoundException e) {
      }
      // if the code reaches here, could not get time series from database
      if (_htsResolver instanceof AlwaysAvailableHistoricalTimeSeriesResolver) {
        final AlwaysAvailableHistoricalTimeSeriesResolver resolver = (AlwaysAvailableHistoricalTimeSeriesResolver) _htsResolver;
        final ManageableHistoricalTimeSeriesInfo info = resolver.getMissingTimeSeriesInfoForUniqueId(tsUid);
        final ExternalIdBundle bundle = info.getExternalIdBundle().toBundle();
        final DataSource source = info.getDataSource() == null ? DataSource.DEFAULT : DataSource.of(info.getDataSource());
        final DataProvider provider = info.getDataProvider() == null ? DataProvider.DEFAULT : DataProvider.of(info.getDataProvider());
        final MarketDataKey key = MarketDataKey.builder().externalIdBundle(bundle).field(DataField.of(info.getDataField())).source(source)
            .provider(provider).build();
        marketDataInfo.addTimeSeriesInfo(key, metaData);
        return;
      }
      LOGGER.warn("Time series with id {} not available from master and could not get ExternalIdBundle from UniqueId", tsUid);
    }
    for (int i = 0; i < node.getInputCount(); i++) {
      final DependencyNode child = node.getInputNode(i);
      getHistoricalTimeSeriesRequirements(htsMaster, child, marketDataInfo, valuationDate);
    }
  }

  /**
   * Creates the meta data object from the properties of a time series requirement.
   * @param properties  the properties
   * @param valuationDate  the valuation date
   * @return  the meta data
   */
  private static TimeSeriesMarketDataMetaData createTimeSeriesMetaData(final ValueProperties properties, final LocalDate valuationDate) {
    final TimeSeriesMarketDataMetaData.Builder builder = TimeSeriesMarketDataMetaData.builder();
    builder.type(LocalDateDoubleTimeSeries.class); //TODO is this right
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)) {
      builder.adjust(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY));
    }
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)) {
      builder.ageLimit(properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY));
    }
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)) {
      // note that the date constraint can be -P7D, so the result from the properties must be evaluated to a date
      builder.startDate(DateConstraint.evaluate(valuationDate, properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)));
    }
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY)) {
      final String includeStart = properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
      builder.includeStart(includeStart.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE));
    }
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)) {
      // note that the date constraint can be -P7D, so the result from the properties must be evaluated to a date
      builder.endDate(DateConstraint.evaluate(valuationDate, properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)));
    }
    if (properties.isDefined(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY)) {
      final String includeEnd = properties.getSingleValue(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
      builder.includeEnd(includeEnd.equals(HistoricalTimeSeriesFunctionUtils.YES_VALUE));
    }
    return builder.build();
  }

  /**
   * Checks that the view is available from the source.
   * @param viewName  the view name
   * @return  the unique id of the view
   */
  private UniqueId ensureConfig(final String viewName) {
    final ViewDefinition viewDef = _configSource.getSingle(ViewDefinition.class, viewName, VersionCorrection.LATEST);
    if (viewDef == null) {
      throw new OpenGammaRuntimeException("Could not get view definition called " + viewName + " from config source");
    }
    return viewDef.getUniqueId();
  }
}
