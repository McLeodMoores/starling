/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.function.BiFunction;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DelegatingComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * A target resolver that does not resolve the targets immediately but returns a deferred handle. This is excellent for consumers of the target that only care
 * about it's unique identifier and don't need the resolution but can obtain it if they do.
 */
public final class LazyComputationTargetResolver extends DelegatingComputationTargetResolver {

  private static final ComputationTargetTypeMap<BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>> RESOLVERS;

  static {
    RESOLVERS = new ComputationTargetTypeMap<>();
    RESOLVERS.put(ComputationTargetType.PORTFOLIO_NODE,
        (underlying, specification) -> new LazyTargetResolverPortfolioNode(underlying, specification));
    RESOLVERS.put(ComputationTargetType.POSITION,
        (underlying, specification) -> new LazyTargetResolverPosition(underlying, specification));
    RESOLVERS.put(ComputationTargetType.TRADE,
        (underlying, specification) -> new LazyTargetResolverTrade(underlying, specification));
  }

  public LazyComputationTargetResolver(final ComputationTargetResolver underlying) {
    super(underlying);
  }

  /**
   * If the specification is lazily resolvable, returns a target that will resolve it on demand. Otherwise it is resolved immediately.
   *
   * @param underlying
   *          the underlying resolver to use for resolution
   * @param specification
   *          the specification to resolve
   * @return the target
   */
  public static ComputationTarget resolve(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecification specification) {
    final BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable> resolver = RESOLVERS
        .get(specification.getType());
    if (resolver != null) {
      final UniqueIdentifiable lazy = resolver.apply(underlying, specification);
      if (specification.getUniqueId().isVersioned()) {
        return new ComputationTarget(specification, lazy);
      }
      return new ComputationTarget(specification.replaceIdentifier(lazy.getUniqueId()), lazy);
    }
    return underlying.resolve(specification);
  }

  public static ComputationTarget resolve(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification,
      final VersionCorrection versionCorrection) {
    final BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable> resolver = RESOLVERS
        .get(specification.getType());
    if (resolver != null) {
      final UniqueIdentifiable lazy = resolver.apply(underlying.atVersionCorrection(versionCorrection), specification);
      if (specification.getUniqueId().isVersioned()) {
        return new ComputationTarget(specification, lazy);
      }
      return new ComputationTarget(specification.replaceIdentifier(lazy.getUniqueId()), lazy);
    }
    return underlying.resolve(specification, versionCorrection);
  }

  /**
   * Tests if the specification can be lazily resolved by a call to {@link #resolve}.
   *
   * @param specification
   *          the specification to test
   * @return true if lazy resolution will happen, false if the underlying will be queried immediately
   */
  public static boolean isLazilyResolvable(final ComputationTargetSpecification specification) {
    return RESOLVERS.get(specification.getType()) != null;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    return resolve(getUnderlying(), specification, versionCorrection);
  }

}
