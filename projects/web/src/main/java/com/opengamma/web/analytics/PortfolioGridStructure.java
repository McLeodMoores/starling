/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * The structure of the grid that displays portfolio data and analytics. Contains the column definitions and
 * the portfolio tree structure.
 */
public class PortfolioGridStructure extends MainGridStructure {

  /** Definition of the view driving the grid. */
  private final ViewDefinition _viewDef;
  /** Meta data for exploded child columns, keyed by the specification of the parent column. */
  private final Map<ColumnSpecification, SortedSet<ColumnMeta>> _inlineColumnMeta;
  /** Rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ PortfolioGridStructure(final List<PortfolioGridRow> rows,
      final GridColumnGroup fixedColumns,
      final GridColumnGroups nonFixedColumns,
      final AnalyticsNode rootNode,
      final TargetLookup targetLookup,
      final UnversionedValueMappings valueMappings,
      final ViewDefinition viewDef) {
    this(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, valueMappings, viewDef,
        Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /* package */ PortfolioGridStructure(final List<PortfolioGridRow> rows,
      final GridColumnGroup fixedColumns,
      final GridColumnGroups nonFixedColumns,
      final AnalyticsNode rootNode,
      final TargetLookup targetLookup,
      final UnversionedValueMappings valueMappings,
      final ViewDefinition viewDef,
      final Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    super(fixedColumns, nonFixedColumns, targetLookup, rootNode, valueMappings);
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(inlineColumnMeta, "inlineColumnCounts");
    _inlineColumnMeta = inlineColumnMeta;
    _rows = rows;
    _viewDef = viewDef;
  }

