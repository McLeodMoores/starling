/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClasspathUtils;

/**
 * Static utility for scanning classes for a particular annotation.
 */
public final class ClassNameAnnotationScannerUtils {

  /**
   * Restricted constructor.
   */
  private ClassNameAnnotationScannerUtils() {
  }

  /**
   * Scans the specified classpath for an annotation.
   *
   * @param classpathElements  the classpath, not null
   * @param annotationClass  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<String> scan(final String[] classpathElements, final Class<?> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotationClass");
    return scan(classpathElements, annotationClass.getName());
  }

  /**
   * Scans the specified classpath for an annotation.
   *
   * @param classpathElements  the classpath, not null
   * @param annotationClassName  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<String> scan(final String[] classpathElements, final String annotationClassName) {
    final URL[] classpathUrls = ClasspathUtils.getClasspathURLs(classpathElements);
    return scan(classpathUrls, annotationClassName);
  }

  /**
   * Scans the specified classpath for an annotation.
   *
   * @param classpathUrls  the classpath, not null
   * @param annotationClassName  the annotation to find, not null
   * @return the matching elements, not null
   */
  @SuppressWarnings("unchecked")
  public static Set<String> scan(final URL[] classpathUrls, final String annotationClassName) {
    try {
      return convertClasses(scan(classpathUrls, (Class<? extends Annotation>) Class.forName(annotationClassName)));
    } catch (final ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Couldn't find class " + annotationClassName, ex);
    }
  }

  private static Set<String> convertClasses(final Set<Class<?>> classes) {
    final Set<String> results = new LinkedHashSet<>(classes.size());
    for (final Class<?> clazz : classes) {
      results.add(clazz.getName());
    }
    return results;
  }
  /**
   * Scans the specified classpath for an annotation.
   *
   * @param classpathUrls  the classpath, not null
   * @param annotationClass  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<Class<?>> scan(final URL[] classpathUrls, final Class<? extends Annotation> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotationClass");
    final Set<URL> urls = new HashSet<>(Arrays.asList(classpathUrls));
    final AnnotationReflector reflector = new AnnotationReflector(
        null, urls, new TypeAnnotationsScanner(),
        ClassNameAnnotationScannerUtils.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
    final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(annotationClass); // getStore().
    if (classes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(classes);
  }

}
