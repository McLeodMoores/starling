/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;

/**
 * Base class for classes that validate that particular criteria were satisfied when the configuration was created
 * e.g. that all curve definitions that are referenced are available from the config source.
 * @param <U>  the type of the configuration to be validated
 * @param <V>  the type of the configurations that have been validated
 */
public abstract class ConfigurationValidator<U, V> {

  /**
   * Validates a configuration, returning information about successfully validated configurations and duplicate, missing
   * and / or unsupported configurations that were referenced by the configuration being tested.
   * @param configuration  the configuration, not null
   * @param versionCorrection  the version correction, not null
   * @param configSource  the config source, not null
   * @return  the validation information
   */
  public abstract ConfigurationValidationInfo<V> validate(U configuration, VersionCorrection versionCorrection, ConfigSource configSource);
}
