/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * In-memory implementation of a {@link MarketDataInjector}.
 */
public class MarketDataInjectorImpl implements MarketDataInjector {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataInjectorImpl.class);

  /**
   * A snapshot of the state of the injection.
   */
  public final class Snapshot {

    private final MarketDataAvailabilityProvider _availability;
    private final Map<ValueRequirement, Object> _valuesByRequirement;
    private final Map<ValueSpecification, Object> _valuesBySpecification;

    private Snapshot(final MarketDataAvailabilityProvider availability, final Map<ValueRequirement, Object> valuesByRequirement, final Map<ValueSpecification, Object> valuesBySpecification) {
      _availability = availability;
      _valuesByRequirement = valuesByRequirement;
      _valuesBySpecification = valuesBySpecification;
    }

    public void init() {
      if (!_valuesByRequirement.isEmpty()) {
        final ComputationTargetResolver.AtVersionCorrection targetResolver = getComputationTargetResolver();
        if (targetResolver != null) {
          final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver = targetResolver.getSpecificationResolver();
          for (final Map.Entry<ValueRequirement, Object> valueByRequirement : _valuesByRequirement.entrySet()) {
            final ComputationTargetSpecification targetSpec = specificationResolver.getTargetSpecification(valueByRequirement.getKey().getTargetReference());
            if (targetSpec != null) {
              final ComputationTarget target = targetResolver.resolve(targetSpec);
              final Object targetValue = target != null ? target.getValue() : null;
              final ValueSpecification resolved = _availability.getAvailability(targetSpec, targetValue, valueByRequirement.getKey());
              if (resolved != null) {
                LOGGER.info("Injecting {} as {}", valueByRequirement, resolved);
                _valuesBySpecification.put(resolved, valueByRequirement.getValue());
              } else {
                LOGGER.debug("Not injecting {} - no availability from {}", valueByRequirement, _availability);
              }
            } else {
              LOGGER.warn("Couldn't resolve {} for injected value requirement", valueByRequirement.getKey());
            }
          }
        } else {
          LOGGER.warn("Values injected by requirement, but no target resolver");
        }
      }
    }

    public Object query(final ValueSpecification value) {
      return _valuesBySpecification.get(value);
    }

  }

  private final ConcurrentMap<ValueRequirement, Object> _valuesByRequirement = new ConcurrentHashMap<>();
  private final ConcurrentMap<ValueSpecification, Object> _valuesBySpecification = new ConcurrentHashMap<>();
  private ComputationTargetResolver.AtVersionCorrection _targetResolver;

  public Snapshot snapshot(final MarketDataAvailabilityProvider availability) {
    if (_valuesByRequirement.isEmpty() && _valuesBySpecification.isEmpty()) {
      return null;
    }
    final Map<ValueRequirement, Object> valuesByRequirement = new HashMap<>(_valuesByRequirement);
    final Map<ValueSpecification, Object> valuesBySpecification = new HashMap<>(_valuesBySpecification);
    if (valuesByRequirement.isEmpty() && valuesBySpecification.isEmpty()) {
      return null;
    }
    return new Snapshot(availability, valuesByRequirement, valuesBySpecification);
  }

  public void setComputationTargetResolver(final ComputationTargetResolver.AtVersionCorrection targetResolver) {
    _targetResolver = targetResolver;
  }

  public ComputationTargetResolver.AtVersionCorrection getComputationTargetResolver() {
    return _targetResolver;
  }

  // MarketDataInjector

  @Override
  public void addValue(final ValueRequirement valueRequirement, final Object value) {
    _valuesByRequirement.put(valueRequirement, value);
  }

  @Override
  public void addValue(final ValueSpecification valueSpecification, final Object value) {
    _valuesBySpecification.put(valueSpecification, value);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    _valuesByRequirement.remove(valueRequirement);
  }

  @Override
  public void removeValue(final ValueSpecification valueSpecification) {
    _valuesBySpecification.remove(valueSpecification);
  }

}
