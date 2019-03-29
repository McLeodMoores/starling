/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with curve configurations consistent with the ISDA method.
 */
@Scriptable
public class ExamplesIsdaCurveConfigsLoader extends AbstractTool<ToolContext> {

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesIsdaCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    ExamplesIsdaCurveConfigsPopulator.populateConfigMaster(getToolContext().getConfigMaster(), getToolContext().getConventionMaster());
  }
}