  /* package */ static PortfolioGridStructure create(final Portfolio portfolio, final UnversionedValueMappings valueMappings) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    // TODO these can be empty, not used any more
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    final AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    return new PortfolioGridStructure(rows,
        GridColumnGroup.empty(),
        GridColumnGroups.empty(),
        rootNode,
        targetLookup,
        valueMappings,
        new ViewDefinition("empty", "dummy"));
  }

  /* package */ PortfolioGridStructure withUpdatedRows(final Portfolio portfolio) {
    final AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final GridColumnGroup fixedColumns = buildFixedColumns(rows);
    final TargetLookup targetLookup = new TargetLookup(super.getValueMappings(), rows);
    final List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, targetLookup);
    final GridColumnGroups nonFixedColumns = new GridColumnGroups(analyticsColumns);
    return new PortfolioGridStructure(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup,
        super.getValueMappings(), _viewDef);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(final CompiledViewDefinition compiledViewDef, final Portfolio portfolio) {
    final AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final GridColumnGroup fixedColumns = buildFixedColumns(rows);
    final UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    final ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    final List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(viewDef, targetLookup);
    final GridColumnGroups nonFixedColumns = new GridColumnGroups(analyticsColumns);
    return new PortfolioGridStructure(rows, fixedColumns, nonFixedColumns, rootNode, targetLookup, valueMappings, viewDef);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(final ResultsCache cache) {
    final Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta = Maps.newHashMap();
    for (final GridColumn column : getColumnStructure().getColumns()) {
      final ColumnSpecification colSpec = column.getSpecification();
      if (Inliner.isDisplayableInline(column.getUnderlyingType(), column.getSpecification())) {
        // ordered set of the union of the column metadata for the whole set. need this to figure out how many unique
        // columns are required
        final SortedSet<ColumnMeta> allColumnMeta = Sets.newTreeSet();
        // traverse every result in the column and get the column metadata
        for (final Iterator<Pair<String, ValueSpecification>> it = getTargetLookup().getTargetsForColumn(colSpec); it.hasNext();) {
          final Pair<String, ValueSpecification> target = it.next();
          if (target != null) {
            final ResultsCache.Result result = cache.getResult(target.getFirst(), target.getSecond(), column.getType());
            final Object value = result.getValue();
            allColumnMeta.addAll(Inliner.columnMeta(value));
          }
        }
        if (!allColumnMeta.isEmpty()) {
          inlineColumnMeta.put(colSpec, allColumnMeta);
        }
      }
    }
    // TODO implement equals() and always return a new instance? conceptually a bit neater but less efficient
    if (!inlineColumnMeta.equals(_inlineColumnMeta)) {
      final List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, getTargetLookup(), inlineColumnMeta);
      return new PortfolioGridStructure(_rows,
          buildFixedColumns(_rows),
          new GridColumnGroups(analyticsColumns),
          getRootNode(),
          getTargetLookup(),
          getValueMappings(),
          _viewDef,
          inlineColumnMeta);
    }
    return this;
  }

  /* package */ PortfolioGridStructure withNode(final AnalyticsNode node) {
    return new PortfolioGridStructure(_rows, getFixedColumns(), getNonFixedColumns(), node, getTargetLookup(),
        super.getValueMappings(), _viewDef);
  }

  /* package */ static GridColumnGroup buildFixedColumns(final List<PortfolioGridRow> rows) {
    final GridColumn labelColumn = new GridColumn("Name", "", null, new PortfolioLabelRenderer(rows));
    return new GridColumnGroup("fixed", ImmutableList.of(labelColumn), false);
  }

  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(final ViewDefinition viewDef, final TargetLookup targetLookup) {
    return buildAnalyticsColumns(viewDef, targetLookup, Collections.<ColumnSpecification, SortedSet<ColumnMeta>>emptyMap());
  }

  /**
   * @param viewDef
   *          The view definition
   * @param targetLookup
   *          looks up the target
   * @param inlineColumnMeta
   *          inline column metadata
   * @return Columns for displaying calculated analytics data, one group per
   *         calculation configuration
   */
  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(final ViewDefinition viewDef,
      final TargetLookup targetLookup,
      final Map<ColumnSpecification, SortedSet<ColumnMeta>> inlineColumnMeta) {
    final List<GridColumnGroup> columnGroups = Lists.newArrayList();
    final Set<Triple<String, String, ValueProperties>> columnSpecs = Sets.newHashSet();
    for (final ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      final List<ColumnSpecification> allSpecs = Lists.newArrayList();
      for (final ViewCalculationConfiguration.Column column : calcConfig.getColumns()) {
        allSpecs.add(new ColumnSpecification(calcConfig.getName(),
            column.getValueName(),
            column.getProperties(),
            column.getHeader()));
      }
      for (final Pair<String, ValueProperties> output : calcConfig.getAllPortfolioRequirements()) {
        allSpecs.add(new ColumnSpecification(calcConfig.getName(), output.getFirst(), output.getSecond()));
      }
      for (final MergedOutput output : calcConfig.getMergedOutputs()) {
        final ValueProperties constraints = ValueProperties.with(ValuePropertyNames.NAME, output.getMergedOutputName()).get();
        allSpecs.add(new ColumnSpecification(calcConfig.getName(), ValueRequirementNames.MERGED_OUTPUT, constraints, output.getMergedOutputName()));
      }
      final List<GridColumn> columns = Lists.newArrayList();
      for (final ColumnSpecification columnSpec : allSpecs) {
        final Class<?> columnType = ValueTypes.getTypeForValueName(columnSpec.getValueName());
        // ensure column isn't a duplicate. can't use a set of col specs because we need to treat columns as duplicates
        // even if they have different headers
        if (columnSpecs.add(Triple.of(columnSpec.getCalcConfigName(), columnSpec.getValueName(), columnSpec.getValueProperties()))) {
          final SortedSet<ColumnMeta> meta = inlineColumnMeta.get(columnSpec);
          if (meta == null) { // column can't be inlined
            columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
          } else {
            int inlineIndex = 0;
            for (final ColumnMeta columnMeta : meta) {
              String header;
              if (inlineIndex++ == 0) {
                header = columnSpec.getHeader() + " / " + columnMeta.getHeader();
              } else {
                header = columnMeta.getHeader();
              }
              columns.add(GridColumn.forSpec(header,
                  columnSpec,
                  columnMeta.getType(),
                  columnMeta.getUnderlyingType(),
                  targetLookup,
                  columnMeta.getKey(),
                  inlineIndex));
            }
          }
        }
      }
      if (!columns.isEmpty()) {
        columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns, true));
      }
    }
    return columnGroups;
  }

  /* package */ static List<PortfolioGridRow> buildRows(final Portfolio portfolio) {
    if (portfolio == null) {
      return Collections.emptyList();
    }
    final PortfolioMapperFunction<List<PortfolioGridRow>> targetFn = new PortfolioMapperFunction<List<PortfolioGridRow>>() {

      @Override
      public List<PortfolioGridRow> apply(final PortfolioNode node) {
        final ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
        String nodeName;
        // if the parent ID is null it's the root node
        if (node.getParentNodeId() == null) {
          // the root node is called "Root" which isn't any use for displaying in the UI, use the portfolio name instead
          nodeName = portfolio.getName();
        } else {
          nodeName = node.getName();
        }
        return Lists.newArrayList(new PortfolioGridRow(target, nodeName, node.getUniqueId()));
      }

      @Override
      public List<PortfolioGridRow> apply(final PortfolioNode parentNode, final Position position) {
        final ComputationTargetSpecification nodeSpec = ComputationTargetSpecification.of(parentNode);
        // TODO I don't think toLatest() will do long term. resolution time available on the result model
        final UniqueId positionId = position.getUniqueId();
        final ComputationTargetSpecification target = nodeSpec.containing(ComputationTargetType.POSITION,
            positionId.toLatest());
        final Security security = position.getSecurity();
        final List<PortfolioGridRow> rows = Lists.newArrayList();
        final UniqueId nodeId = parentNode.getUniqueId();
        if (isFungible(position.getSecurity())) {
          rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          for (final Trade trade : position.getTrades()) {
            final String tradeDate = trade.getTradeDate().toString();
            rows.add(new PortfolioGridRow(ComputationTargetSpecification.of(trade),
                tradeDate,
                security.getUniqueId(),
                nodeId,
                positionId,
                trade.getUniqueId()));
          }
        } else {
          final Collection<Trade> trades = position.getTrades();
          if (trades.isEmpty()) {
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          } else {
            // there is never more than one trade on a position in an OTC security
            final UniqueId tradeId = trades.iterator().next().getUniqueId();
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId, tradeId));
          }
        }
        return rows;
      }
    };
    final List<List<PortfolioGridRow>> rows = PortfolioMapper.map(portfolio.getRootNode(), targetFn);
    final Iterable<PortfolioGridRow> flattenedRows = Iterables.concat(rows);
    return Lists.newArrayList(flattenedRows);
  }

  /**
   * @param security A security
   * @return true if the security is fungible, false if OTC
   */
  private static boolean isFungible(final Security security) {
    if (security instanceof FinancialSecurity) {
      return !((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    }
    return false;
  }
}
