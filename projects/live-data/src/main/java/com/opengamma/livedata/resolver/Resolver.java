/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract base interface for resolvers.
 *
 * @param <A> unresolved object
 * @param <B> resolved object
 */
public interface Resolver<A, B> {

  /**
   * Resolves a specification.
   *
   * @param spec
   *          the specification
   * @return the resolved specification
   */
  B resolve(A spec);

  /**
   * Resolves a collection of specifications.
   *
   * @param specs
   *          the specifications
   * @return a map from specification to resolved specification
   */
  Map<A, B> resolve(Collection<A> specs);
}
