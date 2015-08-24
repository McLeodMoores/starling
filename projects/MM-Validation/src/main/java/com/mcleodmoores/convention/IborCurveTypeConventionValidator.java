/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks that there is a single, unique {@link IborIndexConvention} available from the source for each ibor
 * curve type in a group.
 */
public final class IborCurveTypeConventionValidator extends ConventionValidator<CurveGroupConfiguration, IborIndexConvention> {
  /** A static instance */
  private static final ConventionValidator<CurveGroupConfiguration, IborIndexConvention> INSTANCE = new IborCurveTypeConventionValidator();

  /**
   * Gets an instance.
   * @return  an instance
   */
  public static ConventionValidator<CurveGroupConfiguration, IborIndexConvention> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IborCurveTypeConventionValidator() {
  }

  @Override
  public ConventionValidationInfo<IborIndexConvention> validate(final CurveGroupConfiguration configuration, final VersionCorrection versionCorrection,
      final ConventionSource conventionSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    final Map<String, List<? extends CurveTypeConfiguration>> namesToConfigurations = configuration.getTypesForCurves();
    final Collection<IborIndexConvention> validated = new HashSet<>();
    final Collection<ExternalId> missingConventions = new HashSet<>();
    final Collection<ExternalId> duplicatedConventions = new HashSet<>();
    final Collection<Convention> unsupportedConventions = new HashSet<>();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : namesToConfigurations.entrySet()) {
      for (final CurveTypeConfiguration type : entry.getValue()) {
        if (type instanceof IborCurveTypeConfiguration) {
          final IborCurveTypeConfiguration iborType = (IborCurveTypeConfiguration) type;
          final ExternalId conventionId = iborType.getConvention();
          try {
            final Collection<Convention> conventions = conventionSource.get(conventionId.toBundle(), versionCorrection);
            if (conventions == null || conventions.size() == 0) {
              missingConventions.add(conventionId);
            } else if (conventions.size() > 1) {
              duplicatedConventions.add(conventionId);
            } else {
              final Convention convention = Iterables.getOnlyElement(conventions);
              if (convention instanceof IborIndexConvention) {
                validated.add((IborIndexConvention) convention);
              } else {
                unsupportedConventions.add(convention);
              }
            }
          } catch (final DataNotFoundException e) {
            missingConventions.add(conventionId);
          }
        }
      }
    }
    return new ConventionValidationInfo<>(IborIndexConvention.class, validated, missingConventions, duplicatedConventions, unsupportedConventions);
  }
}
