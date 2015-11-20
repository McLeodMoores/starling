/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.opengamma.util.ArgumentChecker;

/**
 * A container for information about configurations that have been validated:
 * <ul>
 *  <li> The configuration that has been validated given a particular criterion, e.g. if all underlying
 *  configurations were present in the config source.</li>
 *  <li> The names of any configurations that were not found in the config source.</li>
 *  <li> The names of any configurations that were duplicated in the config source.</li>
 *  <li> The names of configurations that were found but that were not of the expected type.</li>
 * </ul>
 * @param <T> The type of the configuration that has been validated.
 */
public class ConfigurationValidationInfo<T> {
  /** The configuration that has been validated */
  private final Class<T> _type;
  /** The configurations that were validated */
  private final Collection<? extends T> _configurations;
  /** The names and types of missing configurations */
  private final Map<String, Class<?>> _missingConfigurations;
  /** The duplicated configurations */
  private final Collection<Object> _duplicatedConfigurations;
  /** The unsupported configurations */
  private final Collection<Object> _unsupportedConfigurations;

  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurations  a map from name to expected type of missing configurations, not null.
   * @param duplicatedConfigurations  the duplicated configurations, not null
   */
  public ConfigurationValidationInfo(final Class<T> type, final Collection<? extends T> configurations,
      final Map<String, Class<?>> missingConfigurations, final Collection<Object> duplicatedConfigurations) {
    this(type, configurations, missingConfigurations, duplicatedConfigurations, Collections.<Object>emptySet());
  }

  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurations  a map from name to expected type of missing configurations, not null.
   * @param duplicatedConfigurations  the duplicated configurations, not null
   * @param unsupportedConfigurations  the unsupported configurations, not null
   */
  public ConfigurationValidationInfo(final Class<T> type, final Collection<? extends T> configurations,
      final Map<String, Class<?>> missingConfigurations, final Collection<Object> duplicatedConfigurations,
      final Collection<Object> unsupportedConfigurations) {
    _type = ArgumentChecker.notNull(type, "type");
    _configurations = ArgumentChecker.notNull(configurations, "configurations");
    _missingConfigurations = ArgumentChecker.notNull(missingConfigurations, "missingConfigurations");
    _duplicatedConfigurations = ArgumentChecker.notNull(duplicatedConfigurations, "duplicatedConfigurations");
    _unsupportedConfigurations = ArgumentChecker.notNull(unsupportedConfigurations, "unsupportedConfigurations");
  }

  /**
   * Gets the type of the configuration that is being validated.
   * @return  the type of the configuration
   */
  public Class<T> getType() {
    return _type;
  }

  /**
   * Gets an unmodifiable collections of the configurations that have been validated.
   * @return  the validated configurations
   */
  public Collection<T> getValidatedConfigurations() {
    return Collections.unmodifiableCollection(_configurations);
  }

  /**
   * Gets an unmodifiable map from missing configuration names to expected type.
   * @return  the missing configurations
   */
  public Map<String, Class<?>> getMissingConfigurations() {
    return Collections.unmodifiableMap(_missingConfigurations);
  }

  /**
   * Gets an unmodifiable collection of duplicated configurations.
   * @return  the duplicated configurations
   */
  public Collection<Object> getDuplicatedConfigurations() {
    return Collections.unmodifiableCollection(_duplicatedConfigurations);
  }

  /**
   * Gets an unmodifiable collection of unsupported configurations.
   * @return  the unsupported configurations
   */
  public Collection<Object> getUnsupportedConfigurations() {
    return Collections.unmodifiableCollection(_unsupportedConfigurations);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_configurations);
    result = prime * result + Objects.hashCode(_duplicatedConfigurations);
    result = prime * result + Objects.hashCode(_missingConfigurations);
    result = prime * result + Objects.hashCode(_unsupportedConfigurations);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConfigurationValidationInfo)) {
      return false;
    }
    final ConfigurationValidationInfo<?> other = (ConfigurationValidationInfo<?>) obj;
    if (!Objects.equals(_configurations, other._configurations)) {
      return false;
    }
    if (!Objects.equals(_missingConfigurations, other._missingConfigurations)) {
      return false;
    }
    if (!Objects.equals(_duplicatedConfigurations, other._duplicatedConfigurations)) {
      return false;
    }
    if (!Objects.equals(_unsupportedConfigurations, other._unsupportedConfigurations)) {
      return false;
    }
    return true;
  }

}