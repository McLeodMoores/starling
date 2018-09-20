/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.AnnotationReflector;
import org.fudgemsg.FudgeRuntimeException;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Scans the class path for classes that contain the given annotations.
 */
public final class ClasspathScanner {

  private final Set<URL> _urls;
  private final Instant _timestamp;

  private static volatile Set<URL> s_classPathElements;

  private boolean _scanClassAnnotations = true;
  private boolean _scanMethodAnnotations = true;
  private boolean _scanParameterAnnotations = true;
  private boolean _scanFieldAnnotations = true;

  public ClasspathScanner() {
    _urls = getClassPathElements();
    _timestamp = timestamp(_urls);
  }

  private Set<URL> getClassPathElements() {
    if (s_classPathElements == null) {
      s_classPathElements = findClassPathElements();
    }
    return s_classPathElements;
  }

  private static Set<URL> findClassPathElements() {
    final Set<URL> results = new LinkedHashSet<>();
    final String javaClassPath = System.getProperty("java.class.path");
    final String[] paths = javaClassPath.split(Pattern.quote(File.pathSeparator));
    for (final String path : paths) {
      final File f = new File(path);
      if (!f.exists()) {
        continue;
      }
      URL url;
      try {
        url = f.toURI().toURL();
      } catch (final MalformedURLException e) {
        throw new FudgeRuntimeException("Could not convert file " + f + " to URL", e);
      }
      results.add(url);
    }
    return results;
  }

  private static Instant timestamp(final Iterable<URL> urls) {
    long ctime = 0;
    for (final URL url : urls) {
      try {
        final File f = new File(url.toURI());
        final long l = f.lastModified();
        if (l > ctime) {
          ctime = l;
        }
      } catch (final URISyntaxException e) {
        // Ignore this one
      }
    }
    return Instant.ofEpochMilli(ctime);
  }

  /**
   * Returns the timestamp of the most recently modified Jar (or class) in the class path.
   *
   * @return the timestamp, not null
   */
  public Instant getTimestamp() {
    return _timestamp;
  }

  /**
   * Scans the classpath to produce a populated cache.
   *
   * @param annotationClass the annotation to search for
   * @return the cache, not null
   */
  @SuppressWarnings({"rawtypes", "unchecked" })
  public AnnotationCache scan(final Class<? extends Annotation> annotationClass) {
    int scanners = 0;
    if (isScanClassAnnotations()) {
      scanners++;
    }
    if (isScanFieldAnnotations()) {
      scanners++;
    }
    if (isScanMethodAnnotations()) {
      scanners++;
    }
    if (isScanParameterAnnotations()) {
      scanners++;
    }
    final Object[] config = new Object[scanners + 3];
    scanners = 0;
    if (isScanClassAnnotations()) {
      config[scanners++] = new TypeAnnotationsScanner();
      config[scanners++] = new SubTypesScanner();
    }
    if (isScanFieldAnnotations()) {
      config[scanners++] = new FieldAnnotationsScanner();
    }
    if (isScanMethodAnnotations()) {
      config[scanners++] = new MethodAnnotationsScanner();
    }
    if (isScanParameterAnnotations()) {
      config[scanners++] = new MethodParameterScanner();
    }
    config[scanners++] = ClasspathScanner.class.getClassLoader();
    config[scanners++] = Thread.currentThread().getContextClassLoader();
    final AnnotationReflector annotationReflector = new AnnotationReflector(null, _urls, config);
    final HashSet<String> classNames = new HashSet<>();
    if (isScanClassAnnotations()) {

      Set<Class<?>> classes;
      try {
        classes = annotationReflector.getReflector().getTypesAnnotatedWith((Class<? extends Annotation>) Class.forName(annotationClass.getName()));
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Can't find class " + annotationClass, ex);
      }
      for (final Class<?> clazz : classes) {
        classNames.add(clazz.getName());
      }
    }
    if (isScanFieldAnnotations()) {
      final Set<Field> fields = annotationReflector.getReflector().getFieldsAnnotatedWith(annotationClass);
      for (final Field field : fields) {
        classNames.add(field.getDeclaringClass().getName());
      }
    }
    if (isScanMethodAnnotations()) {
      final Set<Method> methods = annotationReflector.getReflector().getMethodsAnnotatedWith(annotationClass);
      for (final Method method : methods) {
        classNames.add(method.getDeclaringClass().getName());
      }
      final Set<Constructor> constructors = annotationReflector.getReflector().getConstructorsAnnotatedWith(annotationClass);
      for (final Constructor constructor : constructors) {
        classNames.add(constructor.getDeclaringClass().getName());
      }
    }
    if (isScanParameterAnnotations()) {
      final Set<Method> paramMethods = annotationReflector.getReflector().getMethodsWithAnyParamAnnotated(annotationClass);
      for (final Method method : paramMethods) {
        classNames.add(method.getDeclaringClass().getName());
      }
    }
    return AnnotationCache.create(getTimestamp(), annotationClass, classNames);
  }

  /**
   * Gets the scanClassAnnotations.
   *
   * @return the scanClassAnnotations
   */
  public boolean isScanClassAnnotations() {
    return _scanClassAnnotations;
  }

  /**
   * Sets the scanClassAnnotations.
   *
   * @param scanClassAnnotations the scanClassAnnotations
   */
  public void setScanClassAnnotations(final boolean scanClassAnnotations) {
    _scanClassAnnotations = scanClassAnnotations;
  }

  /**
   * Gets the scanMethodAnnotations.
   *
   * @return the scanMethodAnnotations
   */
  public boolean isScanMethodAnnotations() {
    return _scanMethodAnnotations;
  }

  /**
   * Sets the scanMethodAnnotations.
   *
   * @param scanMethodAnnotations the scanMethodAnnotations
   */
  public void setScanMethodAnnotations(final boolean scanMethodAnnotations) {
    _scanMethodAnnotations = scanMethodAnnotations;
  }

  /**
   * Gets the scanParameterAnnotations.
   *
   * @return the scanParameterAnnotations
   */
  public boolean isScanParameterAnnotations() {
    return _scanParameterAnnotations;
  }

  /**
   * Sets the scanParameterAnnotations.
   *
   * @param scanParameterAnnotations the scanParameterAnnotations
   */
  public void setScanParameterAnnotations(final boolean scanParameterAnnotations) {
    _scanParameterAnnotations = scanParameterAnnotations;
  }

  /**
   * Gets the scanFieldAnnotations.
   *
   * @return the scanFieldAnnotations
   */
  public boolean isScanFieldAnnotations() {
    return _scanFieldAnnotations;
  }

  /**
   * Sets the scanFieldAnnotations.
   *
   * @param scanFieldAnnotations the scanFieldAnnotations
   */
  public void setScanFieldAnnotations(final boolean scanFieldAnnotations) {
    _scanFieldAnnotations = scanFieldAnnotations;
  }

}
