/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.opengamma.util.ArgumentChecker;

//TODO would it be useful to have the ability to say that all collections are empty?
public class ConfigurationValidationInfo<T> {
  private final Class<T> _type;
  private final Collection<? extends T> _configurations;
  /** The names of missing underlying configurations */
  private final Collection<String> _missingConfigurationNames;
  /** The names of duplicated underlying configurations */
  private final Collection<String> _duplicatedConfigurationNames;
  private final Collection<?> _unsupportedConfigurationNames;

  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurationNames  the names of missing curve construction configurations, not null
   * @param duplicatedConfigurationNames  the names of duplicated curve construction configurations, not null
   */
  public ConfigurationValidationInfo(final Class<T> type, final Collection<? extends T> configurations, final Collection<String> missingConfigurationNames,
      final Collection<String> duplicatedConfigurationNames) {
    this(type, configurations, missingConfigurationNames, duplicatedConfigurationNames, Collections.<String>emptySet());
  }
  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurationNames  the names of missing curve construction configurations, not null
   * @param duplicatedConfigurationNames  the names of duplicated curve construction configurations, not null
   */
  public ConfigurationValidationInfo(final Class<T> type, final Collection<? extends T> configurations, final Collection<String> missingConfigurationNames,
      final Collection<String> duplicatedConfigurationNames, final Collection<?> unsupportedConfigurationName) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(configurations, "configurations");
    ArgumentChecker.notNull(missingConfigurationNames, "missingConfigurationNames");
    ArgumentChecker.notNull(duplicatedConfigurationNames, "duplicatedConfigurationNames");
    ArgumentChecker.notNull(unsupportedConfigurationName, "unsupportedConfigurationName");
    _type = type;
    _configurations = configurations;
    _missingConfigurationNames = missingConfigurationNames;
    _duplicatedConfigurationNames = duplicatedConfigurationNames;
    _unsupportedConfigurationNames = unsupportedConfigurationName;
  }

  public Class<T> getType() {
    return _type;
  }

  public Collection<T> getValidatedConfigurations() {
    return Collections.unmodifiableCollection(_configurations);
  }

  /**
   * Returns an unmodifiable collection of missing configuration names.
   * @return  the missing configuration names
   */
  public Collection<String> getMissingConfigurationNames() {
    return Collections.unmodifiableCollection(_missingConfigurationNames);
  }

  /**
   * Returns an unmodifiable collection of duplicated configuration names.
   * @return  the duplicated configuration names
   */
  public Collection<String> getDuplicatedConfigurationNames() {
    return Collections.unmodifiableCollection(_duplicatedConfigurationNames);
  }

  public Collection<?> getUnsupportedConfigurations() {
    return Collections.unmodifiableCollection(_unsupportedConfigurationNames);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_configurations);
    result = prime * result + Objects.hashCode(_duplicatedConfigurationNames);
    result = prime * result + Objects.hashCode(_missingConfigurationNames);
    result = prime * result * Objects.hashCode(_unsupportedConfigurationNames);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ConfigurationValidationInfo)) {
      return false;
    }
    final ConfigurationValidationInfo<?> other = (ConfigurationValidationInfo<?>) obj;
    if (!Objects.equals(_missingConfigurationNames, other._missingConfigurationNames)) {
      return false;
    }
    if (!Objects.equals(_duplicatedConfigurationNames, other._duplicatedConfigurationNames)) {
      return false;
    }
    if (!Objects.equals(_unsupportedConfigurationNames, other._unsupportedConfigurationNames)) {
      return false;
    }
    if (!Objects.equals(_configurations, other._configurations)) {
      return false;
    }
    return true;
  }

}