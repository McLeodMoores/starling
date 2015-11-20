/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * This class checks that the references in {@link DiscountingCurveTypeConfiguration}s are currencies, which are currently
 * the only reference type supported.
 */
public final class DiscountingCurveTypeReferenceValidator extends ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> {
  /** A static instance */
  private static final ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> INSTANCE = new DiscountingCurveTypeReferenceValidator();

  /**
   * Returns a static instance.
   * @return  an instance
   */
  public static ConfigurationValidator<DiscountingCurveTypeConfiguration, Currency> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private DiscountingCurveTypeReferenceValidator() {
  }

  @Override
  public ConfigurationValidationInfo<Currency> validate(final DiscountingCurveTypeConfiguration configuration, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final String reference = configuration.getReference();
    final Collection<Currency> configurations = new HashSet<>();
    final Collection<Object> unsupportedReferences = new HashSet<>();
    try {
      final Currency currency = Currency.of(reference);
      configurations.add(currency);
    } catch (final IllegalArgumentException e) {
      unsupportedReferences.add(reference);
    }
    return new ConfigurationValidationInfo<>(Currency.class, configurations, Collections.<String, Class<?>>emptyMap(), Collections.<Object>emptySet(),
        unsupportedReferences);
  }
}
