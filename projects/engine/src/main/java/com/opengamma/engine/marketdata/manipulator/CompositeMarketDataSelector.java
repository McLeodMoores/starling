/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A market data shift implementation that allows a set of individual market data shifts
 * to be bundled together.
 */
public final class CompositeMarketDataSelector implements MarketDataSelector {

  /** Field name for Fudge message */
  private static final String SELECTORS = "selectors";
  /**
   * The underlying shift specifications.
   */
  private final Set<MarketDataSelector> _underlyingSelectors;

  private CompositeMarketDataSelector(final Set<MarketDataSelector> underlyingSelectors) {
    ArgumentChecker.notNull(underlyingSelectors, "underlyingSpecifications");
    _underlyingSelectors = underlyingSelectors;
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataSelector of(final MarketDataSelector... specifications) {
    return new CompositeMarketDataSelector(ImmutableSet.copyOf(specifications));
  }

  /**
   * Create a composite specification for the specified underlying specifiations.
   *
   * @param specifications the specifications to be combined, neither null nor empty
   * @return a specification combined all the underlying specifications, not null
   */
  public static MarketDataSelector of(final Set<? extends MarketDataSelector> specifications) {
    return new CompositeMarketDataSelector(ImmutableSet.copyOf(specifications));
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(final ValueSpecification valueSpecification,
                                                         final String calculationConfigurationName,
                                                         final SelectorResolver resolver) {

    for (final MarketDataSelector selector : _underlyingSelectors) {
      final DistinctMarketDataSelector matchingSelector =
          selector.findMatchingSelector(valueSpecification, calculationConfigurationName, resolver);
      if (matchingSelector != null) {
        return matchingSelector;
      }
    }
    return null;
  }

  @Override
  public boolean hasSelectionsDefined() {

    for (final MarketDataSelector specification : _underlyingSelectors) {
      if (specification.hasSelectionsDefined()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final CompositeMarketDataSelector that = (CompositeMarketDataSelector) o;
    return _underlyingSelectors.equals(that._underlyingSelectors);
  }

  @Override
  public int hashCode() {
    return _underlyingSelectors.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final MarketDataSelector selector : _underlyingSelectors) {
      serializer.addToMessageWithClassHeaders(msg, SELECTORS, null, selector);
    }
    return msg;
  }

  public static MarketDataSelector fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final Set<MarketDataSelector> selectors = Sets.newHashSet();
    for (final FudgeField field : msg.getAllByName(SELECTORS)) {
      final MarketDataSelector selector = deserializer.fieldValueToObject(MarketDataSelector.class, field);
      selectors.add(selector);
    }
    return of(selectors);
  }
}
