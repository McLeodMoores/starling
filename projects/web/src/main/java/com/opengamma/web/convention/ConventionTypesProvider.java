/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.convention;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.opengamma.core.convention.ConventionMetaData;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ClassUtils;

/**
 * Provides all supported convention types.
 */
public final class ConventionTypesProvider {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionTypesProvider.class);
  /**
   * Singleton instance.
   */
  private static final ConventionTypesProvider INSTANCE = new ConventionTypesProvider();

  /**
   * Map of convention types.
   */
  private final ImmutableSortedMap<String, Class<? extends ManageableConvention>> _conventionTypeMap;
  /**
   * Map of convention descriptions.
   */
  private final ImmutableSortedMap<String, String> _conventionDescriptionMap;
  /**
   * Map of convention group metadata.
   */
  private final ImmutableSortedMap<String, Map<String, String>> _conventionGroups;
  /**
   * Map of convention detailed descriptions.
   */
  private final ImmutableSortedMap<String, Map<String, String>> _conventionDetails;

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   *
   * @return the provider, not null
   */
  public static ConventionTypesProvider getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor
   */
  private ConventionTypesProvider() {
    final Map<String, Class<? extends ManageableConvention>> result = Maps.newHashMap();
    final ImmutableSortedMap.Builder<String, String> descriptions = ImmutableSortedMap.naturalOrder();
    final Map<String, Map<String, String>> groups = new TreeMap<>();
    final Map<String, Map<String, String>> details = new TreeMap<>();
    final AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    final Set<Class<? extends ManageableConvention>> conventionClasses = reflector.getReflector().getSubTypesOf(ManageableConvention.class);
    for (final Class<? extends ManageableConvention> conventionClass : conventionClasses) {
      // ensure this class is fully loaded, to force static initialization
      ClassUtils.initClass(conventionClass);
      // find type
      if (Modifier.isAbstract(conventionClass.getModifiers())) {
        continue;
      }
      ConventionType type;
      try {
        type = (ConventionType) conventionClass.getDeclaredField("TYPE").get(null);
      } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
        LOGGER.warn("Convention class must declare a static variable 'TYPE' but none found: " + conventionClass.getName());
        continue;
      }
      // extract description
      final String description = type.getName().replaceAll(
          String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
      // store
      final Class<?> old = result.put(type.getName(), conventionClass);
      if (old != null) {
        LOGGER.warn("Two classes exist with the same name: " + conventionClass.getSimpleName());
        continue;
      }
      descriptions.put(type.getName(), description);
      // if there's an annotation, extract the information
      final ConventionMetaData annotation = conventionClass.getAnnotation(ConventionMetaData.class);
      if (annotation != null) {
        final Map<String, String> groupInfo = groups.get(annotation.group());
        if (groupInfo != null) {
          groupInfo.put(conventionClass.getSimpleName(), annotation.description());
        } else {
          final Map<String, String> data = new TreeMap<>();
          data.put(conventionClass.getSimpleName(), annotation.description());
          groups.put(annotation.group(), data);
        }
        final Map<String, String> groupDetail = details.get(annotation.group());
        if (groupDetail != null) {
          groupDetail.put(conventionClass.getSimpleName(), annotation.info());
        } else {
          final Map<String, String> data = new TreeMap<>();
          data.put(conventionClass.getSimpleName(), annotation.info());
          details.put(annotation.group(), data);
        }
      } else {
        LOGGER.info("No convention metadata for {}", conventionClass.getSimpleName());
      }
    }
    _conventionTypeMap = ImmutableSortedMap.copyOf(result);
    _conventionDescriptionMap = descriptions.build();
    _conventionGroups = ImmutableSortedMap.copyOf(groups);
    _conventionDetails = ImmutableSortedMap.copyOf(details);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of convention keys.
   *
   * @return the types, not null
   */
  public ImmutableSortedSet<String> getTypeSet() {
    return ImmutableSortedSet.copyOf(_conventionTypeMap.keySet());
  }

  /**
   * Gets the map of convention types by short key.
   *
   * @return the map, not null
   */
  public ImmutableSortedMap<String, Class<? extends ManageableConvention>> getTypeMap() {
    return _conventionTypeMap;
  }

  /**
   * Gets the map of convention descriptions by short key.
   *
   * @return the map, not null
   */
  public ImmutableSortedMap<String, String> getDescriptionMap() {
    return _conventionDescriptionMap;
  }

  /**
   * Gets the description for a type.
   *
   * @param clazz  the convention class, not null
   * @return the description, not null
   */
  public String getDescription(final Class<?> clazz) {
    final String key = HashBiMap.create(_conventionTypeMap).inverse().get(clazz);
    String description = null;
    if (key != null) {
      description = _conventionDescriptionMap.get(key);
    }
    return description != null ? description : clazz.getSimpleName();
  }

  /**
   * Gets a map from the group to the class / short description.
   *
   * @return  the convention descriptions, not null
   */
  public ImmutableSortedMap<String, Map<String, String>> getConventionDescriptions() {
    return _conventionGroups;
  }

  /**
   * Gets a map from the group to the class / detailed information.
   *
   * @return  the convention details, not null
   */
  public ImmutableSortedMap<String, Map<String, String>> getConventionDetails() {
    return _conventionDetails;
  }

  //TODO temporary method
  public ConventionType getConventionTypeForClassName(final String className) {
    for (final Map.Entry<String, Class<? extends ManageableConvention>> entry : _conventionTypeMap.entrySet()) {
      if (entry.getValue().getSimpleName().equals(className)) {
        return ConventionType.of(entry.getKey());
      }
    }
    return ConventionType.of(className);
  }
}
