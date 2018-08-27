/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.threeten.bp.Instant;

import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * In-memory storage of function costs.
 * <p>
 * This implementation does not support history.
 */
public class InMemoryFunctionCostsMaster implements FunctionCostsMaster {

  /**
   * The store of documents.
   */
  private final Map<Pair<String, String>, FunctionCostsDocument> _data = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   */
  public InMemoryFunctionCostsMaster() {
  }

  //-------------------------------------------------------------------------
  @Override
  public FunctionCostsDocument load(final String configuration, final String functionId, final Instant versionAsOf) {
    final Pair<String, String> pair = Pairs.of(configuration, functionId);
    final FunctionCostsDocument doc = _data.get(pair);
    return doc != null ? doc.clone() : null;
  }

  @Override
  public FunctionCostsDocument store(final FunctionCostsDocument costs) {
    final Pair<String, String> pair = Pairs.of(costs.getConfigurationName(), costs.getFunctionId());
    costs.setVersion(Instant.now());
    _data.put(pair, costs.clone());
    return costs;
  }

  /**
   * Gets the number of stored documents, used in testing.
   *
   * @return the size of the master
   */
  public int size() {
    return _data.size();
  }

}
