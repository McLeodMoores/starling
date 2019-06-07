/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * The type of bucketed shift to apply.
 */
public enum BucketedShiftType implements GroovyAliasable {
  /**
   * For a zero shift.
   */
  ZERO("Zero"),
  /**
   * For a forward shift.
   */
  FORWARD("Forward");

  private static final ImmutableList<String> ALIASES;
  static {
    final List<String> result = newArrayList();
    for (final GroovyAliasable value : values()) {
      result.add(value.getGroovyAlias());
    }
    Collections.sort(result);
    ALIASES = ImmutableList.copyOf(result);

  }

  private String _groovyAlias;

  BucketedShiftType(final String groovyAlias) {
    _groovyAlias = groovyAlias;
  }

  /**
   * The alias to use in the groovy script.
   * 
   * @return the alias
   */
  @Override
  public String getGroovyAlias() {
    return _groovyAlias;
  }

  /**
   * The list of available groovy aliases, sorted.
   * 
   * @return list of aliases.
   */
  public static ImmutableList<String> getAliasList() {
    return ALIASES;
  }

}
