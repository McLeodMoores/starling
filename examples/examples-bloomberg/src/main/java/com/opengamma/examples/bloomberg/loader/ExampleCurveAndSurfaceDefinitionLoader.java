/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
@Scriptable
public class ExampleCurveAndSurfaceDefinitionLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   *
   * @param args
   *          the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleCurveAndSurfaceDefinitionLoader().invokeAndTerminate(args);
  }

  // -------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final Map<UnorderedCurrencyPair, String> fxSurfaces = new HashMap<>();
    for (final UnorderedCurrencyPair pair : ExampleVanillaFxOptionPortfolioLoader.CCYS) {
      fxSurfaces.put(pair, "DEFAULT");
    }
    final Map<UnorderedCurrencyPair, Triple<String, String, String>> fxForward = new HashMap<>();
    fxForward.put(UnorderedCurrencyPair.of(Currency.USD, Currency.JPY), Triple.of("DEFAULT", "JPY", "JPY"));
    FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(configMaster, fxSurfaces);
    FXForwardCurveConfigPopulator.populateFXForwardCurveConfigMaster(configMaster, fxForward);
    new ExampleFXImpliedMultiCurveCalculationConfigPopulator(configMaster);
  }

}
