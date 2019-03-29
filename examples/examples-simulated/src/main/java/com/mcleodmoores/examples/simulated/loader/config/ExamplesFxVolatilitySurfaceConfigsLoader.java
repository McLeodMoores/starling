/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.volatility.surface.ExampleFXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with FX volatility surfaces.
 */
@Scriptable
public class ExamplesFxVolatilitySurfaceConfigsLoader extends AbstractTool<ToolContext> {

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesFxVolatilitySurfaceConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    ExampleFXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(getToolContext().getConfigMaster(),
        ExamplesViewsPopulator.CURRENCY_PAIRS);
  }

}
