/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.curve.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mcleodmoores.validation.ConfigurationValidationInfo;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 *
 */
//TODO exogenous configs - should check that there's no clash with discounting, ibor, etc.
public class DiscountingCurveTypeValidator extends ConfigurationValidator<CurveGroupConfiguration, Currency> {

  @Override
  protected ConfigurationValidationInfo<Currency> validate(final CurveGroupConfiguration configuration, final VersionCorrection versionCorrection, final ConfigSource configSource) {
    //TODO check whether there's entries in the currency pairs and currency matrix?
    final Map<String, List<? extends CurveTypeConfiguration>> namesToConfigurations = configuration.getTypesForCurves();
    final Set<Currency> configurations = new HashSet<>();
    final Set<String> unsupportedCurrencies = new HashSet<>();
    final Set<String> duplicatedCurrencies = new HashSet<>();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : namesToConfigurations.entrySet()) {
      if (entry.getValue() instanceof DiscountingCurveTypeConfiguration) {
        final DiscountingCurveTypeConfiguration type = (DiscountingCurveTypeConfiguration) entry.getValue();
        final String reference = type.getReference();
        try {
          final Currency currency = Currency.of(reference);
          if (configurations.contains(currency)) {
            duplicatedCurrencies.add(reference);
          } else {
            configurations.add(currency);
          }
        } catch (final IllegalArgumentException e) {
          unsupportedCurrencies.add(reference);
        }
      }
    }
    return new ConfigurationValidationInfo<>(Currency.class, configurations, duplicatedCurrencies, unsupportedCurrencies);
  }
}
