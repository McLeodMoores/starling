/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.function;

import static com.opengamma.engine.value.ValuePropertyNames.DIVIDEND_TYPE_NONE;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * A function configuration that extends the standard OpenGamma configuration by adding default
 * values for
 */
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleStandardFunctionConfiguration.class);

  /** A map from equity ticker strings to equity ticker default values */
  private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();

  /**
   * Default constructor. Sets default values for the mark to market, cost of carry field, tolerances
   * for yield curve construction and equity option defaults.
   */
  public ExampleStandardFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
    setAbsoluteTolerance(0.0001);
    setRelativeTolerance(0.0001);
    setMaximumIterations(1000);
    setEquityOptionInfo();
  }

  /**
   * Gets an instance of this function configuration.
   * @return The instance
   */
  public static FunctionConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), multicurvePricingFunctions());
  }

  /**
   * Sets defaults for AAPL equity options.
   */
  protected void setEquityOptionInfo() {
    setEquityOptionInfo("AAPL", "USD");
  }

  /**
   * Creates empty default per-equity information objects for equity options.
   * @param ticker The equity ticker
   * @param curveCurrency The currency target of the discounting curve (usually, but not necessarily,
   * the currency of the equity).
   */
  protected void setEquityOptionInfo(final String ticker, final String curveCurrency) {
    final EquityInfo i = defaultEquityInfo(ticker);
    final String discountingCurveConfigName = "DefaultTwoCurve" + curveCurrency + "Config";
    i.setDiscountingCurve("model/equityoption", "Discounting");
    i.setDiscountingCurveConfig("model/equityoption", discountingCurveConfigName);
    i.setDiscountingCurveCurrency("model/equityoption", curveCurrency);
    i.setDividendType("model/equityoption", DIVIDEND_TYPE_NONE);
    i.setForwardCurve("model/equityoption", "Discounting");
    i.setForwardCurveCalculationMethod("model/equityoption", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    i.setForwardCurveInterpolator("model/equityoption", Interpolator1DFactory.DOUBLE_QUADRATIC);
    i.setForwardCurveLeftExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    i.setForwardCurveRightExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    i.setSurfaceCalculationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    i.setSurfaceInterpolationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.SPLINE);
    i.setVolatilitySurface("model/equityoption", "DEFAULT");
    setEquityInfo(ticker, i);
  }

  @Override
  protected FunctionConfigurationSource equityOptionFunctions() {
    final EquityOptionFunctions.EquityForwardDefaults forwardCurveDefaults = new EquityOptionFunctions.EquityForwardDefaults();
    setEquityOptionForwardCurveDefaults(forwardCurveDefaults);
    final EquityOptionFunctions.EquityOptionDefaults surfaceDefaults = new EquityOptionFunctions.EquityOptionDefaults();
    setEquityOptionSurfaceDefaults(surfaceDefaults);
    final FunctionConfigurationSource forwardCurveRepository = getRepository(forwardCurveDefaults);
    final FunctionConfigurationSource surfaceRepository = getRepository(surfaceDefaults);
    return CombiningFunctionConfigurationSource.of(forwardCurveRepository, surfaceRepository);
  }

  /**
   * Sets the per-equity forward curve defaults for equity option functions.
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionForwardCurveDefaults(final EquityOptionFunctions.EquityForwardDefaults defaults) {
    defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, EquityOptionFunctions.EquityInfo>() {
      @Override
      public EquityOptionFunctions.EquityInfo execute(final EquityInfo i) {
        final EquityOptionFunctions.EquityInfo d = new EquityOptionFunctions.EquityInfo();
        setEquityOptionForwardCurveDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Sets the paths for the per-equity ticker default values for the forward curve used
   * in pricing equity options. The keys are:
   * <ul>
   * <li> Forward curve interpolator = model/equityoption
   * <li> Forward curve left extrapolator = model/equityoption
   * <li> Forward curve right extrapolator = model/equityoption
   * <li> Forward curve = model/equityoption
   * <li> Forward curve calculation method = model/equityoption
   * <li> Discounting curve = model/equityoption
   * <li> Discounting curve configuration = model/equityoption
   * <li> Discounting curve currency = model/equityoption
   * <li> Dividend type = model/equityoption
   * </ul>
   * @param i The per-equity info
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionForwardCurveDefaults(final EquityInfo i, final EquityOptionFunctions.EquityInfo defaults) {
    defaults.setForwardCurveInterpolator(i.getForwardCurveInterpolator("model/equityoption"));
    defaults.setForwardCurveLeftExtrapolator(i.getForwardCurveLeftExtrapolator("model/equityoption"));
    defaults.setForwardCurveRightExtrapolator(i.getForwardCurveRightExtrapolator("model/equityoption"));
    defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
    defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
    defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
    defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
    defaults.setDiscountingCurveCurrency(i.getDiscountingCurveCurrency("model/equityoption"));
    defaults.setDividendType(i.getDiscountingCurve("model/equityoption"));
  }

  /**
   * Sets the per-equity surface defaults for equity option functions.
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionSurfaceDefaults(final EquityOptionFunctions.EquityOptionDefaults defaults) {
    defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, EquityOptionFunctions.EquityInfo>() {
      @Override
      public EquityOptionFunctions.EquityInfo execute(final EquityInfo i) {
        final EquityOptionFunctions.EquityInfo d = new EquityOptionFunctions.EquityInfo();
        setEquityOptionSurfaceDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Sets the paths for the per-equity ticker default values for the surface used
   * in pricing equity options. The keys are:
   * <ul>
   * <li> Surface calculation method = model/equityoption
   * <li> Discounting curve name = model/equityoption
   * <li> Discounting curve calculation config = model/equityoption
   * <li> Volatility surface name = model/equityoption
   * <li> Surface interpolation method = model/equityoption
   * <li> Forward curve name = model/equityoption
   * <li> Forward curve calculation method = model/equityoption
   * </ul>
   * @param i The per-equity info
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionSurfaceDefaults(final EquityInfo i, final EquityOptionFunctions.EquityInfo defaults) {
    defaults.setSurfaceCalculationMethod(i.getSurfaceCalculationMethod("model/equityoption"));
    defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
    defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
    defaults.setVolatilitySurface(i.getVolatilitySurface("model/equityoption"));
    defaults.setSurfaceInterpolationMethod(i.getSurfaceInterpolationMethod("model/equityoption"));
    defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
    defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
  }

  /**
   * Sets the map of equity tickers to per-equity ticker default values.
   * @param perEquityInfo A map of equity tickers to per-equity default values.
   */
  public void setPerEquityInfo(final Map<String, EquityInfo> perEquityInfo) {
    _perEquityInfo.clear();
    _perEquityInfo.putAll(perEquityInfo);
  }

  /**
   * Gets the map of equity tickers to per-equity ticker default values.
   * @return The map of equity tickers to per-equity ticker default values
   */
  public Map<String, EquityInfo> getPerEquityInfo() {
    return _perEquityInfo;
  }

  /**
   * Sets per-equity default values for an equity ticker.
   * @param equity The equity ticker
   * @param info The per-equity ticker default values
   */
  public void setEquityInfo(final String equity, final EquityInfo info) {
    _perEquityInfo.put(equity, info);
  }

  /**
   * Gets the per-equity default values for an equity ticker.
   * @param equity The equity ticker
   * @return The per-equity default values
   */
  public EquityInfo getEquityInfo(final String equity) {
    return _perEquityInfo.get(equity);
  }

  /**
   * Creates a per-equity default information object for an equity string.
   * @param equity The equity string
   * @return An empty per-equity info object
   */
  protected EquityInfo defaultEquityInfo(final String equity) {
    return new EquityInfo(equity);
  }

  /**
   * Gets the equity ticker information for a given filter.
   * @param <T> The type of the object that contains default values for an equity ticker
   * @param filter The filter
   * @return T The object that contains default values for an equity ticker
   */
  protected <T> Map<String, T> getEquityInfo(final Function1<EquityInfo, T> filter) {
    final Map<String, T> result = new HashMap<>();
    for (final Map.Entry<String, EquityInfo> e : getPerEquityInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          LOGGER.debug("Skipping {}", e.getKey());
          LOGGER.trace("Caught exception", e);
          continue;
        }
      }
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }


  /**
   * Constants for a particular equity ticker.
   */
  public static class EquityInfo {
    /** The equity ticker */
    private final String _equity;
    /** The discounting curve name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_NAME} property */
    private final Value _discountingCurve = new Value();
    /** The discounting curve calculation configuration name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_CONFIG} property */
    private final Value _discountingCurveConfig = new Value();
    /** The discounting curve currency. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#CURVE_CURRENCY} property */
    private final Value _discountingCurveCurrency = new Value();
    /** The volatility surface name. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#SURFACE} property */
    private final Value _volatilitySurface = new Value();
    /** The volatility surface calculation method. Usually the default value of the
     * {@link com.opengamma.engine.value.ValuePropertyNames#SURFACE_CALCULATION_METHOD} property */
    private final Value _surfaceCalculationMethod = new Value();
    /** The volatility surface interpolation method name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues#PROPERTY_SMILE_INTERPOLATOR}
     * property */
    private final Value _surfaceInterpolationMethod = new Value();
    /** The forward curve name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_NAME} property */
    private final Value _forwardCurve = new Value();
    /** The forward curve interpolator */
    private final Value _forwardCurveInterpolator = new Value();
    /** The forward curve left extrapolator */
    private final Value _forwardCurveLeftExtrapolator = new Value();
    /** The forward curve right extrapolator */
    private final Value _forwardCurveRightExtrapolator = new Value();
    /** The forward curve calculation method name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_CALCULATION_METHOD} property */
    private final Value _forwardCurveCalculationMethod = new Value();
    /** The dividend type. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#DIVIDEND_TYPE} property */
    private final Value _dividendType = new Value();

    /**
     * @param equity The equity ticker.
     */
    public EquityInfo(final String equity) {
      _equity = equity;
    }

    /**
     * Gets the equity id.
     * @return The equity id
     */
    public String getEquity() {
      return _equity;
    }

    /**
     * Sets the discounting curve name for a key.
     * @param key The key
     * @param discountingCurve The discounting curve name
     */
    public void setDiscountingCurve(final String key, final String discountingCurve) {
      _discountingCurve.set(key, discountingCurve);
    }

    /**
     * Gets the discounting curve name for a key.
     * @param key The key
     * @return The discounting curve name
     */
    public String getDiscountingCurve(final String key) {
      return _discountingCurve.get(key);
    }

    /**
     * Sets the discounting curve configuration name.
     * @param key The key
     * @param discountingCurveConfig The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String key, final String discountingCurveConfig) {
      _discountingCurveConfig.set(key, discountingCurveConfig);
    }

    /**
     * Gets the discounting curve configuration name for a key.
     * @param key The key
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig(final String key) {
      return _discountingCurveConfig.get(key);
    }

    /**
     * Sets the discounting curve currency.
     * @param key The key
     * @param discountingCurveCurrency The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String key, final String discountingCurveCurrency) {
      _discountingCurveCurrency.set(key, discountingCurveCurrency);
    }

    /**
     * Gets the discounting curve currency for a key.
     * @param key The key
     * @return The discounting curve currency
     */
    public String getDiscountingCurveCurrency(final String key) {
      return _discountingCurveCurrency.get(key);
    }

    /**
     * Sets the volatility surface name for a key.
     * @param key The key
     * @param volatilitySurface The volatility surface name
     */
    public void setVolatilitySurface(final String key, final String volatilitySurface) {
      _volatilitySurface.set(key, volatilitySurface);
    }

    /**
     * Gets the volatility surface name for a key.
     * @param key The key
     * @return The volatility surface name
     */
    public String getVolatilitySurface(final String key) {
      return _volatilitySurface.get(key);
    }

    /**
     * Sets the volatility surface calculation method for a key.
     * @param key The key
     * @param surfaceCalculationMethod The volatility surface calculation method
     */
    public void setSurfaceCalculationMethod(final String key, final String surfaceCalculationMethod) {
      _surfaceCalculationMethod.set(key, surfaceCalculationMethod);
    }

    /**
     * Gets the volatility surface calculation method for a key.
     * @param key The key
     * @return The volatility surface calculation method
     */
    public String getSurfaceCalculationMethod(final String key) {
      return _surfaceCalculationMethod.get(key);
    }

    /**
     * Sets the volatility surface interpolation method for a key.
     * @param key The key
     * @param surfaceInterpolationMethod The volatility surface interpolation method
     */
    public void setSurfaceInterpolationMethod(final String key, final String surfaceInterpolationMethod) {
      _surfaceInterpolationMethod.set(key, surfaceInterpolationMethod);
    }

    /**
     * Gets the volatility surface interpolation method for a key.
     * @param key The key
     * @return The volatility surface interpolation method
     */
    public String getSurfaceInterpolationMethod(final String key) {
      return _surfaceInterpolationMethod.get(key);
    }

    /**
     * Sets the forward curve name for a key.
     * @param key The key
     * @param forwardCurve The forward curve name
     */
    public void setForwardCurve(final String key, final String forwardCurve) {
      _forwardCurve.set(key, forwardCurve);
    }

    /**
     * Gets the forward curve name for a key.
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurve(final String key) {
      return _forwardCurve.get(key);
    }

    /**
     * Sets the forward curve interpolator name for a key.
     * @param key The key
     * @param forwardCurveInterpolator The forward curve interpolator name
     */
    public void setForwardCurveInterpolator(final String key, final String forwardCurveInterpolator) {
      _forwardCurveInterpolator.set(key, forwardCurveInterpolator);
    }

    /**
     * Gets the forward curve interpolator name for a key.
     * @param key The key
     * @return The forward curve interpolator name
     */
    public String getForwardCurveInterpolator(final String key) {
      return _forwardCurveInterpolator.get(key);
    }

    /**
     * Sets the forward curve left extrapolator name for a key.
     * @param key The key
     * @param forwardCurveLeftExtrapolator The forward curve left extrapolator name
     */
    public void setForwardCurveLeftExtrapolator(final String key, final String forwardCurveLeftExtrapolator) {
      _forwardCurveLeftExtrapolator.set(key, forwardCurveLeftExtrapolator);
    }

    /**
     * Gets the forward curve name for a key.
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurveLeftExtrapolator(final String key) {
      return _forwardCurveLeftExtrapolator.get(key);
    }

    /**
     * Sets the forward curve right extrapolator name for a key.
     * @param key The key
     * @param forwardCurveRightExtrapolator The forward curve right extrapolator name
     */
    public void setForwardCurveRightExtrapolator(final String key, final String forwardCurveRightExtrapolator) {
      _forwardCurveRightExtrapolator.set(key, forwardCurveRightExtrapolator);
    }

    /**
     * Gets the forward curve right extrapolator name for a key.
     * @param key The key
     * @return The forward curve right extrapolator name
     */
    public String getForwardCurveRightExtrapolator(final String key) {
      return _forwardCurveRightExtrapolator.get(key);
    }

    /**
     * Sets the forward curve calculation method for a key.
     * @param key The key
     * @param forwardCurveCalculationMethod The forward curve calculation method
     */
    public void setForwardCurveCalculationMethod(final String key, final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod.set(key, forwardCurveCalculationMethod);
    }

    /**
     * Gets the forward curve calculation method for a key.
     * @param key The key
     * @return The forward curve calculation method
     */
    public String getForwardCurveCalculationMethod(final String key) {
      return _forwardCurveCalculationMethod.get(key);
    }

    /**
     * Sets the dividend type for a key.
     * @param key The key
     * @param dividendType The dividend type
     */
    public void setDividendType(final String key, final String dividendType) {
      _dividendType.set(key, dividendType);
    }

    /**
     * Gets the dividend type for a key.
     * @param key The key
     * @return The dividend type
     */
    public String getDividendType(final String key) {
      return _dividendType.get(key);
    }
  }
}
