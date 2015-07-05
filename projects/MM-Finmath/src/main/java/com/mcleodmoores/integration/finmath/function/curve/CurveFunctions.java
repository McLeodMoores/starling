/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.function.curve;

import java.util.List;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationFunction;
import com.opengamma.financial.analytics.curve.CurveDefinitionFunction;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveMarketDataFunction;
import com.opengamma.financial.analytics.curve.CurveSpecificationFunction;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.timeseries.CurveHistoricalTimeSeriesFunction;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(DiscountCurveFunction.class));
    functionConfigs.add(functionConfiguration(CurveHistoricalTimeSeriesFunction.class));
  }

  public static class Providers extends VersionedFunctionConfigurationBean {
    private ConfigMaster _configMaster;

    public void setConfigMaster(final ConfigMaster configMaster){
      ArgumentChecker.notNull(configMaster, "configMaster");
      _configMaster = configMaster;
    }

    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    protected void addCurveBuildingFunctions(final List<FunctionConfiguration> functions) {
      //TODO test that there's only a single curve in the configs
      final ConfigSearchRequest<? extends AbstractCurveDefinition> request = new ConfigSearchRequest<>();
      request.setVersionCorrection(getVersionCorrection());
      final Class<?>[] curveConstructionConfigurationClasses = new Class[] {CurveConstructionConfiguration.class};
      for (final Class<?> clazz : curveConstructionConfigurationClasses) {
        request.setType(clazz);
        for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), request)) {
          final String documentName = configDocument.getName();
          functions.add(functionConfiguration(CurveConstructionConfigurationFunction.class, documentName));
          final CurveConstructionConfiguration config = ((ConfigItem<CurveConstructionConfiguration>) configDocument.getConfig()).getValue();
          for (final CurveGroupConfiguration group : config.getCurveGroups()) {
            for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
              final String curveName = entry.getKey();
              functions.add(functionConfiguration(CurveMarketDataFunction.class, curveName));
              functions.add(functionConfiguration(CurveDefinitionFunction.class, curveName));
              functions.add(functionConfiguration(CurveSpecificationFunction.class, curveName));
            }
          }
        }
      }
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      addCurveBuildingFunctions(functions);
    }

    //TODO propagate events
  }
}
