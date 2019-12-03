/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import java.lang.annotation.Annotation;
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
 * Factory containing instances of {@link ExchangeTradedInstrumentExpiryCalculator}.
 */
public class ExchangeTradedInstrumentExpiryCalculatorFactory extends AbstractNamedInstanceFactory<ExchangeTradedInstrumentExpiryCalculator> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeTradedInstrumentExpiryCalculatorFactory.class);
  /**
   * An instance of this factory.
   */
  public static final ExchangeTradedInstrumentExpiryCalculatorFactory INSTANCE = new ExchangeTradedInstrumentExpiryCalculatorFactory();

  /**
   * Gets the expiry calculator with this name.
   *
   * @param name
   *          the name of the calculator, not null
   * @return the calculator
   */
  @FromString
  public static ExchangeTradedInstrumentExpiryCalculator of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  protected ExchangeTradedInstrumentExpiryCalculatorFactory() {
    super(ExchangeTradedInstrumentExpiryCalculator.class);
    final Configuration config = new ConfigurationBuilder().setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER)).useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(ExpiryCalculator.class);
    for (final Class<?> clazz : classes) {
      final Annotation[] annotations = clazz.getDeclaredAnnotations();
      ExpiryCalculator annotation = null;
      for (final Annotation a : annotations) {
        if (a.annotationType().equals(ExpiryCalculator.class)) {
          annotation = (ExpiryCalculator) a;
        }
      }
      if (annotation == null) {
        LOGGER.error("Could not LognormalStatistic annotation for {}", clazz.getSimpleName());
        continue;
      }
      ExchangeTradedInstrumentExpiryCalculator instance = null;
      final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (final Constructor<?> constructor : constructors) {
        try {
          if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
            // found the single no-args constructor
            constructor.setAccessible(true); // allow private constructors
            instance = (ExchangeTradedInstrumentExpiryCalculator) constructor.newInstance(new Object[0]);
            addInstance(instance);
          }
          break;
        } catch (final Exception e) {
          LOGGER.warn("Could not add calculator: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Gets the named calculator.
   *
   * @param name
   *          the name of the expiry calculator, not null
   * @return the calculator, not null
   * @throws IllegalArgumentException
   *           if the calculator was not found in the map
   * @deprecated use {@link #of(String)}
   */
  @Deprecated
  public static ExchangeTradedInstrumentExpiryCalculator getCalculator(final String name) {
    return of(name);
  }

}
