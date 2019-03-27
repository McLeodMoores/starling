/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.bond.config.BondDiscountingMethodFunctions;
import com.mcleodmoores.financial.function.fx.config.FxBlackMethodFunctions;
import com.mcleodmoores.financial.function.fx.config.FxDiscountingMethodFunctions;
import com.mcleodmoores.financial.function.rates.config.RatesDiscountingMethodFunctions;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunctions;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 *
 */
public class ExamplesFunctionConfiguration extends StandardFunctionConfiguration {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesFunctionConfiguration.class);

  /** A map from equity ticker strings to equity ticker default values */
  private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();
  private final Map<UnorderedCurrencyPair, FxOptionInfo> _vanillaFxOptionInfo = new HashMap<>();
  private final Map<UnorderedCurrencyPair, FxForwardInfo> _fxForwardInfo = new HashMap<>();
  private final Map<Currency, LinearRatesInfo> _linearRatesInfo = new HashMap<>();
  private final Map<Currency, BondInfo> _bondPerCurrencyInfo = new HashMap<>();
  private final Map<Country, BondInfo> _bondPerCountryInfo = new HashMap<>();

  /**
   *
   */
  public ExamplesFunctionConfiguration() {
    setEquityOptionInfo();
    setVanillaFxOptionInfo();
    setFxForwardInfo();
    setLinearRatesInfo();
    setGovernmentBondInfo();
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  /**
   *
   */
  protected void setEquityOptionInfo() {
  }

  /**
   *
   */
  protected void setVanillaFxOptionInfo() {
  }

  /**
   *
   */
  protected void setFxForwardInfo() {
  }

  /**
   *
   */
  protected void setLinearRatesInfo() {
  }

  /**
   *
   */
  protected void setGovernmentBondInfo() {
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions(), multicurvePricingFunctions(),
        blackDiscountingFunctionConfiguration(), discountingFunctionConfiguration());
  }

  /**
   * Sets the map of equity tickers to per-equity ticker default values.
   *
   * @param perEquityInfo
   *          A map of equity tickers to per-equity default values.
   */
  public void setPerEquityInfo(final Map<String, EquityInfo> perEquityInfo) {
    _perEquityInfo.clear();
    _perEquityInfo.putAll(perEquityInfo);
  }

  /**
   * Gets the map of equity tickers to per-equity ticker default values.
   *
   * @return The map of equity tickers to per-equity ticker default values
   */
  public Map<String, EquityInfo> getPerEquityInfo() {
    return _perEquityInfo;
  }

  /**
   * Sets per-equity default values for an equity ticker.
   *
   * @param equity
   *          The equity ticker
   * @param info
   *          The per-equity ticker default values
   */
  public void setEquityInfo(final String equity, final EquityInfo info) {
    _perEquityInfo.put(equity, info);
  }

  /**
   * Gets the per-equity default values for an equity ticker.
   *
   * @param equity
   *          The equity ticker
   * @return The per-equity default values
   */
  public EquityInfo getEquityInfo(final String equity) {
    return _perEquityInfo.get(equity);
  }

  /**
   * Creates a per-equity default information object for an equity string.
   *
   * @param equity
   *          The equity string
   * @return An empty per-equity info object
   */
  protected EquityInfo defaultEquityInfo(final String equity) {
    return new EquityInfo(equity);
  }

  public void setVanillaFxOptionInfo(final Map<UnorderedCurrencyPair, FxOptionInfo> info) {
    _vanillaFxOptionInfo.clear();
    _vanillaFxOptionInfo.putAll(info);
  }

  public Map<UnorderedCurrencyPair, FxOptionInfo> getVanillaFxOptionInfo() {
    return _vanillaFxOptionInfo;
  }

  public void setVanillaFxOptionInfo(final Currency ccy1, final Currency ccy2, final FxOptionInfo info) {
    _vanillaFxOptionInfo.put(UnorderedCurrencyPair.of(ccy1, ccy2), info);
  }

  public FxOptionInfo getVanillaFxOptionInfo(final Currency ccy1, final Currency ccy2) {
    return _vanillaFxOptionInfo.get(UnorderedCurrencyPair.of(ccy1, ccy2));
  }

  public void setFxForwardInfo(final Map<UnorderedCurrencyPair, FxForwardInfo> info) {
    _fxForwardInfo.clear();
    _fxForwardInfo.putAll(info);
  }

  public Map<UnorderedCurrencyPair, FxForwardInfo> getFxForwardInfo() {
    return _fxForwardInfo;
  }

  public void setFxForwardInfo(final Currency ccy1, final Currency ccy2, final FxForwardInfo info) {
    _fxForwardInfo.put(UnorderedCurrencyPair.of(ccy1, ccy2), info);
  }

  public FxForwardInfo getFxForwardInfo(final Currency ccy1, final Currency ccy2) {
    return _fxForwardInfo.get(UnorderedCurrencyPair.of(ccy1, ccy2));
  }

  public void setLinearRatesInfo(final Map<Currency, LinearRatesInfo> info) {
    _linearRatesInfo.clear();
    _linearRatesInfo.putAll(info);
  }

  public Map<Currency, LinearRatesInfo> getLinearRatesInfo() {
    return _linearRatesInfo;
  }

  protected void setLinearRatesInfo(final Currency ccy, final LinearRatesInfo info) {
    _linearRatesInfo.put(ccy, info);
  }

  protected LinearRatesInfo getLinearRatesInfo(final Currency ccy) {
    return _linearRatesInfo.get(ccy);
  }

  public void setBondPerCurrencyInfo(final Map<Currency, BondInfo> info) {
    _bondPerCurrencyInfo.clear();
    _bondPerCurrencyInfo.putAll(info);
  }

  public Map<Currency, BondInfo> getBondPerCurrencyInfo() {
    return _bondPerCurrencyInfo;
  }

  protected void setBondPerCurrencyInfo(final Currency ccy, final BondInfo info) {
    _bondPerCurrencyInfo.put(ccy, info);
  }

  protected BondInfo getBondPerCurrencyInfo(final Currency ccy) {
    return _bondPerCurrencyInfo.get(ccy);
  }

  public void setBondPerCountryInfo(final Map<Country, BondInfo> info) {
    _bondPerCountryInfo.clear();
    _bondPerCountryInfo.putAll(info);
  }

  public Map<Country, BondInfo> getBondPerCountryInfo() {
    return _bondPerCountryInfo;
  }

  protected void setBondPerCountryInfo(final Country country, final BondInfo info) {
    _bondPerCountryInfo.put(country, info);
  }

  protected BondInfo getBondPerCountryInfo(final Country country) {
    return _bondPerCountryInfo.get(country);
  }

  /**
   * Overridden to allow separate curve default curve names (per currency) to be set for equity options. The parent class sets the same curve names for all
   * equity instruments. {@inheritDoc}
   */
  @Override
  protected FunctionConfigurationSource equityOptionFunctions() {
    super.equityOptionFunctions();
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
   *
   * @param defaults
   *          The object containing the default values
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
   * Sets the per-equity surface defaults for equity option functions.
   *
   * @param defaults
   *          The object containing the default values
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
   * Sets the paths for the per-equity ticker default values for the forward curve used in pricing with the keys:
   * <p>
   * <ul>
   * <li>Forward curve interpolator = model/equityoption
   * <li>Forward curve left extrapolator = model/equityoption
   * <li>Forward curve right extrapolator = model/equityoption
   * <li>Forward curve = model/equityoption
   * <li>Forward curve calculation method = model/equityoption
   * <li>Discounting curve = model/equityoption
   * <li>Discounting curve configuration = model/equityoption
   * <li>Discounting curve currency = model/equityoption
   * <li>Dividend type = model/equityoption
   * </ul>
   *
   * @param i
   *          The per-equity info
   * @param defaults
   *          The object containing the default values
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
   * Sets the paths for the per-equity ticker default values for the surface used in pricing with the keys
   * <p>
   * <ul>
   * <li>Surface calculation method = model/equityoption
   * <li>Discounting curve name = model/equityoption
   * <li>Discounting curve calculation config = model/equityoption
   * <li>Volatility surface name = model/equityoption
   * <li>Surface interpolation method = model/equityoption
   * <li>Forward curve name = model/equityoption
   * <li>Forward curve calculation method = model/equityoption
   * </ul>
   *
   * @param i
   *          The per-equity info
   * @param defaults
   *          The object containing the default values
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

  protected void setVanillaFxOptionDefaults(final FxBlackMethodFunctions.FxOptionDefaults defaults) {
    defaults.setCurrencyPairInfo(getVanillaFxOptionInfo(new Function1<FxOptionInfo, FxBlackMethodFunctions.FxOptionDefaults.CurrencyPairInfo>() {

      @Override
      public FxBlackMethodFunctions.FxOptionDefaults.CurrencyPairInfo execute(final FxOptionInfo i) {
        final FxBlackMethodFunctions.FxOptionDefaults.CurrencyPairInfo d = new FxBlackMethodFunctions.FxOptionDefaults.CurrencyPairInfo();
        setVanillaFxOptionDefaults(i, d);
        return d;
      }

    }));
  }

  protected void setVanillaFxOptionDefaults(final FxOptionInfo i, final FxBlackMethodFunctions.FxOptionDefaults.CurrencyPairInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/vanillafxoption"));
    d.setLeftXExtrapolatorName(i.getLeftXExtrapolatorName("model/vanillafxoption"));
    d.setRightXExtrapolatorName(i.getRightXExtrapolatorName("model/vanillafxoption"));
    d.setSurfaceName(i.getSurfaceName("model/vanillafxoption"));
    d.setXInterpolatorName(i.getXInterpolatorName("model/vanillafxoption"));
  }

  protected void setFxForwardDefaults(final FxDiscountingMethodFunctions.FxForwardDefaults defaults) {
    defaults.setCurrencyPairInfo(getFxForwardInfo(new Function1<FxForwardInfo, FxDiscountingMethodFunctions.FxForwardDefaults.CurrencyPairInfo>() {

      @Override
      public FxDiscountingMethodFunctions.FxForwardDefaults.CurrencyPairInfo execute(final FxForwardInfo i) {
        final FxDiscountingMethodFunctions.FxForwardDefaults.CurrencyPairInfo d = new FxDiscountingMethodFunctions.FxForwardDefaults.CurrencyPairInfo();
        setFxForwardDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setFxForwardDefaults(final FxForwardInfo i, final FxDiscountingMethodFunctions.FxForwardDefaults.CurrencyPairInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/fxforward"));
  }

  protected void setLinearRatesDefaults(final RatesDiscountingMethodFunctions.LinearRatesDefaults defaults) {
    defaults.setCurrencyInfo(getLinearRatesInfo(new Function1<LinearRatesInfo, RatesDiscountingMethodFunctions.LinearRatesDefaults.CurrencyInfo>() {

      @Override
      public RatesDiscountingMethodFunctions.LinearRatesDefaults.CurrencyInfo execute(final LinearRatesInfo i) {
        final RatesDiscountingMethodFunctions.LinearRatesDefaults.CurrencyInfo d = new RatesDiscountingMethodFunctions.LinearRatesDefaults.CurrencyInfo();
        setLinearRatesDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setLinearRatesDefaults(final LinearRatesInfo i, final RatesDiscountingMethodFunctions.LinearRatesDefaults.CurrencyInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/linearrates"));
  }

  protected void setGovernmentBondDefaults(final BondDiscountingMethodFunctions.GovernmentBondDefaults defaults) {
    defaults.setCountryInfo(getBondPerCountryInfo(new Function1<BondInfo, BondDiscountingMethodFunctions.GovernmentBondDefaults.CountryInfo>() {

      @Override
      public BondDiscountingMethodFunctions.GovernmentBondDefaults.CountryInfo execute(final BondInfo i) {
        final BondDiscountingMethodFunctions.GovernmentBondDefaults.CountryInfo d = new BondDiscountingMethodFunctions.GovernmentBondDefaults.CountryInfo();
        setBondPerCountryDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setBondPerCountryDefaults(final BondInfo i, final BondDiscountingMethodFunctions.GovernmentBondDefaults.CountryInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/bond/govt"));
  }

  protected FunctionConfigurationSource discountingFunctionConfiguration() {
    final FxDiscountingMethodFunctions.FxForwardDefaults fxForwardDefaults = new FxDiscountingMethodFunctions.FxForwardDefaults();
    setFxForwardDefaults(fxForwardDefaults);
    final RatesDiscountingMethodFunctions.LinearRatesDefaults linearRatesDefaults = new RatesDiscountingMethodFunctions.LinearRatesDefaults();
    setLinearRatesDefaults(linearRatesDefaults);
    final BondDiscountingMethodFunctions.GovernmentBondDefaults govtBondDefaults = new BondDiscountingMethodFunctions.GovernmentBondDefaults();
    setGovernmentBondDefaults(govtBondDefaults);
    return CombiningFunctionConfigurationSource.of(getRepository(fxForwardDefaults), getRepository(linearRatesDefaults), getRepository(govtBondDefaults));
  }

  protected FunctionConfigurationSource blackDiscountingFunctionConfiguration() {
    final FxBlackMethodFunctions.FxOptionDefaults vanillaDefaults = new FxBlackMethodFunctions.FxOptionDefaults();
    setVanillaFxOptionDefaults(vanillaDefaults);
    return CombiningFunctionConfigurationSource.of(getRepository(vanillaDefaults));
  }

  protected <T> Map<UnorderedCurrencyPair, T> getVanillaFxOptionInfo(final Function1<FxOptionInfo, T> filter) {
    final Map<UnorderedCurrencyPair, T> result = new HashMap<>();
    for (final Map.Entry<UnorderedCurrencyPair, FxOptionInfo> e : getVanillaFxOptionInfo().entrySet()) {
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

  public static class FxOptionInfo {
    private final Value _surfaceName = new Value();
    private final Value _curveExposuresName = new Value();
    private final Value _xInterpolatorName = new Value();
    private final Value _leftXExtrapolatorName = new Value();
    private final Value _rightXExtrapolatorName = new Value();

    public void setSurfaceName(final String key, final String name) {
      _surfaceName.set(key, name);
    }

    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresName.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresName.get(key);
    }

    public void setXInterpolatorName(final String key, final String name) {
      _xInterpolatorName.set(key, name);
    }

    public String getXInterpolatorName(final String key) {
      return _xInterpolatorName.get(key);
    }

    public void setLeftXExtrapolatorName(final String key, final String name) {
      _leftXExtrapolatorName.set(key, name);
    }

    public String getLeftXExtrapolatorName(final String key) {
      return _leftXExtrapolatorName.get(key);
    }

    public void setRightXExtrapolatorName(final String key, final String name) {
      _rightXExtrapolatorName.set(key, name);
    }

    public String getRightXExtrapolatorName(final String key) {
      return _rightXExtrapolatorName.get(key);
    }

  }

  protected <T> Map<UnorderedCurrencyPair, T> getFxForwardInfo(final Function1<FxForwardInfo, T> filter) {
    final Map<UnorderedCurrencyPair, T> result = new HashMap<>();
    for (final Map.Entry<UnorderedCurrencyPair, FxForwardInfo> e : getFxForwardInfo().entrySet()) {
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

  public static class FxForwardInfo {
    private final Value _curveExposuresName = new Value();

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresName.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresName.get(key);
    }

  }

  protected <T> Map<Currency, T> getLinearRatesInfo(final Function1<LinearRatesInfo, T> filter) {
    final Map<Currency, T> result = new HashMap<>();
    for (final Map.Entry<Currency, LinearRatesInfo> e : getLinearRatesInfo().entrySet()) {
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

  public static class LinearRatesInfo {
    private final Value _curveExposuresName = new Value();

    public LinearRatesInfo() {
    }

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresName.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresName.get(key);
    }
  }

  protected <T> Map<Currency, T> getBondPerCurrencyInfo(final Function1<BondInfo, T> filter) {
    final Map<Currency, T> result = new HashMap<>();
    for (final Map.Entry<Currency, BondInfo> e : getBondPerCurrencyInfo().entrySet()) {
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

  protected <T> Map<Country, T> getBondPerCountryInfo(final Function1<BondInfo, T> filter) {
    final Map<Country, T> result = new HashMap<>();
    for (final Map.Entry<Country, BondInfo> e : getBondPerCountryInfo().entrySet()) {
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

  public static class BondInfo {
    private final Value _curveExposuresName = new Value();

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresName.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresName.get(key);
    }
  }

  /**
   * Gets the equity ticker information for a given filter.
   *
   * @param <T>
   *          The type of the object that contains default values for an equity ticker
   * @param filter
   *          The filter
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
    /**
     * The discounting curve name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_NAME} property
     */
    private final Value _discountingCurve = new Value();
    /**
     * The discounting curve calculation configuration name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_CONFIG} property
     */
    private final Value _discountingCurveConfig = new Value();
    /** The discounting curve currency. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#CURVE_CURRENCY} property */
    private final Value _discountingCurveCurrency = new Value();
    /** The volatility surface name. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#SURFACE} property */
    private final Value _volatilitySurface = new Value();
    /**
     * The volatility surface calculation method. Usually the default value of the
     * {@link com.opengamma.engine.value.ValuePropertyNames#SURFACE_CALCULATION_METHOD} property
     */
    private final Value _surfaceCalculationMethod = new Value();
    /**
     * The volatility surface interpolation method name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues#PROPERTY_SMILE_INTERPOLATOR}
     * property
     */
    private final Value _surfaceInterpolationMethod = new Value();
    /**
     * The forward curve name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_NAME} property
     */
    private final Value _forwardCurve = new Value();
    /** The forward curve interpolator */
    private final Value _forwardCurveInterpolator = new Value();
    /** The forward curve left extrapolator */
    private final Value _forwardCurveLeftExtrapolator = new Value();
    /** The forward curve right extrapolator */
    private final Value _forwardCurveRightExtrapolator = new Value();
    /**
     * The forward curve calculation method name. Usually the default value of the
     * {@link com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_CALCULATION_METHOD} property
     */
    private final Value _forwardCurveCalculationMethod = new Value();
    /** The dividend type. Usually the default value of the {@link com.opengamma.engine.value.ValuePropertyNames#DIVIDEND_TYPE} property */
    private final Value _dividendType = new Value();

    /**
     * @param equity
     *          The equity ticker.
     */
    public EquityInfo(final String equity) {
      _equity = equity;
    }

    /**
     * Gets the equity id.
     *
     * @return The equity id
     */
    public String getEquity() {
      return _equity;
    }

    /**
     * Sets the discounting curve name for a key.
     *
     * @param key
     *          The key
     * @param discountingCurve
     *          The discounting curve name
     */
    public void setDiscountingCurve(final String key, final String discountingCurve) {
      _discountingCurve.set(key, discountingCurve);
    }

    /**
     * Gets the discounting curve name for a key.
     *
     * @param key
     *          The key
     * @return The discounting curve name
     */
    public String getDiscountingCurve(final String key) {
      return _discountingCurve.get(key);
    }

    /**
     * Sets the discounting curve configuration name.
     *
     * @param key
     *          The key
     * @param discountingCurveConfig
     *          The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String key, final String discountingCurveConfig) {
      _discountingCurveConfig.set(key, discountingCurveConfig);
    }

    /**
     * Gets the discounting curve configuration name for a key.
     *
     * @param key
     *          The key
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig(final String key) {
      return _discountingCurveConfig.get(key);
    }

    /**
     * Sets the discounting curve currency.
     *
     * @param key
     *          The key
     * @param discountingCurveCurrency
     *          The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String key, final String discountingCurveCurrency) {
      _discountingCurveCurrency.set(key, discountingCurveCurrency);
    }

    /**
     * Gets the discounting curve currency for a key.
     *
     * @param key
     *          The key
     * @return The discounting curve currency
     */
    public String getDiscountingCurveCurrency(final String key) {
      return _discountingCurveCurrency.get(key);
    }

    /**
     * Sets the volatility surface name for a key.
     *
     * @param key
     *          The key
     * @param volatilitySurface
     *          The volatility surface name
     */
    public void setVolatilitySurface(final String key, final String volatilitySurface) {
      _volatilitySurface.set(key, volatilitySurface);
    }

    /**
     * Gets the volatility surface name for a key.
     *
     * @param key
     *          The key
     * @return The volatility surface name
     */
    public String getVolatilitySurface(final String key) {
      return _volatilitySurface.get(key);
    }

    /**
     * Sets the volatility surface calculation method for a key.
     *
     * @param key
     *          The key
     * @param surfaceCalculationMethod
     *          The volatility surface calculation method
     */
    public void setSurfaceCalculationMethod(final String key, final String surfaceCalculationMethod) {
      _surfaceCalculationMethod.set(key, surfaceCalculationMethod);
    }

    /**
     * Gets the volatility surface calculation method for a key.
     *
     * @param key
     *          The key
     * @return The volatility surface calculation method
     */
    public String getSurfaceCalculationMethod(final String key) {
      return _surfaceCalculationMethod.get(key);
    }

    /**
     * Sets the volatility surface interpolation method for a key.
     *
     * @param key
     *          The key
     * @param surfaceInterpolationMethod
     *          The volatility surface interpolation method
     */
    public void setSurfaceInterpolationMethod(final String key, final String surfaceInterpolationMethod) {
      _surfaceInterpolationMethod.set(key, surfaceInterpolationMethod);
    }

    /**
     * Gets the volatility surface interpolation method for a key.
     *
     * @param key
     *          The key
     * @return The volatility surface interpolation method
     */
    public String getSurfaceInterpolationMethod(final String key) {
      return _surfaceInterpolationMethod.get(key);
    }

    /**
     * Sets the forward curve name for a key.
     *
     * @param key
     *          The key
     * @param forwardCurve
     *          The forward curve name
     */
    public void setForwardCurve(final String key, final String forwardCurve) {
      _forwardCurve.set(key, forwardCurve);
    }

    /**
     * Gets the forward curve name for a key.
     *
     * @param key
     *          The key
     * @return The forward curve name
     */
    public String getForwardCurve(final String key) {
      return _forwardCurve.get(key);
    }

    /**
     * Sets the forward curve interpolator name for a key.
     *
     * @param key
     *          The key
     * @param forwardCurveInterpolator
     *          The forward curve interpolator name
     */
    public void setForwardCurveInterpolator(final String key, final String forwardCurveInterpolator) {
      _forwardCurveInterpolator.set(key, forwardCurveInterpolator);
    }

    /**
     * Gets the forward curve interpolator name for a key.
     *
     * @param key
     *          The key
     * @return The forward curve interpolator name
     */
    public String getForwardCurveInterpolator(final String key) {
      return _forwardCurveInterpolator.get(key);
    }

    /**
     * Sets the forward curve left extrapolator name for a key.
     *
     * @param key
     *          The key
     * @param forwardCurveLeftExtrapolator
     *          The forward curve left extrapolator name
     */
    public void setForwardCurveLeftExtrapolator(final String key, final String forwardCurveLeftExtrapolator) {
      _forwardCurveLeftExtrapolator.set(key, forwardCurveLeftExtrapolator);
    }

    /**
     * Gets the forward curve name for a key.
     *
     * @param key
     *          The key
     * @return The forward curve name
     */
    public String getForwardCurveLeftExtrapolator(final String key) {
      return _forwardCurveLeftExtrapolator.get(key);
    }

    /**
     * Sets the forward curve right extrapolator name for a key.
     *
     * @param key
     *          The key
     * @param forwardCurveRightExtrapolator
     *          The forward curve right extrapolator name
     */
    public void setForwardCurveRightExtrapolator(final String key, final String forwardCurveRightExtrapolator) {
      _forwardCurveRightExtrapolator.set(key, forwardCurveRightExtrapolator);
    }

    /**
     * Gets the forward curve right extrapolator name for a key.
     *
     * @param key
     *          The key
     * @return The forward curve right extrapolator name
     */
    public String getForwardCurveRightExtrapolator(final String key) {
      return _forwardCurveRightExtrapolator.get(key);
    }

    /**
     * Sets the forward curve calculation method for a key.
     *
     * @param key
     *          The key
     * @param forwardCurveCalculationMethod
     *          The forward curve calculation method
     */
    public void setForwardCurveCalculationMethod(final String key, final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod.set(key, forwardCurveCalculationMethod);
    }

    /**
     * Gets the forward curve calculation method for a key.
     *
     * @param key
     *          The key
     * @return The forward curve calculation method
     */
    public String getForwardCurveCalculationMethod(final String key) {
      return _forwardCurveCalculationMethod.get(key);
    }

    /**
     * Sets the dividend type for a key.
     *
     * @param key
     *          The key
     * @param dividendType
     *          The dividend type
     */
    public void setDividendType(final String key, final String dividendType) {
      _dividendType.set(key, dividendType);
    }

    /**
     * Gets the dividend type for a key.
     *
     * @param key
     *          The key
     * @return The dividend type
     */
    public String getDividendType(final String key) {
      return _dividendType.get(key);
    }
  }
}
