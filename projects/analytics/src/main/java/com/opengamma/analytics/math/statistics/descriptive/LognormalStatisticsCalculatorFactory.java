/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.lang.reflect.Constructor;
import java.util.Set;

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

/**
 * A factory for named instances of classes that calculate the statistics of lognormal distributions. This
 * factory uses reflection to register the calculators.
 */
public class LognormalStatisticsCalculatorFactory extends AbstractNamedInstanceFactory<LognormalStatisticsCalculator> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(LognormalStatisticsCalculatorFactory.class);
  /** An instance of this factory */
  private static final LognormalStatisticsCalculatorFactory INSTANCE = new LognormalStatisticsCalculatorFactory();

  /**
   * Gets the statistics calculator with this name.
   * @param statisticName  the name, not null
   * @return  the calculator
   */
  @FromString
  public static LognormalStatisticsCalculator of(final String statisticName) {
    return INSTANCE.instance(statisticName);
  }

  /**
   * Restricted constructor.
   */
  protected LognormalStatisticsCalculatorFactory() {
    super(LognormalStatisticsCalculator.class);
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(LognormalStatistic.class);
    for (final Class<?> clazz : classes) {
      final LognormalStatistic annotation = clazz.getDeclaredAnnotation(LognormalStatistic.class);
      final String[] aliases = annotation.aliases();
      LognormalStatisticsCalculator instance = null;
      final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (final Constructor<?> constructor : constructors) {
        try {
          if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
            // found the single no-args constructor
            constructor.setAccessible(true); // allow private constructors
            instance = (LognormalStatisticsCalculator) constructor.newInstance(new Object[0]);
            addInstance(instance, aliases);
          }
          break;
        } catch (final Exception e) {
          LOGGER.warn("Could not add calculator: {}", e.getMessage());
        }
      }
    }
  }
}
