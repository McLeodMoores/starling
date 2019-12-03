/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.core.position.PortfolioNode;

/**
 * Contains utilities to transform the nodes and positions in a portfolio using a {@code PortfolioMapperFunction}.
 */
public final class PortfolioMapper {

  private PortfolioMapper() {
  }

  //-------------------------------------------------------------------------
  public static <T> List<T> map(final PortfolioNode node, final PortfolioMapperFunction<T> fn) {
    final MappingCallback<T, List<T>> callback = new MappingCallback<T, List<T>>(fn, new ArrayList<T>());
    return getValues(node, callback);
  }

  public static <T> List<T> flatMap(final PortfolioNode node, final PortfolioMapperFunction<List<T>> fn) {
    final MappingCallback<List<T>, List<List<T>>> callback =
        new MappingCallback<List<T>, List<List<T>>>(fn, new ArrayList<List<T>>());
    final List<List<T>> values = getValues(node, callback);
    return Lists.newArrayList(Iterables.concat(values));
  }

  public static <T> Set<T> mapToSet(final PortfolioNode node, final PortfolioMapperFunction<T> fn) {
    final MappingCallback<T, Set<T>> callback = new MappingCallback<T, Set<T>>(fn, new HashSet<T>());
    return getValues(node, callback);
  }

  //-------------------------------------------------------------------------
  private static <T, V extends Collection<T>> V getValues(final PortfolioNode node, final MappingCallback<T, V> callback) {
    final PortfolioNodeTraverser traverser = PortfolioNodeTraverser.depthFirst(callback);
    traverser.traverse(node);
    return callback.getValues();
  }

}
