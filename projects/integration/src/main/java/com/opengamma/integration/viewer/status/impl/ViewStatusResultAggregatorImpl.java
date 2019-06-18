/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.integration.viewer.status.AggregateType;
import com.opengamma.integration.viewer.status.ViewColumnType;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.integration.viewer.status.ViewStatusKey;
import com.opengamma.integration.viewer.status.ViewStatusModel;
import com.opengamma.integration.viewer.status.ViewStatusResultAggregator;
import com.opengamma.integration.viewer.status.impl.ViewStatusKeyBean.Meta;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link ViewStatusResultAggregator}.
 */
public class ViewStatusResultAggregatorImpl implements ViewStatusResultAggregator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatusResultAggregatorImpl.class);

  /**
   * Header for Security Type.
   */
  public static final String SECURITY_HEADER = "SecurityType";
  /**
   * Header for Value Requirement Name.
   */
  public static final String VALUE_REQUIREMENT_NAME_HEADER = "ValueRequirementName";
  /**
   * Header for Currency.
   */
  public static final String CURRENCY_HEADER = "Currency";
  /**
   * Header for Target type.
   */
  public static final String TARGET_TYPE_HEADER = "Target Type";
  /**
   * Header for status.
   */
  public static final String STATUS = "Status";

  private final Map<ViewStatusKey, ViewStatus> _viewStatusResult = Maps.newConcurrentMap();

  private static final Map<MetaProperty<?>, String> HEADERS = Maps.newHashMap();
  static {
    final Meta statusKeyMeta = ViewStatusKeyBean.meta();
    HEADERS.put(statusKeyMeta.securityType(), SECURITY_HEADER);
    HEADERS.put(statusKeyMeta.valueRequirementName(), VALUE_REQUIREMENT_NAME_HEADER);
    HEADERS.put(statusKeyMeta.currency(), CURRENCY_HEADER);
    HEADERS.put(statusKeyMeta.targetType(), TARGET_TYPE_HEADER);
  }

  private static final String[] DEFAULT_HEADERS = { TARGET_TYPE_HEADER, SECURITY_HEADER, VALUE_REQUIREMENT_NAME_HEADER, CURRENCY_HEADER, STATUS };

  private static final String EMPTY_STR = StringUtils.EMPTY;

  @Override
  public ViewStatusModel aggregate(final AggregateType aggregateType) {
    ArgumentChecker.notNull(aggregateType, "aggregateType");
    if (aggregateType == AggregateType.NO_AGGREGATION) {
      return defaultModel();
    }
    return aggregate(aggregateType.getColumnTypes());
  }

  private ViewStatusModel aggregate(final List<ViewColumnType> columnTypes) {
    ArgumentChecker.notNull(columnTypes, "aggregations");

    if (columnTypes.isEmpty()) {
      return defaultModel();
    }

    final Iterable<ViewColumnType> fixedColumnTypes = Iterables.limit(columnTypes, columnTypes.size() - 1);
    final Map<List<String>, Set<String>> fixedRow2Columns = Maps.newHashMap();

    for (final ViewStatusKey viewStatusKey : _viewStatusResult.keySet()) {
      final ViewStatusKeyBean viewStatusKeyBean = new ViewStatusKeyBean(viewStatusKey.getSecurityType(), viewStatusKey.getValueRequirementName(),
          viewStatusKey.getCurrency(), viewStatusKey.getTargetType());
      final List<String> key = viewStatusKey(viewStatusKeyBean, fixedColumnTypes);

      Set<String> columnValues = fixedRow2Columns.get(key);
      if (columnValues == null) {
        columnValues = Sets.newHashSet();
        fixedRow2Columns.put(key, columnValues);
      }
      columnValues.addAll(viewStatusKey(viewStatusKeyBean, Collections.singletonList(Iterables.getLast(columnTypes))));
    }

    final Set<String> extraColumns = getExtraColumns(fixedRow2Columns);

    final List<List<String>> columnHeaders = makeHeaders(columnTypes, extraColumns);
    final List<List<Object>> rowData = createRowData(fixedRow2Columns, extraColumns, columnTypes);

    return new SimpleViewStatusModel(columnHeaders, rowData, _viewStatusResult);
  }

  private List<List<Object>> createRowData(final Map<List<String>, Set<String>> fixedRow2Columns, final Set<String> extraColumns,
      final List<ViewColumnType> columnTypes) {

    final List<List<String>> rows = Lists.newArrayList(fixedRow2Columns.keySet());
    final Comparator<List<String>> rowComparator = new Comparator<List<String>>() {

      @Override
      public int compare(final List<String> left, final List<String> right) {
        int compare = 0;
        for (int i = 0; i < left.size(); i++) {
          compare = left.get(i).compareTo(right.get(i));
          if (compare != 0) {
            return compare;
          }
        }
        return compare;
      }
    };

    Collections.sort(rows, rowComparator);

    final List<List<Object>> processedRows = Lists.newArrayListWithCapacity(rows.size());

    final String[] currentRow = new String[Iterables.getFirst(rows, Lists.newArrayList()).size()];
    for (final List<String> row : rows) {
      final List<Object> processedRow = Lists.newArrayList();
      final Iterable<String> columns = Iterables.limit(row, row.size() - 1);
      int count = 0;
      for (final String col : columns) {
        if (currentRow[count] == null || !col.equals(currentRow[count])) {
          currentRow[count] = col;
          processedRow.add(col);
        } else {
          processedRow.add(EMPTY_STR);
        }
        count++;
      }
      processedRow.add(Iterables.getLast(row));

      for (final String col : extraColumns) {
        final List<String> keyMemebers = Lists.newArrayList(row);
        keyMemebers.add(col);
        final ViewStatus status = getStatus(keyFromRowValues(keyMemebers, columnTypes));
        if (status == null) {
          processedRow.add(EMPTY_STR);
        } else {
          processedRow.add(status);
        }
      }
      processedRows.add(processedRow);
    }
    return processedRows;
  }

  private ViewStatusKey keyFromRowValues(final List<String> keyValues, final List<ViewColumnType> columnTypes) {

    final Meta keyMeta = ViewStatusKeyBean.meta();
    final BeanBuilder<? extends ViewStatusKeyBean> beanBuilder = keyMeta.builder();
    final Iterator<String> keyItr = keyValues.iterator();
    final Iterator<ViewColumnType> colTypeItr = columnTypes.iterator();

    while (keyItr.hasNext() && colTypeItr.hasNext()) {
      beanBuilder.set(colTypeItr.next().getMetaProperty(), keyItr.next());
    }
    final ViewStatusKeyBean result = beanBuilder.build();
    LOGGER.debug("{} built from properties: {} and types: {}", result, keyValues, columnTypes);
    return result;
  }

  private Set<String> getExtraColumns(final Map<List<String>, Set<String>> fixedRow2Columns) {
    final Set<String> extraColumns = Sets.newTreeSet();
    Iterables.addAll(extraColumns, Iterables.concat(fixedRow2Columns.values()));
    return extraColumns;
  }

  private List<List<String>> makeHeaders(final List<ViewColumnType> columnTypes, final Set<String> extraColumns) {
    final List<List<String>> headers = Lists.newArrayListWithCapacity(2);
    final int colSize = columnTypes.size() + extraColumns.size() - 1;
    headers.add(topColumnHeaders(columnTypes, colSize));
    headers.add(subColumnHeaders(extraColumns, colSize));
    return headers;
  }

  private List<String> viewStatusKey(final ViewStatusKeyBean viewStatusKeyBean, final Iterable<ViewColumnType> fixedColumnTypes) {

    final List<String> result = Lists.newArrayList();
    for (final ViewColumnType keyType : fixedColumnTypes) {
      final MetaProperty<String> metaProperty = keyType.getMetaProperty();

      if (ViewStatusKeyBean.meta().currency().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getCurrency());
      } else if (ViewStatusKeyBean.meta().securityType().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getSecurityType());
      } else if (ViewStatusKeyBean.meta().targetType().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getTargetType());
      } else if (ViewStatusKeyBean.meta().valueRequirementName().equals(metaProperty)) {
        result.add(viewStatusKeyBean.getValueRequirementName());
      }
    }
    return ImmutableList.copyOf(result);
  }

  private List<String> subColumnHeaders(final Set<String> extraColumnHeaders, final int colsize) {
    final List<String> subHeader = Lists.newArrayListWithCapacity(colsize);
    final int emptySize = colsize - extraColumnHeaders.size();
    for (int i = 0; i < emptySize; i++) {
      subHeader.add(StringUtils.EMPTY);
    }
    Iterables.addAll(subHeader, extraColumnHeaders);
    return subHeader;
  }

  private List<String> topColumnHeaders(final List<ViewColumnType> columnTypes, final int colsize) {
    final List<String> topHeader = Lists.newArrayListWithCapacity(colsize);
    for (final ViewColumnType columnType : columnTypes) {
      topHeader.add(HEADERS.get(columnType.getMetaProperty()));
    }
    final int emptySize = colsize - columnTypes.size();
    for (int i = 0; i < emptySize; i++) {
      topHeader.add(StringUtils.EMPTY);
    }
    return topHeader;
  }

  @Override
  public void putStatus(final ViewStatusKey key, final ViewStatus status) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(status, "status");

    _viewStatusResult.put(ImmutableViewStatusKey.of(key), status);
  }

  @Override
  public ViewStatus getStatus(final ViewStatusKey key) {
    if (key == null) {
      return null;
    }
    return _viewStatusResult.get(ImmutableViewStatusKey.of(key));
  }

  @Override
  public Set<ViewStatusKey> keySet() {
    return Sets.newHashSet(_viewStatusResult.keySet());
  }

  private ViewStatusModel defaultModel() {
    final List<List<String>> columnHeaders = Lists.newArrayListWithCapacity(1);
    columnHeaders.add(Arrays.asList(DEFAULT_HEADERS));

    final List<List<Object>> rowData = Lists.newArrayListWithCapacity(_viewStatusResult.size());
    for (final ViewStatusKey key : _viewStatusResult.keySet()) {
      final List<Object> row = Lists.newArrayList();
      final ViewStatus status = _viewStatusResult.get(key);

      row.add(key.getTargetType());
      row.add(key.getSecurityType());
      row.add(key.getValueRequirementName());
      row.add(key.getCurrency());
      row.add(status.getValue());
      rowData.add(row);
    }
    return new SimpleViewStatusModel(columnHeaders, rowData, _viewStatusResult);
  }

  /**
   * Immutable key into view status result map.
   */
  static class ImmutableViewStatusKey implements ViewStatusKey {

    private final String _securityType;

    private final String _valueName;

    private final String _currency;

    private final String _targetType;

    ImmutableViewStatusKey(final String securityType, final String valueName, final String currency, final String targetType) {
      ArgumentChecker.notNull(securityType, "securityType");
      ArgumentChecker.notNull(valueName, "valueName");
      ArgumentChecker.notNull(currency, "currency");
      ArgumentChecker.notNull(targetType, "targetType");

      _securityType = securityType;
      _valueName = valueName;
      _currency = currency;
      _targetType = targetType;
    }

    @Override
    public String getSecurityType() {
      return _securityType;
    }

    @Override
    public String getValueRequirementName() {
      return _valueName;
    }

    @Override
    public String getCurrency() {
      return _currency;
    }

    @Override
    public String getTargetType() {
      return _targetType;
    }

    public static ImmutableViewStatusKey of(final ViewStatusKey key) {
      ArgumentChecker.notNull(key, "key");
      return new ImmutableViewStatusKey(key.getSecurityType(), key.getValueRequirementName(), key.getCurrency(), key.getTargetType());
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

}
