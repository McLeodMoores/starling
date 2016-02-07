/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

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
 * A factory for {@link FinmathDayCount} instances. This factory scans for files annotated
 * with {@link FinmathDayCountType}. The name and aliases of the instances are determined by:
 * <ul>
 *  <li> If the {@link FinmathDayCountType#name()} and {@link FinmathDayCountType#aliases()} properties
 *  are empty, try to construct the object using a no-args constructor;
 *  <li> If the name property is empty but the aliases are not, use the first value from the aliases as the name and
 *  the remainder of the entries as the aliases. The name is used to try to initialize the object using
 *  a constructor that takes a single String.
 *  <li> Use name property and aliases from the annotation as above.
 *  <li> If the day count adapter does not have any appropriate constructors this is logged but the
 *  class will not be added to the factory.
 * </ul>
 */
public final class FinmathDayCountFactory extends AbstractNamedInstanceFactory<FinmathDayCount> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FinmathDayCountFactory.class);
  /**
   * A static instance.
   */
  public static final FinmathDayCountFactory INSTANCE = new FinmathDayCountFactory();

  /**
   * Returns a named instance of {@link FinmathDayCount}.
   * @param name The name, not null
   * @return The instance, if found.
   */
  @FromString
  public static FinmathDayCount of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor that scans the classpath for classes annotated with {@link FinmathBusinessDayType}.
   */
  protected FinmathDayCountFactory() {
    super(FinmathDayCount.class);
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(FinmathDayCountType.class);
    for (final Class<?> clazz : classes) {
      try {
        final FinmathDayCountType annotation = clazz.getDeclaredAnnotation(FinmathDayCountType.class);
        final String name = annotation.name();
        final String[] aliases = annotation.aliases();
        if (name.isEmpty() && aliases.length == 0) {
          final Constructor<?> constructor = clazz.getDeclaredConstructor();
          addInstance((FinmathDayCount) constructor.newInstance());
        }
        final String nameToUse;
        final String[] aliasesToUse;
        if (name.isEmpty()) {
          nameToUse = aliases[0];
          aliasesToUse = new String[aliases.length - 1];
          System.arraycopy(aliases, 1, aliasesToUse, 0, aliasesToUse.length);
        } else {
          nameToUse = name;
          aliasesToUse = aliases;
        }
        final Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
        addInstance((FinmathDayCount) constructor.newInstance(nameToUse), aliasesToUse);
      } catch (final Exception e) {
        LOGGER.warn("Could not add day count: {}", e.getMessage());
      }
    }
  }
}
