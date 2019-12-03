/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.curve.functions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.financial.function.credit.cds.isda.IsdaYieldCurveFunction;
import com.mcleodmoores.financial.function.curve.NelsonSiegelBondCurveFunction;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.BeanDynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantSpreadCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationFunction;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveDefinitionFunction;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveMarketDataFunction;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveSpecificationFunction;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.SpreadCurveDefinition;
import com.opengamma.financial.analytics.model.curve.ConstantMultiCurveFunction;
import com.opengamma.financial.analytics.model.curve.FXMatrixFunction;
import com.opengamma.financial.analytics.model.curve.InflationIssuerProviderDiscountingFunction;
import com.opengamma.financial.analytics.model.curve.InflationProviderDiscountingFunction;
import com.opengamma.financial.analytics.model.curve.IssuerProviderDiscountingFunction;
import com.opengamma.financial.analytics.model.curve.IssuerProviderInterpolatedFunction;
import com.opengamma.financial.analytics.model.curve.MultiCurveDiscountingFunction;
import com.opengamma.financial.analytics.model.curve.MultiCurveInterpolatedFunction;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhiteDiscountingFunction;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions that create curves.
 */
public class CurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a source exposing the functions.
   *
   * @return the source
   */
  public static FunctionConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }

  public static FunctionConfigurationSource providers(final ConfigMaster configMaster, final Map<String, CurveType> curveTypes) {
    return new BeanDynamicFunctionConfigurationSource(ConfigMasterChangeProvider.of(configMaster)) {

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        final Providers providers = new Providers();
        providers.setConfigMaster(configMaster);
        providers.setCurveInfo(curveTypes);
        return providers;
      }

      @Override
      protected boolean isPropogateEvent(final ChangeEvent event) {
        final String type = event.getObjectId().getValue();
        return CurveConstructionConfiguration.class.getName().equals(type) || CurveDefinition.class.getName().equals(type)
            || InterpolatedCurveDefinition.class.getName().equals(type) || SpreadCurveDefinition.class.getName().equals(type)
            || ConstantCurveDefinition.class.getName().equals(type) || ConstantSpreadCurveDefinition.class.getName().equals(type)
            || CurveNodeIdMapper.class.getName().equals(type);
      }
    };
  }

  public static class Providers extends VersionedFunctionConfigurationBean {

    private final Map<String, CurveType> _configurationTypes = new HashMap<>();

    public void setCurveInfo(final String configurationName, final CurveType type) {
      _configurationTypes.put(configurationName, ArgumentChecker.notNull(type, "type"));
    }

    public void setCurveInfo(final Map<String, CurveType> configurationTypes) {
      _configurationTypes.clear();
      _configurationTypes.putAll(configurationTypes);
    }

    private ConfigMaster _configMaster;

    public void setConfigMaster(final ConfigMaster configMaster) {
      _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
    }

    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<String, CurveType> entry : _configurationTypes.entrySet()) {
        final String config = entry.getKey();
        final CurveType type = entry.getValue();
        switch (type) {
          case BOND:
            functions.add(functionConfiguration(IssuerProviderDiscountingFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            functions.add(functionConfiguration(NelsonSiegelBondCurveFunction.class, config));
            addCurveFunctions(functions, Collections.<Class<? extends AbstractCurveDefinition>> singletonList(InterpolatedCurveDefinition.class), config);
            break;
          case BOND_INTERPOLATED:
            functions.add(functionConfiguration(IssuerProviderInterpolatedFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Collections.<Class<? extends AbstractCurveDefinition>> singletonList(InterpolatedCurveDefinition.class), config);
            break;
          case CONSTANT:
            functions.add(functionConfiguration(ConstantMultiCurveFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Collections.<Class<? extends AbstractCurveDefinition>> singletonList(ConstantCurveDefinition.class), config);
            break;
          case DISCOUNTING:
            functions.add(functionConfiguration(MultiCurveDiscountingFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Arrays.asList(InterpolatedCurveDefinition.class, ConstantSpreadCurveDefinition.class, SpreadCurveDefinition.class),
                config);
            break;
          case HULL_WHITE:
            functions.add(functionConfiguration(HullWhiteDiscountingFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Arrays.asList(InterpolatedCurveDefinition.class, ConstantSpreadCurveDefinition.class, SpreadCurveDefinition.class),
                config);
            break;
          case INFLATION:
            functions.add(functionConfiguration(InflationProviderDiscountingFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Arrays.asList(InterpolatedCurveDefinition.class, ConstantSpreadCurveDefinition.class, SpreadCurveDefinition.class),
                config);
            break;
          case INFLATION_BOND:
            functions.add(functionConfiguration(InflationIssuerProviderDiscountingFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Arrays.asList(InterpolatedCurveDefinition.class, ConstantSpreadCurveDefinition.class, SpreadCurveDefinition.class),
                config);
            break;
          case INTERPOLATED:
            functions.add(functionConfiguration(MultiCurveInterpolatedFunction.class, config));
            functions.add(functionConfiguration(FXMatrixFunction.class, config));
            addCurveFunctions(functions, Collections.<Class<? extends AbstractCurveDefinition>> singletonList(InterpolatedCurveDefinition.class), config);
            break;
          case ISDA:
            functions.add(functionConfiguration(IsdaYieldCurveFunction.class, config));
            addCurveFunctions(functions, Collections.<Class<? extends AbstractCurveDefinition>> singletonList(InterpolatedCurveDefinition.class), config);
            break;
          default:
            throw new IllegalArgumentException("Unhandled curve type " + type);
        }
        functions.add(functionConfiguration(CurveConstructionConfigurationFunction.class, config));
      }
    }

    private void addCurveFunctions(final List<FunctionConfiguration> functions, final List<Class<? extends AbstractCurveDefinition>> types,
        final String config) {
      final ConfigSearchRequest<CurveConstructionConfiguration> cccRequest = new ConfigSearchRequest<>(CurveConstructionConfiguration.class);
      cccRequest.setName(config);
      cccRequest.setVersionCorrection(getVersionCorrection());
      final ConfigSearchResult<CurveConstructionConfiguration> cccResult = getConfigMaster().search(cccRequest);
      for (final ConfigItem<CurveConstructionConfiguration> cccItem : cccResult.getValues()) {
        final List<CurveGroupConfiguration> groups = cccItem.getValue().getCurveGroups();
        for (final CurveGroupConfiguration group : groups) {
          final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = group.getTypesForCurves();
          for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : curveTypes.entrySet()) {
            for (final Class<? extends AbstractCurveDefinition> definition : types) {
              final ConfigSearchRequest<?> dRequest = new ConfigSearchRequest<>();
              dRequest.setVersionCorrection(getVersionCorrection());
              dRequest.setType(definition);
              dRequest.setName(entry.getKey());
              final ConfigSearchResult<?> dResult = getConfigMaster().search(dRequest);
              for (final ConfigItem<?> dItem : dResult.getValues()) {
                functions.add(functionConfiguration(CurveMarketDataFunction.class, dItem.getName()));
                functions.add(functionConfiguration(CurveDefinitionFunction.class, dItem.getName()));
                functions.add(functionConfiguration(CurveSpecificationFunction.class, dItem.getName()));
              }
            }
          }
        }
      }
    }

  }

  public enum CurveType {
    /**
     * Uses discounting to calculate bond curves.
     */
    BOND,
    /**
     * Direct interpolation of market data inputs for bond curves.
     */
    BOND_INTERPOLATED,
    /**
     * Produces a constant yield curve.
     */
    CONSTANT,
    /**
     * Uses discounting to calculate yield curves.
     */
    DISCOUNTING,
    /**
     * Uses the Hull-White method to provide a convexity adjustment.
     */
    HULL_WHITE,
    /**
     * Uses discounting to calculate inflation curves.
     */
    INFLATION,
    /**
     * Uses discounting to calculate inflation bond curves.
     */
    INFLATION_BOND,
    /**
     * Direct interpolation of market data inputs for yield curves.
     */
    INTERPOLATED,
    /**
     * Uses the ISDA method to calculate yield curves.
     */
    ISDA,
  }
}
