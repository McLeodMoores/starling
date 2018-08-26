/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

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
import com.opengamma.core.config.Config;

/**
 * Provides all supported configuration types
 */
public final class ConfigTypesProvider {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTypesProvider.class);
  /**
   * Singleton instance.
   */
  private static final ConfigTypesProvider INSTANCE = new ConfigTypesProvider();

  /**
   * Map of config types.
   */
  private final ImmutableSortedMap<String, Class<?>> _configTypeMap;
  /**
   * Map of config descriptions.
   */
  private final ImmutableSortedMap<String, String> _configDescriptionMap;
  /**
   * Map of config groups.
   */
  private final Map<String, Map<String, String>> _configGroupMap;

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   *
   * @return the provider, not null
   */
  public static ConfigTypesProvider getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor
   */
  private ConfigTypesProvider() {
    _configGroupMap = new TreeMap<>();
    final Map<String, Class<?>> result = Maps.newHashMap();
    final ImmutableSortedMap.Builder<String, String> descriptions = ImmutableSortedMap.naturalOrder();
    final AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    final Set<Class<?>> configClasses = reflector.getReflector().getTypesAnnotatedWith(Config.class);
    for (final Class<?> configClass : configClasses) {
      final Config configValueAnnotation = configClass.getAnnotation(Config.class);
      if (configValueAnnotation != null) {
        // extract config type
        Class<?> configType = configValueAnnotation.searchType();
        if (configType == Object.class) {
          configType = configClass;
        }
        // extract grouping
        final String group = configValueAnnotation.group();
        // extract description
        String description = configValueAnnotation.description();
        if (description.length() == 0) {
          description = configType.getSimpleName();
        }
        // store
        final Class<?> old = result.put(configType.getSimpleName(), configType);
        if (old != null) {
          LOGGER.warn("Two classes exist with the same name: " + configType.getSimpleName());
        }
        descriptions.put(configType.getSimpleName(), description);
        if (_configGroupMap.containsKey(group)) {
          _configGroupMap.get(group).put(configType.getSimpleName(), description);
        } else {
          final Map<String, String> value = new TreeMap<>();
          value.put(configType.getSimpleName(), description);
          _configGroupMap.put(group, value);
        }

      }
    }
    _configTypeMap = ImmutableSortedMap.copyOf(result);
    _configDescriptionMap = descriptions.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of config keys.
   *
   * @return the types, not null
   */
  public ImmutableSortedSet<String> getConfigTypes() {
    return ImmutableSortedSet.copyOf(_configTypeMap.keySet());
  }

  /**
   * Gets the map of config types by short key.
   *
   * @return the map, not null
   */
  public ImmutableSortedMap<String, Class<?>> getConfigTypeMap() {
    return _configTypeMap;
  }

  /**
   * Gets the map of config descriptions by short key.
   *
   * @return the map, not null
   */
  public ImmutableSortedMap<String, String> getDescriptionMap() {
    return _configDescriptionMap;
  }

  /**
   * Gets the map of config groups by short key.
   *
   * @return the map, not null
   */
  public Map<String, Map<String, String>> getGroupMap() {
    return _configGroupMap;
  }

  /**
   * Gets the description for a type.
   *
   * @param type  the type, not null
   * @return the description, not null
   */
  public String getDescription(final Class<?> type) {
    final String key = HashBiMap.create(_configTypeMap).inverse().get(type);
    String description = null;
    if (key != null) {
      description = _configDescriptionMap.get(key);
    }
    return description != null ? description : type.getSimpleName();
  }

}
