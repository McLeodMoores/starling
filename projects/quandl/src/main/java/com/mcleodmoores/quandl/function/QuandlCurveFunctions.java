/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.function;

import java.util.List;

import com.mcleodmoores.quandl.financial.curve.QuandlCurveHistoricalTimeSeriesFunction;
import com.mcleodmoores.quandl.financial.curve.QuandlFxMatrixFunction;
import com.mcleodmoores.quandl.financial.curve.QuandlMultiCurveDiscountingFunction;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.BeanDynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.VersionedFunctionConfigurationBean;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationFunction;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveDefinitionFunction;
import com.opengamma.financial.analytics.curve.CurveMarketDataFunction;
import com.opengamma.financial.analytics.curve.CurveSpecificationFunction;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.model.curve.CurveDefaults;
import com.opengamma.financial.analytics.timeseries.CurveConfigurationHistoricalTimeSeriesFunction;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * Adds functions that produce curves that use Quandl data.
 */
public class QuandlCurveFunctions extends VersionedFunctionConfigurationBean {

  /**
   * Gets an instance of this function configuration source.
   * @return The function configuration source
   */
  public static FunctionConfigurationSource instance() {
    return new QuandlCurveFunctions().getObjectCreating();
  }

  /**
   * Returns a function configuration source that sets up monitored types to allow functions to detect
   * changes in the configurations.
   * @param configMaster The config master
   * @return The function configuration source
   */
  public static FunctionConfigurationSource providers(final ConfigMaster configMaster) {
    return new BeanDynamicFunctionConfigurationSource(ConfigMasterChangeProvider.of(configMaster)) {

      @Override
      protected boolean isPropogateEvent(final ChangeEvent event) {
        return Providers.isMonitoredType(event.getObjectId().getValue());
      }

      @Override
      protected VersionedFunctionConfigurationBean createConfiguration() {
        final Providers providers = new Providers();
        providers.setConfigMaster(configMaster);
        return providers;
      }
    };
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functionConfigs) {
  }

  /**
   * A class that provides curve function configurations that use information from the config master during
   * initialization and compilation.
   */
  public static class Providers extends VersionedFunctionConfigurationBean {
    /** The config master */
    private ConfigMaster _configMaster;

    /**
     * Sets the config master.
     * @param configMaster The config master, not null
     */
    public void setConfigMaster(final ConfigMaster configMaster) {
      ArgumentChecker.notNull(configMaster, "configMaster");
      _configMaster = configMaster;
    }

    /**
     * Gets the config master.
     * @return The config master
     */
    public ConfigMaster getConfigMaster() {
      return _configMaster;
    }


    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final ConfigSearchRequest<?> searchRequest = new ConfigSearchRequest<>();
      searchRequest.setVersionCorrection(getVersionCorrection());
      final Class<?>[] curveConstructionConfigurationClasses = new Class[] {CurveConstructionConfiguration.class};
      for (final Class<?> clazz : curveConstructionConfigurationClasses) {
        searchRequest.setType(clazz);
        for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
          final String configName = configDocument.getName();
          functions.add(functionConfiguration(QuandlMultiCurveDiscountingFunction.class, configName));
          functions.add(functionConfiguration(QuandlFxMatrixFunction.class, configName));
          functions.add(functionConfiguration(CurveConstructionConfigurationFunction.class, configName));
        }
      }
      searchRequest.setType(InterpolatedCurveDefinition.class);
      searchRequest.setVersionCorrection(getVersionCorrection());
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(getConfigMaster(), searchRequest)) {
        final String curveName = configDocument.getName();
        functions.add(functionConfiguration(CurveMarketDataFunction.class, curveName));
        functions.add(functionConfiguration(CurveDefinitionFunction.class, curveName));
        functions.add(functionConfiguration(CurveSpecificationFunction.class, curveName));
      }
    }

    /**
     * Returns true if the configuration is a {@link CurveConstructionConfiguration}, {@link CurveDefinition} or
     * {@link InterpolatedCurveDefinition}.
     * @param type The type
     * @return True if the type is a monitored type.
     */
    protected static boolean isMonitoredType(final String type) {
      return CurveConstructionConfiguration.class.getName().equals(type) || CurveDefinition.class.getName().equals(type)
          || InterpolatedCurveDefinition.class.getName().equals(type);
    }
  }

  /**
   * A class that provides default values for parameters used in curve root-finding.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {
    /** The absolute tolerance */
    private double _absoluteTolerance;
    /** The relative tolerance */
    private double _relativeTolerance;
    /** The maximum number of iterations */
    private int _maxIterations;

    /**
     * Gets the absolute tolerance.
     * @return The absolute tolerance
     */
    public double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    /**
     * Sets the absolute tolerance.
     * @param absoluteTolerance The absolute tolerance, greater than zero
     */
    public void setAbsoluteTolerance(final double absoluteTolerance) {
      ArgumentChecker.isTrue(absoluteTolerance > 0, "Absolute tolerance must be greater than zero; have {}", absoluteTolerance);
      _absoluteTolerance = absoluteTolerance;
    }

    /**
     * Gets the relative tolerance.
     * @return The relative tolerance
     */
    public double getRelativeTolerance() {
      return _relativeTolerance;
    }

    /**
     * Sets the relative tolerance.
     * @param relativeTolerance The relative tolerance, greater than zero
     */
    public void setRelativeTolerance(final double relativeTolerance) {
      ArgumentChecker.isTrue(relativeTolerance > 0, "Relative tolerance must be greater than zero; have {}", relativeTolerance);
      _relativeTolerance = relativeTolerance;
    }

    /**
     * Gets the maximum number of iterations.
     * @return The maximum number of iterations
     */
    public double getMaximumIterations() {
      return _maxIterations;
    }

    /**
     * Sets the maximum number of iterations.
     * @param maxIterations The maximum number of iterations, greater than zero
     */
    public void setMaximumIterations(final int maxIterations) {
      ArgumentChecker.isTrue(maxIterations > 0, "Maximum iterations must be greater than zero; have {}", maxIterations);
      _maxIterations = maxIterations;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(CurveConfigurationHistoricalTimeSeriesFunction.class));
      functions.add(functionConfiguration(QuandlCurveHistoricalTimeSeriesFunction.class));
      final String[] defaults = new String[] {Double.toString(_absoluteTolerance), Double.toString(_relativeTolerance), Integer.toString(_maxIterations)};
      functions.add(functionConfiguration(CurveDefaults.class, defaults));
    }

  }
}
