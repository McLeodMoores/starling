/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.lookup.SecurityAttribute;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BlotterGridStructure extends PortfolioGridStructure {

  /** Maps the shared blotter columns to properties in the different security types. */
  private final SecurityAttributeMapper _columnMapper;
  /** The view definition that's driving the grid. */
  private final ViewDefinition _viewDef;

  /* package */ BlotterGridStructure(final List<PortfolioGridRow> rows,
                                     final GridColumnGroup fixedColumns,
                                     final GridColumnGroup blotterColumns,
                                     final List<GridColumnGroup> analyticsColumns,
                                     final AnalyticsNode rootNode,
                                     final TargetLookup targetLookup,
                                     final SecurityAttributeMapper columnMapper,
                                     final UnversionedValueMappings valueMappings,
                                     final ViewDefinition viewDef) {
    super(rows, fixedColumns, createGroups(blotterColumns, analyticsColumns), rootNode, targetLookup, valueMappings, viewDef);
    ArgumentChecker.notNull(columnMapper, "columnMapper");
    ArgumentChecker.notNull(viewDef, "viewDef");
    _viewDef = viewDef;
    _columnMapper = columnMapper;
  }

  private static GridColumnGroups createGroups(final GridColumnGroup blotterColumns, final List<GridColumnGroup> analyticsColumns) {
    final List<GridColumnGroup> groups = Lists.newArrayList(blotterColumns);
    groups.addAll(analyticsColumns);
    return new GridColumnGroups(groups);
  }

  /* package */ static BlotterGridStructure create(final Portfolio portfolio, final SecurityAttributeMapper columnMapper) {
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final UnversionedValueMappings valueMappings = new UnversionedValueMappings();
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    return new BlotterGridStructure(rows,
                                    GridColumnGroup.empty(),
                                    GridColumnGroup.empty(),
                                    Collections.<GridColumnGroup>emptyList(),
                                    AnalyticsNode.portfolioRoot(portfolio),
                                    targetLookup,
                                    columnMapper,
                                    valueMappings,
                                    new ViewDefinition("empty", "dummy"));
  }


  private static GridColumnGroup buildBlotterColumns(final SecurityAttributeMapper columnMapper, final List<PortfolioGridRow> rows) {
    final GridColumn quantityColumn = new GridColumn(SecurityAttribute.QUANTITY.getName(), "", Double.class,
                                               new BlotterColumnRenderer(SecurityAttribute.QUANTITY, columnMapper, rows));
    final List<GridColumn> columns = Lists.newArrayList(
        blotterColumn(SecurityAttribute.TYPE, columnMapper, rows),
        blotterColumn(SecurityAttribute.PRODUCT, columnMapper, rows),
        quantityColumn,
        blotterColumn(SecurityAttribute.DIRECTION, columnMapper, rows),
        blotterColumn(SecurityAttribute.START, columnMapper, rows),
        blotterColumn(SecurityAttribute.MATURITY, columnMapper, rows),
        blotterColumn(SecurityAttribute.RATE, columnMapper, rows),
        blotterColumn(SecurityAttribute.INDEX, columnMapper, rows),
        blotterColumn(SecurityAttribute.FREQUENCY, columnMapper, rows),
        blotterColumn(SecurityAttribute.FLOAT_FREQUENCY, columnMapper, rows));
    return new GridColumnGroup("Blotter", columns, false);
  }

  private static GridColumn blotterColumn(final SecurityAttribute column,
                                          final SecurityAttributeMapper columnMappings,
                                          final List<PortfolioGridRow> rows) {
    return new GridColumn(column.getName(), "", String.class, new BlotterColumnRenderer(column, columnMappings, rows));
  }

  // TODO combine with the method below
  @Override
  /* package */ BlotterGridStructure withUpdatedRows(final Portfolio portfolio) {
    final AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final TargetLookup targetLookup = new TargetLookup(getValueMappings(), rows);
    final GridColumnGroup fixedColumns = buildFixedColumns(rows);
    final GridColumnGroup blotterColumns = buildBlotterColumns(_columnMapper, rows);
    final List<GridColumnGroup> analyticsColumns = Collections.emptyList();
    //List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(_viewDef, targetLookup);
    return new BlotterGridStructure(rows, fixedColumns, blotterColumns, analyticsColumns, rootNode, targetLookup,
                                    _columnMapper, getValueMappings(), _viewDef);
  }

  @Override
  /* package */ BlotterGridStructure withUpdatedStructure(final CompiledViewDefinition compiledViewDef, final Portfolio portfolio) {
    final AnalyticsNode rootNode = AnalyticsNode.portfolioRoot(portfolio);
    final List<PortfolioGridRow> rows = buildRows(portfolio);
    final UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    final ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    final List<GridColumnGroup> analyticsColumns = Collections.emptyList();
    //List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(viewDef, targetLookup);
    final GridColumnGroup fixedColumns = buildFixedColumns(rows);
    final GridColumnGroup blotterColumns = buildBlotterColumns(_columnMapper, rows);
    return new BlotterGridStructure(rows, fixedColumns, blotterColumns, analyticsColumns, rootNode, targetLookup,
                                    _columnMapper, valueMappings, viewDef);
  }

  // TODO handle inlining of values into columns
  @Override
  PortfolioGridStructure withUpdatedStructure(final ResultsCache cache) {
    return this;
  }
}
