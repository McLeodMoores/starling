/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.SingletonFactoryBean;

/**
 * A Spring factory bean for obtaining a list of classes with a particular annotation.
 */
public class AnnotationScanningStringListFactoryBean extends SingletonFactoryBean<List<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationScanningStringListFactoryBean.class);

  private String _cacheFile;
  private String _forceScanSystemProperty;
  private String _annotationClassName;

  public String getCacheFile() {
    return _cacheFile;
  }

  public void setCacheFile(final String cacheFile) {
    _cacheFile = cacheFile;
  }

  public String getForceScanSystemProperty() {
    return _forceScanSystemProperty;
  }

  public void setForceScanSystemProperty(final String forceScanSystemProperty) {
    _forceScanSystemProperty = forceScanSystemProperty;
  }

  public String getAnnotationClassName() {
    return _annotationClassName;
  }

  public void setAnnotationClassName(final String annotationClassName) {
    _annotationClassName = annotationClassName;
  }

  @Override
  protected List<String> createObject() {
    try {
      final boolean forceScan = shouldForceScan();
      if (!forceScan && getCacheFile() != null) {
        final ClassPathResource cacheFileResource = new ClassPathResource(getCacheFile());
        if (cacheFileResource.exists()) {
          final File cacheFile = cacheFileResource.getFile();
          LOGGER.debug("Getting classes containing annotation {} from cache {}", getAnnotationClassName(), cacheFile.getAbsoluteFile());
          return getFromCache(cacheFile);
        }
      }
      LOGGER.debug("Scanning for classes containing annotation {}", getAnnotationClassName());
      return new ArrayList<>(getByScanning(getAnnotationClassName()));
    } catch (final Exception e) {
      LOGGER.warn("Unable to retrieve classes containing annotation " + getAnnotationClassName(), e);
      return Collections.emptyList();
    }
  }

  private boolean shouldForceScan() {
    if (getForceScanSystemProperty() == null) {
      LOGGER.debug("Force scan system property not specified");
      return false;
    }
    final String forceScanPropertyValue = System.getProperty(getForceScanSystemProperty());
    LOGGER.debug("Force scan system property set to '{}'", forceScanPropertyValue);
    return forceScanPropertyValue != null;
  }

  private List<String> getFromCache(final File cacheFile) {
    final List<String> stringList = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
      String nextLine;
      while ((nextLine = reader.readLine()) != null) {
        stringList.add(nextLine);
      }
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException("File not found: " + cacheFile.getAbsoluteFile(), e);
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Error while reading file: " + cacheFile.getAbsoluteFile(), e);
    }
    return stringList;
  }

  @SuppressWarnings("unchecked")
  private Set<String> getByScanning(final String annotationClassName) {
    Set<Class<?>> annotated;
    try {
      annotated = AnnotationReflector.getDefaultReflector().getReflector().getTypesAnnotatedWith((Class<? extends Annotation>) Class.forName(annotationClassName));
    } catch (final ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Annotation " + annotationClassName + " not found", ex);
    }
    final Set<String> annotatedNames = new LinkedHashSet<>();
    for (final Class<?> clazz : annotated) {
      annotatedNames.add(clazz.getName());
    }
    LOGGER.debug("Found {} classes containing annotation: {}", annotated.size(), annotated);
    return annotatedNames;
  }

}
