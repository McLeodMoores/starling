/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.volatility.surface.ExampleFXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.tool.ToolContext;

/**
 *
 */
public class ExamplesFxVolatilitySurfaceConfigsLoader extends AbstractTool<ToolContext> {

  public static void main(final String[] args) {
    new ExamplesFxVolatilitySurfaceConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    ExampleFXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(getToolContext().getConfigMaster(), ExamplesViewsPopulator.CURRENCY_PAIRS);
  }

}
