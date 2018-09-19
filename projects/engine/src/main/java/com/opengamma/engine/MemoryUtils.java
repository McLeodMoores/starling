/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.NormalizingWeakInstanceCache;
import com.opengamma.util.WeakInstanceCache;

/**
 * Badly named class collating all of the hacks used to reduce the memory footprint of some algorithms.
 */
public final class MemoryUtils {

  // TODO: Why do we need value specifications everywhere; can't we just work with the longs that go into the jobs?
  // Converting ValueSpecifications to/from the longs is cheap enough - if in the cache, map operations no more
  // costly than the approach here

  private static final WeakInstanceCache<? extends ComputationTargetReference> COMPUTATION_TARGET_REFERENCE =
      new NormalizingWeakInstanceCache<ComputationTargetReference>() {
    @Override
    protected ComputationTargetReference normalize(final ComputationTargetReference value) {
      return value.normalize();
    }
  };

  private static final WeakInstanceCache<ValueProperties> VALUE_PROPERTIES = new WeakInstanceCache<>();

  private static final WeakInstanceCache<ValueRequirement> VALUE_REQUIREMENT = new NormalizingWeakInstanceCache<ValueRequirement>() {
    @Override
    protected ValueRequirement normalize(final ValueRequirement valueRequirement) {
      final ComputationTargetReference ctspec = instance(valueRequirement.getTargetReference());
      final ValueProperties constraints = instance(valueRequirement.getConstraints());
      if (ctspec == valueRequirement.getTargetReference() && constraints == valueRequirement.getConstraints()) {
        return valueRequirement;
      } else {
        return new ValueRequirement(valueRequirement.getValueName(), ctspec, constraints);
      }
    }
  };

  private static final WeakInstanceCache<ValueSpecification> VALUE_SPECIFICATION = new NormalizingWeakInstanceCache<ValueSpecification>() {
    @Override
    protected ValueSpecification normalize(final ValueSpecification valueSpecification) {
      final ComputationTargetSpecification ctspec = instance(valueSpecification.getTargetSpecification());
      final ValueProperties properties = instance(valueSpecification.getProperties());
      if (ctspec == valueSpecification.getTargetSpecification() && properties == valueSpecification.getProperties()) {
        return valueSpecification;
      } else {
        return new ValueSpecification(valueSpecification.getValueName(), ctspec, properties);
      }
    }
  };

  private MemoryUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <T extends ComputationTargetReference> T instance(final T computationTargetReference) {
    return ((WeakInstanceCache<T>) COMPUTATION_TARGET_REFERENCE).get(computationTargetReference);
  }

  public static ValueProperties instance(final ValueProperties valueProperties) {
    return VALUE_PROPERTIES.get(valueProperties);
  }

  public static ValueRequirement instance(final ValueRequirement valueRequirement) {
    return VALUE_REQUIREMENT.get(valueRequirement);
  }

  public static ValueSpecification instance(final ValueSpecification valueSpecification) {
    return VALUE_SPECIFICATION.get(valueSpecification);
  }

  /**
   * Estimate the size of an object in memory. This is based on its serialized form which is crude but better than nothing.
   *
   * @param object the object to estimate the size of
   * @return the size estimate in bytes
   */
  public static long estimateSize(final Serializable object) {
    if (object == null) {
      return 0;
    }
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final ObjectOutputStream out = new ObjectOutputStream(baos);
      out.writeObject(object);
      out.close();
      return baos.toByteArray().length;
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("I/O error", e);
    }
  }

}
