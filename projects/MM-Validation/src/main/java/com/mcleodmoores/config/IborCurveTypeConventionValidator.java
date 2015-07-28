/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.mcleodmoores.convention.ConventionValidationInfo;
import com.mcleodmoores.convention.ConventionValidator;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
//TODO exogenous configs - should check that there's no clash with discounting, ibor, etc.
public class IborCurveTypeConventionValidator extends ConventionValidator<CurveGroupConfiguration, IborIndexConvention> {

  @Override
  protected ConventionValidationInfo<IborIndexConvention> validate(final CurveGroupConfiguration configuration, final VersionCorrection versionCorrection, final ConventionSource conventionSource) {
    final Map<String, List<? extends CurveTypeConfiguration>> namesToConfigurations = configuration.getTypesForCurves();
    final Collection<IborIndexConvention> configurations = new HashSet<>();
    final Collection<ExternalId> missingConventions = new HashSet<>();
    final Collection<ExternalId> duplicatedConventions = new HashSet<>();
    final Collection<Convention> unsupportedConventions = new HashSet<>();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : namesToConfigurations.entrySet()) {
      if (entry.getValue() instanceof IborCurveTypeConfiguration) {
        final IborCurveTypeConfiguration type = (IborCurveTypeConfiguration) entry.getValue();
        final ExternalId conventionId = type.getConvention();
        try {
          final Collection<Convention> conventions = conventionSource.get(conventionId.toBundle(), versionCorrection);
          if (conventions == null || conventions.size() == 0) {
            missingConventions.add(conventionId);
          } else if (conventions.size() > 1) {
            duplicatedConventions.add(conventionId);
          } else {
            final Convention convention = Iterables.getOnlyElement(conventions);
            if (convention instanceof IborIndexConvention) {
              configurations.add((IborIndexConvention) convention);
            } else {
              unsupportedConventions.add(convention);
            }
          }
        } catch (final DataNotFoundException e) {
          missingConventions.add(conventionId);
        }
      }
    }
    return new ConventionValidationInfo<>(IborIndexConvention.class, configurations, missingConventions,
        duplicatedConventions, unsupportedConventions);
  }
}
