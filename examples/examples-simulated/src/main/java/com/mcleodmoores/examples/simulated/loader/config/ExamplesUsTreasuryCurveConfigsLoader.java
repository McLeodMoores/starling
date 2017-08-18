/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 *
 */
public class ExamplesUsTreasuryCurveConfigsLoader extends AbstractTool<ToolContext> {

  public static void main(final String[] args) {
    new ExamplesUsTreasuryCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesUsTreasuryCurveConfigsPopulator.populateConfigMaster(configMaster);
  }
}
