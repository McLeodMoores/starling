/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.AnnotationReflector;
import org.joda.convert.FromString;
import org.reflections.Configuration;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for named instances of {@link NamedInterpolator}. The classes must have a no-argument constructor
 * to be created by this factory. In the cases where the interpolator is not a named interpolator, this factory
 * returns instances of {@link Interpolator1dAdapter}, which wraps the original interpolator and implements
 * {@link com.opengamma.util.NamedInstance}.
 */
@SuppressWarnings("rawtypes")
public final class NamedInterpolator1dFactory extends AbstractNamedInstanceFactory<NamedInterpolator1d> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(NamedInterpolator1dFactory.class);
  /**
   * An instance of this factory.
   */
  public static final NamedInterpolator1dFactory INSTANCE = new NamedInterpolator1dFactory();

  /**
   * Gets the interpolator with this name.
   *
   * @param interpolatorName
   *          the name, not null
   * @return the interpolator
   */
  @FromString
  public static NamedInterpolator1d of(final String interpolatorName) {
    ArgumentChecker.notNull(interpolatorName, "interpolatorName");
    try {
      return INSTANCE.instance(interpolatorName);
    } catch (final IllegalArgumentException e) {
      // TODO move this logic into the combining interpolator adapter
      // might have to parse name
      if (interpolatorName.startsWith("Combined")) {
        final String trimmed = interpolatorName.substring(9, interpolatorName.length() - 1);
        final String[] pairs = trimmed.split(",");
        if (pairs.length == 1) {
          final String interpolator = StringUtils.trimToEmpty(pairs[0].split("=")[1]);
          return INSTANCE.instance(interpolator);
        } else if (pairs.length == 2) {
          final String interpolator = StringUtils.trimToEmpty(pairs[0].split("=")[1]);
          final String extrapolator = StringUtils.trimToEmpty(pairs[1].split("=")[1]);
          return of(interpolator, extrapolator);
        } else if (pairs.length == 3) {
          final String interpolator = StringUtils.trimToEmpty(pairs[0].split("=")[1]);
          final String leftExtrapolator = StringUtils.trimToEmpty(pairs[1].split("=")[1]);
          final String rightExtrapolator = StringUtils.trimToEmpty(pairs[2].split("=")[1]);
          return of(interpolator, leftExtrapolator, rightExtrapolator);
        } else {
          throw new IllegalArgumentException("Cannot get interpolator called " + interpolatorName);
        }
      }
    }
    throw new IllegalArgumentException("Cannot get interpolator called " + interpolatorName);
  }

  /**
   * Gets a combined interpolator and extrapolator that uses the same
   * extrapolation method on the left and right.
   *
   * @param interpolatorName
   *          the interpolator name, not null
   * @param extrapolatorName
   *          the extrapolator name, not null
   * @return a combined interpolator and extrapolator
   */
  public static NamedInterpolator1d of(final String interpolatorName, final String extrapolatorName) {
    return of(interpolatorName, extrapolatorName, extrapolatorName);
  }

  /**
   * Gets a combined interpolator and extrapolator.
   * @param interpolatorName  the interpolator name, not null
   * @param leftExtrapolatorName  the left extrapolator name, not null
   * @param rightExtrapolatorName  the right extrapolator name, not null
   * @return  a combined interpolator and extrapolator
   */
  public static NamedInterpolator1d of(final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    final NamedInterpolator1d interpolator = INSTANCE.instance(interpolatorName);
    NamedInterpolator1d leftExtrapolator = null;
    try {
      // for those extrapolators that do not need information about the adjacent interpolator
      leftExtrapolator = INSTANCE.instance(leftExtrapolatorName);
    } catch (final IllegalArgumentException e) {
      // try with transformed name
      leftExtrapolator = INSTANCE.instance(transformName(leftExtrapolatorName, interpolatorName));
    }
    if (leftExtrapolator != null && !leftExtrapolator.isExtrapolator()) {
      throw new IllegalArgumentException("Interpolator called " + leftExtrapolatorName + " cannot extrapolate values");
    }
    NamedInterpolator1d rightExtrapolator = null;
    try {
      rightExtrapolator = INSTANCE.instance(rightExtrapolatorName);
    } catch (final IllegalArgumentException e) {
      rightExtrapolator = INSTANCE.instance(transformName(rightExtrapolatorName, interpolatorName));
    }
    if (rightExtrapolator != null && !rightExtrapolator.isExtrapolator()) {
      throw new IllegalArgumentException("Interpolator called " + rightExtrapolatorName + " cannot extrapolate values");
    }
    if (interpolator instanceof Interpolator1dAdapter && leftExtrapolator instanceof Interpolator1dAdapter
        && rightExtrapolator instanceof Interpolator1dAdapter) {
      return new CombinedInterpolatorExtrapolator1dAdapter((Interpolator1dAdapter) interpolator,
          (Interpolator1dAdapter) leftExtrapolator, (Interpolator1dAdapter) rightExtrapolator);
    }
    throw new IllegalArgumentException("Could not construct combined interpolator with interpolator = " + interpolatorName
        + " left extrapolator = " + leftExtrapolatorName + " right extrapolator = " + rightExtrapolatorName);
  }

  /**
   * Restricted constructor.
   */
  protected NamedInterpolator1dFactory() {
    super(NamedInterpolator1d.class);
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(InterpolationType.class);
    for (final Class<?> clazz : classes) {
      try {
        final Annotation[] annotations = clazz.getDeclaredAnnotations();
        InterpolationType annotation = null;
        for (final Annotation a : annotations) {
          if (a.annotationType().equals(InterpolationType.class)) {
            annotation = (InterpolationType) a;
          }
        }
        if (annotation == null) {
          LOGGER.error("Could not get InterpolationType annotation for {}", clazz.getSimpleName());
          continue;
        }
        final String name = annotation.name();
        final String[] aliases = annotation.aliases();
        NamedInterpolator1d instance = null;
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
          // single no-args constructor, name hard-coded in constructor
          instance = (NamedInterpolator1d) constructors[0].newInstance(name);
          addInstance(instance);
        } else if (constructors.length == 2) {
          for (final Constructor constructor : constructors) {
            final Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
              instance = (NamedInterpolator1d) constructor.newInstance(name);
              addInstance(instance, aliases);
              break;
            }
          }
          if (instance != null && instance instanceof Interpolator1dAdapter && !((Interpolator1dAdapter) instance).isExtrapolator()) {
            final String newLinearName = transformName(LinearExtrapolator1dAdapter.NAME, name);
            final String newLogLinearName = transformName(LogLinearExtrapolator1dAdapter.NAME, name);
            final String newQuadraticLeftName = transformName(QuadraticLeftExtrapolator1dAdapter.NAME, name);
            final String[] linearExtrapolatorAliases = LinearExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final String[] logLinearExtrapolatorAliases = LogLinearExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final String[] quadraticLeftExtrapolatorAliases = QuadraticLeftExtrapolator1dAdapter.class.getAnnotation(InterpolationType.class).aliases();
            final List<String> newLinearAliases = new ArrayList<>();
            final List<String> newLogLinearAliases = new ArrayList<>();
            final List<String> newQuadraticLeftAliases = new ArrayList<>();
            for (final String extrapolatorAlias : linearExtrapolatorAliases) {
              newLinearAliases.add(transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newLinearAliases.add(transformName(LinearExtrapolator1dAdapter.NAME, alias));
                newLinearAliases.add(transformName(extrapolatorAlias, alias));
              }
            }
            for (final String extrapolatorAlias : logLinearExtrapolatorAliases) {
              newLogLinearAliases.add(transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newLogLinearAliases.add(transformName(LogLinearExtrapolator1dAdapter.NAME, alias));
                newLogLinearAliases.add(transformName(extrapolatorAlias, alias));
              }
            }
            for (final String extrapolatorAlias : quadraticLeftExtrapolatorAliases) {
              newQuadraticLeftAliases.add(transformName(extrapolatorAlias, name));
              for (final String alias : aliases) {
                newQuadraticLeftAliases.add(transformName(QuadraticLeftExtrapolator1dAdapter.NAME, alias));
                newQuadraticLeftAliases.add(transformName(extrapolatorAlias, alias));
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

  /**
   * Transforms the name of the interpolator into the extrapolator: (EXTRAPOLATOR_NAME, NAME) -> EXTRAPOLATOR_NAME[NAME].
   * @param extrapolatorName  the version of the name of the linear extrapolator, not null
   * @param interpolatorName  the interpolator name, not null
   * @return  the transformed name
   */
  public static String transformName(final String extrapolatorName, final String interpolatorName) {
    return ArgumentChecker.notNull(extrapolatorName, "extrapolatorName") + "[" + ArgumentChecker.notNull(interpolatorName, "name") + "]";
  }

}
