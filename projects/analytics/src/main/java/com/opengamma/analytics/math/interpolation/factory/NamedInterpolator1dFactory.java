/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.joda.convert.FromString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.AbstractNamedInstanceFactory;

/**
 * A factory for named instances of {@link NamedInterpolator}. The classes must have a no-argument constructor
 * to be created by this factory. In the cases where the interpolator is not a named interpolator, this factory
 * returns instances of {@link Interpolator1dAdapter}, which wraps the original interpolator and implements
 * {@link com.opengamma.util.NamedInstance}.
 */
@SuppressWarnings("rawtypes")
public final class NamedInterpolator1dFactory extends AbstractNamedInstanceFactory<NamedInterpolator> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(NamedInterpolator1dFactory.class);
  /**
   * An instance of this factory.
   */
  public static final NamedInterpolator1dFactory INSTANCE = new NamedInterpolator1dFactory();

  /**
   * Gets the interpolator with this name.
   * @param name The name, not null
   * @return  The interpolator
   */
  @FromString
  public static NamedInterpolator of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  protected NamedInterpolator1dFactory() {
    super(NamedInterpolator.class);
    final AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(InterpolationType.class);
    for (final Class<?> clazz : classes) {
      try {
        final InterpolationType annotation = clazz.getDeclaredAnnotation(InterpolationType.class);
        final String name = annotation.name();
        final String[] aliases = annotation.aliases();
        NamedInterpolator instance = null;
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
          // single no-args constructor, name hard-coded in constructor
          instance = (NamedInterpolator) constructors[0].newInstance(name);
          addInstance(instance);
        } else if (constructors.length == 2) {
          for (final Constructor constructor : constructors) {
            final Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
              instance = (NamedInterpolator) constructor.newInstance(name);
              addInstance(instance, aliases);
              break;
            }
          }
          if (instance != null && instance instanceof Interpolator1dAdapter && !((Interpolator1dAdapter) instance).isExtrapolator()) {
            final String newLinearName = LinearExtrapolator1dAdapter.transformName(LinearExtrapolator1dAdapter.NAME, name);
            final String newLogLinearName = LinearExtrapolator1dAdapter.transformName(LogLinearExtrapolator1dAdapter.NAME, name);
            final String newQuadraticLeftName = LinearExtrapolator1dAdapter.transformName(QuadraticLeftExtrapolator1dAdapter.NAME, name);
            final String[] linearExtrapolatorAliases = LinearExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final String[] logLinearExtrapolatorAliases = LogLinearExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final String[] quadraticLeftExtrapolatorAliases = QuadraticLeftExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final List<String> newLinearAliases = new ArrayList<>();
            final List<String> newLogLinearAliases = new ArrayList<>();
            final List<String> newQuadraticLeftAliases = new ArrayList<>();
            for (final String extrapolatorAlias : linearExtrapolatorAliases) {
              newLinearAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newLinearAliases.add(LinearExtrapolator1dAdapter.transformName(LinearExtrapolator1dAdapter.NAME, alias));
                newLinearAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, alias));
              }
            }
            for (final String extrapolatorAlias : logLinearExtrapolatorAliases) {
              newLogLinearAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newLogLinearAliases.add(LinearExtrapolator1dAdapter.transformName(LogLinearExtrapolator1dAdapter.NAME, alias));
                newLogLinearAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, alias));
              }
            }
            for (final String extrapolatorAlias : quadraticLeftExtrapolatorAliases) {
              newQuadraticLeftAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newQuadraticLeftAliases.add(LinearExtrapolator1dAdapter.transformName(QuadraticLeftExtrapolator1dAdapter.NAME, alias));
                newQuadraticLeftAliases.add(LinearExtrapolator1dAdapter.transformName(extrapolatorAlias, alias));
              }
            }
            addInstance(new LinearExtrapolator1dAdapter((Interpolator1dAdapter) instance, newLinearName),
                newLinearAliases.toArray(new String[newLinearAliases.size()]));
            addInstance(new LogLinearExtrapolator1dAdapter((Interpolator1dAdapter) instance, newLogLinearName),
                newLogLinearAliases.toArray(new String[newLogLinearAliases.size()]));
            addInstance(new QuadraticLeftExtrapolator1dAdapter((Interpolator1dAdapter) instance, newQuadraticLeftName),
                newQuadraticLeftAliases.toArray(new String[newQuadraticLeftAliases.size()]));
          }
        }
      } catch (final Exception e) {
        LOGGER.warn("Could not add interpolator: {}", e.getMessage());
      }
    }

  }

}
