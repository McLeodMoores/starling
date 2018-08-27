/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 *
 */
public final class PrimitivesGridStructure extends MainGridStructure {

  /** Target type for anything that isn't part of a portoflio structure. */
  private static final ComputationTargetType NON_PRIMITIVE =
      ComputationTargetType.PORTFOLIO_NODE
          .or(ComputationTargetType.POSITION)
          .or(ComputationTargetType.TRADE)
          .or(ComputationTargetType.SECURITY);

  /** Creates names for the label column in the grid. */
  private static final ComputationTargetReferenceVisitor<String> NAME_VISITOR = new ComputationTargetReferenceVisitor<String>() {

    @Override
    public String visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      return StringUtils.join(requirement.getIdentifiers().iterator(), ", ");
    }

    @Override
    public String visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      if (specification.getUniqueId() != null) {
        return specification.getUniqueId().toString();
      } else {
        return "No target";
      }
    }
  };

  private PrimitivesGridStructure() {
  }

  private PrimitivesGridStructure(final GridColumnGroup fixedColumns,
                                  final GridColumnGroups nonFixedColumns,
                                  final TargetLookup targetLookup,
                                  final UnversionedValueMappings valueMappings) {
    super(fixedColumns, nonFixedColumns, targetLookup, valueMappings);
  }

  /* package */ static PrimitivesGridStructure create(final CompiledViewDefinition compiledViewDef) {
    final List<MainGridStructure.Row> rows = rows(compiledViewDef);
    final GridColumn labelColumn = new GridColumn("Name", "", String.class, new PrimitivesLabelRenderer(rows));
    final GridColumnGroup fixedColumns = new GridColumnGroup("fixed", ImmutableList.of(labelColumn), false);
    final UnversionedValueMappings valueMappings = new UnversionedValueMappings(compiledViewDef);
    final TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    final List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    return new PrimitivesGridStructure(fixedColumns, new GridColumnGroups(analyticsColumns), targetLookup, valueMappings);
  }

  private static List<GridColumnGroup> buildAnalyticsColumns(final ViewDefinition viewDef, final TargetLookup targetLookup) {
    final List<GridColumnGroup> columnGroups = Lists.newArrayList();
    final Set<ColumnSpecification> columnSpecs = Sets.newHashSet();
    for (final ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      final List<GridColumn> columns = Lists.newArrayList();
      for (final ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!specificRequirement.getTargetReference().getType().isTargetType(NON_PRIMITIVE)) {
          final String valueName = specificRequirement.getValueName();
          final Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          final ValueProperties constraints = specificRequirement.getConstraints();
          final ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
          // ensure columnSpec isn't a duplicate
          if (columnSpecs.add(columnSpec)) {
            columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
          }
        }
      }
      if (!columns.isEmpty()) {
        columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns, true));
      }
    }
    return columnGroups;
  }

  private static List<MainGridStructure.Row> rows(final CompiledViewDefinition compiledViewDef) {
    final Set<ComputationTargetReference> targetRefs = Sets.newLinkedHashSet();
    for (final ViewCalculationConfiguration calcConfig : compiledViewDef.getViewDefinition().getAllCalculationConfigurations()) {
      for (final ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        targetRefs.add(specificRequirement.getTargetReference());
      }
    }
    final List<MainGridStructure.Row> rows = Lists.newArrayList();
    for (final ComputationTargetReference targetRef : targetRefs) {
      rows.add(new Row(targetRef, targetRef.accept(NAME_VISITOR)));
    }
    return rows;
  }

  /* package */ static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }
}
