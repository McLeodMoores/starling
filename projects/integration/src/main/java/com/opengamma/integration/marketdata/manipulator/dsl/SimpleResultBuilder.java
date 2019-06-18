/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Builds {@link SimpleResultModel} instances from results calculated by the engine.
 */
public class SimpleResultBuilder {

  private final Map<ObjectId, Integer> _idToIndex;
  private final Map<ColumnSpec, Integer> _colToIndex;
  private final CompiledViewDefinition _compiledViewDef;
  private final List<String> _columnNames;
  private final List<UniqueIdentifiable> _targets;

  public SimpleResultBuilder(final CompiledViewDefinition compiledViewDef) {
    _compiledViewDef = ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");

    final PortfolioMapperFunction<List<UniqueIdentifiable>> mapperFn = new PortfolioMapperFunction<List<UniqueIdentifiable>>() {
      @Override
      public List<UniqueIdentifiable> apply(final PortfolioNode node) {
        return Collections.<UniqueIdentifiable> singletonList(node);
      }

      @Override
      public List<UniqueIdentifiable> apply(final PortfolioNode parent, final Position position) {
        final List<UniqueIdentifiable> targets = Lists.<UniqueIdentifiable> newArrayList(position);
        for (final Trade trade : position.getTrades()) {
          targets.add(trade);
        }
        return targets;
      }
    };
    final List<UniqueIdentifiable> targets = PortfolioMapper.flatMap(compiledViewDef.getPortfolio().getRootNode(), mapperFn);
    _idToIndex = Maps.newHashMapWithExpectedSize(targets.size());
    int rowIndex = 0;
    for (final UniqueIdentifiable target : targets) {
      _idToIndex.put(target.getUniqueId().getObjectId(), rowIndex++);
    }

    // ---------------------------------------------------

    final Collection<ViewCalculationConfiguration> calcConfigs = compiledViewDef.getViewDefinition().getAllCalculationConfigurations();
    final Set<ColumnSpec> columns = Sets.newLinkedHashSet();
    for (final ViewCalculationConfiguration calcConfig : calcConfigs) {
      for (final ViewCalculationConfiguration.Column column : calcConfig.getColumns()) {
        columns.add(new ColumnSpec(calcConfig.getName(), column.getValueName(), column.getProperties(), column.getHeader()));
      }
      for (final Pair<String, ValueProperties> output : calcConfig.getAllPortfolioRequirements()) {
        String header;
        if (calcConfigs.size() == 1) {
          // if there's only 1 calc config then use the value name as the column header
          header = output.getFirst();
        } else {
          // if there are multiple calc configs need to include the calc config name
          header = calcConfig.getName() + "/" + output.getFirst();
        }
        columns.add(new ColumnSpec(calcConfig.getName(), output.getFirst(), output.getSecond(), header));
      }
    }
    _colToIndex = Maps.newHashMapWithExpectedSize(columns.size());
    final List<String> columnNames = Lists.newArrayListWithCapacity(columns.size());
    int colIndex = 0;
    for (final ColumnSpec column : columns) {
      _colToIndex.put(column, colIndex++);
      columnNames.add(column._header);
    }
    _targets = Collections.unmodifiableList(targets);
    _columnNames = Collections.unmodifiableList(columnNames);
  }

  /**
   * Builds a {@link ScenarioResultModel} from the data calculated in a single cycle.
   *
   * @param resultModel
   *          The results calculated by the engine in a single calculation cycle
   * @return A simple result model built from the results
   */
  public SimpleResultModel build(final ViewResultModel resultModel) {
    return build(resultModel, _columnNames);
  }

  /**
   * Builds a {@link ScenarioResultModel} from the data calculated in a single cycle.
   *
   * @param resultModel
   *          the results calculated by the engine in a single calculation cycle
   * @param columnNames
   *          column name overrides
   * @return A simple result model built from the results
   * @throws IllegalArgumentException
   *           if the number of column names doesn't match the number of columns
   */
  public SimpleResultModel build(final ViewResultModel resultModel, final List<String> columnNames) {
    ArgumentChecker.notNull(columnNames, "columnNames");
    ArgumentChecker.notNull(resultModel, "resultModel");

    if (columnNames.size() != _columnNames.size()) {
      throw new IllegalArgumentException("Wrong number of column names. expected: " + _columnNames.size()
          + ", actual: " + columnNames.size());
    }
    final int rowCount = _idToIndex.size();
    final int colCount = columnNames.size();
    final ContiguousSet<Integer> rowIndices = ContiguousSet.create(Range.closedOpen(0, rowCount), DiscreteDomain.integers());
    final ContiguousSet<Integer> colIndices = ContiguousSet.create(Range.closedOpen(0, colCount), DiscreteDomain.integers());
    final Table<Integer, Integer, Object> table = ArrayTable.create(rowIndices, colIndices);

    for (final ViewResultEntry entry : resultModel.getAllResults()) {
      final String calcConfigName = entry.getCalculationConfiguration();
      final ComputedValueResult value = entry.getComputedValue();
      final ValueSpecification valueSpec = value.getSpecification();
      final CompiledViewCalculationConfiguration calcConfig = _compiledViewDef.getCompiledCalculationConfiguration(calcConfigName);
      final Set<ValueRequirement> valueReqs = calcConfig.getTerminalOutputSpecifications().get(valueSpec);

      for (final ValueRequirement valueReq : valueReqs) {
        final ColumnSpec colSpec = new ColumnSpec(calcConfigName, valueReq.getValueName(), valueReq.getConstraints());
        final Integer colIndex = _colToIndex.get(colSpec);
        final Integer rowIndex = _idToIndex.get(valueReq.getTargetReference().getSpecification().getUniqueId().getObjectId());

        // there won't be a row or column index for specific outputs (e.g. curves)
        if (colIndex != null && rowIndex != null) {
          table.put(rowIndex, colIndex, value);
        }
        // TODO handle specific outputs
      }
    }
    return new SimpleResultModel(_targets, columnNames, table, resultModel.getViewCycleExecutionOptions());
  }

  private static final class ColumnSpec {

    /** Name of the calculation configuration that produces the column data. */
    private final String _calcConfigName;
    /** Value name of the column's data. */
    private final String _valueName;
    /** Value properties used when calculating the column's data. */
    private final ValueProperties _valueProperties;
    /** Column header. */
    private final String _header;

    private ColumnSpec(final String calcConfigName, final String valueName, final ValueProperties valueProperties, final String header) {
      _calcConfigName = calcConfigName;
      _valueName = valueName;
      _valueProperties = valueProperties;
      _header = header;
    }

    ColumnSpec(final String calcConfigName, final String valueName, final ValueProperties properties) {
      this(calcConfigName, valueName, properties, null);
    }

    // header is deliberately ignored for the purposes of equals and hashCode
    @Override
    public int hashCode() {
      return Objects.hash(_calcConfigName, _valueName, _valueProperties);
    }

    // header is deliberately ignored for the purposes of equals and hashCode
    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final ColumnSpec other = (ColumnSpec) obj;
      return Objects.equals(this._calcConfigName, other._calcConfigName)
          && Objects.equals(this._valueName, other._valueName)
          && Objects.equals(this._valueProperties, other._valueProperties);
    }
  }
}
