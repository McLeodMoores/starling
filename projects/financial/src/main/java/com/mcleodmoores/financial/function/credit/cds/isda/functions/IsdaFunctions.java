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
import com.mcleodmoores.financial.function.credit.cds.isda.IsdaBondCreditSpreadHazardRateFunction;
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

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(IsdaCreditCurveFunction.class));
    functions.add(functionConfiguration(CreditCurveMarketDataFunction.class));
    functions.add(functionConfiguration(CreditCurveSpecificationFunction.class));
    functions.add(functionConfiguration(CreditCurveDefinitionFunction.class));
    functions.add(functionConfiguration(IsdaCdsAnalyticsFunction.class));
    functions.add(functionConfiguration(IsdaBondCreditSpreadHazardRateFunction.class));
  }

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
