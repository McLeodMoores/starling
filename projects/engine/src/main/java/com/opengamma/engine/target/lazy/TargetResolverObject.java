/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.ComputationTargetResolver;

/**
 * Base class for target resolver based object.
 */
public class TargetResolverObject implements Serializable {

  private static final AtomicInteger NEXT_IDENTIFIER = new AtomicInteger();
  private static final ConcurrentMap<ComputationTargetResolver.AtVersionCorrection, Integer> RESOLVER_TO_IDENTIFIER = new MapMaker().weakKeys().makeMap();
  private static final ConcurrentMap<Integer, ComputationTargetResolver.AtVersionCorrection> IDENTIFIER_TO_RESOLVER = new MapMaker().weakValues().makeMap();

  private transient ComputationTargetResolver.AtVersionCorrection _targetResolver;

  protected TargetResolverObject(final ComputationTargetResolver.AtVersionCorrection targetResolver) {
    _targetResolver = targetResolver;
  }

  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return _targetResolver;
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    Integer id = RESOLVER_TO_IDENTIFIER.get(getTargetResolver());
    if (id == null) {
      id = NEXT_IDENTIFIER.getAndIncrement();
      IDENTIFIER_TO_RESOLVER.put(id, getTargetResolver());
      final Integer existing = RESOLVER_TO_IDENTIFIER.putIfAbsent(getTargetResolver(), id);
      if (existing != null) {
        IDENTIFIER_TO_RESOLVER.remove(id);
        id = existing;
      }
    }
    out.writeInt(id.intValue());
  }

  private void readObject(final ObjectInputStream in) throws IOException {
    final int targetResolver = in.readInt();
    _targetResolver = IDENTIFIER_TO_RESOLVER.get(targetResolver);
    assert _targetResolver != null;
  }

}
