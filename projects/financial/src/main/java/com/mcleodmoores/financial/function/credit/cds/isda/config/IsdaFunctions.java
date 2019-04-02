/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda.config;

import java.util.List;

import com.mcleodmoores.financial.function.credit.cds.isda.IsdaYieldCurveFunction;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.BeanDynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions that price instruments using the ISDA CDS model.
 */
public class IsdaFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a source exposing the functions.
   *
   * @return the source
   */
  public static FunctionConfigurationSource instance() {
    return new IsdaFunctions().getObjectCreating();
  }

  /**
   * Returns a configuration populated with yield curve building functions.
   *
   * @param configMaster
   *          a config master
   * @return a populated configuration
   */
  public static FunctionConfigurationSource providers(final ConfigMaster configMaster) {
    return new BeanDynamicFunctionConfigurationSource(ConfigMasterChangeProvider.of(configMaster)) {

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        final Providers providers = new Providers();
        providers.setConfigMaster(configMaster);
        return providers;
      }

      @Override
      protected boolean isPropogateEvent(final ChangeEvent event) {
        return Providers.isMonitoredType(event.getObjectId().getValue());
      }
    };
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }

  /**
   * Function configuration source for ISDA yield curve functions based on the items in a {@link ConfigMaster}.
   */
  public static class Providers extends VersionedFunctionConfigurationBean {
    private ConfigMaster _configMaster;

    /**
     * Sets the config master.
     *
     * @param configMaster
     *          a config master, not null
     */
    public void setConfigMaster(final ConfigMaster configMaster) {
      _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
    }

    /**
     * Gets the config master.
     *
     * @return the config master
     */
    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    /**
     * Monitored types are {@link CurveConstructionConfiguration}, {@link CurveNodeIdMapper} and {@link CurveDefinition}.
     *
     * @param type
     *          the type
     * @return true if the type is a monitored type
     */
    static boolean isMonitoredType(final String type) {
      return CurveConstructionConfiguration.class.getName().equals(type) || CurveNodeIdMapper.class.getName().equals(type)
          || CurveDefinition.class.getName().equals(type);
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<CurveDefinition> request = new ConfigSearchRequest<>();
      request.setVersionCorrection(getVersionCorrection());
      request.setType(CurveConstructionConfiguration.class);
      for (final ConfigDocument doc : ConfigSearchIterator.iterable(getConfigMaster(), request)) {
        final String name = doc.getName();
        final CurveConstructionConfiguration config = (CurveConstructionConfiguration) doc.getConfig().getValue();
        if (config.getCurveGroups().size() == 2) {
          final CurveGroupConfiguration group0 = config.getCurveGroups().get(0);
          final CurveGroupConfiguration group1 = config.getCurveGroups().get(1);
          if (group0.getTypesForCurves().size() == 1 && group1.getTypesForCurves().size() == 1) {
            final List<? extends CurveTypeConfiguration> types0 = group0.getTypesForCurves().values().iterator().next();
            final List<? extends CurveTypeConfiguration> types1 = group1.getTypesForCurves().values().iterator().next();
            if (types0.size() == 1 && types1.size() == 1) {
              if (types0.get(0) instanceof DiscountingCurveTypeConfiguration && types1.get(0) instanceof IborCurveTypeConfiguration
                  || types1.get(0) instanceof DiscountingCurveTypeConfiguration && types0.get(0) instanceof IborCurveTypeConfiguration) {
                functions.add(functionConfiguration(IsdaYieldCurveFunction.class, name));
              }
            }
          }
        } else if (config.getCurveGroups().size() == 1) {
          for (final CurveGroupConfiguration group : config.getCurveGroups()) {
            for (final List<? extends CurveTypeConfiguration> types : group.getTypesForCurves().values()) {
              if (types.size() == 2) {
                boolean include = true;
                for (final CurveTypeConfiguration type : types) {
                  if (!(type instanceof DiscountingCurveTypeConfiguration || type instanceof IborCurveTypeConfiguration)) {
                    include = false;
                  }
                }
                if (include) {
                  functions.add(functionConfiguration(IsdaYieldCurveFunction.class, name));
                }
              }
            }
          }
        }
      }
    }
  }
}
