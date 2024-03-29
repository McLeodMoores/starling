/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of how the view columns should be displayed
 *
 * <p>
 * Create a type from any combination of valid chars. ValidChars are 'T', 'S', 'V', 'C'
 *
 * <pre>
 * e.g AggregateType [TSVC]  will be interpreted as
 *
 *  1st column = Target Type
 *  2nd column = Security Type
 *  3rd column = ValueRequirement
 *  4th column = Currency
 * </pre>
 */
public final class AggregateType {

  private static final List<Character> VALID_AGGRAGATION_CHARS = ImmutableList.of('T', 'S', 'V', 'C');

  /**
   * Represents NO_AGGREGATION.
   */
  public static final AggregateType NO_AGGREGATION = new AggregateType(Collections.<ViewColumnType> emptyList());

  private final List<ViewColumnType> _columnTypes;

  private AggregateType(final List<ViewColumnType> aggregationTypes) {
    ArgumentChecker.notNull(aggregationTypes, "aggregationTypes");

    _columnTypes = ImmutableList.copyOf(aggregationTypes);
  }

  public static AggregateType of(final String aggregateStrType) {
    ArgumentChecker.notNull(aggregateStrType, "aggregateStrType");

    validateAggregateType(aggregateStrType.toUpperCase());

    final List<ViewColumnType> types = Lists.newArrayListWithCapacity(VALID_AGGRAGATION_CHARS.size());
    final char[] chars = aggregateStrType.toCharArray();
    for (final char character : chars) {
      final String shortName = String.valueOf(character);
      final ViewColumnType type = ViewColumnType.of(shortName);
      if (type == null) {
        throw new IllegalArgumentException("Unsupported aggregate type: " + shortName);
      }
      types.add(type);
    }
    return new AggregateType(types);
  }

  private static void validateAggregateType(final String aggregateStrType) {
    if (aggregateStrType.length() != VALID_AGGRAGATION_CHARS.size() || hasDepulicateChar(aggregateStrType) || hasInvalidCharacter(aggregateStrType)) {
      throw new IllegalArgumentException("Invalid aggregate type: " + aggregateStrType);
    }
  }

  private static boolean hasInvalidCharacter(final String aggregationOptionType) {
    final char[] aggregateTypeChars = aggregationOptionType.toCharArray();
    for (final char character : aggregateTypeChars) {
      if (!VALID_AGGRAGATION_CHARS.contains(character)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasDepulicateChar(final String aggregateStrType) {
    final char[] aggregateTypeChars = aggregateStrType.toCharArray();
    final Set<Character> uniqueChars = Sets.newHashSet();
    for (final Character character : aggregateTypeChars) {
      uniqueChars.add(character);
    }
    return uniqueChars.size() != VALID_AGGRAGATION_CHARS.size();
  }

  /**
   * Gets the list of view column types.
   *
   * @return the aggregationTypeList
   */
  public List<ViewColumnType> getColumnTypes() {
    return _columnTypes;
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
    final StringBuilder buf = new StringBuilder();
    for (final ViewColumnType columnType : _columnTypes) {
      buf.append(columnType.getShortName());
    }
    return "AggregateType [" + buf + "]";
  }

}
