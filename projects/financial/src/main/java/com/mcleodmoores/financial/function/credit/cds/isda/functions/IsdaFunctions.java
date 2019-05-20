/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.credit.cds.isda.CreditCurveDefinitionFunction;
import com.mcleodmoores.financial.function.credit.cds.isda.CreditCurveMarketDataFunction;
import com.mcleodmoores.financial.function.credit.cds.isda.CreditCurveSpecificationFunction;
import com.mcleodmoores.financial.function.credit.cds.isda.IsdaCdsAnalyticsFunction;
import com.mcleodmoores.financial.function.credit.cds.isda.IsdaCreditCurveFunction;
import com.mcleodmoores.financial.function.defaults.CdsPerCurrencyDefaults;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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

  // /**
  // * Returns a configuration populated with yield curve building functions.
  // *
  // * @param configMaster
  // * a config master
  // * @return a populated configuration
  // */
  // public static FunctionConfigurationSource providers(final ConfigMaster configMaster) {
  // return new BeanDynamicFunctionConfigurationSource(ConfigMasterChangeProvider.of(configMaster)) {
  //
  // @Override
  // protected VersionedFunctionConfigurationBean createConfiguration() {
  // final Providers providers = new Providers();
  // providers.setConfigMaster(configMaster);
  // return providers;
  // }
  //
  // @Override
  // protected boolean isPropogateEvent(final ChangeEvent event) {
  // return Providers.isMonitoredType(event.getObjectId().getValue());
  // }
  // };
  // }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(IsdaCreditCurveFunction.class));
    functions.add(functionConfiguration(CreditCurveMarketDataFunction.class));
    functions.add(functionConfiguration(CreditCurveSpecificationFunction.class));
    functions.add(functionConfiguration(CreditCurveDefinitionFunction.class));
    functions.add(functionConfiguration(IsdaCdsAnalyticsFunction.class));
  }

  // /**
  // * Function configuration source for ISDA yield curve functions based on the items in a {@link ConfigMaster}.
  // */
  // public static class Providers extends VersionedFunctionConfigurationBean {
  // private ConfigMaster _configMaster;
  //
  // /**
  // * Sets the config master.
  // *
  // * @param configMaster
  // * a config master, not null
  // */
  // public void setConfigMaster(final ConfigMaster configMaster) {
  // _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
  // }
  //
  // /**
  // * Gets the config master.
  // *
  // * @return the config master
  // */
  // public ConfigMaster getConfigMaster() {
  // return _configMaster;
  // }
  //
  // /**
  // * Monitored types are {@link CurveConstructionConfiguration}, {@link CurveNodeIdMapper} and {@link InterpolatedCurveDefinition}.
  // *
  // * @param type
  // * the type
  // * @return true if the type is a monitored type
  // */
  // static boolean isMonitoredType(final String type) {
  // return CurveConstructionConfiguration.class.getName().equals(type) || CurveNodeIdMapper.class.getName().equals(type)
  // || InterpolatedCurveDefinition.class.getName().equals(type);
  // }
  //
  // @Override
  // protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  // final ConfigSearchRequest<CurveDefinition> request = new ConfigSearchRequest<>();
  // request.setVersionCorrection(getVersionCorrection());
  // request.setType(CurveConstructionConfiguration.class);
  // for (final ConfigDocument doc : ConfigSearchIterator.iterable(getConfigMaster(), request)) {
  // final String name = doc.getName();
  // final CurveConstructionConfiguration config = (CurveConstructionConfiguration) doc.getConfig().getValue();
  // if (config.getCurveGroups().size() == 2) {
  // final CurveGroupConfiguration group0 = config.getCurveGroups().get(0);
  // final CurveGroupConfiguration group1 = config.getCurveGroups().get(1);
  // if (group0.getTypesForCurves().size() == 1 && group1.getTypesForCurves().size() == 1) {
  // final List<? extends CurveTypeConfiguration> types0 = group0.getTypesForCurves().values().iterator().next();
  // final List<? extends CurveTypeConfiguration> types1 = group1.getTypesForCurves().values().iterator().next();
  // if (types0.size() == 1 && types1.size() == 1) {
  // if (types0.get(0) instanceof DiscountingCurveTypeConfiguration && types1.get(0) instanceof IborCurveTypeConfiguration
  // || types1.get(0) instanceof DiscountingCurveTypeConfiguration && types0.get(0) instanceof IborCurveTypeConfiguration) {
  // functions.add(functionConfiguration(IsdaYieldCurveFunction.class, name));
  // }
  // }
  // }
  // } else if (config.getCurveGroups().size() == 1) {
  // for (final CurveGroupConfiguration group : config.getCurveGroups()) {
  // for (final List<? extends CurveTypeConfiguration> types : group.getTypesForCurves().values()) {
  // if (types.size() == 2) {
  // boolean include = true;
  // for (final CurveTypeConfiguration type : types) {
  // if (!(type instanceof DiscountingCurveTypeConfiguration || type instanceof IborCurveTypeConfiguration)) {
  // include = false;
  // }
  // }
  // if (include) {
  // functions.add(functionConfiguration(IsdaYieldCurveFunction.class, name));
  // }
  // }
  // }
  // }
  // }
  // }
  // }
  // }

  /**
   * Provides default property values for CDS.
   */
  public static class CdsDefaults extends AbstractFunctionConfigurationBean {

    /**
     * Provides per-currency property values for CDS.
     */
    public static class CurrencyInfo implements InitializingBean {
      private String _curveExposuresName;

      /**
       * Sets the curve exposure function name.
       *
       * @param curveExposuresName
       *          the name, not null
       */
      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      /**
       * Gets the curve exposure function name.
       *
       * @return the name
       */
      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
      }
    }

    private final Map<Currency, CurrencyInfo> _perCurrencyInfo = new HashMap<>();

    /**
     * Sets the per-currency property values.
     *
     * @param info
     *          the values
     */
    public void setCurrencyInfo(final Map<Currency, CurrencyInfo> info) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(info);
    }

    /**
     * Adds functions providing default values.
     *
     * @param functions
     *          a list of functions
     */
    protected void addDefaults(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<Currency, CurrencyInfo> entry : _perCurrencyInfo.entrySet()) {
        final String[] args = { entry.getKey().getCode(), entry.getValue().getCurveExposuresName() };
        functions.add(functionConfiguration(CdsPerCurrencyDefaults.class, args));
      }
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      addDefaults(functions);
    }
  }
}
