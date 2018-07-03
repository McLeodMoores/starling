/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.bond;

import java.util.Map;
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

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.AbstractNamedInstanceFactory;

/**
 *
 */
public class BondYieldConventionFactory extends AbstractNamedInstanceFactory<BondYieldConvention> {

  public static final BondYieldConventionFactory INSTANCE = new BondYieldConventionFactory();
  private static final Logger LOGGER = LoggerFactory.getLogger(BondYieldConventionFactory.class);

  @FromString
  public static BondYieldConvention of(final String conventionName) {
    return INSTANCE.instance(conventionName);
  }

  private BondYieldConventionFactory() {
    super(BondYieldConvention.class);
    // add annotated types
    final Configuration config = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forManifest(ClasspathHelper.forJavaClassPath()))
        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        .filterInputsBy(FilterBuilder.parse(AnnotationReflector.DEFAULT_ANNOTATION_REFLECTOR_FILTER))
        .useParallelExecutor();
    final AnnotationReflector reflector = new AnnotationReflector(config);
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(YieldConventionType.class);
    for (final Class<?> clazz : classes) {
      try {
        final YieldConventionType annotation = clazz.getDeclaredAnnotation(YieldConventionType.class);
        final String[] aliases = annotation.aliases();
        final BondYieldConvention instance = (BondYieldConvention) clazz.newInstance();
        addInstance(instance, aliases);
      } catch (final Exception e) {
        LOGGER.warn("Could not add yield convention: {}", e.getMessage());
      }
    }
    // add old simple yield conventions
    for (final Map.Entry<String, YieldConvention> entry : YieldConventionFactory.INSTANCE.instanceMap().entrySet()) {

    }
  }

}
