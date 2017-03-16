/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.lang.reflect.Constructor;
import java.util.Locale;
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
import com.opengamma.util.ArgumentChecker;

import cern.colt.Arrays;

/**
 * A factory for named instances of classes that calculate descriptive statistics (e.g. mean, skewness). This
 * factory uses reflection to register the calculators.
 */
public class DescriptiveStatisticsFactory extends AbstractNamedInstanceFactory<DescriptiveStatisticsCalculator> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptiveStatisticsFactory.class);
  /** An instance of this factory */
  private static final DescriptiveStatisticsFactory INSTANCE = new DescriptiveStatisticsFactory();

  /**
   * Gets the statistics calculator with this name.
   * @param statisticName  the name, not null
   * @return  the calculator
   */
  @FromString
  public static DescriptiveStatisticsCalculator of(final String statisticName) {
    return INSTANCE.instance(statisticName);
  }

  /**
   * Gets the statistic calculator with this name and arguments. This method provides a similar way of getting a calculator
   * to {@link DescriptiveStatisticsFactory#of(String)}, but the instances are not cached, i.e. a new instance will be
   * created each time this method is called.
   * @param statisticName  the name, not null
   * @param args  the arguments, not null
   * @return  the calculator
   */
  public static DescriptiveStatisticsCalculator of(final String statisticName, final Object... args) {
    ArgumentChecker.notNull(statisticName, "statisticName");
    ArgumentChecker.notNull(args, "calculatorArgs");
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final String lowerName = statisticName.toLowerCase(Locale.ENGLISH);
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(DescriptiveStatistic.class);
    for (final Class<?> clazz : classes) {
      final DescriptiveStatistic annotation = clazz.getDeclaredAnnotation(DescriptiveStatistic.class);
      final String name = annotation.name().toLowerCase(Locale.ENGLISH);
      final String[] aliases = annotation.aliases();
      if (!name.equals(lowerName)) {
        boolean match = false;
        for (final String alias : aliases) {
          if (alias.toLowerCase(Locale.ENGLISH).equals(lowerName)) {
            match = true;
            break;
          }
        }
        if (!match) {
          continue;
        }
      }
      final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (final Constructor<?> constructor : constructors) {
        try {
          if (constructors.length == args.length) {
            constructor.setAccessible(true); // allow private constructors
            return (DescriptiveStatisticsCalculator) constructor.newInstance(args);
          }
        } catch (final Exception e) {
          throw new IllegalArgumentException("Could not get calculator called " + statisticName + " with arguments "
                + Arrays.toString(args) + ": " + e.getMessage());
        }
      }
    }
    throw new IllegalArgumentException("Could not get calculator called " + statisticName + " with arguments " + Arrays.toString(args));
  }

  /**
   * Restricted constructor.
   */
  protected DescriptiveStatisticsFactory() {
    super(DescriptiveStatisticsCalculator.class);
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(DescriptiveStatistic.class);
    for (final Class<?> clazz : classes) {
      final DescriptiveStatistic annotation = clazz.getDeclaredAnnotation(DescriptiveStatistic.class);
      final String[] aliases = annotation.aliases();
      DescriptiveStatisticsCalculator instance = null;
      final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (final Constructor<?> constructor : constructors) {
        try {
          if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
            // found the single no-args constructor
            constructor.setAccessible(true); // allow private constructors
            instance = (DescriptiveStatisticsCalculator) constructor.newInstance(new Object[0]);
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
