/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.curve.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

//TODO would it be useful to have the ability to say that all collections are empty?
public abstract class SecurityValidationInfo<T extends Security> {
  private final Class<T> _type;
  private final Collection<T> _configurations;
  /** The names of missing underlying configurations */
  private final Collection<ExternalId> _missingConfigurationNames;
  /** The names of duplicated underlying configurations */
  private final Collection<ExternalId> _duplicatedConfigurationNames;
  private final Collection<?> _unsupportedConfigurationNames;

  //TODO maybe ExternalIdBundle
  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurationNames  the names of missing curve construction configurations, not null
   * @param duplicatedConfigurationNames  the names of duplicated curve construction configurations, not null
   */
  public SecurityValidationInfo(final Class<T> type, final Collection<T> configurations, final Collection<ExternalId> missingConfigurationNames,
      final Collection<ExternalId> duplicatedConfigurationNames) {
    this(type, configurations, missingConfigurationNames, duplicatedConfigurationNames, Collections.<String>emptySet());
  }
  /**
   * Creates an instance.
   * @param type  the type of the configurations being tested, not null
   * @param configurations  the configurations that were found, not null
   * @param missingConfigurationNames  the names of missing curve construction configurations, not null
   * @param duplicatedConfigurationNames  the names of duplicated curve construction configurations, not null
   */
  public SecurityValidationInfo(final Class<T> type, final Collection<T> configurations, final Collection<ExternalId> missingConfigurationNames,
      final Collection<ExternalId> duplicatedConfigurationNames, final Collection<?> unsupportedConfigurationName) {
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

  public Collection<T> getConfigurations() {
    return Collections.unmodifiableCollection(_configurations);
  }

  /**
   * Returns an unmodifiable collection of missing configuration names.
   * @return  the missing configuration names
   */
  public Collection<ExternalId> getMissingCurveConstructionConfigurationNames() {
    return Collections.unmodifiableCollection(_missingConfigurationNames);
  }

  /**
   * Returns an unmodifiable collection of duplicated configuration names.
   * @return  the duplicated configuration names
   */
  public Collection<ExternalId> getDuplicatedCurveConstructionConfigurationNames() {
    return Collections.unmodifiableCollection(_duplicatedConfigurationNames);
  }

  public Collection<?> getUnsupportedConfigurationNames() {
    return Collections.unmodifiableCollection(_unsupportedConfigurationNames);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_type);
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
    if (!(obj instanceof SecurityValidationInfo)) {
      return false;
    }
    final SecurityValidationInfo<?> other = (SecurityValidationInfo<?>) obj;
    if (!Objects.equals(_type, _type)) {
      return false;
    }
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