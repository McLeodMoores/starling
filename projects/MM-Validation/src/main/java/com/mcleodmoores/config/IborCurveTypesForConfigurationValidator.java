/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.mcleodmoores.config.IborCurveTypesForConfigurationValidator.IborCurveInformation;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * This class checks that there are no duplicated ibor curve references in any of the {@link IborCurveTypeConfiguration}s in the
 * configuration, including all exogenous configurations.
 */
public final class IborCurveTypesForConfigurationValidator extends ConfigurationValidator<CurveConstructionConfiguration, IborCurveInformation> {
  /** A static instance */
  private static final ConfigurationValidator<CurveConstructionConfiguration, IborCurveInformation> INSTANCE =
      new IborCurveTypesForConfigurationValidator();

  /**
   * Returns a static instance.
   * @return  an instance
   */
  public static ConfigurationValidator<CurveConstructionConfiguration, IborCurveInformation> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IborCurveTypesForConfigurationValidator() {
  }

  @Override
  public ConfigurationValidationInfo<IborCurveInformation> validate(final CurveConstructionConfiguration configuration,
      final VersionCorrection versionCorrection, final ConfigSource configSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Set<IborCurveInformation> references = new HashSet<>();
    final Set<Object> duplicatedReferences = new HashSet<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        for (final CurveTypeConfiguration type : entry.getValue()) {
          if (type instanceof IborCurveTypeConfiguration) {
            final IborCurveTypeConfiguration iborType = (IborCurveTypeConfiguration) type;
            final ExternalId reference = iborType.getConvention();
            final Tenor tenor = iborType.getTenor();
            final IborCurveInformation ibor = new IborCurveInformation(reference, tenor);
            if (references.remove(ibor)) {
              duplicatedReferences.add(ibor);
            } else {
              if (!duplicatedReferences.contains(ibor)) {
                references.add(ibor);
              }
            }
          }
        }
      }
    }
    if (configuration.getExogenousConfigurations().isEmpty()) {
      return new ConfigurationValidationInfo<>(IborCurveInformation.class, references, Collections.<String, Class<?>>emptyMap(), duplicatedReferences);
    }
    for (final String name : configuration.getExogenousConfigurations()) {
      final Collection<ConfigItem<CurveConstructionConfiguration>> exogenousConfiguration =
          configSource.get(CurveConstructionConfiguration.class, name, versionCorrection);
      if (exogenousConfiguration.size() == 1) {
        final ConfigurationValidationInfo<IborCurveInformation> underlyingInfo =
            validate(Iterables.getOnlyElement(exogenousConfiguration).getValue(), versionCorrection, configSource);
        duplicatedReferences.addAll(underlyingInfo.getDuplicatedConfigurations());
        for (final IborCurveInformation validatedReference : underlyingInfo.getValidatedConfigurations()) {
          if (references.remove(validatedReference)) {
            duplicatedReferences.add(validatedReference);
          } else {
            references.add(validatedReference);
          }
        }
        for (final Object duplicatedReference : duplicatedReferences) {
          references.remove(duplicatedReference);
        }
      }
    }
    return new ConfigurationValidationInfo<>(IborCurveInformation.class, references, Collections.<String, Class<?>>emptyMap(), duplicatedReferences);
  }

  /**
   * Container for information about ibor curve types - the id of the convention or security and the index tenor. This is used instead of a
   * {@link com.opengamma.util.tuple.Pair} to avoid problems with generics.
   */
  public static class IborCurveInformation {
    /** The convention or security id */
    private final ExternalId _externalId;
    /** The index tenor */
    private final Tenor _tenor;

    /**
     * Creates an instance.
     * @param externalId  the convention or security id, not null
     * @param tenor  the index tenor, not null
     */
    public IborCurveInformation(final ExternalId externalId, final Tenor tenor) {
      _externalId = ArgumentChecker.notNull(externalId, "externalId");
      _tenor = ArgumentChecker.notNull(tenor, "tenor");
    }

    /**
     * Gets the convention or security id.
     * @return  the id
     */
    public ExternalId getId() {
      return _externalId;
    }

    /**
     * Gets the tenor.
     * @return  the tenor
     */
    public Tenor getTenor() {
      return _tenor;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Objects.hashCode(_externalId);
      result = prime * result + Objects.hashCode(_tenor);
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof IborCurveInformation)) {
        return false;
      }
      final IborCurveInformation other = (IborCurveInformation) obj;
      return Objects.equals(_externalId, other._externalId) && Objects.equals(_tenor, other._tenor);
    }

  }
}
