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

import com.mcleodmoores.financial.function.bond.functions.BondDiscountingMethodFunctions;
import com.mcleodmoores.financial.function.credit.cds.isda.functions.IsdaFunctions;
import com.mcleodmoores.financial.function.curve.functions.CurveFunctions;
import com.mcleodmoores.financial.function.curve.functions.CurveFunctions.CurveType;
import com.mcleodmoores.financial.function.curve.functions.CurveFunctions.Providers;
import com.mcleodmoores.financial.function.fx.functions.FxBlackMethodFunctions;
import com.mcleodmoores.financial.function.fx.functions.FxDiscountingMethodFunctions;
import com.mcleodmoores.financial.function.rates.functions.RatesDiscountingMethodFunctions;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunctions;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Sets up the function configurations used for the examples project.
 */
public class ExamplesFunctionConfiguration extends StandardFunctionConfiguration {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesFunctionConfiguration.class);

  /** A map from equity ticker strings to equity ticker default values */
  private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();
  private final Map<UnorderedCurrencyPair, FxOptionInfo> _vanillaFxOptionInfo = new HashMap<>();
  private final Map<UnorderedCurrencyPair, FxForwardInfo> _fxForwardInfo = new HashMap<>();
  private final Map<Currency, LinearRatesInfo> _linearRatesInfo = new HashMap<>();
  private final Map<Currency, BondInfo> _governmentBondPerCurrencyInfo = new HashMap<>();
  private final Map<Currency, BondInfo> _corporateBondPerCurrencyInfo = new HashMap<>();
  private final Map<Country, BondInfo> _governmentBondPerCountryInfo = new HashMap<>();
  private final Map<Country, BondInfo> _corporateBondPerCountryInfo = new HashMap<>();
  private final Map<Currency, CdsInfo> _cdsPerCurrencyInfo = new HashMap<>();
  private final Map<String, CurveType> _curveInfo = new HashMap<>();
  private final ConfigMaster _configMaster;

  /**
   *
   */
  public ExamplesFunctionConfiguration(final ConfigMaster configMaster) {
    _configMaster = configMaster;
    setEquityOptionInfo();
    setVanillaFxOptionInfo();
    setFxForwardInfo();
    setLinearRatesInfo();
    setGovernmentBondInfo();
    setCorporateBondInfo();
    setCdsInfo();
    setCurveInfo();
  }

  protected ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  /**
   * Sets up equity option defaults.
   */
  protected void setEquityOptionInfo() {
  }

  /**
   * Sets up vanilla FX options defaults.
   */
  protected void setVanillaFxOptionInfo() {
  }

  /**
   * Sets up FX forward defaults.
   */
  protected void setFxForwardInfo() {
  }

  /**
   * Sets up linear rates instrument defaults.
   */
  protected void setLinearRatesInfo() {
  }

  /**
   * Sets up government bond defaults.
   */
  protected void setGovernmentBondInfo() {
  }

  /**
   * Sets up corporate bond defaults.
   */
  protected void setCorporateBondInfo() {
  }

  /**
   * Sets up CDS defaults.
   */
  protected void setCdsInfo() {
  }

  /**
   * Sets up curve type defaults.
   */
  public void setCurveInfo() {
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions(), multicurvePricingFunctions(),
        blackDiscountingFunctionConfiguration(), discountingFunctionConfiguration(), creditFunctionConfiguration());
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

  /**
   * Sets the defaults for vanilla FX options.
   *
   * @param info
   *          the defaults
   */
  public void setVanillaFxOptionInfo(final Map<UnorderedCurrencyPair, FxOptionInfo> info) {
    _vanillaFxOptionInfo.clear();
    _vanillaFxOptionInfo.putAll(info);
  }

  /**
   * Gets the defaults for vanilla FX options.
   *
   * @return the defaults
   */
  public Map<UnorderedCurrencyPair, FxOptionInfo> getVanillaFxOptionInfo() {
    return _vanillaFxOptionInfo;
  }

  /**
   * Sets the defaults for a currency pair.
   *
   * @param ccy1
   *          the first currency
   * @param ccy2
   *          the second currency
   * @param info
   *          the defaults
   */
  public void setVanillaFxOptionInfo(final Currency ccy1, final Currency ccy2, final FxOptionInfo info) {
    _vanillaFxOptionInfo.put(UnorderedCurrencyPair.of(ccy1, ccy2), info);
  }

  /**
   * Gets the defaults for a currency pair.
   *
   * @param ccy1
   *          the first currency
   * @param ccy2
   *          the second currency
   * @return the defaults
   */
  public FxOptionInfo getVanillaFxOptionInfo(final Currency ccy1, final Currency ccy2) {
    return _vanillaFxOptionInfo.get(UnorderedCurrencyPair.of(ccy1, ccy2));
  }

  /**
   * Sets the defaults for FX forwards.
   *
   * @param info
   *          the defaults
   */
  public void setFxForwardInfo(final Map<UnorderedCurrencyPair, FxForwardInfo> info) {
    _fxForwardInfo.clear();
    _fxForwardInfo.putAll(info);
  }

  /**
   * Gets the defaults for FX forwards.
   *
   * @return the defaults
   */
  public Map<UnorderedCurrencyPair, FxForwardInfo> getFxForwardInfo() {
    return _fxForwardInfo;
  }

  /**
   * Sets the defaults for a currency pair.
   *
   * @param ccy1
   *          the first currency
   * @param ccy2
   *          the second currency
   * @param info
   *          the defaults
   */
  public void setFxForwardInfo(final Currency ccy1, final Currency ccy2, final FxForwardInfo info) {
    _fxForwardInfo.put(UnorderedCurrencyPair.of(ccy1, ccy2), info);
  }

  /**
   * Gets the defaults for a currency pair.
   *
   * @param ccy1
   *          the first currency
   * @param ccy2
   *          the second currency
   * @return the defaults
   */
  public FxForwardInfo getFxForwardInfo(final Currency ccy1, final Currency ccy2) {
    return _fxForwardInfo.get(UnorderedCurrencyPair.of(ccy1, ccy2));
  }

  /**
   * Sets the defaults for linear rate instruments.
   *
   * @param info
   *          the defaults
   */
  public void setLinearRatesInfo(final Map<Currency, LinearRatesInfo> info) {
    _linearRatesInfo.clear();
    _linearRatesInfo.putAll(info);
  }

  /**
   * Gets the defaults for linear rate instruments.
   *
   * @return the defaults
   */
  public Map<Currency, LinearRatesInfo> getLinearRatesInfo() {
    return _linearRatesInfo;
  }

  /**
   * Sets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @param info
   *          the defaults
   */
  protected void setLinearRatesInfo(final Currency ccy, final LinearRatesInfo info) {
    _linearRatesInfo.put(ccy, info);
  }

  /**
   * Gets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @return the defaults
   */
  protected LinearRatesInfo getLinearRatesInfo(final Currency ccy) {
    return _linearRatesInfo.get(ccy);
  }

  /**
   * Sets per-currency defaults for bonds.
   *
   * @param info
   *          the defaults
   */
  public void setGovernmentBondPerCurrencyInfo(final Map<Currency, BondInfo> info) {
    _governmentBondPerCurrencyInfo.clear();
    _governmentBondPerCurrencyInfo.putAll(info);
  }

  /**
   * Sets per-currency defaults for bonds.
   *
   * @param info
   *          the defaults
   */
  public void setCorporateBondPerCurrencyInfo(final Map<Currency, BondInfo> info) {
    _corporateBondPerCurrencyInfo.clear();
    _corporateBondPerCurrencyInfo.putAll(info);
  }

  /**
   * Sets per-currency defaults for bonds.
   *
   * @return the defaults
   */
  public Map<Currency, BondInfo> getGovernmentBondPerCurrencyInfo() {
    return _governmentBondPerCurrencyInfo;
  }

  /**
   * Sets per-currency defaults for bonds.
   *
   * @return the defaults
   */
  public Map<Currency, BondInfo> getCorporateBondPerCurrencyInfo() {
    return _corporateBondPerCurrencyInfo;
  }

  /**
   * Sets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @param info
   *          the defaults
   */
  protected void setGovernmentBondPerCurrencyInfo(final Currency ccy, final BondInfo info) {
    _governmentBondPerCurrencyInfo.put(ccy, info);
  }

  /**
   * Sets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @param info
   *          the defaults
   */
  protected void setCorporateBondPerCurrencyInfo(final Currency ccy, final BondInfo info) {
    _corporateBondPerCurrencyInfo.put(ccy, info);
  }

  /**
   * Gets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @return the defaults
   */
  protected BondInfo getGovernmentBondPerCurrencyInfo(final Currency ccy) {
    return _governmentBondPerCurrencyInfo.get(ccy);
  }

  /**
   * Gets the defaults for a currency.
   *
   * @param ccy
   *          the currency
   * @return the defaults
   */
  protected BondInfo getCorporateBondPerCurrencyInfo(final Currency ccy) {
    return _corporateBondPerCurrencyInfo.get(ccy);
  }

  /**
   * Sets per-country defaults for bonds.
   *
   * @param info
   *          the defaults
   */
  public void setGovernmentBondPerCountryInfo(final Map<Country, BondInfo> info) {
    _governmentBondPerCountryInfo.clear();
    _governmentBondPerCountryInfo.putAll(info);
  }

  /**
   * Sets per-country defaults for bonds.
   *
   * @param info
   *          the defaults
   */
  public void setCorporateBondPerCountryInfo(final Map<Country, BondInfo> info) {
    _corporateBondPerCountryInfo.clear();
    _corporateBondPerCountryInfo.putAll(info);
  }

  /**
   * Gets per-country defaults for bonds.
   *
   * @return the defaults
   */
  public Map<Country, BondInfo> getGovernmentBondPerCountryInfo() {
    return _governmentBondPerCountryInfo;
  }

  /**
   * Gets per-country defaults for bonds.
   *
   * @return the defaults
   */
  public Map<Country, BondInfo> getCorporateBondPerCountryInfo() {
    return _corporateBondPerCountryInfo;
  }

  /**
   * Sets the defaults for a country.
   *
   * @param country
   *          the country
   * @param info
   *          the defaults
   */
  protected void setGovernmentBondPerCountryInfo(final Country country, final BondInfo info) {
    _governmentBondPerCountryInfo.put(country, info);
  }

  /**
   * Sets the defaults for a country.
   *
   * @param country
   *          the country
   * @param info
   *          the defaults
   */
  protected void setCorporateBondPerCountryInfo(final Country country, final BondInfo info) {
    _corporateBondPerCountryInfo.put(country, info);
  }

  /**
   * Gets the defaults for a country.
   *
   * @param country
   *          the country
   * @return the defaults
   */
  protected BondInfo getGovernmentBondPerCountryInfo(final Country country) {
    return _governmentBondPerCountryInfo.get(country);
  }

  /**
   * Gets the defaults for a country.
   *
   * @param country
   *          the country
   * @return the defaults
   */
  protected BondInfo getCorporateBondPerCountryInfo(final Country country) {
    return _corporateBondPerCountryInfo.get(country);
  }

  /**
   * Sets the defaults for CDS.
   *
   * @param info
   *          the defaults
   */
  public void setCdsPerCurrencyInfo(final Map<Currency, CdsInfo> info) {
    _cdsPerCurrencyInfo.clear();
    _cdsPerCurrencyInfo.putAll(info);
  }

  /**
   * Gets the defaults for CDS.
   *
   * @return the defaults
   */
  public Map<Currency, CdsInfo> getCdsPerCurrencyInfo() {
    return _cdsPerCurrencyInfo;
  }

  /**
   * Sets up defaults for a currency.
   *
   * @param currency
   *          the currency
   * @param info
   *          the defaults
   */
  protected void setCdsPerCurrencyInfo(final Currency currency, final CdsInfo info) {
    _cdsPerCurrencyInfo.put(currency, info);
  }

  /**
   * Gets the defaults for a currency.
   *
   * @param currency
   *          the currency
   * @return the defaults
   */
  protected CdsInfo getCdsPerCurrencyInfo(final Currency currency) {
    return _cdsPerCurrencyInfo.get(currency);
  }

  /**
   * Sets the curve construction information.
   *
   * @param info
   *          the information
   */
  public void setCurveInfo(final Map<String, CurveType> info) {
    _curveInfo.clear();
    _curveInfo.putAll(info);
  }

  /**
   * Gets the types for curve construction configurations.
   *
   * @return the types
   */
  public Map<String, CurveType> getCurveInfo() {
    return _curveInfo;
  }

  /**
   * Sets the type of a curve construction configuration.
   *
   * @param name
   *          the name
   * @param type
   *          the type
   */
  protected void setCurveInfo(final String name, final CurveType type) {
    _curveInfo.put(name, type);
  }

  /**
   * Gets the type of a curve construction configuration.
   *
   * @param name
   *          the name of the configuration
   * @return the type
   */
  protected CurveType getCurveInfo(final String name) {
    return _curveInfo.get(name);
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
   * <li>Dividend type = model/equityoption.
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
   * <li>Forward curve calculation method = model/equityoption.
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

  protected void setCdsIsdaDefaults(final IsdaFunctions.CdsDefaults defaults) {
    defaults.setCurrencyInfo(getCdsPerCurrencyInfo(new Function1<CdsInfo, IsdaFunctions.CdsDefaults.CurrencyInfo>() {

      @Override
      public IsdaFunctions.CdsDefaults.CurrencyInfo execute(final CdsInfo i) {
        final IsdaFunctions.CdsDefaults.CurrencyInfo d = new IsdaFunctions.CdsDefaults.CurrencyInfo();
        setCdsPerCurrencyDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setCdsPerCurrencyDefaults(final CdsInfo i, final IsdaFunctions.CdsDefaults.CurrencyInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/credit/cds"));
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

  protected void setGovernmentBondDefaults(final BondDiscountingMethodFunctions.BondDefaults defaults) {
    defaults.setCountryInfo(getGovernmentBondPerCountryInfo(new Function1<BondInfo, BondDiscountingMethodFunctions.BondDefaults.CountryInfo>() {

      @Override
      public BondDiscountingMethodFunctions.BondDefaults.CountryInfo execute(final BondInfo i) {
        final BondDiscountingMethodFunctions.BondDefaults.CountryInfo d = new BondDiscountingMethodFunctions.BondDefaults.CountryInfo();
        setGovernmentBondPerCountryDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setGovernmentBondPerCountryDefaults(final BondInfo i, final BondDiscountingMethodFunctions.BondDefaults.CountryInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/bond/govt"));
    d.setBondType(i.getBondType("model/bond/govt"));
  }

  protected void setCorporateBondDefaults(final BondDiscountingMethodFunctions.BondDefaults defaults) {
    defaults.setCountryInfo(getCorporateBondPerCountryInfo(new Function1<BondInfo, BondDiscountingMethodFunctions.BondDefaults.CountryInfo>() {

      @Override
      public BondDiscountingMethodFunctions.BondDefaults.CountryInfo execute(final BondInfo i) {
        final BondDiscountingMethodFunctions.BondDefaults.CountryInfo d = new BondDiscountingMethodFunctions.BondDefaults.CountryInfo();
        setCorporateBondPerCountryDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setCorporateBondPerCountryDefaults(final BondInfo i, final BondDiscountingMethodFunctions.BondDefaults.CountryInfo d) {
    d.setCurveExposuresName(i.getCurveExposureName("model/bond/corp"));
    d.setBondType(i.getBondType("model/bond/corp"));
  }

  protected void setCurveTypeInformation(final CurveFunctions.Providers providers) {
    providers.setConfigMaster(getConfigMaster());
    providers.setCurveInfo(getCurveInfo());
  }

  protected FunctionConfigurationSource discountingFunctionConfiguration() {
    final FxDiscountingMethodFunctions.FxForwardDefaults fxForwardDefaults = new FxDiscountingMethodFunctions.FxForwardDefaults();
    setFxForwardDefaults(fxForwardDefaults);
    final RatesDiscountingMethodFunctions.LinearRatesDefaults linearRatesDefaults = new RatesDiscountingMethodFunctions.LinearRatesDefaults();
    setLinearRatesDefaults(linearRatesDefaults);
    final BondDiscountingMethodFunctions.BondDefaults govtBondDefaults = new BondDiscountingMethodFunctions.BondDefaults();
    setGovernmentBondDefaults(govtBondDefaults);
    final BondDiscountingMethodFunctions.BondDefaults corpBondDefaults = new BondDiscountingMethodFunctions.BondDefaults();
    setCorporateBondDefaults(corpBondDefaults);
    return CombiningFunctionConfigurationSource.of(getRepository(fxForwardDefaults), getRepository(linearRatesDefaults), getRepository(govtBondDefaults),
        getRepository(corpBondDefaults));
  }

  @Override
  protected FunctionConfigurationSource curveFunctions() {
    final Providers providers = new CurveFunctions.Providers();
    setCurveTypeInformation(providers);
    final com.opengamma.financial.analytics.model.curve.CurveFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.curve.CurveFunctions.Defaults();
    setCurveDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(defaults));
  }

  protected FunctionConfigurationSource creditFunctionConfiguration() {
    final IsdaFunctions.CdsDefaults cdsIsdaDefaults = new IsdaFunctions.CdsDefaults();
    setCdsIsdaDefaults(cdsIsdaDefaults);
    return CombiningFunctionConfigurationSource.of(getRepository(cdsIsdaDefaults));
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

  protected <T> Map<Currency, T> getGovernmentBondPerCurrencyInfo(final Function1<BondInfo, T> filter) {
    final Map<Currency, T> result = new HashMap<>();
    for (final Map.Entry<Currency, BondInfo> e : getGovernmentBondPerCurrencyInfo().entrySet()) {
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

  protected <T> Map<Currency, T> getCorporateBondPerCurrencyInfo(final Function1<BondInfo, T> filter) {
    final Map<Currency, T> result = new HashMap<>();
    for (final Map.Entry<Currency, BondInfo> e : getCorporateBondPerCurrencyInfo().entrySet()) {
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

  protected <T> Map<Country, T> getGovernmentBondPerCountryInfo(final Function1<BondInfo, T> filter) {
    final Map<Country, T> result = new HashMap<>();
    for (final Map.Entry<Country, BondInfo> e : getGovernmentBondPerCountryInfo().entrySet()) {
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

  protected <T> Map<Country, T> getCorporateBondPerCountryInfo(final Function1<BondInfo, T> filter) {
    final Map<Country, T> result = new HashMap<>();
    for (final Map.Entry<Country, BondInfo> e : getCorporateBondPerCountryInfo().entrySet()) {
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
    private final Value _bondType = new Value();

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresName.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresName.get(key);
    }

    public void setBondType(final String key, final String name) {
      _bondType.set(key, name);
    }

    public String getBondType(final String key) {
      return _bondType.get(key);
    }
  }

  protected <T> Map<Currency, T> getCdsPerCurrencyInfo(final Function1<CdsInfo, T> filter) {
    final Map<Currency, T> result = new HashMap<>();
    for (final Map.Entry<Currency, CdsInfo> e : getCdsPerCurrencyInfo().entrySet()) {
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

  public static class CdsInfo {
    private final Value _curveExposuresNames = new Value();

    public void setCurveExposureName(final String key, final String name) {
      _curveExposuresNames.set(key, name);
    }

    public String getCurveExposureName(final String key) {
      return _curveExposuresNames.get(key);
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
