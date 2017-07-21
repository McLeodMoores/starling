/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.curve.forward.InterpolatedForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurvePerTickerDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityNetMarketValueFunction;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceDefaults;
import com.opengamma.financial.analytics.model.futureoption.BarrierOptionDistanceFunction;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * A configuration bean that adds equity option pricing functions to a repository and sets up default
 * properties for volatility surfaces, forward curves and discount curves.
 * @deprecated Use the configurations in {@link EquityOptionFunctions}.
 */
@Deprecated
public class OptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  /**
   * Equity ticker-specific data.
   */
  public static class EquityInfo implements InitializingBean {
    /** The discounting curve name */
    private String _discountingCurve;
    /** The discounting curve calculation configuration name */
    private String _discountingCurveConfig;
    /** The discounting curve currency */
    private String _discountingCurveCurrency;
    /** The volatility surface name */
    private String _volatilitySurface;
    /** The volatility surface calculation method */
    private String _surfaceCalculationMethod;
    /** The volatility surface interpolation method name */
    private String _surfaceInterpolationMethod;
    /** The forward curve name */
    private String _forwardCurve;
    /** The forward curve interpolator name */
    private String _forwardCurveInterpolator;
    /** The forward curve left extrapolator name */
    private String _forwardCurveLeftExtrapolator;
    /** The forward curve right extrapolator name */
    private String _forwardCurveRightExtrapolator;
    /** The forward curve calculation method name */
    private String _forwardCurveCalculationMethod;
    /** The dividend type */
    private String _dividendType;

    /**
     * Gets the discounting curve name.
     * @return The discounting curve name
     */
    public String getDiscountingCurve() {
      return _discountingCurve;
    }

    /**
     * Sets the discounting curve name.
     * @param discountingCurve The discounting curve name
     */
    public void setDiscountingCurve(final String discountingCurve) {
      _discountingCurve = discountingCurve;
    }

    /**
     * Gets the discounting curve configuration name.
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig() {
      return _discountingCurveConfig;
    }

    /**
     * Sets the discounting curve configuration name.
     * @param discountingCurveConfig The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String discountingCurveConfig) {
      _discountingCurveConfig = discountingCurveConfig;
    }

    /**
     * Gets the discounting curve currency.
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveCurrency() {
      return _discountingCurveCurrency;
    }

    /**
     * Sets the discounting curve currency.
     * @param discountingCurveCurrency The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String discountingCurveCurrency) {
      _discountingCurveCurrency = discountingCurveCurrency;
    }

    /**
     * Gets the volatility surface name.
     * @return The volatility surface name
     */
    public String getVolatilitySurface() {
      return _volatilitySurface;
    }

    /**
     * Sets the volatility surface name.
     * @param volatilitySurface The volatility surface name
     */
    public void setVolatilitySurface(final String volatilitySurface) {
      ArgumentChecker.notNull(volatilitySurface, "volatilitySurface");
      _volatilitySurface = volatilitySurface;
    }

    /**
     * Gets the volatility surface calculation method name.
     * @return The volatility surface calculation method name
     */
    public String getSurfaceCalculationMethod() {
      return _surfaceCalculationMethod;
    }

    /**
     * Sets the volatility surface calculation method name.
     * @param surfaceCalculationMethod The volatility surface calculation method name
     */
    public void setSurfaceCalculationMethod(final String surfaceCalculationMethod) {
      ArgumentChecker.notNull(surfaceCalculationMethod, "surfaceCalculationMethod");
      _surfaceCalculationMethod = surfaceCalculationMethod;
    }

    /**
     * Gets the volatility surface interpolation method name.
     * @return The volatility surface interpolation method name
     */
    public String getSurfaceInterpolationMethod() {
      return _surfaceInterpolationMethod;
    }

    /**
     * Sets the volatility surface interpolation method name.
     * @param surfaceInterpolationMethod The volatility surface interpolation method name
     */
    public void setSurfaceInterpolationMethod(final String surfaceInterpolationMethod) {
      ArgumentChecker.notNull(surfaceInterpolationMethod, "surfaceInterpolationMethod");
      _surfaceInterpolationMethod = surfaceInterpolationMethod;
    }

    /**
     * Gets the forward curve name.
     * @return The forward curve name
     */
    public String getForwardCurve() {
      return _forwardCurve;
    }

    /**
     * Sets the forward curve name.
     * @param forwardCurve The forward curve name
     */
    public void setForwardCurve(final String forwardCurve) {
      _forwardCurve = forwardCurve;
    }

    /**
     * Gets the forward curve interpolator name.
     * @return The forward curve interpolator name
     */
    public String getForwardCurveInterpolator() {
      return _forwardCurveInterpolator;
    }

    /**
     * Sets the forward curve interpolator name.
     * @param forwardCurveInterpolator The forward curve interpolator name
     */
    public void setForwardCurveInterpolator(final String forwardCurveInterpolator) {
      ArgumentChecker.notNull(forwardCurveInterpolator, "forwardCurveInterpolator");
      _forwardCurveInterpolator = forwardCurveInterpolator;
    }

    /**
     * Gets the forward curve left extrapolator name.
     * @return The forward curve left extrapolator name
     */
    public String getForwardCurveLeftExtrapolator() {
      return _forwardCurveLeftExtrapolator;
    }

    /**
     * Sets the forward curve left extrapolator name.
     * @param forwardCurveLeftExtrapolator The forward curve left extrapolator name
     */
    public void setForwardCurveLeftExtrapolator(final String forwardCurveLeftExtrapolator) {
      ArgumentChecker.notNull(forwardCurveLeftExtrapolator, "forwardCurveLeftExtrapolator");
      _forwardCurveLeftExtrapolator = forwardCurveLeftExtrapolator;
    }

    /**
     * Gets the forward curve right extrapolator name.
     * @return The forward curve right extrapolator name
     */
    public String getForwardCurveRightExtrapolator() {
      return _forwardCurveRightExtrapolator;
    }

    /**
     * Sets the forward curve right extrapolator name.
     * @param forwardCurveRightExtrapolator The forward curve right extrapolator name
     */
    public void setForwardCurveRightExtrapolator(final String forwardCurveRightExtrapolator) {
      ArgumentChecker.notNull(forwardCurveRightExtrapolator, "forwardCurveRightExtrapolator");
      _forwardCurveRightExtrapolator = forwardCurveRightExtrapolator;
    }

    /**
     * Gets the forward curve calculation method name.
     * @return The forward curve calculation method name
     */
    public String getForwardCurveCalculationMethod() {
      return _forwardCurveCalculationMethod;
    }

    /**
     * Sets the forward curve calculation method name.
     * @param forwardCurveCalculationMethod The forward curve calculation method name.
     */
    public void setForwardCurveCalculationMethod(final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    }

    /**
     * Gets the dividend type.
     * @return The dividend type
     */
    public String getDividendType() {
      return _dividendType;
    }

    /**
     * Sets the dividend type.
     * @param dividendType The dividend type
     */
    public void setDividendType(final String dividendType) {
      ArgumentChecker.notNull(dividendType, "dividendType");
      _dividendType = dividendType;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getDiscountingCurve(), "discountingCurve");
      ArgumentChecker.notNullInjected(getDiscountingCurveConfig(), "discountingCurveConfig");
      ArgumentChecker.notNullInjected(getForwardCurve(), "forwardCurve");
      ArgumentChecker.notNullInjected(getForwardCurveCalculationMethod(), "forwardCurveCalculationMethod");
      // Not checking other properties because they are not all required by volatility surfaces
      // and forward curves. The null check is performed in the setter.
    }
  }

  /**
   * Contains default values for forward curves.
   */
  public static class EquityForwardDefaults extends AbstractFunctionConfigurationBean {
    /** The number of default parameters that a forward curve requires */
    private static final int N_CURVE_ARGS = 5;
    /** The number of default parameters that the interpolator requires */
    private static final int N_INTERPOLATOR_ARGS = 3;
    /** The per-equity defaults */
    private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();
    /** The interpolator name */
    private String _interpolator;
    /** The left extrapolator name */
    private String _leftExtrapolator;
    /** The right extrapolator name */
    private String _rightExtrapolator;
    /** The discounting curve name */
    private String _discountingCurve;
    /** The discounting curve calculation config name */
    private String _discountingCurveConfig;
    /** The discounting curve currency */
    private String _discountingCurveCurrency;
    /** The dividend type */
    private String _dividendType;

    /**
     * Sets the forward curve defaults for a set of equity tickers.
     * @param perEquityInfo The per-equity defaults
     */
    public void setPerEquityInfo(final Map<String, EquityInfo> perEquityInfo) {
      _perEquityInfo.clear();
      _perEquityInfo.putAll(perEquityInfo);
    }

    /**
     * Gets the forward curve defaults for a set of equity tickers.
     * @return The per-equity defaults
     */
    public Map<String, EquityInfo> getPerEquityInfo() {
      return _perEquityInfo;
    }

    /**
     * Gets the interpolator name.
     * @return The interpolator name
     */
    public String getInterpolator() {
      return _interpolator;
    }

    /**
     * Sets the interpolator name.
     * @param interpolator The interpolator name
     */
    public void setInterpolator(final String interpolator) {
      _interpolator = interpolator;
    }

    /**
     * Gets the left extrapolator name.
     * @return The left extrapolator name
     */
    public String getLeftExtrapolator() {
      return _leftExtrapolator;
    }

    /**
     * Sets the left extrapolator name.
     * @param leftExtrapolator The left extrapolator name
     */
    public void setLeftExtrapolator(final String leftExtrapolator) {
      _leftExtrapolator = leftExtrapolator;
    }

    /**
     * Gets the right extrapolator name.
     * @return The right extrapolator name
     */
    public String getRightExtrapolator() {
      return _rightExtrapolator;
    }

    /**
     * Sets the right extrapolator name.
     * @param rightExtrapolator The right extrapolator name
     */
    public void setRightExtrapolator(final String rightExtrapolator) {
      _rightExtrapolator = rightExtrapolator;
    }

    /**
     * Gets the discounting curve name.
     * @return The discounting curve name
     */
    public String getDiscountingCurve() {
      return _discountingCurve;
    }

    /**
     * Sets the discounting curve name.
     * @param discountingCurve The discounting curve name
     */
    public void setDiscountingCurve(final String discountingCurve) {
      _discountingCurve = discountingCurve;
    }

    /**
     * Gets the discounting curve configuration name.
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig() {
      return _discountingCurveConfig;
    }

    /**
     * Sets the discounting curve configuration name.
     * @param discountingCurveConfig The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String discountingCurveConfig) {
      _discountingCurveConfig = discountingCurveConfig;
    }

    /**
     * Gets the discounting curve currency.
     * @return The discounting curve currency
     */
    public String getDiscountingCurveCurrency() {
      return _discountingCurveCurrency;
    }

    /**
     * Sets the discounting curve currency.
     * @param discountingCurveCurrency The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String discountingCurveCurrency) {
      _discountingCurveCurrency = discountingCurveCurrency;
    }

    /**
     * Gets the dividend type.
     * @return The dividend type
     */
    public String getDividendType() {
      return _dividendType;
    }

    /**
     * Sets the dividend type.
     * @param dividendType The dividend type
     */
    public void setDividendType(final String dividendType) {
      _dividendType = dividendType;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final Map<String, EquityInfo> perEquityInfo = getPerEquityInfo();
      final String[] interpolatorArgs = new String[perEquityInfo.size() * N_INTERPOLATOR_ARGS];
      final String[] curveArgs = new String[1 + perEquityInfo.size() * N_CURVE_ARGS];
      curveArgs[0] = PriorityClass.ABOVE_NORMAL.name();
      int i = 0;
      int j = 1;
      for (final Map.Entry<String, EquityInfo> entry : perEquityInfo.entrySet()) {
        final String ticker = entry.getKey();
        final EquityInfo equityInfo = entry.getValue();
        interpolatorArgs[i++] = equityInfo.getForwardCurveInterpolator();
        interpolatorArgs[i++] = equityInfo.getForwardCurveLeftExtrapolator();
        interpolatorArgs[i++] = equityInfo.getForwardCurveRightExtrapolator();
        curveArgs[j++] = ticker;
        curveArgs[j++] = equityInfo.getDiscountingCurveCurrency();
        curveArgs[j++] = equityInfo.getDiscountingCurve();
        curveArgs[j++] = equityInfo.getDiscountingCurveConfig();
        curveArgs[j++] = equityInfo.getDividendType();
      }
      functions.add(functionConfiguration(InterpolatedForwardCurveDefaults.class, interpolatorArgs));
      functions.add(functionConfiguration(EquityForwardCurvePerTickerDefaults.class, curveArgs));
    }
  }

  /**
   * Contains default values for volatility surfaces.
   */
  public static class EquityOptionDefaults extends AbstractFunctionConfigurationBean {
    /** The number of default values that a volatility surface requires */
    private static final int N_SURFACE_ARGS = 7;
    /** The per-equity defaults */
    private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();
    /** The interpolator name */
    private String _interpolator;
    /** The left extrapolator name */
    private String _leftExtrapolator;
    /** The right extrapolator name */
    private String _rightExtrapolator;
    /** The discounting curve name */
    private String _discountingCurve;
    /** The discounting curve calculation config name */
    private String _discountingCurveConfig;
    /** The discounting curve currency */
    private String _discountingCurveCurrency;
    /** The dividend type */
    private String _dividendType;
    /** The surface calculation method */
    private String _surfaceCalculationMethod;

    /**
     * Sets the volatility surface defaults for a set of equity tickers.
     * @param perEquityInfo The per-equity defaults
     */
    public void setPerEquityInfo(final Map<String, EquityInfo> perEquityInfo) {
      _perEquityInfo.clear();
      _perEquityInfo.putAll(perEquityInfo);
    }

    /**
     * Gets the volatility surface defaults for a set of equity tickers.
     * @return The per-equity defaults
     */
    public Map<String, EquityInfo> getPerEquityInfo() {
      return _perEquityInfo;
    }

    /**
     * Gets the interpolator name.
     * @return The interpolator name
     */
    public String getInterpolator() {
      return _interpolator;
    }

    /**
     * Sets the interpolator name.
     * @param interpolator The interpolator name
     */
    public void setInterpolator(final String interpolator) {
      _interpolator = interpolator;
    }

    /**
     * Gets the left extrapolator name.
     * @return The left extrapolator name
     */
    public String getLeftExtrapolator() {
      return _leftExtrapolator;
    }

    /**
     * Sets the left extrapolator name.
     * @param leftExtrapolator The left extrapolator name
     */
    public void setLeftExtrapolator(final String leftExtrapolator) {
      _leftExtrapolator = leftExtrapolator;
    }

    /**
     * Gets the right extrapolator name.
     * @return The right extrapolator name
     */
    public String getRightExtrapolator() {
      return _rightExtrapolator;
    }

    /**
     * Sets the right extrapolator name.
     * @param rightExtrapolator The right extrapolator name
     */
    public void setRightExtrapolator(final String rightExtrapolator) {
      _rightExtrapolator = rightExtrapolator;
    }

    /**
     * Gets the discounting curve name.
     * @return The discounting curve name
     */
    public String getDiscountingCurve() {
      return _discountingCurve;
    }

    /**
     * Sets the discounting curve name.
     * @param discountingCurve The discounting curve name
     */
    public void setDiscountingCurve(final String discountingCurve) {
      _discountingCurve = discountingCurve;
    }

    /**
     * Gets the discounting curve configuration name.
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig() {
      return _discountingCurveConfig;
    }

    /**
     * Sets the discounting curve configuration name.
     * @param discountingCurveConfig The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String discountingCurveConfig) {
      _discountingCurveConfig = discountingCurveConfig;
    }

    /**
     * Gets the discounting curve currency.
     * @return The discounting curve currency
     */
    public String getDiscountingCurveCurrency() {
      return _discountingCurveCurrency;
    }

    /**
     * Sets the discounting curve currency.
     * @param discountingCurveCurrency The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String discountingCurveCurrency) {
      _discountingCurveCurrency = discountingCurveCurrency;
    }

    /**
     * Gets the dividend type.
     * @return The dividend type
     */
    public String getDividendType() {
      return _dividendType;
    }

    /**
     * Sets the dividend type.
     * @param dividendType The dividend type
     */
    public void setDividendType(final String dividendType) {
      _dividendType = dividendType;
    }

    /**
     * Gets the surface calculation method.
     * @return The surface calculation method
     */
    public String getSurfaceCalculationMethod() {
      return _surfaceCalculationMethod;
    }

    /**
     * Sets the surface calculation method.
     * @param surfaceCalculationMethod The surface calculation method
     */
    public void setSurfaceCalculationMethod(final String surfaceCalculationMethod) {
      _surfaceCalculationMethod = surfaceCalculationMethod;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final Map<String, EquityInfo> perEquityInfo = getPerEquityInfo();
      final String[] calculationMethodArgs = new String[1 + perEquityInfo.size() * 2];
      final String[] surfaceArgs = new String[1 + perEquityInfo.size() * N_SURFACE_ARGS];
      calculationMethodArgs[0] = PriorityClass.ABOVE_NORMAL.name();
      surfaceArgs[0] = PriorityClass.ABOVE_NORMAL.name();
      int i = 1;
      int j = 1;
      for (final Map.Entry<String, EquityInfo> entry : perEquityInfo.entrySet()) {
        final String ticker = entry.getKey();
        final EquityInfo equityInfo = entry.getValue();
        calculationMethodArgs[i++] = ticker;
        calculationMethodArgs[i++] = equityInfo.getSurfaceCalculationMethod();
        surfaceArgs[j++] = ticker;
        surfaceArgs[j++] = equityInfo.getDiscountingCurve();
        surfaceArgs[j++] = equityInfo.getDiscountingCurveConfig();
        surfaceArgs[j++] = equityInfo.getVolatilitySurface();
        surfaceArgs[j++] = equityInfo.getSurfaceInterpolationMethod();
        surfaceArgs[j++] = equityInfo.getForwardCurve();
        surfaceArgs[j++] = equityInfo.getForwardCurveCalculationMethod();
      }
      functions.add(functionConfiguration(EquityOptionSurfaceCalculationMethodPerEquityDefaults.class, calculationMethodArgs));
      functions.add(functionConfiguration(EquityOptionInterpolatedBlackLognormalPerEquityDefaults.class, surfaceArgs));
    }

  }

  /**
   * Gets the default values for calculations
   * @return The repository with equity option defaults set
   */
  public static FunctionConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * @param overhedge The overhedge to use for equity barrier options
   * @param callSpreadFullWidth The width of the call spread to use for barrier options
   * @param barrierFormat the barrier output display format
   * @return The repository with equity barrier option defaults set
   */
  public static FunctionConfigurationSource defaults(final double overhedge, final double callSpreadFullWidth, final String barrierFormat) {
    final Defaults factory = new Defaults();
    factory.setOverhedge(overhedge);
    factory.setCallSpreadFullWidth(callSpreadFullWidth);
    factory.setBarrierDistanceFormat(barrierFormat);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default function for equity barrier options contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private double _overhedge; /* = 0.0; */
    private double _callSpreadFullWidth = 0.001;
    private String _barrierFormat = BarrierOptionDistanceFunction.BARRIER_ABS;

    public void setOverhedge(final double overhedge) {
      _overhedge = overhedge;
    }

    public double getOverhedge() {
      return _overhedge;
    }

    public void setCallSpreadFullWidth(final double callSpreadFullWidth) {
      _callSpreadFullWidth = callSpreadFullWidth;
    }

    public double getCallSpreadFullWidth() {
      return _callSpreadFullWidth;
    }

    public void setBarrierDistanceFormat(final String format) {
      _barrierFormat = format;
    }

    public String getBarrierDistanceFormat() {
      return _barrierFormat;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityVanillaBarrierOptionDefaults.class, Double.toString(getOverhedge()), Double.toString(getCallSpreadFullWidth())));
      functions.add(functionConfiguration(BarrierOptionDistanceDefaults.class, getBarrierDistanceFormat()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionPDEPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionPDEScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityOptionSpotIndexFunction.class));

    functions.add(functionConfiguration(EquityOptionBlackBasicPresentValueFunction.class));

    functions.add(functionConfiguration(ListedEquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackScenarioPnLFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionBjerksundStenslandScenarioPnLFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyPresentValueFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyGreeksFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyImpliedVolFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyValueDeltaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyValueGammaFunction.class));
    functions.add(functionConfiguration(ListedEquityOptionRollGeskeWhaleyScenarioPnLFunction.class));
    functions.add(functionConfiguration(EquityOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionPresentValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionRhoFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVommaFunction.class));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_DELTA, ValueRequirementNames.DELTA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_GAMMA, ValueRequirementNames.GAMMA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_RHO, ValueRequirementNames.RHO));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_THETA, ValueRequirementNames.THETA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_VEGA, ValueRequirementNames.VEGA));
    functions.add(functionConfiguration(PositionGreeksFunction.class, ValueRequirementNames.POSITION_WEIGHTED_VEGA, ValueRequirementNames.WEIGHTED_VEGA));
    functions.add(functionConfiguration(WeightedVegaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionDistanceFunction.class));
    functions.add(functionConfiguration(NetCapitalFunction.class));
    functions.add(functionConfiguration(EquityNetCapitalFunction.class));
    functions.add(functionConfiguration(EquityNetMarketValueFunction.class));
  }

}
