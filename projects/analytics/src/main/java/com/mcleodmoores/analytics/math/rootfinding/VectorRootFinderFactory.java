/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.rootfinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.rootfinding.VectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.VectorRootFinderType;
import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory that produces instances of vector root finders.
 */
public final class VectorRootFinderFactory extends AbstractNamedInstanceFactory<VectorRootFinder> {
  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(VectorRootFinderFactory.class);

  private static final ConcurrentMap<String, Class<VectorRootFinder>> CLASS_REFERENCES = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, VectorRootFinder> DEFAULT_INSTANCES = new ConcurrentHashMap<>();

  static {
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(VectorRootFinderType.class);
    for (final Class<?> clazz : classes) {
      try {
        final VectorRootFinderType annotation = clazz.getDeclaredAnnotation(VectorRootFinderType.class);
        final String name = annotation.name();
        final VectorRootFinder instance = (VectorRootFinder) clazz.getConstructor().newInstance(new Object[0]);
        CLASS_REFERENCES.putIfAbsent(name, (Class<VectorRootFinder>) clazz);
        DEFAULT_INSTANCES.putIfAbsent(name, instance);
      } catch (final Exception e) {
        LOGGER.warn("Could not add vector root finder: {}", e.getMessage());
      }
    }
  }

  /**
   * An instance of this factory.
   */
  public static final VectorRootFinderFactory INSTANCE = new VectorRootFinderFactory();

  @FromString
  public static VectorRootFinder of(final String methodName) {
    return DEFAULT_INSTANCES.get(methodName);
  }

  public static VectorRootFinder of(final String methodName, final double absoluteTolerance, final double relativeTolerance, final int maxSteps) {
    final Class<VectorRootFinder> clazz = CLASS_REFERENCES.get(methodName);
    Constructor<VectorRootFinder> constructor;
    try {
      constructor = clazz.getConstructor(Double.TYPE, Double.TYPE, Integer.TYPE);
    } catch (final NoSuchMethodException e1) {
      // try with Double, Double, Integer
      try {
        constructor = clazz.getConstructor(Double.class, Double.class, Integer.class);
      } catch (final NoSuchMethodException e2) {
        throw new IllegalArgumentException("Could not get " + methodName + " with these parameters");
      }
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(absoluteTolerance, relativeTolerance, maxSteps);
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
      }
    }
    throw new IllegalArgumentException("Could not get " + methodName + " with these parameters");
  }

  public static VectorRootFinder of(final String methodName, final double absoluteTolerance, final double relativeTolerance, final int maxSteps,
      final Decomposition<?> decomposition) {
    final Class<VectorRootFinder> clazz = CLASS_REFERENCES.get(methodName);
    Constructor<VectorRootFinder> constructor;
    try {
      constructor = clazz.getConstructor(Double.TYPE, Double.TYPE, Integer.TYPE, Decomposition.class);
    } catch (final NoSuchMethodException e1) {
      // try with Double, Double, Integer
      try {
        constructor = clazz.getConstructor(Double.class, Double.class, Integer.class, Decomposition.class);
      } catch (final NoSuchMethodException e2) {
        throw new IllegalArgumentException("Could not get " + methodName + " with these parameters");
      }
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(absoluteTolerance, relativeTolerance, maxSteps, decomposition);
      } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
      }
    }
    throw new IllegalArgumentException("Could not get " + methodName + " with these parameters");
  }

  /**
   * Restricted constructor.
   */
  protected VectorRootFinderFactory() {
    super(VectorRootFinder.class);
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
